package dev.tbm00.spigot.market64.listener;

import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import xzot1k.plugins.ds.api.events.ShopTransactionEvent;
import dev.tbm00.spigot.market64.Market64;
import dev.tbm00.spigot.market64.StallHandler;
import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.data.Stall;

public class ShopTransaction implements Listener {
    private final Market64 javaPlugin;
    private final StallHandler stallHandler;

    public ShopTransaction(Market64 javaPlugin, StallHandler stallHandler) {
        this.javaPlugin = javaPlugin;
        this.stallHandler = stallHandler;
    }

    /**
     * Updates the stall's last transaction date if its active.
     *
     * @param event the ShopTransactionEvent
     */
    @EventHandler
    public void onShopTransaction(ShopTransactionEvent event) {
        final UUID shopId = event.getShop().getShopId();
        final int initialStock = event.getShop().getStock();
        for (Stall stall : stallHandler.getStalls()) {
            if ((stall != null && stall.isRented()) && stall.getShopUuids().contains(shopId)) {
                final int stallId = stall.getId();

                // Schedule the transaction check later
                Bukkit.getScheduler().runTaskLater(javaPlugin, () -> {
                    if (stallHandler.dsHook.pl.getManager().getShopById(shopId).getStock()!=initialStock) {
                        stallHandler.getStall(stallId).setLastTransaction(new Date());

                        if (!stallHandler.getStallDao().update(stallHandler.getStall(stallId))) {
                            StaticUtil.log(ChatColor.RED, "stallHandler.getStallDao().update(stallHandler.getStall(stallId)) failed after updating shop transaction for stall " + stall.getId() +"!");
                        }
                    }
                }, 1200L);
            }
        }
    }
}