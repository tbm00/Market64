

package dev.tbm00.spigot.shopstalls64.command;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import xzot1k.plugins.ds.api.objects.Shop;
import xzot1k.plugins.ds.api.objects.LocationClone;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;
import dev.tbm00.spigot.shopstalls64.utils.*;

public class AdminCmd implements TabExecutor {
    private final String ADMIN_PERM = "shopstalls64.admin";

    public AdminCmd() {}

    /**
     * Handles the /testshopadmin command.
     * 
     * @param player the command sender
     * @param consoleCommand the command being executed
     * @param alias the alias used for the command
     * @param args the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!Utils.hasPermission(sender, ADMIN_PERM)) {
            Utils.sendMessage(sender, "&cNo permission!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) return ShopUtils.handleGuiAdminCmd(player);

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "help":
                return handleHelpCmd(player);
            case "transfer":
                return handleTransferCmd(player, args);
            case "pos1":
                return handlePosCmd(player, subCmd);
            case "pos2":
                return handlePosCmd(player, subCmd);
            case "copy":
                return handlePosCmd(player, subCmd);
            case "paste":
                return handlePasteCmd(player);
            case "count":
                return handleCountCmd(player);
            case "gui":
                return ShopUtils.handleGuiAdminCmd(player);
            default:
                return ShopUtils.handleAdminSearch(player, args);
        }
    }
    
    /**
     * Handles the sub command for the help menu.
     * 
     * @param player the command sender
     * @return true after displaying help menu
     */
    private boolean handleHelpCmd(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "--- " + ChatColor.LIGHT_PURPLE + "Shop Admin Commands" + ChatColor.DARK_PURPLE + " ---\n"
            + ChatColor.WHITE + "/testshopadmin" + ChatColor.GRAY + " View/manage all shops\n"
            + ChatColor.WHITE + "/testshopadmin <item/player>" + ChatColor.GRAY + " View/manage all <item/player> shops\n"
            + ChatColor.WHITE + "/testshopadmin transfer <playerTo> <playerFrom>" + ChatColor.GRAY + " Change shops' owner\n"
            + ChatColor.WHITE + "/testshopadmin [pos1/pos2/copy]" + ChatColor.GRAY + " Set copy positions\n"
            + ChatColor.WHITE + "/testshopadmin paste" + ChatColor.GRAY + " Set paste position & paste"
        );
        return true;
    }
    
    /**
     * Handles the sub command for transfering shops from player-to-player.
     * 
     * @param sender the command sender
     * @param args the arguments passed to the command
     * @return true if after processing command
     */
    private boolean handleTransferCmd(Player sender, String[] args) {
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();

        if (args.length<3) {
            Utils.sendMessage(sender, ChatColor.RED + "Usage: /testshopadmin transfer <playerTo> <playerFrom>");
            return true;
        }

        UUID uuidA = UUID.fromString(ShopStalls64.repHook.getRepManager().getPlayerUUID(args[1]));
        if (uuidA == null) {
            Utils.sendMessage(sender, ChatColor.RED + "Could not find target (from) player!");
            return true;
        } 
        UUID uuidB = UUID.fromString(ShopStalls64.repHook.getRepManager().getPlayerUUID(args[2]));
        if (uuidB == null) {
            Utils.sendMessage(sender, ChatColor.RED + "Could not find target (to) player!");
            return true;
        } 

        int i = 0;
        for (Shop shop : dsMap.values()) {
            if (shop.getOwnerUniqueId()!=null && shop.getOwnerUniqueId().equals(uuidA)) {
                shop.setOwnerUniqueId(uuidB);
                ++i;
            }
        }

        Utils.sendMessage(sender, ChatColor.YELLOW + "Transferred " + i + " shops to " + args[2]);
        return true;
    }

    /**
     * Handles the sub command for setting clipboard positioning.
     * 
     * @param sender the command sender
     * @param subCmd the position to save
     * @return true if after processing command
     */
    private boolean handlePosCmd(Player sender, String subCmd) {
        if (subCmd.equalsIgnoreCase("pos1")) {
            ShopUtils.x1 = (int) Math.floor(sender.getLocation().getX());
            ShopUtils.y1 = (int) Math.floor(sender.getLocation().getY());
            ShopUtils.z1 = (int) Math.floor(sender.getLocation().getZ());
        } else if (subCmd.equalsIgnoreCase("pos2")) {
            ShopUtils.x2 = (int) Math.floor(sender.getLocation().getX());
            ShopUtils.y2 = (int) Math.floor(sender.getLocation().getY());
            ShopUtils.z2 = (int) Math.floor(sender.getLocation().getZ());
        } else if (subCmd.equalsIgnoreCase("copy")) {
            ShopUtils.xc = (int) Math.floor(sender.getLocation().getX());
            ShopUtils.yc = (int) Math.floor(sender.getLocation().getY());
            ShopUtils.zc = (int) Math.floor(sender.getLocation().getZ());
            ShopUtils.clipboardWorld = sender.getWorld().getName();
        }

        Utils.sendMessage(sender, ChatColor.GREEN + subCmd + " coords saved!");
        return true;
    }

    /**
     * Handles the sub command for changing shop coords in region.
     * 
     * @param sender the command sender
     * @param subCmd the position to save
     * @return true if after processing command
     */
    private boolean handlePasteCmd(Player sender) {
        int xp = (int) Math.floor(sender.getLocation().getX()),
            yp = (int) Math.floor(sender.getLocation().getY()),
            zp = (int) Math.floor(sender.getLocation().getZ());
        int xd = xp-ShopUtils.xc, yd = yp-ShopUtils.yc, zd = zp-ShopUtils.zc;
        String worldp = sender.getWorld().getName();

        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        int i = 0;
        for (Shop shop : dsMap.values()) {
            LocationClone shopLoc = shop.getBaseLocation();
            if (!shopLoc.getWorldName().equalsIgnoreCase(ShopUtils.clipboardWorld)) continue;
            if (!isInRegion(shopLoc.getX(), shopLoc.getY(), shopLoc.getZ())) continue;
            double x = shopLoc.getX(), y=shopLoc.getY(), z=shopLoc.getZ();

            shop.unRegister();

            shopLoc.setWorldName(worldp);
            shopLoc.setX(x+xd);
            shopLoc.setY(y+yd);
            shopLoc.setZ(z+zd);

            shop.setBaseLocation(shopLoc);
            shop.register();
            ++i;
        }
        Utils.sendMessage(sender, ChatColor.GREEN + "Moved " + i + " shops!");
        return true;
    }
    
    /**
     * Handles the sub command for counting shops in region.
     * 
     * @param sender the command sender
     * @param subCmd the position to save
     * @return true if after processing command
     */
    private boolean handleCountCmd(Player sender) {
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        int i = 0;
        for (Shop shop : dsMap.values()) {
            LocationClone shopLoc = shop.getBaseLocation();
            if (!shopLoc.getWorldName().equalsIgnoreCase(ShopUtils.clipboardWorld)) continue;
            if (!isInRegion(shopLoc.getX(), shopLoc.getY(), shopLoc.getZ())) continue;
            ++i;
        }
        Utils.sendMessage(sender, ChatColor.GREEN + "Found " + i + " shops!");
        return true;
    }

    /**
     * Checks if the given coordinate is within the region.
     *
     * @param x     The x-coordinate.
     * @param y     The y-coordinate.
     * @param z     The z-coordinate.
     * @return true if the coordinate is within the region, false otherwise.
     */
    public boolean isInRegion(double x, double y, double z) {
        // Calculate the minimum and maximum boundaries on each axis
        int minX = Math.min(ShopUtils.x1, ShopUtils.x2);
        int maxX = Math.max(ShopUtils.x1, ShopUtils.x2);
        int minY = Math.min(ShopUtils.y1, ShopUtils.y2);
        int maxY = Math.max(ShopUtils.y1, ShopUtils.y2);
        int minZ = Math.min(ShopUtils.z1, ShopUtils.z2);
        int maxZ = Math.max(ShopUtils.z1, ShopUtils.z2);

        // Check if the coordinate is within the boundaries
        return (x >= minX && x <= maxX) &&
               (y >= minY && y <= maxY) &&
               (z >= minZ && z <= maxZ);
    }

    /**
     * Handles tab completion for the /testshopadmin command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.clear();
            String[] subCmds = new String[]{"<item>","<player>","transfer","pos1","pos2","copy","paste"};
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
            if (args[0].equals("transfer")) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getName().startsWith(args[1]))
                        list.add(player.getName());
                });
            }
        } else if (args.length == 3) {
            if (args[0].equals("transfer")) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getName().startsWith(args[1]))
                        list.add(player.getName());
                });
            }
        }
        return list;
    }
}