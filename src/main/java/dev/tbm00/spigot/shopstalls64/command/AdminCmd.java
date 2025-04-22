

package dev.tbm00.spigot.shopstalls64.command;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.shopstalls64.StaticUtils;
import dev.tbm00.spigot.shopstalls64.StallHandler;
import dev.tbm00.spigot.shopstalls64.data.Stall;

public class AdminCmd implements TabExecutor {
    private final StallHandler stallHandler;
    private final String ADMIN_PERM = "shopstalls64.admin";

    public AdminCmd(StallHandler stallHandler) {
        this.stallHandler = stallHandler;
    }

    /**
     * Handles the /teststalladmin command.
     * 
     * @param player the command sender
     * @param consoleCommand the command being executed
     * @param alias the alias used for the command
     * @param args the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!StaticUtils.hasPermission(sender, ADMIN_PERM)) {
            StaticUtils.sendMessage(sender, "&cNo permission!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) return false;

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "help":
                // implement: return handleHelpCmd(player);
            case "create":
                // implement: return handleCreateCmd(player);
            case "delete":
                // implement: return handleDeleteCmd(player, args);
            case "status":
                // implement: return handleStatusCmd(player, subCmd);
            case "evict":
                // implement: return handleEvictCmd(player, subCmd);
            case "dailyTask":
                // implement: return handleDailyTaskCmd(player, subCmd);
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
        player.sendMessage(ChatColor.DARK_PURPLE + "--- " + ChatColor.LIGHT_PURPLE + "Stall Admin Commands" + ChatColor.DARK_PURPLE + " ---\n"
            + ChatColor.WHITE + "/teststalladmin create <id> <x,y,z> <initialPrice> <renewalPrice> <claimUUID>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/teststalladmin delete <id>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/teststalladmin status <id>/all" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/teststalladmin evict <id>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/teststalladmin dailyTask" + ChatColor.GRAY + " "
        );
        return true;
    }

    /**
     * Handles tab completion for the /teststalladmin command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}