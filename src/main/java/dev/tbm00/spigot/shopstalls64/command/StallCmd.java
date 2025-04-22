

package dev.tbm00.spigot.shopstalls64.command;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.UUID;

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
import dev.tbm00.spigot.shopstalls64.StaticUtils;
import dev.tbm00.spigot.shopstalls64.StallHandler;
import dev.tbm00.spigot.shopstalls64.data.Stall;

public class StallCmd implements TabExecutor {
    private final StallHandler stallHandler;
    private final String PLAYER_PERM = "shopstalls64.player";

    public StallCmd(StallHandler stallHandler) {
        this.stallHandler = stallHandler;
    }

    /**
     * Handles the /teststall command.
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
            StaticUtils.sendMessage(sender, "&cThis command cannot be run through the console!");
            return true;
        } else if (!StaticUtils.hasPermission(sender, PLAYER_PERM)) {
            StaticUtils.sendMessage(sender, "&cNo permission!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0)
            return false;

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "help":
                return handleHelpCmd(player);
            case "rent":
                // implement: return handleRentCmd(player, args);
            case "renew":
                // implement: return handleRenewCmd(player, args);
            case "abandon":
                // implement: return handleAbandonCmd(player, args);
            default:
                return false;
        }
    }
    
    /**
     * Handles the sub command for the help menu.
     * 
     * @param player the command sender
     * @return true after displaying help menu
     */
    private boolean handleHelpCmd(Player player) {
        player.sendMessage(ChatColor.DARK_AQUA + "--- " + ChatColor.AQUA + "Stall Commands" + ChatColor.DARK_AQUA + " ---\n"
            + ChatColor.WHITE + "/teststall rent <id>" + ChatColor.GRAY + " Rent a stall for a week, renews automaticaly if you have enough money in your pocket\n"
            + ChatColor.WHITE + "/teststall renew [id]" + ChatColor.GRAY + " Renew your stall early, if [id] is not null, it will abandon your only stall\n"
            + ChatColor.WHITE + "/teststall abandon [id]" + ChatColor.GRAY + " Abandon your stall, if [id] is not null, it will abandon your only stall\n"
        );
        return true;
    }

