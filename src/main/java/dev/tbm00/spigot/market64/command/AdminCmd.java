

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

        if (args.length == 0) return false;

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "help":
                return handleHelpCmd(sender);
            case "evict":
                return handleEvictCmd(sender, args);
            case "delete":
                return handleDeleteCmd(sender, args);
            case "dailytask":
                return handleDailyTaskCmd(sender);
            case "status":
                return handleStatusCmd(sender, args);
            case "info":
                return handleInfoCmd(sender);
            case "update":
                return handleUpdateCmd(sender, args);
            default:
                return false;
        }
    }
    
    /**
     * Handles the sub command for the help menu.
     * 
     * @param sender the command sender
     * @return true after displaying help menu
     */
    private boolean handleHelpCmd(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "--- " + ChatColor.LIGHT_PURPLE + "Stall Admin Commands" + ChatColor.DARK_PURPLE + " ---\n"
            + ChatColor.WHITE + "/stalladmin delete <id>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/stalladmin status <id>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/stalladmin evict <id>" + ChatColor.GRAY + " \n"
            + ChatColor.WHITE + "/stalladmin dailyTask" + ChatColor.GRAY + " "
        );
        return true;
    }

    private boolean handleEvictCmd(CommandSender sender, String[] args) {
        if (args.length<2) {
            StaticUtil.sendMessage(sender, "&cYou must provide a stall ID to evict!");
            return false;
        } 

        int id;
        try {
            id = Integer.valueOf(args[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(sender, "&cCould not parse ID '"+args[1]+"'!");
            return true;
        }

        if (stallHandler.clearStall(id, "staff eviction", false)) {
            StaticUtil.sendMessage(sender, "&aEvicted stall "+id+"!");
            return true;
        } else {
            StaticUtil.sendMessage(sender, "&cFailed to evict stall "+id+"!");
            return true;
        }
    }

    private boolean handleDailyTaskCmd(CommandSender sender) {
        int count = stallHandler.dailyTask();
        StaticUtil.sendMessage(sender, "&aEvicted "+count+" stalls!");
        return true;
    }

    private boolean handleDeleteCmd(CommandSender sender, String[] args) {
        if (args.length<2) {
            StaticUtil.sendMessage(sender, "&cYou must provide a stall ID to delete!");
            return false;
        } 

        int id;
        try {
            id = Integer.valueOf(args[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(sender, "&cCould not parse ID '"+args[1]+"'!");
            return true;
        }

        if (stallHandler.deleteStall(id)) {
            StaticUtil.sendMessage(sender, "&aDeleted stall "+id+"!");
            return true;
        } else {
            StaticUtil.sendMessage(sender, "&cFailed to delete stall "+id+"!");
            return true;
        }
    }

    private boolean handleStatusCmd(CommandSender sender, String[] args) {
        if (args.length<2) {
            StaticUtil.sendMessage(sender, "&cYou must provide a stall ID to check out!");
            return false;
        } 

        int id;
        try {
            id = Integer.valueOf(args[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(sender, "&cCould not parse ID '"+args[1]+"'!");
            return true;
        }

        Stall stall = stallHandler.getStall(id);
        if (stall==null) {
            StaticUtil.sendMessage(sender, "&cFailed find stall "+id+"!");
            return true;
        }

        sender.sendMessage(ChatColor.DARK_PURPLE + "--- " + ChatColor.LIGHT_PURPLE + "Stall " + id + ChatColor.DARK_PURPLE + " ---\n"
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
            + ChatColor.WHITE + "Rented: " + ChatColor.GRAY + stall.isRented()
        );

        if (stall.getRenterUuid()!=null) {
            sender.sendMessage(ChatColor.WHITE + "RenterUuid: " + ChatColor.GRAY + stall.getRenterUuid().toString());
        } else sender.sendMessage(ChatColor.WHITE + "RenterUuid: " + ChatColor.GRAY + "null");

        if (stall.getRenterName()!=null) {
            sender.sendMessage(ChatColor.WHITE + "RenterName: " + ChatColor.GRAY + stall.getRenterName());
        } else sender.sendMessage(ChatColor.WHITE + "RenterName: " + ChatColor.GRAY + "null");

        if (stall.getEvictionDate()!=null) {
            sender.sendMessage(ChatColor.WHITE + "EvictionDate: " + ChatColor.GRAY + stall.getEvictionDate().toString());
        } else sender.sendMessage(ChatColor.WHITE + "EvictionDate: " + ChatColor.GRAY + "null");

        if (stall.getLastTransaction()!=null) {
            sender.sendMessage(ChatColor.WHITE + "LastTransaction: " + ChatColor.GRAY + stall.getLastTransaction().toString());
        } else sender.sendMessage(ChatColor.WHITE + "LastTransaction: " + ChatColor.GRAY + "null");

        return true;
    }

    private boolean handleUpdateCmd(CommandSender sender, String[] args) {
        if (args.length!=4) {
            StaticUtil.sendMessage(sender, "&cUsage: /stalladmin update <id> <param> <value>");
            return true;
        } 

        int id;
        try {
            id = Integer.valueOf(args[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(sender, "&cCould not parse ID '"+args[1]+"'!");
            return true;
        }

        Stall stall = stallHandler.getStall(id);
        if (stall==null) {
            StaticUtil.sendMessage(sender, "&cFailed find stall "+id+"!");
            return true;
        }

        String param = args[2].toLowerCase(), value = args[3];

        try {
        switch (param) {
            case "initialprice":
                stall.setInitialPrice(Double.valueOf(value));
                break;
            case "renewalprice":
                stall.setRenewalPrice(Double.valueOf(value));
                break;
            case "rentaltimedays":
                stall.setRentalTimeDays((int)Integer.valueOf(value));
                break;
            case "playtimedays":
                stall.setPlayTimeDays((int)Integer.valueOf(value));
                break;
            default:
                break;
        }
        } catch (Exception e) {
            StaticUtil.sendMessage(sender, "&cCaught exception updating "+param+" to "+value+"!");
            e.printStackTrace();
            return true;
        }

        if (stallHandler.updateStallInDAO(id)) {
            StaticUtil.sendMessage(sender, "&aUpdated stall #"+id+"'s "+param+" to "+value+"!");
            if (!stall.isRented()) {
                StaticUtil.StallSignSetAvaliable(stall);
            }
        } else {
            StaticUtil.sendMessage(sender, "&cUpdated stall locally but not in SQL..! Stall #"+id+"'s "+param+" to "+value+"!");
        }
        return true;
    }

    private boolean handleInfoCmd(CommandSender sender) {
        stallHandler.getShopInfo((Player) sender);
        return true;
    }

    /**
     * Handles tab completion for the /stalladmin command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (StaticUtil.hasPermission(sender, StaticUtil.ADMIN_PERM)) {
            if (args.length == 1) {
                list.clear();
                String[] subCmds = new String[]{"help","delete","status","evict","dailyTask","update"};
                for (String n : subCmds) {
                    if (n!=null && n.startsWith(args[0])) 
                        list.add(n);
                }
            }
            else if (args.length == 2) {
                list.clear();
                for (Stall stall : stallHandler.getStalls()) {
                    if (stall==null) continue;
                    list.add(String.valueOf(stall.getId()));
                }
            }
            else if (args.length == 3 && args[0].equalsIgnoreCase("update")) {
                list.clear();
                String[] subCmds = new String[]{"initialPrice","renewalPrice","rentalTimeDays","playTimeDays"};
                for (String n : subCmds) {
                    if (n!=null && n.startsWith(args[2])) 
                        list.add(n);
                }
            }
        }

        return list;
    }
}