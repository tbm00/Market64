package dev.tbm00.spigot.shopstalls64.task;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import xzot1k.plugins.ds.api.objects.Shop;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;

public class DescChangeTask {

    /**
     * DescChange task updates shop descriptions for specific items.
     */
    public DescChangeTask() {
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        for (Shop shop : dsMap.values()) {
            if (shop.getShopItem()==null) continue;
            Double buyPrice = shop.getBuyPrice(false), 
                    sellPrice = shop.getSellPrice(false);
            if (buyPrice<0 && sellPrice<0) continue;

            ItemStack item = shop.getShopItem().clone();
            ItemMeta meta = item.getItemMeta();

            if (item.getType().equals(Material.SPAWNER) && meta.hasLore())
                shop.setDescription(meta.getLore().get(0)
                                    .replace("§e", "§c")
                                    .replace("_", " "));
        }
    }
}