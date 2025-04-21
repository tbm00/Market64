

package dev.tbm00.spigot.shopstalls64.command;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;
import dev.tbm00.spigot.shopstalls64.gui.SellGui;
import dev.tbm00.spigot.shopstalls64.ConfigHandler;
import dev.tbm00.spigot.shopstalls64.utils.*;

public class SellCmd implements TabExecutor {
    private final ShopStalls64 javaPlugin;
    private final ConfigHandler configHandler;
    private final String PLAYER_PERM = "shopstalls64.player";

    public SellCmd(ShopStalls64 javaPlugin, ConfigHandler configHandler) {
        this.javaPlugin = javaPlugin;
        this.configHandler = configHandler;
    }

    /**
     * Handles the /testsell & command.
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
            case "armor":
            case "tools":
            case "tool":
            case "pog":
                return ShopUtils.handleCategoryCmd(player, "shoppog");
            case "ores":
                return ShopUtils.handleCategoryCmd(player, "shopores");
            case "blocks":
                return ShopUtils.handleCategoryCmd(player, "shopblocks");
            case "drops":
            case "mobdrops":
                return ShopUtils.handleCategoryCmd(player, "shopdrops");
            case "food":
                return ShopUtils.handleCategoryCmd(player, "shopfood");
            case "farm":
            case "farming":
                return ShopUtils.handleCategoryCmd(player, "shopfarm");
            case "all":
            case "inv":
                return parseSellInvCmd(player, args);
            case "gui":
                return parseSellGuiCmd(player, args);
            default:
                return ShopUtils.handleSearch(player, args, 3);
        }
    }
    
    /**
     * Handles the sub command for the help menu.
     * 
     * @param player the command sender
     * @return true after displaying help menu
     */
    private boolean handleHelpCmd(Player player) {
        player.sendMessage(ChatColor.DARK_AQUA + "--- " + ChatColor.AQUA + "Shopper Commands" + ChatColor.DARK_AQUA + " ---\n"
            + ChatColor.WHITE + "/testsell" + ChatColor.GRAY + " Open shop category GUI\n"
            + ChatColor.WHITE + "/testsell <item>" + ChatColor.GRAY + " Find all <item> shops you can sell to\n"
            + ChatColor.WHITE + "/testsell <player>" + ChatColor.GRAY + " Find all <player>'s shops you can sell to\n"
            + ChatColor.WHITE + "/testsellinv <#>" + ChatColor.GRAY + " Sell all items in your inv for a minimum of $<#> each\n"
            + ChatColor.WHITE + "/testsellgui <#>" + ChatColor.GRAY + " Open a GUI and sell items for a minimum of $<#> each"
        );
        return true;
    }

    /**
     * Handles the sub command for selling all items in your inv.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true after creating gui instance
     */
    private boolean parseSellInvCmd(Player player, String[] args) {
        if (args.length<2) {
            Utils.sendMessage(player, "&f/testsell inv <#> &7Sell all items in your inv for a minimum of $<#> each");
            return true;
        }

        // Determine minimum sell price
        Double sell_per;
        try {sell_per = Math.floor(Double.parseDouble(args[1]));} 
        catch (Exception e) {
            Utils.sendMessage(player, "&cMinimum sell price per item must be numerical!");
            return true;
        }
        if (sell_per < 0) {
            Utils.sendMessage(player, "&cMinimum sell price per item cannot be less than 0!");
            return true;
        }

        ShopUtils.handleSellInv(player, player.getInventory(), sell_per);
        return true;
    }

    /**
     * Handles the sub command for selling all items in your inv.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true after creating gui instance
     */
    private boolean parseSellGuiCmd(Player player, String[] args) {
        if (args.length<2) {
            Utils.sendMessage(player, "&f/testsell gui <#> &7Open a GUI and sell items for a minimum of $<#> each");
            return true;
        }

        // Determine minimum sell price
        Double sell_per;
        try {sell_per = Math.floor(Double.parseDouble(args[1]));} 
        catch (Exception e) {
            Utils.sendMessage(player, "&cMinimum sell price per item must be numerical!");
            return true;
        }
        if (sell_per < 0) {
            Utils.sendMessage(player, "&cMinimum sell price per item cannot be less than 0!");
            return true;
        }

        new SellGui(javaPlugin, player, sell_per);
        return true;
    }

    /**
     * Handles tab completion for the /testsell command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.clear();
            String[] subCmds = new String[]{"<item>","<player>","inv","gui"};
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
            if (args[0].equalsIgnoreCase("all") || 
                args[0].equalsIgnoreCase("inv") || 
                args[0].equalsIgnoreCase("gui")) {
                    list.add("<#>");
            }
        }
        return list;
    }
}