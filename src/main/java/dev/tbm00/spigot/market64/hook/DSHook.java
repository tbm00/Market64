package dev.tbm00.spigot.market64.hook;

import xzot1k.plugins.ds.DisplayShops;
import xzot1k.plugins.ds.api.objects.LocationClone;

import com.griefdefender.lib.flowpowered.math.vector.Vector3i;

import dev.tbm00.spigot.market64.Market64;

public class DSHook {

    public DisplayShops pl;
    
    public DSHook(Market64 javaPlugin) {
        pl = ((DisplayShops) javaPlugin.getServer().getPluginManager().getPlugin("DisplayShops"));
    }


    public boolean isInRegion(LocationClone location, Vector3i lowerNorthWestCorner, Vector3i upperSouthEastCorner) {
        double x = location.getX(), y = location.getY(), z = location.getZ();

        // Calculate the minimum and maximum boundaries on each axis
        int minX = Math.min(lowerNorthWestCorner.getX(), upperSouthEastCorner.getX());
        int maxX = Math.max(lowerNorthWestCorner.getX(), upperSouthEastCorner.getX());
        int minY = Math.min(lowerNorthWestCorner.getY(), upperSouthEastCorner.getY());
        int maxY = Math.max(lowerNorthWestCorner.getY(), upperSouthEastCorner.getY());
        int minZ = Math.min(lowerNorthWestCorner.getZ(), upperSouthEastCorner.getZ());
        int maxZ = Math.max(lowerNorthWestCorner.getZ(), upperSouthEastCorner.getZ());

        // Check if the coordinate is within the boundaries
        return (x >= minX && x <= maxX) &&
               (y >= minY && y <= maxY) &&
               (z >= minZ && z <= maxZ);
    }


    // THE FOLLOWING METHODS ARE LEFTOVERS FROM DISPLAYSHOPADDON64

    /**
     * Handles the sub command for buying shop creation items.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true after sudoing the command
     */
    /*private boolean handleBuyCmd(Player player, String[] args) {
        int count;
        if (args.length<2 || args[1]==null || args[1].isBlank()) count = 1;
        else {
            try {
                count = Integer.valueOf(args[1]);
            } catch (NumberFormatException e) {
                StaticUtil.sendMessage(player, "&cAmount must be an integer!");
                return true;
            }
        }

        int totalPrice = 10 * count;
        double pocketBal = Market64.ecoHook.pl.getBalance(player);

        if (totalPrice>pocketBal) {
            StaticUtil.sendMessage(player, "&cYou do not have the $" + StaticUtil.formatInt(totalPrice) + " required to buy " + count + " shop items!");
            return true;
        } else {
            if (StaticUtil.removeMoney(player, totalPrice)) {
                ItemStack shopItem = Market64.dsHook.pl.getManager().buildShopCreationItem(player, count);
                shopItem.setAmount(count);
                StaticUtil.giveItem(player, shopItem);
                StaticUtil.sendMessage(player, "&aYou bought " + count + " shop items for $" + StaticUtil.formatInt(totalPrice) + "!");
                return true;
            } else {
                StaticUtil.sendMessage(player, "&cAn error occured when buying the shop item(s)!");
                return true;
            } 
        }
    }*/

