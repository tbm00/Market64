package dev.tbm00.spigot.market64;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.Listener;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.event.ChangeClaimEvent;
import com.griefdefender.api.event.CreateClaimEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.tbm00.spigot.market64.hook.WGHook;
import dev.tbm00.spigot.market64.listener.ClaimCreation;
import dev.tbm00.spigot.market64.listener.ClaimExpansion;

public class ClaimHandler implements Listener {

    public ClaimHandler(Market64 javaPlugin, WGHook wgHook) {

        World world = javaPlugin.getServer().getWorld(StaticUtil.MARKET_WORLD);
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

        ProtectedRegion wgRegion = wgManager.getRegion(StaticUtil.MARKET_REGION);
        if (wgRegion == null) {
            StaticUtil.log(ChatColor.RED, "Required region " + StaticUtil.MARKET_REGION + "' was not found in WorldGuard!");
            throw new IllegalStateException();
        }

        GriefDefender.getEventManager().getBus().subscribe(CreateClaimEvent.class, event -> new ClaimCreation(event, wgRegion));
        GriefDefender.getEventManager().getBus().subscribe(ChangeClaimEvent.class, event -> new ClaimExpansion(event, wgRegion));
    }
}