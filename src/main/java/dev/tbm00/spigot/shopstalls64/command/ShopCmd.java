

package dev.tbm00.spigot.shopstalls64.command;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import xzot1k.plugins.ds.api.objects.Shop;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;
import dev.tbm00.spigot.shopstalls64.ConfigHandler;
import dev.tbm00.spigot.shopstalls64.utils.*;
import dev.tbm00.spigot.shopstalls64.gui.*;

public class ShopCmd implements TabExecutor {
    private final ShopStalls64 javaPlugin;
    private final ConfigHandler configHandler;
    private final String PLAYER_PERM = "shopstalls64.player";
    private final String STORE_PERM = "shopstalls64.player.store-inv";
    private final String MONEY_PERM = "shopstalls64.player.money-move";

    public ShopCmd(ShopStalls64 javaPlugin, ConfigHandler configHandler) {
        this.javaPlugin = javaPlugin;
        this.configHandler = configHandler;
    }

    /**
     * Handles the /testshop command.
     * 
     * @param player the command sender
     * @param consoleCommand the command being executed
     * @param alias the alias used for the command
     * @param args the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            Utils.sendMessage(sender, "&cThis command cannot be run through the console!");
            return true;
        } else if (!Utils.hasPermission(sender, PLAYER_PERM)) {
            Utils.sendMessage(sender, "&cNo permission!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0)
            return ShopUtils.handleCategoryCmd(player, configHandler.getGuiDefaultCategory());

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "help":
                return handleHelpCmd(player);
            case "buy":
                return handleBuyCmd(player, args);
            case "advertise":
                return handleAdvertiseCmd(player);
            case "list":
                return handleListCmd(player, args);
            case "store-inv":
                return handleStoreInvCmd(player);
            case "deposit-all":
                return handleDepositCmd(player, args);
            case "withdraw-all":
                return handleWithdrawCmd(player, args);
            case "armor":
            case "tools":
            case "tool":
            case "pog":
                return ShopUtils.handleCategoryCmd(player, "shoppog");
            case "ores":
                return ShopUtils.handleCategoryCmd(player, "shopores");
            case "blocks":
                return ShopUtils.handleCategoryCmd(player, "shopblocks");
            case "mobdrops":
            case "drops":
                return ShopUtils.handleCategoryCmd(player, "shopdrops");
            case "food":
                return ShopUtils.handleCategoryCmd(player, "shopfood");
            case "farm":
            case "farming":
                return ShopUtils.handleCategoryCmd(player, "shopfarm");
            case "gui":
                return ShopUtils.handleGuiCmd(player);
            case "searchgui":
                return handleSearchGuiCmd(player);
            default:
                return ShopUtils.handleSearch(player, args, 0);
        }
    }
    
    /**
     * Handles the sub command for the help menu.
     * 
     * @param player the command sender
     * @return true after displaying help menu
     */
    private boolean handleHelpCmd(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "--- " + ChatColor.LIGHT_PURPLE + "Shop Owner Commands" + ChatColor.DARK_PURPLE + " ---\n"
            + ChatColor.WHITE + "/testshop buy <#>" + ChatColor.GRAY + " Buy shop creation item(s)\n"
            + ChatColor.WHITE + "/testshop list" + ChatColor.GRAY + " Open your shop list & manage GUI\n"
            + ChatColor.WHITE + "/testshop advertise" + ChatColor.GRAY + " Broadcast the shop you're looking at\n"
            + ChatColor.WHITE + "/testshop store-inv" + ChatColor.GRAY + " Deposit all appicable items from your inv into your shops\n"
            + ChatColor.WHITE + "/testshop deposit-all <#>/max" + ChatColor.GRAY + " Deposit money into all your shops\n"
            + ChatColor.WHITE + "/testshop withdraw-all <#>/max" + ChatColor.GRAY + " Withdraw money from all your shops"
        );
        player.sendMessage(ChatColor.DARK_AQUA + "--- " + ChatColor.AQUA + "Shopper Commands" + ChatColor.DARK_AQUA + " ---\n"
            + ChatColor.WHITE + "/testshop " + ChatColor.GRAY + " Open shop category GUI\n"
            + ChatColor.WHITE + "/testshop <item>" + ChatColor.GRAY + " Find all <item> shops\n"
            + ChatColor.WHITE + "/testshop <player>" + ChatColor.GRAY + " Find all <player>'s shops\n"
            + ChatColor.WHITE + "/testsellinv <#>" + ChatColor.GRAY + " Sell all items in your inv for a minimum of $<#> each\n"
            + ChatColor.WHITE + "/testsellgui <#>" + ChatColor.GRAY + " Open a GUI and sell items for a minimum of $<#> each"
        );
        return true;
    }
    
    /**
     * Handles the sub command for buying shop creation items.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true after sudoing the command
     */
    private boolean handleBuyCmd(Player player, String[] args) {
        int count;
        if (args.length<2 || args[1]==null || args[1].isBlank()) count = 1;
        else {
            try {
                count = Integer.valueOf(args[1]);
            } catch (NumberFormatException e) {
                Utils.sendMessage(player, "&cAmount must be an integer!");
                return true;
            }
        }

        int totalPrice = configHandler.getDSCreationItemPrice() * count;
        double pocketBal = ShopStalls64.ecoHook.getBalance(player);

        if (totalPrice>pocketBal) {
            Utils.sendMessage(player, "&cYou do not have the $" + Utils.formatInt(totalPrice) + " required to buy " + count + " shop items!");
            return true;
        } else {
            if (Utils.removeMoney(player, totalPrice)) {
                ItemStack shopItem = ShopStalls64.dsHook.getManager().buildShopCreationItem(player, count);
                shopItem.setAmount(count);
                Utils.giveItem(player, shopItem);
                Utils.sendMessage(player, "&aYou bought " + count + " shop items for $" + Utils.formatInt(totalPrice) + "!");
                return true;
            } else {
                Utils.sendMessage(player, "&cAn error occured when buying the shop item(s)!");
                return true;
            } 
        }
    }
    
    /**
     * Handles the sub command for advertising shops.
     * 
     * @param player the command sender
     * @return true if after sudoing command
     */
    private boolean handleAdvertiseCmd(Player player) {
        Utils.sudoCommand(player, "ds advertise");
        return true;
    }

    /**
     * Handles the sub command for listing, locating, and teleporting to your own display shops.
     * 
     * @param sender the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    private boolean handleListCmd(Player sender, String[] args) {
        String targetUUID = sender.getUniqueId().toString();
        String targetName = sender.getName();

        if (args.length > 1) {
            targetName = args[1];
            targetUUID = ShopStalls64.repHook.getRepManager().getPlayerUUID(targetName);
            if (targetUUID==null) {
                Utils.sendMessage(sender, "&cCouldn't find target!");
                return true;
            }
        }

        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        new ListResultsPlayerGui(javaPlugin, dsMap, sender, targetUUID, targetName, 0, 1);
        return true;
    }

    /**
     * Handles the sub command for storing inventory items into display shops.
     * 
     * @param player the command sender
     * @return true if command was processed successfully
     */
    private boolean handleStoreInvCmd(Player player) {
        if (!Utils.hasPermission(player, STORE_PERM)) {
            Utils.sendMessage(player, "&cNo permission!");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        int MAX_STOCK = configHandler.getDSMaxStoredStock();
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
                    Utils.sendMessage(player, "&c" + shop.getShopItem().getType().toString().toLowerCase() + " &7shop @ "
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
        
        if (item_stored<1) Utils.sendMessage(player, "&cCouldn't find any applicable shops for your inv items!");
        else Utils.sendMessage(player, "&aStored a total of " + item_stored + " items into your shops!");

        return true;
    }

    /**
     * Handles the sub command for depositing money into shops.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    private boolean handleDepositCmd(Player player, String[] args) {
        if (!Utils.hasPermission(player, MONEY_PERM)) {
            Utils.sendMessage(player, "&cNo permission!");
            return true;
        }

        if (args.length<2) {
            Utils.sendMessage(player, "&f/testshop deposit-all <#>/max &7Deposit money into all your shops");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        UUID uuid = player.getUniqueId();
        int shop_count = ShopUtils.countPlayerShops(dsMap, uuid);
        if (shop_count<1) {
            Utils.sendMessage(player, "&cCouldn't find any of your DisplayShops!");
            return true;
        }

        // Determine how much to deposit into each shop
        String passedString = args[1];
        double pocket_balance = ShopStalls64.ecoHook.getBalance(player), deposit_per;
        double max_possible_deposit_per = Math.floor(pocket_balance / shop_count);
        if (passedString.equalsIgnoreCase("max")) {
            deposit_per = max_possible_deposit_per;
        } else {
            Double potential_deposit_per;
            try {potential_deposit_per = Math.floor(Double.parseDouble(passedString));} 
            catch (Exception e) {
                Utils.sendMessage(player, "&cEntered amount must be numerical or 'max'!");
                return true;
            }
            if (potential_deposit_per < 1) {
                Utils.sendMessage(player, "&cEntered amount must be greater than 1!");
                return true;
            }

            if (potential_deposit_per>max_possible_deposit_per) {
                Utils.sendMessage(player, "&fYou can not afford to deposit $" + potential_deposit_per + " into each of your display shops. Using $" 
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
                if (stored_balance >= configHandler.getDSMaxStoredBalance()) continue;
                if (stored_balance+deposit_per >= configHandler.getDSMaxStoredBalance())
                    deposit_amount = configHandler.getDSMaxStoredBalance()-stored_balance;
                else deposit_amount = deposit_per;

                if (Utils.removeMoney(player, deposit_amount)) {
                    shop.setStoredBalance(stored_balance+deposit_amount);
                    amount_deposited += deposit_amount;
                    ++shops_affected;
                }
            }
        }

        Utils.sendMessage(player, "&aDeposited a total of $" + Utils.formatInt(amount_deposited) + " into " + shops_affected + " of your shops!");
        return true;
    }

    /**
     * Handles the sub command for withdrawing money into shops.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    private boolean handleWithdrawCmd(Player player, String[] args) {
        if (!Utils.hasPermission(player, MONEY_PERM)) {
            Utils.sendMessage(player, "&cNo permission!");
            return true;
        }

        if (args.length<2) {
            Utils.sendMessage(player, "&f/testshop withdraw-all <#>/max &7Withdraw money from all your shops");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        UUID uuid = player.getUniqueId();
        int shop_count = ShopUtils.countPlayerShops(dsMap, uuid);
        if (shop_count<1) {
            Utils.sendMessage(player, "&cCouldn't find any of your DisplayShops!");
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
                Utils.sendMessage(player, "&cEntered amount must be numerical or 'max'!");
                return true;
            }
            if (withdraw_per < 1) {
                Utils.sendMessage(player, "&cEntered amount must be greater than 1!");
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

                if (Utils.addMoney(player, withdraw_amount)) {
                    shop.setStoredBalance(stored_balance-withdraw_amount);
                    amount_withdrew += withdraw_amount;
                    ++shops_affected;
                }
            }
        }

        Utils.sendMessage(player, "&aWithdrew a total of $" + Utils.formatInt(amount_withdrew) + " from " + shops_affected + " of your shops!");
        return true;
    }

    /**
     * Handles the sub command for opening the search gui.
     * 
     * @param player the command sender
     * @return true after creating gui instance
     */
    private boolean handleSearchGuiCmd(Player player) {
        new SearchGui(javaPlugin, player);
        return true;
    }

    /**
     * Handles tab completion for the /testshop command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.clear();
            String[] subCmds = new String[]{"<item>","<player>","help","buy","advertise","deposit-all","withdraw-all","store-inv","list"};
            for (String n : subCmds) {
                if (n!=null && n.startsWith(args[0])) 
                    list.add(n);
            }
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getName().startsWith(args[0])&&args[0].length()>0)
                    list.add(player.getName());
            });
            for (Material mat : Material.values()) {
                if (mat.name().toLowerCase().startsWith(args[0].toLowerCase())&&args[0].length()>1)
                    list.add(mat.name().toLowerCase());
            }
        } else if (args.length == 2) {
            if (args[0].equals("deposit-all") || args[0].equals("withdraw-all")) {
                list.add("<#>");
                list.add("max");
            } if (args[0].equals("buy")) {
                list.add("<#>");
            } if (args[0].equals("list")) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getName().startsWith(args[1])&&args[1].length()>0)
                        list.add(player.getName());
                });
                for (Material mat : Material.values()) {
                    if (mat.name().toLowerCase().startsWith(args[1].toLowerCase())&&args[1].length()>1)
                        list.add(mat.name().toLowerCase());
                }
            }
        }
        return list;
    }
}