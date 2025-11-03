package dev.tbm00.spigot.market64;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.griefdefender.api.claim.Claim;

import dev.tbm00.papermc.playershops64.data.structure.Shop;

import dev.tbm00.spigot.market64.data.MySQLConnection;
import dev.tbm00.spigot.market64.data.Stall;
import dev.tbm00.spigot.market64.data.StallDAO;
import dev.tbm00.spigot.market64.hook.PSHook;
import dev.tbm00.spigot.market64.hook.EcoHook;
import dev.tbm00.spigot.market64.hook.GDHook;

public class StallHandler {
    private final Market64 javaPlugin;
    private final StallDAO dao;
    public final PSHook psHook;
    public final GDHook gdHook;
    private final EcoHook ecoHook;

    // stored sorted in order of stalls' ids
    private final List<Stall> stalls = new ArrayList<>();

    public StallHandler(Market64 javaPlugin, MySQLConnection db, PSHook psHook, GDHook gdHook, EcoHook ecoHook) {
        this.javaPlugin = javaPlugin;
        this.dao = new StallDAO(db);
        this.psHook = psHook;
        this.gdHook = gdHook;
        this.ecoHook = ecoHook;
        
        reloadAll();
    }

    /**
     * Process (in order):
     *  - Clear current internal cache 
     *  - Get all stall data from sql
     *  - Get the active claim using the world and uuid
     *  - Get the active contained shops using the claim's 3d corner blocks
     *  - Load all stalls to internal cache
     */
    public void reloadAll() {
        stalls.clear();
        List<Stall> stallsToLoad = dao.loadAll();

        // Load stalls' claims
        for (Stall stall : stallsToLoad) {
            if (stall==null) continue;
            stall.setClaim(gdHook.getClaimByUuid(stall.getWorld(), stall.getClaimUuid()));
        }

        // Load stalls' shops
        Map<UUID, Shop> psMap = psHook.pl.getShopHandler().getShopView();
        shopLoop:
        for (Shop shop : psMap.values()) {
            Location shopLoc = shop.getLocation();

            for (Stall stall : stallsToLoad) {
                if (stall==null) continue;
                Claim claim = stall.getClaim();

                if (psHook.isInRegion(shopLoc, gdHook.getLowerNorthWestCorner(claim), gdHook.getUpperSouthEastCorner(claim))) {
                    stall.addShopUuidToSet(shop.getUuid());
                    continue shopLoop;
                }
            }
        }
        stalls.addAll(stallsToLoad);
    }

    /**
     * Process (in order): 
     *  - Get the active claim using the world and uuid
     *  - Get the active contained shops using the claim's 3d corner blocks
     *  - Sets each shop's data:
     *      - Sets shop owner to null
     *      - Clears shop manager's
     *      - Sets shop limits to null
     *      - Sets shop itemstack to null
     *      - Sets shop buyPrice and sellPrice to -1
     *      - Sets shop stackSize to 1
     *      - Sets stored balance to 0
     *      - Sets stored stock to 0
     *  - Creates internal Stall object, placed at the IDth index
     *  - Adds Stall entry to SQL database
     *  - Returns true on success, false when there was an error
     * @param stallId int
     * @param rentalTime int
     * @param initialPrice double
     * @param renewalPrice double
     * @param world String
     * @param storageLoc String in form of "x,y,z"
     * @param claimUuid UUID
     * @returns true on success, false if error
     */
    public boolean createStall(int stallId, int rentalTime, int maxPlayTime, double initialPrice, double renewalPrice, String worldName, Location signLocation, String storageLoc, UUID claimUuid) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;

        Claim claim = gdHook.getClaimByUuid(world, claimUuid);
        if (claim == null) return false;

        int[] storageCoords = Arrays.stream(storageLoc.split(","))
                             .mapToInt(Integer::parseInt)
                             .toArray();

        Map<UUID, Shop> psMap = psHook.pl.getShopHandler().getShopView();
        Set<UUID> shopUuids = new HashSet<>();
        shopLoop:
        for (Shop shop : psMap.values()) {
            Location shopLoc = shop.getLocation();

            if (psHook.isInRegion(shopLoc, gdHook.getLowerNorthWestCorner(claim), gdHook.getUpperSouthEastCorner(claim))) {
                shop.setMoneyStock(BigDecimal.ZERO);
                shop.setItemStock(0);
                shop.setStackSize(1);
                shop.setItemStack(null);
                shop.setBuyPrice(null);
                shop.setSellPrice(null);
                shop.setOwnerName(null);
                shop.setOwnerUuid(null);
                shop.setAssistants(new HashSet<>());

                psHook.upsertShop(shop);
                shopUuids.add(shop.getUuid());
                continue shopLoop;
            }
        }
        
