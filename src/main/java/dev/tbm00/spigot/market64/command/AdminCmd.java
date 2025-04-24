

package dev.tbm00.spigot.market64.command;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.StallHandler;

public class AdminCmd implements TabExecutor {
    private final StallHandler stallHandler;
    private final String ADMIN_PERM = "market64.admin";

    public AdminCmd(StallHandler stallHandler) {
        this.stallHandler = stallHandler;
    }

    /**
     * Handles the /stalladmin command.
     * 
     * @param player the command sender
     * @param consoleCommand the command being executed
     * @param alias the alias used for the command
     * @param args the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!StaticUtil.hasPermission(sender, ADMIN_PERM)) {
            StaticUtil.sendMessage(sender, "&cNo permission!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) return false;

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "help":
                return handleHelpCmd(player);
            case "info":
                return handleInfoCmd(player);
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
            + ChatColor.WHITE + "/stalladmin create <id> <rentalTime> <maxPlaytime> <initialPrice> <renewalPrice> <world> <x,y,z> <claimUUID>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/stalladmin delete <id>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/stalladmin status <id>/all" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/stalladmin evict <id>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/stalladmin dailyTask" + ChatColor.GRAY + " "
        );
        return true;
    }

    private boolean handleInfoCmd(Player player) {
        stallHandler.getShopInfo(player);
        return true;
    }

    /**
     * Handles tab completion for the /stalladmin command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.clear();
            String[] subCmds = new String[]{"create","delete","status","evict","dailyTask"};
            for (String n : subCmds) {
                if (n!=null && n.startsWith(args[0])) 
                    list.add(n);
            }
        }
        return list;
    }
}