    /**
     * Handles tab completion for the /teststall command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.clear();
            String[] subCmds = new String[]{"rent","renew","abandon"};
            for (String n : subCmds) {
                if (n!=null && n.startsWith(args[0])) 
                    list.add(n);
            }
        } else if (args.length == 2) {
            if (args[0].equals("rent") || args[0].equals("abandon") || args[0].equals("renew")) {
                for (Stall stall : stallHandler.getStalls()) {
                    if (sender instanceof Player && sender.getName().equalsIgnoreCase(stall.getRenterName()))
                        list.add(String.valueOf(stall.getId()));
                }
            }
        }
        return list;
    }

    // THE FOLLOWING METHODS ARE LEFTOVERS FROM DISPLAYSHOPADDON64

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
                StaticUtils.sendMessage(player, "&cAmount must be an integer!");
                return true;
            }
        }

        int totalPrice = 10 * count;
        double pocketBal = ShopStalls64.ecoHook.pl.getBalance(player);

        if (totalPrice>pocketBal) {
            StaticUtils.sendMessage(player, "&cYou do not have the $" + StaticUtils.formatInt(totalPrice) + " required to buy " + count + " shop items!");
            return true;
        } else {
            if (StaticUtils.removeMoney(player, totalPrice)) {
                ItemStack shopItem = ShopStalls64.dsHook.pl.getManager().buildShopCreationItem(player, count);
                shopItem.setAmount(count);
                StaticUtils.giveItem(player, shopItem);
                StaticUtils.sendMessage(player, "&aYou bought " + count + " shop items for $" + StaticUtils.formatInt(totalPrice) + "!");
                return true;
            } else {
                StaticUtils.sendMessage(player, "&cAn error occured when buying the shop item(s)!");
                return true;
            } 
        }
    }

    /**
     * Handles the sub command for storing inventory items into display shops.
     * 
     * @param player the command sender
     * @return true if command was processed successfully
     */
    private boolean handleStoreInvCmd(Player player) {
        if (!StaticUtils.hasPermission(player, PLAYER_PERM)) {
            StaticUtils.sendMessage(player, "&cNo permission!");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.pl.getManager().getShopMap();
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
                    StaticUtils.sendMessage(player, "&c" + shop.getShopItem().getType().toString().toLowerCase() + " &7shop @ "
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
        
        if (item_stored<1) StaticUtils.sendMessage(player, "&cCouldn't find any applicable shops for your inv items!");
        else StaticUtils.sendMessage(player, "&aStored a total of " + item_stored + " items into your shops!");

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
        if (!StaticUtils.hasPermission(player, PLAYER_PERM)) {
            StaticUtils.sendMessage(player, "&cNo permission!");
            return true;
        }

        if (args.length<2) {
            StaticUtils.sendMessage(player, "&f/teststall deposit-all <#>/max &7Deposit money into all your shops");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.pl.getManager().getShopMap();
        UUID uuid = player.getUniqueId();
        int shop_count = 0; //ShopUtils.countPlayerShops(dsMap, uuid);
        if (shop_count<1) {
            StaticUtils.sendMessage(player, "&cCouldn't find any of your DisplayShops!");
            return true;
        }

        // Determine how much to deposit into each shop
        String passedString = args[1];
        double pocket_balance = ShopStalls64.ecoHook.pl.getBalance(player), deposit_per;
        double max_possible_deposit_per = Math.floor(pocket_balance / shop_count);
        if (passedString.equalsIgnoreCase("max")) {
            deposit_per = max_possible_deposit_per;
        } else {
            Double potential_deposit_per;
            try {potential_deposit_per = Math.floor(Double.parseDouble(passedString));} 
            catch (Exception e) {
                StaticUtils.sendMessage(player, "&cEntered amount must be numerical or 'max'!");
                return true;
            }
            if (potential_deposit_per < 1) {
                StaticUtils.sendMessage(player, "&cEntered amount must be greater than 1!");
                return true;
            }

            if (potential_deposit_per>max_possible_deposit_per) {
                StaticUtils.sendMessage(player, "&fYou can not afford to deposit $" + potential_deposit_per + " into each of your display shops. Using $" 
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

                if (StaticUtils.removeMoney(player, deposit_amount)) {
                    shop.setStoredBalance(stored_balance+deposit_amount);
                    amount_deposited += deposit_amount;
                    ++shops_affected;
                }
            }
        }

        StaticUtils.sendMessage(player, "&aDeposited a total of $" + StaticUtils.formatInt(amount_deposited) + " into " + shops_affected + " of your shops!");
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
        if (!StaticUtils.hasPermission(player, PLAYER_PERM)) {
            StaticUtils.sendMessage(player, "&cNo permission!");
            return true;
        }

        if (args.length<2) {
            StaticUtils.sendMessage(player, "&f/teststall withdraw-all <#>/max &7Withdraw money from all your shops");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.pl.getManager().getShopMap();
        UUID uuid = player.getUniqueId();
        int shop_count = 0; //ShopUtils.countPlayerShops(dsMap, uuid);
        if (shop_count<1) {
            StaticUtils.sendMessage(player, "&cCouldn't find any of your DisplayShops!");
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
                StaticUtils.sendMessage(player, "&cEntered amount must be numerical or 'max'!");
                return true;
            }
            if (withdraw_per < 1) {
                StaticUtils.sendMessage(player, "&cEntered amount must be greater than 1!");
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

                if (StaticUtils.addMoney(player, withdraw_amount)) {
                    shop.setStoredBalance(stored_balance-withdraw_amount);
                    amount_withdrew += withdraw_amount;
                    ++shops_affected;
                }
            }
        }

        StaticUtils.sendMessage(player, "&aWithdrew a total of $" + StaticUtils.formatInt(amount_withdrew) + " from " + shops_affected + " of your shops!");
        return true;
    }
}