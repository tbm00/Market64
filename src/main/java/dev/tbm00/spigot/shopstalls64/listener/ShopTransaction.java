package dev.tbm00.spigot.shopstalls64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import dev.tbm00.spigot.shopstalls64.StallHandler;
import xzot1k.plugins.ds.api.events.ShopTransactionEvent;

public class ShopTransaction implements Listener {
    private final StallHandler stallHandler;

    public ShopTransaction(StallHandler stallHandler) {
        this.stallHandler = stallHandler;
    }

    /**
     * Triggers stallHandler.updateLatestTranscation(Stall) if the shop belongs to
     * a stall (i.e. event.getShop().getShopId() is in stallHandler.getStalls())
     * Cancels pending teleports if any.
     *
     * @param event the PlayerMoveEvent
     */
    @EventHandler
    public void onShopTransaction(ShopTransactionEvent event) {

    }
}

