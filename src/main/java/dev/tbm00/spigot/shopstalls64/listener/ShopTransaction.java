package dev.tbm00.spigot.shopstalls64.listener;

import java.util.Date;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import xzot1k.plugins.ds.api.events.ShopTransactionEvent;

import dev.tbm00.spigot.shopstalls64.StallHandler;
import dev.tbm00.spigot.shopstalls64.data.Stall;

public class ShopTransaction implements Listener {
    private final StallHandler stallHandler;

    public ShopTransaction(StallHandler stallHandler) {
        this.stallHandler = stallHandler;
    }

    /**
     * Process (in order): 
     *  - Updates the stall's last transcation date
     * 
     *  - Updates internal Stall object
     *  - Updates Stall entry in SQL database
     *  
     *  - Returns true on success, false when there was an error
     */
    /**
     * Updates the stall's last transcation date if its active.
     *
     * @param event the ShopTransactionEvent
     */
    @EventHandler
    public void onShopTransaction(ShopTransactionEvent event) {
        UUID shopId = event.getShop().getShopId();
        for (Stall stall : stallHandler.getStalls()) {
            if ((stall != null && stall.isRented()) && stall.getShopUuids().contains(shopId)) {
                stall.setLastTranscation(new Date());
                stallHandler.getStallDao().update(stall);
            }
        }
    }
}

