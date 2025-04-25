

package dev.tbm00.spigot.market64.command;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.data.Stall;
import dev.tbm00.spigot.market64.StallHandler;

public class AdminCmd implements TabExecutor {
    private final StallHandler stallHandler;

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
        if (!StaticUtil.hasPermission(sender, StaticUtil.ADMIN_PERM)) {
            StaticUtil.sendMessage(sender, "&cNo permission!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) return false;

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "help":
                return handleHelpCmd(player);
            case "evict":
                return handleEvictCmd(player, args);
            case "delete":
                return handleDeleteCmd(player, args);
            case "dailytask":
                return handleDailyTaskCmd(player);
            case "status":
                return handleStatusCmd(player, args);
            case "info":
                return handleInfoCmd(player);
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
            + ChatColor.WHITE + "/stalladmin delete <id>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/stalladmin status <id>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/stalladmin evict <id>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/stalladmin dailyTask" + ChatColor.GRAY + " "
        );
        return true;
    }

    private boolean handleEvictCmd(Player player, String[] args) {
        if (args.length<1) {
            StaticUtil.sendMessage(player, "&cYou must provide a stall ID to evict!");
            return false;
        } 

        int id;
        try {
            id = Integer.valueOf(args[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(player, "&cCould not parse ID '"+args[1]+"'!");
            return true;
        }

        if (stallHandler.clearStall(id, "staff eviction", false)) {
            StaticUtil.sendMessage(player, "&aEvicted stall "+id+"!");
            return true;
        } else {
            StaticUtil.sendMessage(player, "&cFailed to evict stall "+id+"!");
            return true;
        }
    }

    private boolean handleDailyTaskCmd(Player player) {
        int count = stallHandler.dailyTask();
        StaticUtil.sendMessage(player, "&aEvicted "+count+" stalls!");
        return true;
    }

    private boolean handleDeleteCmd(Player player, String[] args) {
        if (args.length<1) {
            StaticUtil.sendMessage(player, "&cYou must provide a stall ID to delete!");
            return false;
        } 

        int id;
        try {
            id = Integer.valueOf(args[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(player, "&cCould not parse ID '"+args[1]+"'!");
            return true;
        }

        if (stallHandler.deleteStall(id)) {
            StaticUtil.sendMessage(player, "&aDeleted stall "+id+"!");
            return true;
        } else {
            StaticUtil.sendMessage(player, "&cFailed to delete stall "+id+"!");
            return true;
        }
    }

    private boolean handleStatusCmd(Player player, String[] args) {
        if (args.length<1) {
            StaticUtil.sendMessage(player, "&cYou must provide a stall ID to check out!");
            return false;
        } 

        int id;
        try {
            id = Integer.valueOf(args[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(player, "&cCould not parse ID '"+args[1]+"'!");
            return true;
        }

        Stall stall = stallHandler.getStall(id);
        if (stall==null) {
            StaticUtil.sendMessage(player, "&cFailed find stall "+id+"!");
            return true;
        }

        player.sendMessage(ChatColor.DARK_PURPLE + "--- " + ChatColor.LIGHT_PURPLE + "Stall " + id + ChatColor.DARK_PURPLE + " ---\n"
            + ChatColor.YELLOW + "Claim: " + ChatColor.GRAY + stall.getClaimUuid().toString() + " \n"
            + ChatColor.YELLOW + "Shops: " + ChatColor.GRAY + stall.getShopMap().size() + " \n"
            + ChatColor.YELLOW + "ShopUuids: " + ChatColor.GRAY + stall.getShopUuids().size() + " \n"
            + ChatColor.YELLOW + "World: " + ChatColor.GRAY + stall.getWorld().getName() + " \n"
            + ChatColor.YELLOW + "Sign: " + ChatColor.GRAY + stall.getSignLocation().getBlockX()+","+stall.getSignLocation().getBlockY()+","+stall.getSignLocation().getBlockZ() + " \n"
            + ChatColor.YELLOW + "Barrel: " + ChatColor.GRAY + stall.getStorageCoords().toString() + " \n"
            + ChatColor.YELLOW + "InitialPrice: " + ChatColor.GRAY + "$" + StaticUtil.formatInt(stall.getInitialPrice()) + " \n"
            + ChatColor.YELLOW + "RenewalPrice: " + ChatColor.GRAY + "$" + StaticUtil.formatInt(stall.getRenewalPrice()) + " \n"
            + ChatColor.YELLOW + "RentalTime: " + ChatColor.GRAY + stall.getPlayTimeDays() + " days \n"
            + ChatColor.YELLOW + "MaxPlayTime: " + ChatColor.GRAY + stall.getRentalTimeDays() + " days \n"
            + ChatColor.WHITE + "Rented: " + ChatColor.GRAY + stall.isRented() + " \n"
            + ChatColor.WHITE + "RenterUuid: " + ChatColor.GRAY + stall.getRenterUuid().toString() + " \n"
            + ChatColor.WHITE + "RenterName: " + ChatColor.GRAY + stall.getRenterName() + " \n"
            + ChatColor.WHITE + "EvictionDate: " + ChatColor.GRAY + stall.getEvictionDate().toString() + " \n"
            + ChatColor.WHITE + "LastTransaction: " + ChatColor.GRAY + stall.getLastTransaction().toString() + " \n"
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
            String[] subCmds = new String[]{"help","delete","status","evict","dailyTask"};
            for (String n : subCmds) {
                if (n!=null && n.startsWith(args[0])) 
                    list.add(n);
            }
        }
        return list;
    }
}