    /**
     * Handles the sub command for storing inventory items into display shops.
     * 
     * @param player the command sender
     * @return true if command was processed successfully
     */
    /*private boolean handleStoreInvCmd(Player player) {
        if (!StaticUtil.hasPermission(player, PLAYER_PERM)) {
            StaticUtil.sendMessage(player, "&cNo permission!");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = Market64.dsHook.pl.getManager().getShopMap();
        int MAX_STOCK = 10;
        UUID uuid = player.getUniqueId();
        int item_stored = 0;

        itemStackFor:
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;

            Material invMat = itemStack.getType();
            MaterialData invData = itemStack.getData();
            ItemMeta invMeta = itemStack.getItemMeta();
            int invAmount = itemStack.getAmount();

            for (Shop shop : dsMap.values()) {
                ItemStack item = shop.getShopItem();
                if (item==null) continue;
                if (shop.getOwnerUniqueId()==null || !shop.getOwnerUniqueId().equals(uuid)) continue;
                if (item.getType()==null || !item.getType().equals(invMat)) continue;
                if (item.getData()==null || !item.getData().equals(invData)) continue;
                if (item.getItemMeta()==null || !item.getItemMeta().equals(invMeta)) continue;

                // move inv -> shop
                int stock = shop.getStock();
                int potential_deposit_all = shop.getStock() + invAmount;
                if (shop.getStock()>=MAX_STOCK) {
                    StaticUtil.sendMessage(player, "&c" + shop.getShopItem().getType().toString().toLowerCase() + " &7shop @ "
                    + shop.getBaseLocation().getWorldName() + ": " + shop.getBaseLocation().getX() + ", "
                    + shop.getBaseLocation().getY() + ", " + shop.getBaseLocation().getZ() + " &cstock is full!");
                } else if (potential_deposit_all>MAX_STOCK && stock!=-1) {
                    int leftover_amount = potential_deposit_all - MAX_STOCK;
                    itemStack.setAmount(leftover_amount);
                    shop.setStock(MAX_STOCK);
                    item_stored+=invAmount-leftover_amount;
                } else {
                    itemStack.setAmount(0);
                    if (stock!=-1) shop.setStock(potential_deposit_all);
                    item_stored+=invAmount;
                    continue itemStackFor;
                }
            }
        }
        
        if (item_stored<1) StaticUtil.sendMessage(player, "&cCouldn't find any applicable shops for your inv items!");
        else StaticUtil.sendMessage(player, "&aStored a total of " + item_stored + " items into your shops!");

        return true;
    }*/

    /**
     * Handles the sub command for depositing money into shops.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    /*private boolean handleDepositCmd(Player player, String[] args) {
        if (!StaticUtil.hasPermission(player, PLAYER_PERM)) {
            StaticUtil.sendMessage(player, "&cNo permission!");
            return true;
        }

        if (args.length<2) {
            StaticUtil.sendMessage(player, "&f/teststall deposit-all <#>/max &7Deposit money into all your shops");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = Market64.dsHook.pl.getManager().getShopMap();
        UUID uuid = player.getUniqueId();
        int shop_count = 0; //ShopUtils.countPlayerShops(dsMap, uuid);
        if (shop_count<1) {
            StaticUtil.sendMessage(player, "&cCouldn't find any of your DisplayShops!");
            return true;
        }

        // Determine how much to deposit into each shop
        String passedString = args[1];
        double pocket_balance = Market64.ecoHook.pl.getBalance(player), deposit_per;
        double max_possible_deposit_per = Math.floor(pocket_balance / shop_count);
        if (passedString.equalsIgnoreCase("max")) {
            deposit_per = max_possible_deposit_per;
        } else {
            Double potential_deposit_per;
            try {potential_deposit_per = Math.floor(Double.parseDouble(passedString));} 
            catch (Exception e) {
                StaticUtil.sendMessage(player, "&cEntered amount must be numerical or 'max'!");
                return true;
            }
            if (potential_deposit_per < 1) {
                StaticUtil.sendMessage(player, "&cEntered amount must be greater than 1!");
                return true;
            }

            if (potential_deposit_per>max_possible_deposit_per) {
                StaticUtil.sendMessage(player, "&fYou can not afford to deposit $" + potential_deposit_per + " into each of your display shops. Using $" 
                                    + max_possible_deposit_per + " instead (max based on your pocket balance & shop count).");
                deposit_per = max_possible_deposit_per;
            } else deposit_per = potential_deposit_per;
        }

        // Deposit into shops 1 by 1
        double amount_deposited = 0;
        int shops_affected = 0;
        for (Shop shop : dsMap.values()) {
            // confirm shop belongs to target
            if (shop.getShopItem()==null) continue;
            if (shop.getOwnerUniqueId()==null || !shop.getOwnerUniqueId().equals(uuid)) continue;
            if (shop.getStoredBalance()==-1) continue;
            else {
                // caculate amount to set
                double stored_balance = shop.getStoredBalance(), deposit_amount;
                if (stored_balance >= 10) continue;
                if (stored_balance+deposit_per >= 10)
                    deposit_amount = 10-stored_balance;
                else deposit_amount = deposit_per;

                if (StaticUtil.removeMoney(player, deposit_amount)) {
                    shop.setStoredBalance(stored_balance+deposit_amount);
                    amount_deposited += deposit_amount;
                    ++shops_affected;
                }
            }
        }

        StaticUtil.sendMessage(player, "&aDeposited a total of $" + StaticUtil.formatInt(amount_deposited) + " into " + shops_affected + " of your shops!");
        return true;
    }*/

