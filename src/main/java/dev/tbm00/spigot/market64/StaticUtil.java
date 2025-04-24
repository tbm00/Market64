package dev.tbm00.spigot.market64;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.ShulkerBox;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import net.md_5.bungee.api.chat.TextComponent;

import com.griefdefender.api.claim.Claim;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.tbm00.spigot.market64.data.ConfigHandler;

public class StaticUtil {
    private static Market64 javaPlugin;
    private static ConfigHandler configHandler;

    public static final int MAX_CONTAINED_CLAIMS = 2;
    public static final String MARKET_WORLD = "Tadow";
    public static final String MARKET_REGION = "market";

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
        if (sender instanceof Player && ((Player)sender).getGameMode()==GameMode.CREATIVE) return false;
        return sender.hasPermission(perm) || sender instanceof ConsoleCommandSender;
    }

    /**
     * Sends a message to a target CommandSender.
     * 
     * @param target the CommandSender to send the message to
     * @param string the message to send
     */
    public static void sendMessage(CommandSender target, String string) {
        if (!string.isBlank())
            target.spigot().sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', configHandler.getChatPrefix() + string)));
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
}