

package dev.tbm00.spigot.shopstalls64.command;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;
import dev.tbm00.spigot.shopstalls64.gui.SellGui;
import dev.tbm00.spigot.shopstalls64.utils.*;

public class SellGuiCmd implements TabExecutor {
    private final ShopStalls64 javaPlugin;
    private final String SELL_GUI_PERM = "shopstalls64.player.sell-gui";

    public SellGuiCmd(ShopStalls64 javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    /**
     * Handles the /testsellgui & command.
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
        } else if (!Utils.hasPermission(sender, SELL_GUI_PERM)) {
            Utils.sendMessage(sender, "&cNo permission!");
            return true;
        }

        Player player = (Player) sender;

        return handleSellGuiCmd(player, args);
    }

    /**
     * Handles the sub command for selling all items in your inv.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true after creating gui instance
     */
    private boolean handleSellGuiCmd(Player player, String[] args) {
        if (args.length<1) {
            Utils.sendMessage(player, "&f/testsellgui <#> &7Open a GUI and sell items for a minimum of $<#> each");
            return true;
        }

        // Determine minimum sell price
        Double sell_per;
        try {sell_per = Math.floor(Double.parseDouble(args[0]));} 
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
     * Handles tab completion for the /testsellgui command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("<#>");
        }
        return list;
    }
}