    /**
     * Handles the sub command for withdrawing money into shops.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    /*private boolean handleWithdrawCmd(Player player, String[] args) {
        if (!StaticUtil.hasPermission(player, PLAYER_PERM)) {
            StaticUtil.sendMessage(player, "&cNo permission!");
            return true;
        }

        if (args.length<2) {
            StaticUtil.sendMessage(player, "&f/teststall withdraw-all <#>/max &7Withdraw money from all your shops");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = Market64.dsHook.pl.getManager().getShopMap();
        UUID uuid = player.getUniqueId();
        int shop_count = 0; //ShopUtils.countPlayerShops(dsMap, uuid);
        if (shop_count<1) {
            StaticUtil.sendMessage(player, "&cCouldn't find any of your DisplayShops!");
            return true;
        }

        // Determine how much to withdraw from each shop
        String passedString = args[1];
        boolean usingMax = false;
        Double withdraw_per = 0.0;
        if (passedString.equalsIgnoreCase("max")) usingMax = true;
        else {
            try {withdraw_per = Math.floor(Double.parseDouble(passedString));} 
            catch (Exception e) {
                StaticUtil.sendMessage(player, "&cEntered amount must be numerical or 'max'!");
                return true;
            }
            if (withdraw_per < 1) {
                StaticUtil.sendMessage(player, "&cEntered amount must be greater than 1!");
                return true;
            }
        }
        
        // Deposit into shops 1 by 1
        double amount_withdrew = 0;
        int shops_affected = 0;
        for (Shop shop : dsMap.values()) {
            // confirm shop belongs to target
            if (shop.getOwnerUniqueId()==null || !shop.getOwnerUniqueId().equals(uuid)) continue;
            if (shop.getStoredBalance()==-1) continue;
            else {
                // caculate amount to set
                double stored_balance = shop.getStoredBalance(), withdraw_amount;
                if (stored_balance <= 0) continue;
                if (usingMax==true)
                    withdraw_amount = stored_balance;
                else if (stored_balance < withdraw_per)
                    withdraw_amount = stored_balance;
                else withdraw_amount = withdraw_per;

                if (StaticUtil.addMoney(player, withdraw_amount)) {
                    shop.setStoredBalance(stored_balance-withdraw_amount);
                    amount_withdrew += withdraw_amount;
                    ++shops_affected;
                }
            }
        }

        StaticUtil.sendMessage(player, "&aWithdrew a total of $" + StaticUtil.formatInt(amount_withdrew) + " from " + shops_affected + " of your shops!");
        return true;
    }*/

    /**
     * Transfers display shops from source player to target player.
     * 
     * @return true once transfer completes
     */
    /*private boolean tShops() {
        ConcurrentHashMap<String, Shop> dsMap = Data64.dsHook.getManager().getShopMap();

        UUID uuidA = playerA.getUniqueId(), uuidB = playerB.getUniqueId();

        int i = 0;
        for (Shop shop : dsMap.values()) {
            if (shop.getOwnerUniqueId()!=null && shop.getOwnerUniqueId().equals(uuidA)) {
                shop.setOwnerUniqueId(uuidB);
                ++i;
            }
        }

        javaPlugin.sendMessage(sender, ChatColor.YELLOW + "tShops: " + i);
        return true;
    }*/
}