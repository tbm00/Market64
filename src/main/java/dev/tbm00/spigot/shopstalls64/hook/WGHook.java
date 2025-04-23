package dev.tbm00.spigot.shopstalls64.hook;

import com.sk89q.worldguard.WorldGuard;

public class WGHook {

    public WorldGuard pl;

    public WGHook() {
        pl = WorldGuard.getInstance();
    }
}
