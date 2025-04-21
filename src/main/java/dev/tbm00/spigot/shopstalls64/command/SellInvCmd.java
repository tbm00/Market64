

package dev.tbm00.spigot.shopstalls64.command;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.shopstalls64.utils.*;

public class SellInvCmd implements TabExecutor {
    private final String SELL_INV_PERM = "shopstalls64.player.sell-inv";

    public SellInvCmd() {}

    /**
     * Handles the /testsellinv & command.
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
        } else if (!Utils.hasPermission(sender, SELL_INV_PERM)) {
            Utils.sendMessage(sender, "&cNo permission!");
            return true;
        }

        Player player = (Player) sender;

        return parseSellInvCmd(player, args);
    }

    /**
     * Handles the sub command for selling all items in your inv.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true after creating gui instance
     */
    private boolean parseSellInvCmd(Player player, String[] args) {
        if (args.length<1) {
            Utils.sendMessage(player, "&f/testsellinv <#> &7Sell all items in your inv for a minimum of $<#> each");
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

        ShopUtils.handleSellInv(player, player.getInventory(), sell_per);
        return true;
    }

    /**
     * Handles tab completion for the /testsellinv command.
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