        Stall newStall = new Stall(stallId, claimUuid, claim, shopUuids, world, signLocation, storageCoords, initialPrice, renewalPrice,
                                    rentalTime, maxPlayTime, false, null, null, null, null);
            
        if (!dao.insert(newStall)) return false;
        ensureCapacity(stallId);
        stalls.set(stallId, newStall);

        Collection<Entity> coll = world.getNearbyEntities(signLocation, StaticUtil.NEARBY_BLOCKS, StaticUtil.NEARBY_BLOCKS, StaticUtil.NEARBY_BLOCKS);
        for (Entity ent : coll) {
            if (ent instanceof Villager) {
                Villager vill = (Villager) ent;
                if (!vill.hasAI() && vill.isInvulnerable()) {
                    vill.setCustomName("Unemployed Stall Keeper");
                    break;
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                StaticUtil.StallSignSetAvaliable(newStall);
            }
        }.runTaskLater(javaPlugin, 4L);

        return true;
    }

    /**
     * Process (in order): 
     *  - Charges the player the initial price
     *  - Resets the each shop's data:
     *      - Sets shop owner to player.getUniqueId()
     *      - Sets shop buyPrice to 100 and sellPrice to 50
     *  - Sets stalls' 'rented' attribute to true
     *  - Sets stalls' eviction date to 7 days from the present
     *  - Updates internal Stall object
     *  - Updates Stall entry in SQL database
     * @returns true on success, false if error
     */
    public boolean fillStall(Stall stall, Player player) {
        if (stall == null) {
            StaticUtil.sendMessage(player, "&cCould not find stall!");
            return false;
        } else if (stall.isRented()) {
            StaticUtil.sendMessage(player, "&cStall is currently rented!");
            return false;
        }

        for (Stall s : stalls) {
            if (s!=null && s.isRented() && s.getRenterUuid()!=null && s.getRenterUuid().equals(player.getUniqueId())) {
                StaticUtil.sendMessage(player, "&cYou already have a stall!");
                return false;
            }
        }

        if (stall.getPlayTimeDays()!=-1 && StaticUtil.getPlaytimeSeconds((OfflinePlayer)player)>(stall.getPlayTimeDays()*86400)) {
            StaticUtil.sendMessage(player, "&cYou have too much playtime to rent that stall!");
            return false;
        }
        
        double price = stall.getInitialPrice();
        
        if (!ecoHook.hasMoney(player, price)) {
            StaticUtil.sendMessage(player, "&cYou don't have enough money! ($" + StaticUtil.formatInt(price)+")");
            return false;
        } else if (!ecoHook.removeMoney(player, price)) {
            StaticUtil.sendMessage(player, "&cError when charging you!");
            return false;
        }

        for (Shop shop : getStallsShops(stall).values()) {
            // Pre-log
            if (shop.getItemStack()==null) logShop(shop, ChatColor.YELLOW, null, shop.getItemStock(), -1, -1);
            else logShop(shop, ChatColor.YELLOW, shop.getItemStack().getType(), shop.getItemStock(), -1, -1);

            shop.setOwnerUuid(player.getUniqueId());
            shop.setOwnerName(player.getName());
            shop.setBuyPrice(BigDecimal.valueOf(100));
            shop.setSellPrice(BigDecimal.valueOf(50));

            psHook.upsertShop(shop);

            // Post-log
            if (shop.getItemStack()==null) logShop(shop, ChatColor.YELLOW, null, shop.getItemStock(), -1, -1);
            else logShop(shop, ChatColor.YELLOW, shop.getItemStack().getType(), shop.getItemStock(), -1, -1);
        }

        stall.setRented(true);
        stall.setRenterName(player.getName());
        stall.setRenterUuid(player.getUniqueId());
        stall.setLastTransaction(null);
        Date dateBase = new Date();
        Instant newExpiry = dateBase.toInstant().plus(stall.getRentalTimeDays(), ChronoUnit.DAYS);
        stall.setEvictionDate(Date.from(newExpiry));

        if (!dao.update(stall)) {
            StaticUtil.log(ChatColor.RED, "dao.update(stall) failed after filling stall " + stall.getId() +"!");
        }

        Collection<Entity> coll = stall.getSignLocation().getWorld().getNearbyEntities(stall.getSignLocation(), StaticUtil.NEARBY_BLOCKS, StaticUtil.NEARBY_BLOCKS, StaticUtil.NEARBY_BLOCKS);
        for (Entity ent : coll) {
            if (ent instanceof Villager) {
                Villager vill = (Villager) ent;
                if (!vill.hasAI() && vill.isInvulnerable()) {
                    vill.setCustomName(player.getName()+"'s Stall Keeper");
                    break;
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                StaticUtil.StallSignSetUnavaliable(stall);
            }
        }.runTaskLater(javaPlugin, 4L);

        return true;
    }

    /**
     * Process (in order): 
     *  - Charges the player the renewal price
     *  - Increases stalls' eviction date by 7 days
     *  - Updates internal Stall object
     *  - Updates Stall entry in SQL database
     *  - Returns true on success, false when there was an error
     */
    public boolean renewStall(Stall stall, boolean auto) {
        if (stall == null) {
            StaticUtil.log(ChatColor.RED, "Could not find stall!");
            return false;
        }

        if (!stall.isRented()) {
            StaticUtil.log(ChatColor.RED, "Stall is not rented!");
            return false;
        }

        OfflinePlayer offlinePlayer = null;
        if (stall.getRenterUuid()!=null) {
            offlinePlayer = Bukkit.getOfflinePlayer(stall.getRenterUuid());
        } else {
            offlinePlayer = Bukkit.getOfflinePlayer(stall.getRenterName());
            stall.setRenterUuid(offlinePlayer.getUniqueId());
        } if (offlinePlayer == null) {
            StaticUtil.log(ChatColor.RED, "Couldn't find offline player by stall.getRenterUuid() or stall.getRenterName()!");
            return false;
        }
    
        Player player = offlinePlayer.getPlayer();
        if (!auto && player==null) {
            StaticUtil.log(ChatColor.RED, "Could not find online player!");
            return false;
        }

        if (!stall.isRented()) {
            if (!auto) StaticUtil.sendMessage(player, "&cThis stall is not currently rented!");
            return false;
        } else if (!offlinePlayer.getUniqueId().equals(stall.getRenterUuid())) {
            if (!auto) StaticUtil.sendMessage(player, "&cThis stall is not yours!");
            return false;
        }

        Date dateBase = stall.getEvictionDate() != null ? 
                        stall.getEvictionDate() : new Date();

        Instant twoWeeksFromToday = (new Date()).toInstant().plus(2*stall.getRentalTimeDays(), ChronoUnit.DAYS);
        Instant newExpiry = dateBase.toInstant().plus(stall.getRentalTimeDays(), ChronoUnit.DAYS);

        if (newExpiry.isAfter(twoWeeksFromToday)) {
            if (!auto) StaticUtil.sendMessage(player, "&cYou cannot renew your stall until the next payment period!");
            else StaticUtil.sendMail(offlinePlayer, "&cYou cannot renew your stall until the next payment period!");
            return false;
        }

        double price = stall.getRenewalPrice();
        if (!ecoHook.hasMoney(offlinePlayer, price) || !ecoHook.removeMoney(offlinePlayer, price)) {
            double left_to_pay = price;

            // search through shops and try to get payment
            for (Shop shop : getStallsShops(stall).values()) {
                double initialBalance = shop.getMoneyStock().doubleValue();
                if (initialBalance>0 & left_to_pay>0) {
                    if (initialBalance >= left_to_pay) {
                        shop.setMoneyStock(BigDecimal.valueOf(initialBalance-left_to_pay));
                        psHook.upsertShop(shop);
                        left_to_pay = 0;
                        break;
                    } else {
                        shop.setMoneyStock(BigDecimal.ZERO);
                        psHook.upsertShop(shop);
                        left_to_pay=left_to_pay-initialBalance;
                        continue;
                    }
                }
            } if (left_to_pay>0) {
                // refund player taken money if shops didnt contain enough
                double paid = price - left_to_pay;
                ecoHook.giveMoney(offlinePlayer, paid);

                if (!auto) StaticUtil.sendMessage(player, "&cYou didn't have enough money in your pocket or your stall's shops to renew your stall! ($"+StaticUtil.formatInt(price)+")");
                else StaticUtil.sendMail(offlinePlayer, "&cYou didn't have enough money in your pocket or your stall's shops to renew your stall! ($"+StaticUtil.formatInt(price)+")");
                
                return false;
            } else { 
                if (!auto) StaticUtil.sendMessage(player, "&fRenewed your stall for &a$"+StaticUtil.formatInt(price)+"&f! &7(money taken from your stall's shops)");
                else StaticUtil.sendMail(offlinePlayer, "&fRenewed your stall for &a$"+StaticUtil.formatInt(price)+"&f! &7(money taken from your stall's shops)");
            }
        } else {
            if (!auto) StaticUtil.sendMessage(player, "&fRenewed your stall for &a$"+StaticUtil.formatInt(price)+"&f! &7(money taken from your pocket)");
            else StaticUtil.sendMail(offlinePlayer, "&fRenewed your stall for &a$"+StaticUtil.formatInt(price)+"&f! &7(money taken from your pocket)");
        }

        stall.setEvictionDate(Date.from(newExpiry));

        if (!dao.update(stall)) {
            StaticUtil.log(ChatColor.RED, "dao.update(stall) failed after renewing stall " + stall.getId() +"!");
        } else {
            StaticUtil.log(ChatColor.GREEN, "Renewed " + offlinePlayer.getName() + "'s stall, next date: " + stall.getEvictionDate());
        }

        return true;
    }

    /**
     * Process (in order): 
     *  - Resets each shop's data:
     *      - Transfers stored balance to player
     *      - Saves stored itemstacks to be transfered at the end of the method
     *      - Sets shop owner to null
     *      - Clears shop attributes
     *          - Sets shop owner to null
     *          - Sets shop limits to null
     *          - Sets shop itemstack to null
     *          - Sets shop buyPrice and sellPrice to -1
     *          - Sets shop stackSize to 1
     *          - etc.
     *  - Creates SHULKER itemstack(s) named "Stall #<stallId> - <renterName> - <currentDate> - #<shulkerIndex>"
     *  - Moves saved itemstacks into the shulker(s)
     *  - Moves saved shulker(s)
     *      - into the player's inventory if they're online and they have enough inventory space
     *      - into the stall's storage location if they're not online or they dont have enough inventory space
     *          - if the storage location is not a barrel, create a barrel at the location
     *          - if the storage barrel doesn't have space, create a new barrel object 1 block below it, and store it in there, then update the storage block location
     *  - Creates PAPER itemstack named "Stall #<stallId> - <renterName> - <currentDate>"
     *      - Adds lore to PAPER itemstack: "Stall: <stallId>"
     *      - Adds lore to PAPER itemstack: "Renter: <renterName>"
     *      - Adds lore to PAPER itemstack: "Date: <currentDate>" 
     *      - Adds lore to PAPER itemstack: "Reason: <reason>"
     *      - Adds lore to PAPER itemstack: "Money: <moneyTransferred>"
     *      - Adds lore to PAPER itemstack: "Last Payment: <lastPaymentDate>"
     *      - Adds lore to PAPER itemstack: "Items: <numberOfShulkers>, <toInv or toStallStorage>"
     *  - Puts PAPER itemstack itemstacks in the stall's storage location
     *      - if the storage location is not a barrel, create a barrel at the location
     *      - if the storage barrel doesn't have space, create a new barrel object 1 block below it, and store it in there, then update the storage block location
     * 
     *  - Sets stalls' 'rented' attribute to false
     * 
     *  - Updates internal Stall object & other attributes accordingly
     *  - Updates Stall entry in SQL database
     *  
     *  - Returns true on success, false when there was an error
     */
    public boolean clearStall(Stall stall, String reason, boolean auto) {
        if (stall == null) {
            StaticUtil.log(ChatColor.RED, "Could not find stall!");
            return false;
        }

        if (!stall.isRented()) {
            StaticUtil.log(ChatColor.RED, "Stall is not rented!");
            return false;
        }

        OfflinePlayer offlinePlayer = null;
        if (stall.getRenterUuid()!=null) {
            offlinePlayer = Bukkit.getOfflinePlayer(stall.getRenterUuid());
        } else {
            offlinePlayer = Bukkit.getOfflinePlayer(stall.getRenterName());
        } if (offlinePlayer == null) {
            StaticUtil.log(ChatColor.RED, "Couldn't find offline player by stall.getRenterUuid() or stall.getRenterName()!");
            return false;
        }
    
        Player player = offlinePlayer.getPlayer();
        if (!auto && player==null) {
            StaticUtil.log(ChatColor.RED, "Could not find online player!");
            return false;
        }

        // Capture shops' stored contents and reset data
        double totalRefund = 0;
        List<ItemStack> itemsFromShops = new ArrayList<>();
        for (Shop shop : getStallsShops(stall).values()) {
                        
            // Capture & reset stored stock
            ItemStack prototype = shop.getItemStack();
            if (prototype!=null) {

                int stock = shop.getItemStock();
                int maxStacksize = prototype.getMaxStackSize();
                int stacks = stock/maxStacksize, leftovers = stock%maxStacksize;

                // Prelog
                logShop(shop, ChatColor.YELLOW, prototype.getType(), stock, stacks, leftovers);

                for (int i = 0; i<stacks; i++) {
                    ItemStack batch = prototype.clone();
                    batch.setAmount(maxStacksize);
                    itemsFromShops.add(batch);
                }
                if (leftovers>0) {
                    ItemStack batch = prototype.clone();
                    batch.setAmount(leftovers);
                    itemsFromShops.add(batch);
                }
            }

            // Refund & reset stored balance
            double storedBal = shop.getMoneyStock().doubleValue();
            totalRefund += storedBal;
            if (ecoHook.giveMoney(offlinePlayer, storedBal)) {
                shop.setMoneyStock(BigDecimal.ZERO);
            } else {
                StaticUtil.log(ChatColor.RED, "Failed to remove " + storedBal + " from shop " + shop.getUuid() + " during stall clear!");
            }

            // Reset other shop data
            shop.setItemStock(0);
            shop.setStackSize(1);
            shop.setItemStack(null);
            shop.setBuyPrice(null);
            shop.setSellPrice(null);
            shop.setOwnerName(null);
            shop.setOwnerUuid(null);
            shop.setAssistants(new HashSet<>());

            psHook.upsertShop(shop);

            // Post-log
            int stock = shop.getItemStock();
            int maxStacksize = 64;
            int stacks = stock/maxStacksize, leftovers = stock%maxStacksize;
            logShop(shop, ChatColor.GREEN, null, stock, stacks, leftovers);
        }

        // Create shulker and deliever them
        int boxIndex = 1;
        List<ItemStack> shulkerBoxes = new ArrayList<>();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        while (!itemsFromShops.isEmpty()) {
            String name = String.format("Stall #%d - %s - %s - #%d", stall.getId(), stall.getRenterName(), dateStr, boxIndex++);
            ItemStack shulker = StaticUtil.createShulkerBox(name, itemsFromShops);
            shulkerBoxes.add(shulker);
        }
        boolean toInv = false, toStallStorage = false;
        for (ItemStack box : shulkerBoxes) {
            if (player!=null && player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(box);
                toInv = true;
            } else {
                addToBarrel(stall, box);
                toStallStorage = true;
            }
        }

        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta pm = paper.getItemMeta();
        String name = String.format("Stall #%d - %s - %s", stall.getId(), stall.getRenterName(), dateStr);
        pm.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add("Stall: " + stall.getId());
        lore.add("Renter: " + stall.getRenterName());
        lore.add("Date: " + dateStr);
        lore.add("Reason: " + reason);
        lore.add("Money: $" + StaticUtil.formatInt(totalRefund));
        if (toInv && toStallStorage)
            lore.add("Boxes: " + shulkerBoxes.size() + " to player's inv & stall's storage");
        else if (toInv)
            lore.add("Boxes: " + shulkerBoxes.size() + " to player's inv");
        else if (toStallStorage)
            lore.add("Boxes: " + shulkerBoxes.size() + " to stall's storage");
        else lore.add("Boxes: " + shulkerBoxes.size() + " to null inv");
        pm.setLore(lore);
        paper.setItemMeta(pm);

        StaticUtil.log(null, "calling PAPER addToBarrel()...");
        addToBarrel(stall, paper);

        stall.setRented(false);
        stall.setEvictionDate(null);
        stall.setLastTransaction(null);
        stall.setRenterName(null);
        stall.setRenterUuid(null);

        if (!dao.update(stall)) {
            StaticUtil.log(ChatColor.RED, "dao.update(stall) failed after clearing stall " + stall.getId() +"!");
        }

        if (!auto) {
            if (toInv && toStallStorage)
                StaticUtil.sendMessage(player, "&fYour stall was vacated, money was returned, and items returned to your inventory..! (there were some items that didn't fit in your inventory, ask staff for help getting them back)");
            else if (toStallStorage)
                StaticUtil.sendMessage(player, "&fYour stall was vacated and money was returned. Your items didn't fit in your inventory, ask staff for help getting them back.");
            else if (toInv)
                StaticUtil.sendMessage(player, "&fYour stall was vacated, money was returned, and items returned to your inventory..!");
            else StaticUtil.sendMessage(player, "&fYour stall was vacated and money was returned!");
        } else {
            if (toInv && toStallStorage)
                StaticUtil.sendMail(offlinePlayer, "&fYour stall was vacated, money was returned, and items returned to your inventory..! (there were some items that didn't fit in your inventory, ask staff for help getting them back)");
            else if (toStallStorage)
                StaticUtil.sendMail(offlinePlayer, "&fYour stall was vacated and money was returned. Your items didn't fit in your inventory, ask staff for help getting them back.");
            else if (toInv)
                StaticUtil.sendMail(offlinePlayer, "&fYour stall was vacated, money was returned, and items returned to your inventory..!");
            else StaticUtil.sendMail(offlinePlayer, "&fYour stall was vacated and money was returned!");
        }

        Collection<Entity> coll = stall.getSignLocation().getWorld().getNearbyEntities(stall.getSignLocation(), StaticUtil.NEARBY_BLOCKS, StaticUtil.NEARBY_BLOCKS, StaticUtil.NEARBY_BLOCKS);
        for (Entity ent : coll) {
            if (ent instanceof Villager) {
                Villager vill = (Villager) ent;
                if (!vill.hasAI() && vill.isInvulnerable()) {
                    vill.setCustomName("Unemployed Stall Keeper");
                    break;
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                StaticUtil.StallSignSetAvaliable(stall);
            }
        }.runTaskLater(javaPlugin, 4L);

        return true;
    }


    private void addToBarrel(Stall stall, ItemStack item) {
        Block primary = stall.getWorld().getBlockAt(stall.getStorageCoords()[0], stall.getStorageCoords()[1], stall.getStorageCoords()[2]);
        if (primary.getType() != Material.BARREL) {
            StaticUtil.log(ChatColor.RED, "Stall #"+stall.getId()+"'s barrel storage location isn't a barrel.. setting it now!");
            primary.setType(Material.BARREL);
        }

        Inventory primaryInv = ((Barrel) primary.getState()).getInventory();
        Map<Integer, ItemStack> leftover = primaryInv.addItem(item);

        if (!leftover.isEmpty()) { // create new barrel below, and update stall
            Block lower = primary.getRelative(BlockFace.DOWN);
            if (lower.getType() != Material.BARREL) {
                lower.setType(Material.BARREL);
            }
    
            Inventory lowerInv = ((Barrel) lower.getState()).getInventory();
            Map<Integer, ItemStack> leftover2 = new HashMap<>();
    
            for (ItemStack stack : leftover.values()) {
                leftover2.putAll(lowerInv.addItem(stack));
            }
    
            if (!leftover2.isEmpty()) {
                for (ItemStack drop : leftover2.values()) {
                    stall.getWorld().dropItemNaturally(stall.getSignLocation().add(0.5,1,0.5), drop);
                }
            }
    
            int[] newCoords = {stall.getStorageCoords()[0], stall.getStorageCoords()[1] - 1, stall.getStorageCoords()[2]};
            stall.setStorageCoords(newCoords);
            if (!dao.update(stall)) {
                StaticUtil.log(ChatColor.RED, "dao.update(stall) failed after adjusting storage block coords for stall " + stall.getId() +"!");
            }
        }
    }

    /**
     * @param stallId int
     * @returns true on success, false if error
     */
    public boolean deleteStall(int stallId) {
        if (stallId >= 0 && stallId < stalls.size()) {
            StaticUtil.StallSignSetDeleted(getStall(stallId));

            //new BukkitRunnable() {
                //@Override
                //public void run() {
                    if (dao.delete(stallId)) {
                        stalls.set(stallId, null);
                        stalls.remove(stallId);
                    //}
                //}
            //}.runTaskLater(javaPlugin, 4L);
            return true;
            }
        }
        return false;
    }

    /**
     * @returns true on success, false if error
     */
    public boolean rescanClaims() {
        int errorCount = 0;
        for (Stall stall : stalls) {
            Claim claim = gdHook.getClaimByLocation(stall.getSignLocation());
            stall.setClaimUuid(claim.getUniqueId());
            stall.setClaim(claim);
            if (!dao.update(stall)) errorCount++;
        }
        reloadAll();
        if (errorCount!=0) return false;
        else return true;
    }

    /**
     * @returns true on success, false if error
     */
    public boolean lowerStorageCoords() {
        int errorCount = 0;
        for (Stall stall : stalls) {
            int[] coords = stall.getStorageCoords();
            coords[1] += -3;
            stall.setStorageCoords(coords);
            if (!dao.update(stall)) errorCount++;
        }
        reloadAll();
        if (errorCount!=0) return false;
        else return true;
    }

    /**
     * Process (in order, for each stall): 
     *  - If stall is active, check if the renewal date has passed.
     *     - check if lastTranscation is within last 3*rentalTimeDays
     *        - if not, evict & continue
     *     - check if ownerPlaytime is less than max
     *        - if not, evict & continue
     *     - check if eviction/renewal date exists
     *        - if not, initialize it & continue
     *        - if so, check if date has passed
     *           - if not, continue
     *           - if so, check if stall's shops are majority empty
     *              - if so, evict & continue
     *              - if not, try automatic renewal payment
     *                 - if fails, evict & continue
     *                 - else, continue
     */
    public int dailyTask() {
        StaticUtil.log(ChatColor.GOLD, "---[ Before Daily Task ]---");
        StaticUtil.log(ChatColor.GOLD, "-- Stall Count: "+ stalls.size());
        int i = 0;
        for (Stall stall : stalls) {
            if (stall.isRented()) {
                i++;
                stall.setLastTransaction(getLastTranscation(stall));
                dao.update(stall);

                StaticUtil.log(ChatColor.YELLOW, i+"- Stall #"+ stall.getId() + ": "+Bukkit.getOfflinePlayer(stall.getRenterUuid()).getName());
                StaticUtil.log(ChatColor.YELLOW, i+"-   LastTranscaction: "+ stall.getLastTransaction() + ", EvictionDate: "+stall.getEvictionDate());
            }
        }
        
        int count = 0;
        for (Stall stall : stalls) {
            if (stall==null) continue;
            if (!stall.isRented()) continue;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(stall.getRenterUuid());

            Date lastTransaction = stall.getLastTransaction();
            Date dateBase = new Date();
            Instant sinceDate = dateBase.toInstant().minus(3*stall.getRentalTimeDays(), ChronoUnit.DAYS);
            if (lastTransaction!=null && lastTransaction.before(Date.from(sinceDate))) {
                if (clearStall(stall, "no sales", true)) {
                    StaticUtil.sendMail(offlinePlayer, "&cYou were evicted from stall #"+stall.getId()+" since it had no transactions for at least "+3*stall.getRentalTimeDays()+" days!");
                    ++count;
                    continue;
                }
            }

            if (stall.getPlayTimeDays()!=-1 && StaticUtil.getPlaytimeSeconds(offlinePlayer)>(stall.getPlayTimeDays()*86400)) {
                if (clearStall(stall, "max playtime", true)) {
                    StaticUtil.sendMail(offlinePlayer, "&cYou were evicted from stall #"+stall.getId()+" since you exceed the max playtime for that stall! ("+stall.getPlayTimeDays()+" days)");
                    ++count;
                    continue;
                }
            }

            Date evictionDate = stall.getEvictionDate();
            if (evictionDate==null) {
                Instant newExpiry = (new Date()).toInstant().plus(stall.getRentalTimeDays(), ChronoUnit.DAYS);
                stall.setEvictionDate(Date.from(newExpiry));
                if (!dao.update(stall)) {
                    StaticUtil.log(ChatColor.RED, "dao.update(stall) failed after setting null->current+stall.getRentalTimeDays() eviction date for stall " + stall.getId() +"!");
                }
                continue;
            } else if ((new Date()).after(evictionDate)) {
                ConcurrentHashMap<UUID,Shop> shopMap = getStallsShops(stall);
                int shop_count = shopMap.size();
                if (shop_count != 0) {
                    int empty_count = 0;
                    for (Shop shop : shopMap.values()) {
                        ItemStack prototype = shop.getItemStack();
                        if (prototype==null || prototype.getType()==Material.AIR) {
                            empty_count++;
                        }
                    }
                    if ((empty_count / shop_count) > .65) {
                        if (clearStall(stall, "majority empty", true)) {
                            StaticUtil.sendMail(offlinePlayer, "&cYou were evicted from stall #"+stall.getId()+" since the majority of its shops were empty! ("+empty_count+"/"+shop_count+")");
                            ++count;
                            continue;
                        }
                    }
                }

                if (renewStall(stall, true)) {
                    continue;
                } else {
                    if (clearStall(stall, "missed payment", true)) {
                        StaticUtil.sendMail(offlinePlayer, "&cYou were evicted from stall #"+stall.getId()+" for a missing an automatic payment!");
                        ++count;
                        continue;
                    }
                }
            }
        }

        StaticUtil.log(ChatColor.GOLD, "---[ After Daily Task ]---");
        StaticUtil.log(ChatColor.GOLD, "-- Stall Count: "+ stalls.size());
        int j = 0;
        for (Stall stall : stalls) {
            if (stall.isRented()) {
                j++;
                StaticUtil.log(ChatColor.YELLOW, j+"- Stall #"+ stall.getId() + ": "+Bukkit.getOfflinePlayer(stall.getRenterUuid()).getName());
                StaticUtil.log(ChatColor.YELLOW, j+"-   LastTranscaction: "+ stall.getLastTransaction() + ", EvictionDate: "+stall.getEvictionDate());
            }
        }

        return count;
    }

    public boolean getShopInfo(Player player) {
        Set<UUID> shopUuids = psHook.pl.getShopHandler().getPlayersShops(player.getUniqueId());
        for (UUID shopUuid : shopUuids) {
            Shop shop = psHook.pl.getShopHandler().getShop(shopUuid);
            int stock = shop.getItemStock();

            if (shop.getItemStack()!=null) {
                int maxStacksize = shop.getItemStack().getMaxStackSize();
                int stacks = stock/maxStacksize, leftovers = stock%maxStacksize;
                logShop(shop, ChatColor.WHITE, shop.getItemStack().getType(), stock, stacks, leftovers);
            } else {
                int maxStacksize = 64;
                int stacks = stock/maxStacksize, leftovers = stock%maxStacksize;
                logShop(shop, ChatColor.WHITE, null, stock, stacks, leftovers);
            }
        } 
        return true;
    }

    private void logShop(Shop shop, ChatColor color, Material material, int stock, int stacks, int leftovers) {
        String mat = (material==null) ? "null item" : material.toString();
        StaticUtil.log(color, shop.getUuid().toString() +": "+ mat);
        StaticUtil.log(color, "Stored Stock: "+stock+", "+stacks+" stacks "+leftovers+" leftover, Stack Size: "+shop.getStackSize());
        StaticUtil.log(color, "Stored Money: "+shop.getMoneyStock()+", B:"+shop.getBuyPrice()+" S:"+shop.getSellPrice());
        if (shop.getOwnerName()!=null) StaticUtil.log(color, "Owner: "+shop.getOwnerName());
        StaticUtil.log(color, "Assistants: ");
        for (UUID uuid : shop.getAssistants()) {
            if (uuid==null) continue;
            StaticUtil.log(color, "* "+Bukkit.getOfflinePlayer(uuid).getName());
        } StaticUtil.log(color, "-");
    }

    /** Get a stall by ID. */
    public Stall getStall(int id) {
        for (Stall stall : stalls) {
            if (stall.getId()==id) {
                return stall;
            }
        }
        return null;
    }

    /** Return unmodifiable view of all internal cached stalls. */
    public List<Stall> getStalls() {
        return Collections.unmodifiableList(stalls);
    }

    /** Return dao object for accessing database. */
    public StallDAO getStallDao() {
        return dao;
    }

    /** Return dao object for accessing database. */
    public boolean updateStallInDAO(int stallId) {
        return dao.update(getStall(stallId));
    }

    private void ensureCapacity(int id) {
        while (stalls.size() <= id) stalls.add(null);
    }

    public ConcurrentHashMap<UUID, Shop> getStallsShops(Stall stall) {
        if (stall == null) return null;

        Claim claim = stall.getClaim();
        if (claim == null) return null;

        Map<UUID, Shop> psMap = psHook.pl.getShopHandler().getShopView();
        ConcurrentHashMap<UUID, Shop> stallShops = new ConcurrentHashMap<>();

        for (Shop shop : psMap.values()) {
            Location shopLoc = shop.getLocation();

            if (psHook.isInRegion(shopLoc, gdHook.getLowerNorthWestCorner(claim), gdHook.getUpperSouthEastCorner(claim))) {
                stallShops.put(shop.getUuid(), shop);
            }
        }

        return stallShops;
    }

    public Date getLastTranscation(Stall stall) {
        Date newestDate = null;
        for (Shop shop : getStallsShops(stall).values()) {
            if (shop.getLastTransactionDate()==null || shop.getLastTransactionDate().equals(null))
                continue;
            if (newestDate == null || newestDate.equals(null)) {
                newestDate = shop.getLastTransactionDate();
                continue;
            } if (shop.getLastTransactionDate().after(newestDate)) 
                newestDate = shop.getLastTransactionDate();
        }
        return newestDate;
    }

    public void rescanPlayerShops() {
        for (Stall stall : stalls) {
            if (stall == null) continue;

            Claim claim = stall.getClaim();
            if (claim == null) continue;

            Map<UUID, Shop> psMap = psHook.pl.getShopHandler().getShopView();
            Set<UUID> shopUuids = new HashSet<>();

            UUID renterUuid = null;
            if (stall.isRented() && stall.getRenterUuid()!=null) {
                renterUuid = stall.getRenterUuid();
            }

            for (Shop shop : psMap.values()) {
                Location shopLoc = shop.getLocation();

                if (psHook.isInRegion(shopLoc, gdHook.getLowerNorthWestCorner(claim), gdHook.getUpperSouthEastCorner(claim))) {
                    shopUuids.add(shop.getUuid());
                    if (renterUuid==null && (shop.getOwnerUuid()!=null || shop.getOwnerName()!=null)) {
                        shop.setOwnerUuid(null);
                        shop.setOwnerName(null);
                        psHook.upsertShop(shop);
                    } else if (renterUuid!=null && 
                                ((shop.getOwnerUuid()!=null || shop.getOwnerName()==null) || !shop.getOwnerUuid().equals(renterUuid) || !shop.getOwnerName().equals(stall.getRenterName()))
                                ) {
                        shop.setOwnerUuid(renterUuid);
                        shop.setOwnerName(stall.getRenterName());
                        psHook.upsertShop(shop);
                    }
                }
            }
            stall.setShopUuids(shopUuids);
        }
    }

    public void divideStallPrices(Double factor) {
        if (factor==null) return;
        for (Stall stall : stalls) {
            if (stall == null) continue;

            Double initialRenew = stall.getRenewalPrice();
            Double initialInitial = stall.getInitialPrice();

            stall.setRenewalPrice(initialRenew/factor);
            stall.setInitialPrice(initialInitial/factor);

            dao.update(stall);
        }

        for (Stall stall : stalls) {
            if (stall == null) continue;
            if (!stall.isRented()) StaticUtil.StallSignSetAvaliable(stall);
        }
    }
}