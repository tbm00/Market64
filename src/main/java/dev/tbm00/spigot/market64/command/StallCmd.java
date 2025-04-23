

package dev.tbm00.spigot.market64.command;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.StallHandler;
import dev.tbm00.spigot.market64.data.Stall;

public class StallCmd implements TabExecutor {
    private final StallHandler stallHandler;
    private final String PLAYER_PERM = "market64.player";

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
            StaticUtil.sendMessage(sender, "&cThis command cannot be run through the console!");
            return true;
        } else if (!StaticUtil.hasPermission(sender, PLAYER_PERM)) {
            StaticUtil.sendMessage(sender, "&cNo permission!");
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
}