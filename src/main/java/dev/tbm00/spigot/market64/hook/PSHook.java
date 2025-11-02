package dev.tbm00.spigot.market64.hook;

import org.bukkit.Location;

import com.griefdefender.lib.flowpowered.math.vector.Vector3i;

import dev.tbm00.papermc.playershops64.PlayerShops64;
import dev.tbm00.papermc.playershops64.data.structure.Shop;

import dev.tbm00.spigot.market64.Market64;

public class PSHook {

    public PlayerShops64 pl;
    
    public PSHook(Market64 javaPlugin) {
        pl = ((PlayerShops64) javaPlugin.getServer().getPluginManager().getPlugin("PlayerShops64"));
    }

    public boolean isInRegion(Location location, Vector3i lowerNorthWestCorner, Vector3i upperSouthEastCorner) {
        double x = location.getX(), y = location.getY(), z = location.getZ();

        // Calculate the minimum and maximum boundaries on each axis
        int minX = Math.min(lowerNorthWestCorner.getX(), upperSouthEastCorner.getX());
        int maxX = Math.max(lowerNorthWestCorner.getX(), upperSouthEastCorner.getX());
        int minY = Math.min(lowerNorthWestCorner.getY(), upperSouthEastCorner.getY());
        int maxY = Math.max(lowerNorthWestCorner.getY(), upperSouthEastCorner.getY());
        int minZ = Math.min(lowerNorthWestCorner.getZ(), upperSouthEastCorner.getZ());
        int maxZ = Math.max(lowerNorthWestCorner.getZ(), upperSouthEastCorner.getZ());

        // Check if the coordinate is within the boundaries
        return (x >= minX && x <= maxX) &&
               (y >= minY && y <= maxY) &&
               (z >= minZ && z <= maxZ);
    }

    public void upsertShop(Shop shop) {
        pl.getShopHandler().upsertShopObject(null);
    }
}