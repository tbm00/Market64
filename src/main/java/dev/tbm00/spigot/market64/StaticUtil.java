package dev.tbm00.spigot.market64;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.chat.TextComponent;

import com.griefdefender.api.claim.Claim;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.tbm00.spigot.market64.data.ConfigHandler;
import dev.tbm00.spigot.market64.data.Stall;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.PaginatedGui;

public class StaticUtil {
    private static Market64 javaPlugin;
    private static ConfigHandler configHandler;

    public static final boolean EDITOR_PREVENTION = true;
    public static final int MAX_AREA = 6400;
    public static final int MAX_SIDE_LENGTH = 120;
    public static final int MAX_CONTAINED_CLAIMS = 2;
    public static final String MARKET_WORLD = "Tadow";
    public static final String MARKET_REGION = "market";
    public static final String PLAYER_PERM = "market64.player";
    public static final String PATH_PERM = "market64.edit-path";
    public static final String ADMIN_PERM = "market64.admin";

    public static final List<String> pendingTeleports = new CopyOnWriteArrayList<>();

    public static void init(Market64 javaPlugin, ConfigHandler configHandler) {
        StaticUtil.javaPlugin = javaPlugin;
        StaticUtil.configHandler = configHandler;
    }

    /**
     * Logs one or more messages to the server console with the prefix & specified chat color.
     *
     * @param chatColor the chat color to use for the log messages
     * @param strings one or more message strings to log
     */
    public static void log(ChatColor chatColor, String... strings) {
        if (chatColor==null) chatColor = ChatColor.WHITE;
		for (String s : strings)
            javaPlugin.getServer().getConsoleSender().sendMessage("[DSA64] " + chatColor + s);
	}

