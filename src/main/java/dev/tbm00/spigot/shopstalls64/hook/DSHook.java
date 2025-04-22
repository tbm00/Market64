package dev.tbm00.spigot.shopstalls64.hook;

import xzot1k.plugins.ds.DisplayShops;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;

public class DSHook {

    public DisplayShops pl;
    
    public DSHook(ShopStalls64 javaPlugin) {
        pl = (DisplayShops) javaPlugin.getServer().getPluginManager().getPlugin("DisplayShops");
    }
}
