package dev.tbm00.spigot.market64.listener;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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

public class MarketClaimListener implements Listener {
    World world = null;
    ProtectedRegion wgRegion = null;

    public MarketClaimListener(Market64 javaPlugin, WGHook wgHook) {
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
     * Cancels the creation event if the player has X claims within the region already
     *
     * @param event the CreateClaimEvent
     */
    public void onClaimCreation(CreateClaimEvent event) {

        Player player = (Player) event.getSourceUser().getOnlinePlayer();
        if (player != null && player instanceof Player && (player.hasPermission(StaticUtil.BYPASS_PERM))) { 
            return;
        }

        Claim claim = event.getClaim();

        if (!claim.getWorldName().equalsIgnoreCase(StaticUtil.MARKET_WORLD)) return;
        if (!StaticUtil.isClaimContained(wgRegion, claim)) return;

        if (player != null && player instanceof Player && (player.hasPermission(StaticUtil.MARKET_DENIED_PERM))) { 
            event.cancelled(true);
            StaticUtil.sendMessage(player, "&4Error: &cYou are blacklisted from the market..!");
            return;
        }

        if (claim.isSubdivision()) {
            event.cancelled(true);
            StaticUtil.sendMessage(player, "&4Error: &cCannot make subdivision claims in the market..!");
            return;
        }

        if (claim.isTown()) {
            event.cancelled(true);
            StaticUtil.sendMessage(player, "&4Error: &cCannot make town claims in the market..!");
            return;
        }

        if (StaticUtil.isClaimTooLarge(claim)) {
            event.cancelled(true);

            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &cMax plot area is currently "+StaticUtil.MARKET_MAX_AREA+" blocks... It will increase as the market grows -- please leave room for others!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimCreation listener!");
            }
            return;
        }

        if (StaticUtil.areSidesTooLarge(claim)) {
            event.cancelled(true);

            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &cMax plot side length is currently "+StaticUtil.MARKET_MAX_SIDE_LENGTH+" blocks... It will increase as the market grows -- please leave room for others!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimCreation listener!");
            }
            return;
        }
    
        if (StaticUtil.hasMaxContainedClaims(wgRegion, event.getSourceUser().getPlayerData().getClaims(), StaticUtil.MARKET_MAX_CONTAINED_CLAIMS)) {
            event.cancelled(true);

            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &cMax market plots per player is "+StaticUtil.MARKET_MAX_CONTAINED_CLAIMS+"... It will increase as the market grows -- please leave room for others!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimCreation listener!");
            }
            return;
        }

        if (StaticUtil.isClaimPartlyNotContained(wgRegion, claim)) {
            event.cancelled(true);

            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &cCannot create claims that are partly not inside our market region!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimExpansion listener!");
            }
            return;
        }
    }

    /**
     * Cancels the change event if the claim is not completely inside the market
     *
     * @param event the ClaimExpansion
     */
    public void onClaimExpansion(ChangeClaimEvent event) {

        Player player = (Player) event.getSourceUser().getOnlinePlayer();
        if (player != null && player instanceof Player && (player.hasPermission(StaticUtil.BYPASS_PERM))) { 
            return;
        }

        Claim claim = event.getClaim();

        if (!claim.getWorldName().equalsIgnoreCase(StaticUtil.MARKET_WORLD)) return;
        if (!StaticUtil.isClaimContained(wgRegion, claim)) return;

        if (player != null && player instanceof Player && (player.hasPermission(StaticUtil.MARKET_DENIED_PERM))) { 
            event.cancelled(true);
            StaticUtil.sendMessage(player, "&4Error: &cYou are blacklisted from the market..!");
            return;
        }

        if (claim.isSubdivision()) {
            event.cancelled(true);
            StaticUtil.sendMessage(player, "&4Error: &cCannot make subdivision claims in the market..!");
            return;
        }

        if (claim.isTown()) {
            event.cancelled(true);
            StaticUtil.sendMessage(player, "&4Error: &cCannot make town claims in the market..!");
            return;
        }

        if (StaticUtil.isClaimTooLarge(claim)) {
            event.cancelled(true);

            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &cMax plot area is currently "+StaticUtil.MARKET_MAX_AREA+" blocks... It will increase as the market grows -- please leave room for others!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimExpansion listener!");
            }
            return;
        }

        if (StaticUtil.areSidesTooLarge(claim)) {
            event.cancelled(true);

            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &cMax plot side length is currently "+StaticUtil.MARKET_MAX_SIDE_LENGTH+" blocks... It will increase as the market grows -- please leave room for others!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimExpansion listener!");
            }
            return;
        }

        if (StaticUtil.isClaimPartlyNotContained(wgRegion, claim)) {
            event.cancelled(true);

            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &cCannot expand claims into our market!");
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimExpansion listener!");
            }
            return;
        }
    }
}