

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

import dev.tbm00.spigot.shopstalls64.ConfigHandler;
import dev.tbm00.spigot.shopstalls64.utils.*;

public class BuyCmd implements TabExecutor {
    private final ConfigHandler configHandler;
    private final String PLAYER_PERM = "shopstalls64.player";

    public BuyCmd(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    /**
     * Handles the /testbuy & command.
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
            case "gui":
                return ShopUtils.handleGuiCmd(player);
            default:
                return ShopUtils.handleSearch(player, args, 2);
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
            + ChatColor.WHITE + "/testbuy" + ChatColor.GRAY + " Open shop category GUI\n"
            + ChatColor.WHITE + "/testbuy <item>" + ChatColor.GRAY + " Find all <item> shops you can buy from\n"
            + ChatColor.WHITE + "/testbuy <player>" + ChatColor.GRAY + " Find all <player>'s shops you can buy from"
        );
        return true;
    }

    /**
     * Handles tab completion for the /testbuy command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.clear();
            String[] subCmds = new String[]{"<item>","<player>"};
            for (String n : subCmds) {
                if (n!=null && n.startsWith(args[0])) 
                    list.add(n);
            }
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getName().startsWith(args[0]))
                    list.add(player.getName());
            });
            for (Material mat : Material.values()) {
                if (mat.name().toLowerCase().startsWith(args[0].toLowerCase()))
                    list.add(mat.name().toLowerCase());
            }
        }
        return list;
    }
}