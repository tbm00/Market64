package dev.tbm00.spigot.shopstalls64.hook;

import xzot1k.plugins.ds.DisplayShops;
import xzot1k.plugins.ds.api.objects.LocationClone;

import com.griefdefender.lib.flowpowered.math.vector.Vector3i;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;

public class DSHook {

    public DisplayShops pl;
    
    public DSHook(ShopStalls64 javaPlugin) {
        pl = ((DisplayShops) javaPlugin.getServer().getPluginManager().getPlugin("DisplayShops"));
    }


    public boolean isInRegion(LocationClone location, Vector3i lowerNorthWestCorner, Vector3i upperSouthEastCorner) {
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
}