    /**
     * Formats int to "200,000" style
     * 
     * @param amount the amount to format
     * @return the formatted string
     */
    public static String formatInt(int amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    /**
     * Formats double to "200,000" style
     * 
     * @param amount the amount to format
     * @return the formatted string
     */
    public static String formatInt(double amount) {
        return formatInt((int) amount);
    }

    /**
     * Formats material to title case
     * 
     * @param amount the material to format
     * @return the formatted string
     */
    public static String formatMaterial(Material material) {
        if (material == null) return "null";

        StringBuilder builder = new StringBuilder();
        for(String word : material.toString().split("_"))
            builder.append(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase() + " ");
     
        return builder.toString().trim();
    }

    /**
     * Retrieves a player by their name.
     * 
     * @param arg the name of the player to retrieve
     * @return the Player object, or null if not found
     */
    public static Player getPlayer(String arg) {
        return javaPlugin.getServer().getPlayer(arg);
    }

    /**
     * Checks if the sender has a specific permission.
     * 
     * @param sender the command sender
     * @param perm the permission string
     * @return true if the sender has the permission, false otherwise
     */
    public static boolean hasPermission(CommandSender sender, String perm) {
        return sender.hasPermission(perm) || sender instanceof ConsoleCommandSender;
    }

    /**
     * Translates a String to use alternative color codes.
     * 
     * @param string the String to translate
     */
    public static String translate(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Sends a message to a target CommandSender.
     * 
     * @param target the CommandSender to send the message to
     * @param string the message to send
     */
    public static void sendMessage(CommandSender target, String string) {
        if (!string.isBlank())
            target.spigot().sendMessage(new TextComponent(translate(configHandler.getChatPrefix() + string)));
    }

    /**
     * Sends an Essentials mail message to a target OfflinePlayer.
     * 
     * @param target the OfflinePlayer to send the message to
     * @param string the message to send
     */
    public static void sendMail(OfflinePlayer target, String string) {
        if (!string.isBlank())
            runCommand("mail send "+target.getName()+" "+string);
    }

    /**
     * Gives a player an ItemStack.
     * If they have a full inv, it drops on the ground.
     * 
     * @param player the player to give to
     * @param item the item to give
     */
    public static void giveItem(Player player, ItemStack item) {
        if ((player.getInventory().firstEmpty() == -1)) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        } else player.getInventory().addItem(item);
    }

    /**
     * Creates a shulker box containing items from the provided list.
     * 
     * @param name the display name for the shulker box
     * @param items the list of ItemStack objects to store in the shulker box; items are removed as they are added
     * @return the created shulker box ItemStack
     */
    public static ItemStack createShulkerBox(String name, List<ItemStack> items) {
        ItemStack shulker = new ItemStack(Material.SHULKER_BOX);
        BlockStateMeta meta = (BlockStateMeta) shulker.getItemMeta();
        if (meta == null) return shulker;

        ShulkerBox shulkerBlock = (ShulkerBox) meta.getBlockState();
        Inventory shulkerInv = shulkerBlock.getInventory();
        
        // Fill the shulker
        for (int i = 0; i < 27 && !items.isEmpty(); i++) {
            shulkerInv.setItem(i, items.remove(0));
        }
        
        meta.setBlockState(shulkerBlock);
        meta.setDisplayName(name);
        
        shulker.setItemMeta(meta);
        return shulker;
    }

    public static boolean StallSignSetAvaliable(Stall stall) {
        Location loc = stall.getSignLocation();
        if (loc==null) {
            log(ChatColor.RED, "Could not find stall location for stall " + stall.getId());
            return false;
        }

        Block block = loc.getBlock();
        if (block==null || !(block.getState() instanceof Sign)) {
            log(ChatColor.RED, "Could not find sign block at signLocation for stall " + stall.getId());
            return false;
        }

        Sign sign = (Sign) block.getState();
        try {
            sign.getSide(Side.FRONT).setLine(0, translate("&1[Stall " + stall.getId() +"]"));
            sign.getSide(Side.FRONT).setLine(1, translate("&aAvailable to rent"));
            sign.getSide(Side.FRONT).setLine(2, translate("&2$"+formatInt(stall.getInitialPrice())));
            if (stall.getPlayTimeDays()!=-1)
                sign.getSide(Side.FRONT).setLine(3, translate("&cMax Playtime: "+stall.getPlayTimeDays()+"d"));
            else sign.getSide(Side.FRONT).setLine(3, translate(" "));
            sign.update();
        } catch (Exception e) {
            StaticUtil.log(ChatColor.RED, "Caught exception setting front sign text!" + e.getMessage());
        }

        try {
            sign.getSide(Side.BACK).setLine(0, translate("&1[Stall " + stall.getId() +"]"));
            sign.getSide(Side.BACK).setLine(1, translate("&aAvailable to rent"));
            sign.getSide(Side.BACK).setLine(2, translate("&2$"+formatInt(stall.getInitialPrice())));
            if (stall.getPlayTimeDays()!=-1)
                sign.getSide(Side.BACK).setLine(3, translate("&cMax Playtime: "+stall.getPlayTimeDays()+"d"));
            else sign.getSide(Side.BACK).setLine(3, translate(" "));
            sign.update();
        } catch (Exception e) {
            StaticUtil.log(ChatColor.RED, "Caught exception setting front sign text!" + e.getMessage());
        }

        return true;
    }

    public static boolean StallSignSetUnavaliable(Stall stall) {
        Location loc = stall.getSignLocation();
        if (loc==null) {
            log(ChatColor.RED, "Could not find stall location for stall " + stall.getId());
            return false;
        }

        Block block = loc.getBlock();
        if (block==null || !(block.getState() instanceof Sign)) {
            log(ChatColor.RED, "Could not find sign block at signLocation for stall " + stall.getId());
            return false;
        }

        Sign sign = (Sign) block.getState();
        try {
            sign.getSide(Side.FRONT).setLine(0, translate("&1[Stall " + stall.getId() +"]"));
            sign.getSide(Side.FRONT).setLine(1, translate("&bRented by"));
            sign.getSide(Side.FRONT).setLine(2, translate("&b"+stall.getRenterName()));
            sign.update();
        } catch (Exception e) {
            StaticUtil.log(ChatColor.RED, "Caught exception setting front sign text!" + e.getMessage());
        }

        try {
            sign.getSide(Side.BACK).setLine(0, translate("&1[Stall " + stall.getId() +"]"));
            sign.getSide(Side.BACK).setLine(1, translate("&bRented by"));
            sign.getSide(Side.BACK).setLine(2, translate("&b"+stall.getRenterName()));
            sign.update();
        } catch (Exception e) {
            StaticUtil.log(ChatColor.RED, "Caught exception setting front sign text!" + e.getMessage());
        }

        return true;
    }

    /**
     * Teleports a player to the given world and coordinates after a 5-second delay.
     * If the player moves during the delay, the teleport is cancelled.
     *
     * @param player the player to teleport
     * @param world the target world
     * @param x target x-coordinate
     * @param y target y-coordinate
     * @param z target z-coordinate
     * @return true if the teleport countdown was started, false if the player was already waiting
     */
    public static boolean teleportPlayer(Player player, World world, int x, int y, int z) {
        String playerName = player.getName();
        if (pendingTeleports.contains(playerName)) {
            sendMessage(player, "&cYou are already waiting for a teleport!");
            return false;
        }
        pendingTeleports.add(playerName);
        sendMessage(player, "&aTeleporting in 3 seconds -- don't move!");

        // Schedule the teleport to run later
        Bukkit.getScheduler().runTaskLater(javaPlugin, () -> {
            if (pendingTeleports.contains(playerName)) {
                // Remove player from pending list and teleport
                pendingTeleports.remove(playerName);
                if (world != null) {
                    Location targetLocation = new Location(world, x, y, z);
                    player.teleport(targetLocation);
                } else {
                    sendMessage(player, "&cWorld not found!");
                }
            }
        }, 60L);

        return true;
    }

    public static void disableAll(BaseGui gui) {
        gui.disableItemDrop();
        gui.disableItemPlace();
        gui.disableItemSwap();
        gui.disableItemTake();
        gui.disableOtherActions();
    }

    /**
     * Formats and sets the main GUI's info item.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setAboutItemInGui(BaseGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&fYou can rent shop stalls in our market!");
        lore.add(" ");
        lore.add("&7Some stalls are available for everyone,");
        lore.add("&7some are only available for newbies.");
        lore.add(" ");
        lore.add("&7Your stall will automatically renew when");
        lore.add("&7the time runs out, unless");
        lore.add("&7- you don't keep enough money stored in your pocket,");
        lore.add("&7- your stall's shops haven't had any recent transactions, or");
        lore.add("&7- you're no longer a newbie, if it's a newbie stall.");
        lore.add(" ");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dAbout Stalls"));
        item.setItemMeta(meta);
        item.setType(Material.DARK_OAK_SIGN);
        gui.setItem(6, 5, ItemBuilder.from(item).asGuiItem(event -> {event.setCancelled(true);}));
        lore.clear();
    }

    /**
     * Formats and sets the main GUI's info item.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setAboutStallItemInGui(BaseGui gui, ItemStack item, ItemMeta meta, List<String> lore, Stall stall) {
        lore.add("&8-----------------------");
        lore.add("&fYou can rent shop stalls in our market!");
        lore.add(" ");
        lore.add("&7Some stalls are available for everyone,");
        lore.add("&7some are only available for newbies.");
        lore.add(" ");
        lore.add("&7Your stall will automatically renew when");
        lore.add("&7the time runs out, unless");
        lore.add("&7- you don't keep enough money stored in your pocket,");
        lore.add("&7- your stall's shops haven't had any recent transactions, or");
        lore.add("&7- you're no longer a newbie, if it's a newbie stall.");
        lore.add(" ");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dAbout Stalls"));
        item.setItemMeta(meta);
        item.setType(Material.DARK_OAK_SIGN);
        gui.setItem(6, 5, ItemBuilder.from(item).asGuiItem(event -> {event.setCancelled(true);}));
        lore.clear();
    }

    /**
     * Executes a command as the console.
     * 
     * @param command the command to execute
     * @return true if the command was successfully executed, false otherwise
     */
    public static boolean runCommand(String command) {
        ConsoleCommandSender console = javaPlugin.getServer().getConsoleSender();
        try {
            return Bukkit.dispatchCommand(console, command);
        } catch (Exception e) {
            log(ChatColor.RED, "Caught exception running command " + command + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Executes a command as a specific player.
     * 
     * @param target the player to execute the command as
     * @param command the command to execute
     * @return true if the command was successfully executed, false otherwise
     */
    public static boolean sudoCommand(Player target, String command) {
        try {
            return Bukkit.dispatchCommand(target, command);
        } catch (Exception e) {
            log(ChatColor.RED, "Caught exception sudoing command: " + target.getName() + " : /" + command + ": " + e.getMessage());
            return false;
        }
    }

   /**
     * Executes a command as a specific human entity.
     * 
     * @param target the player to execute the command as
     * @param command the command to execute
     * @return true if the command was successfully executed, false otherwise
     */
    public static boolean sudoCommand(HumanEntity target, String command) {
        try {
            return Bukkit.dispatchCommand(target, command);
        } catch (Exception e) {
            log(ChatColor.RED, "Caught exception sudoing command: " + target.getName() + " : /" + command + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets OfflinePlayer's cumulative playtime in seconds.
     *
     * @return int representing the player playtime in seconds
     */
    public static int getPlaytimeSeconds(OfflinePlayer player) {
        int current_play_ticks;
        try {
            current_play_ticks = player.getStatistic(Statistic.valueOf("PLAY_ONE_MINUTE"));
        } catch (Exception e) {
            log(ChatColor.RED, "CCaught exception getting player statistic PLAY_ONE_MINUTE: " + e.getMessage());
            try {
                current_play_ticks = player.getStatistic(Statistic.valueOf("PLAY_ONE_TICK"));
            } catch (Exception e2) {
                log(ChatColor.RED, "CCaught exception getting player statistic PLAY_ONE_TICK: " + e2.getMessage());
                current_play_ticks = 0;
            }
        }
        return current_play_ticks/20;
    }

    /**
     * Checks if the GriefDefender claim is contained within the WorldGuard region
     *
     * @return true if contained, false otherwise
     */
    public static boolean isClaimTooLarge(Claim userClaim) {
        if (userClaim==null) return false;

        int len, wid, area;
        len = userClaim.getLength();
        wid = userClaim.getWidth();
        if (len>MAX_SIDE_LENGTH || wid>MAX_SIDE_LENGTH) return true;

        area = len*wid;
        if (area>MAX_AREA) return true;

        return false;
    }

    /**
     * Checks if the GriefDefender claim is contained within the WorldGuard region
     *
     * @return true if contained, false otherwise
     */
    public static boolean isClaimContained(ProtectedRegion wgRegion, Claim userClaim) {
        if (userClaim==null) return false;

        Vector3i northWest  = userClaim.getLesserBoundaryCorner();
        Vector3i southEast = userClaim.getGreaterBoundaryCorner();

        Vector3i[] claimedBlocks = new Vector3i[] {
            new Vector3i(northWest.getX(),  100, northWest.getZ()),     // NW
            new Vector3i(southEast.getX(), 100, northWest.getZ()),      // NE
            new Vector3i(southEast.getX(), 100, southEast.getZ()),      // SE
            new Vector3i(northWest.getX(),  100, southEast.getZ()),      // SW
            new Vector3i(((northWest.getX()+southEast.getX())/2),  100, southEast.getZ()), // S
            new Vector3i(((northWest.getX()+southEast.getX())/2),  100, northWest.getZ()), // N
            new Vector3i(northWest.getX(),  100, ((northWest.getX()+southEast.getX())/2)), // W
            new Vector3i(southEast.getX(),  100, ((northWest.getX()+southEast.getX())/2)), // E
        };

        for (Vector3i block : claimedBlocks) {
            if (wgRegion.contains(block.getX(), 100, block.getZ())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the GriefDefender claim is contained within the WorldGuard region
     *
     * @return true if contained, false otherwise
     */
    public static boolean isClaimPartlyContained(ProtectedRegion wgRegion, Claim userClaim) {
        if (userClaim==null) return false;

        if (!isClaimContained(wgRegion, userClaim)) return false;

        Vector3i northWest  = userClaim.getLesserBoundaryCorner();
        Vector3i southEast = userClaim.getGreaterBoundaryCorner();

        Vector3i[] claimedBlocks = new Vector3i[] {
            new Vector3i(northWest.getX(),  100, northWest.getZ()),     // NW
            new Vector3i(southEast.getX(), 100, northWest.getZ()),      // NE
            new Vector3i(southEast.getX(), 100, southEast.getZ()),      // SE
            new Vector3i(northWest.getX(),  100, southEast.getZ()),      // SW
            new Vector3i(((northWest.getX()+southEast.getX())/2),  100, southEast.getZ()), // S
            new Vector3i(((northWest.getX()+southEast.getX())/2),  100, northWest.getZ()), // N
            new Vector3i(northWest.getX(),  100, ((northWest.getX()+southEast.getX())/2)), // W
            new Vector3i(southEast.getX(),  100, ((northWest.getX()+southEast.getX())/2)), // E
        };

        for (Vector3i block : claimedBlocks) {
            if (!wgRegion.contains(block.getX(), 100, block.getZ())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the GriefDefender claim is contained within the WorldGuard region
     *
     * @return true if contained, false otherwise
     */
    public static boolean hasMaxContainedClaims(ProtectedRegion wgRegion, Set<Claim> userClaims) {
        if (userClaims==null || userClaims.isEmpty()) return false;

        int count = -1;
        for (Claim claim : userClaims) {
            if (isClaimContained(wgRegion, claim)) {
                count++;
            }
            if (count>=MAX_CONTAINED_CLAIMS) return true;
        }
        return false;
    }

    /**
     * Formats and sets the main GUI's footer's previous page button.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param label holder for gui's title
     */
    public static void setGuiItemPageBack(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, String label) {
        lore.add("&8-----------------------");
        lore.add("&6Click to go to the previous page");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fPrevious Page"));
        item.setItemMeta(meta);
        item.setType(Material.STONE_BUTTON);
        gui.setItem(6, 4, ItemBuilder.from(item).asGuiItem(event -> handlePageClick(event, gui, false, label)));
        lore.clear();
    }

    /**
     * Formats and sets the main GUI's footer's next page button.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param label holder for gui's title
     */
    public static void setGuiItemPageNext(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, String label) {
        lore.add("&8-----------------------");
        lore.add("&6Click to go to the next page");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fNext Page"));
        item.setItemMeta(meta);
        item.setType(Material.STONE_BUTTON);
        gui.setItem(6, 6, ItemBuilder.from(item).asGuiItem(event -> handlePageClick(event, gui, true, label)));
        lore.clear();
    }

    /**
     * Handles the event when a page button is clicked.
     * 
     * @param event the inventory click event
     * @param next true to go to the next page; false to go to the previous page
     */
    private static void handlePageClick(InventoryClickEvent event, PaginatedGui gui, boolean next, String label) {
        event.setCancelled(true);
        if (next) gui.next();
        else gui.previous();
        gui.updateTitle(label + gui.getCurrentPageNum() + "/" + gui.getPagesNum());
    }
}