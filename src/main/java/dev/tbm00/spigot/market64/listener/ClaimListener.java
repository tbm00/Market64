package dev.tbm00.spigot.market64.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.event.ChangeClaimEvent;
import com.griefdefender.api.event.CreateClaimEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.tbm00.spigot.market64.Market64;
import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.hook.WGHook;

public class ClaimListener implements Listener {
    World world = null;
    ProtectedRegion wgRegion = null;

    public ClaimListener(Market64 javaPlugin, WGHook wgHook) {
        world = javaPlugin.getServer().getWorld(StaticUtil.MARKET_WORLD);
        if (world == null) {
            StaticUtil.log(ChatColor.RED, "Required world " + StaticUtil.MARKET_WORLD + " is not loaded on the server!");
            throw new IllegalStateException();
        }

        com.sk89q.worldedit.world.World wgWorld = BukkitAdapter.adapt(world);
        if (wgWorld == null) {
            StaticUtil.log(ChatColor.RED, "Required world " + StaticUtil.MARKET_WORLD + " was not found in WorldGuard!");
            throw new IllegalStateException();
        }

        RegionManager wgManager = wgHook.pl.getPlatform().getRegionContainer().get(wgWorld);
        if (wgManager == null) {
            StaticUtil.log(ChatColor.RED, "Required world " + StaticUtil.MARKET_WORLD + "'s region manager was not found in WorldGuard!");
            throw new IllegalStateException();
        }

        wgRegion = wgManager.getRegion(StaticUtil.MARKET_REGION);
        if (wgRegion == null) {
            StaticUtil.log(ChatColor.RED, "Required region " + StaticUtil.MARKET_REGION + "' was not found in WorldGuard!");
            throw new IllegalStateException();
        }

        GriefDefender.getEventManager().getBus().subscribe(CreateClaimEvent.class, event -> onClaimCreation(event));
        GriefDefender.getEventManager().getBus().subscribe(ChangeClaimEvent.class, event -> onClaimExpansion(event));
    }

    /**
     * Cancels the break block event if block is a path block
     *
     * @param event the ClaimExpansion
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (!block.getType().equals(Material.DIRT_PATH)) return;
        if (!block.getWorld().equals(world)) return;
        if (!block.getRelative(BlockFace.DOWN).getType().equals(Material.BEDROCK)) return;
        if (wgRegion.contains(block.getX(), block.getY(), block.getZ()) && !event.getPlayer().hasPermission(StaticUtil.ADMIN_PERM)) {
            StaticUtil.sendMessage(event.getPlayer(), "&cError: &fMarket path protection!");
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Cancels the creation event if the player has X claims within the region already
     *
     * @param event the CreateClaimEvent
     */
    public void onClaimCreation(CreateClaimEvent event) {

        Claim claim = event.getClaim();

        if (!claim.getWorldName().equalsIgnoreCase(StaticUtil.MARKET_WORLD)) return;
        if (!StaticUtil.isClaimContained(wgRegion, claim)) return;

        if (StaticUtil.isClaimTooLarge(claim)) {
            event.cancelled(true);

            Player player = (Player) event.getSourceUser().getOnlinePlayer();
            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &fClaim is too big for our market... please leave room for others!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimCreation listener!");
            }
        }
    
        if (StaticUtil.hasMaxContainedClaims(wgRegion, event.getSourceUser().getPlayerData().getClaims())) {
            event.cancelled(true);

            Player player = (Player) event.getSourceUser().getOnlinePlayer();
            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &fYou cannot have more than "+StaticUtil.MAX_CONTAINED_CLAIMS+" claims in our market!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimCreation listener!");
            }
        }
    }

    /**
     * Cancels the change event if the claim is not completely inside the market
     *
     * @param event the ClaimExpansion
     */
    public void onClaimExpansion(ChangeClaimEvent event) {

        Claim claim = event.getClaim();

        if (!claim.getWorldName().equalsIgnoreCase(StaticUtil.MARKET_WORLD)) return;
        if (!StaticUtil.isClaimContained(wgRegion, claim)) return;

        if (StaticUtil.isClaimTooLarge(claim)) {
            event.cancelled(true);

            Player player = (Player) event.getSourceUser().getOnlinePlayer();
            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &fClaim is too big for our market... please leave room for others!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimCreation listener!");
            }
        }

        if (StaticUtil.isClaimPartlyContained(wgRegion, claim)) {
            event.cancelled(true);

            Player player = (Player) event.getSourceUser().getOnlinePlayer();
            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &fYou cannot expand a claim into our market!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimExpansion listener!");
            }
        }
    }
}