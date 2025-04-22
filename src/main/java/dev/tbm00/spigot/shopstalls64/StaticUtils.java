package dev.tbm00.spigot.shopstalls64;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.ShulkerBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.EconomyResponse;

import dev.tbm00.spigot.shopstalls64.data.ConfigHandler;

public class StaticUtils {
    private static ShopStalls64 javaPlugin;
    private static ConfigHandler configHandler;

    public static void init(ShopStalls64 javaPlugin, ConfigHandler configHandler) {
        StaticUtils.javaPlugin = javaPlugin;
        StaticUtils.configHandler = configHandler;
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
    private ItemStack createShulkerBox(String name, List<ItemStack> items) {
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
     * Attempts to remove a specified amount of money from the player's account.
     *
     * @param player the player from whose account the money will be withdrawn
     * @param amount the amount of money to remove from the account
     * @return true if the withdrawal transaction was successful, false otherwise
     */
    public static boolean removeMoney(Player player, double amount) {
        EconomyResponse r = ShopStalls64.ecoHook.pl.withdrawPlayer(player, amount);
        if (r.transactionSuccess()) {
            return true;
        } else return false;
    }

    /**
     * Attempts to add a specified amount of money to the player's account.
     *
     * @param player the player whose account will receive the deposit
     * @param amount the amount of money to add to the account
     * @return true if the deposit transaction was successful, false otherwise
     */
    public static boolean addMoney(Player player, double amount) {
        EconomyResponse r = ShopStalls64.ecoHook.pl.depositPlayer(player, amount);
        if (r.transactionSuccess()) {
            return true;
        } else return false;
    }
}