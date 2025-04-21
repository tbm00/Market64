package dev.tbm00.spigot.shopstalls64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import dev.tbm00.spigot.shopstalls64.utils.Utils;

public class PlayerMovement implements Listener {

    /**
     * Handles the player movement event.
     * Cancels pending teleports if any.
     *
     * @param event the PlayerMoveEvent
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Utils.pendingTeleports.contains(event.getPlayer().getName())) {
            Utils.pendingTeleports.remove(event.getPlayer().getName());
            Utils.sendMessage(event.getPlayer(), "&cTeleport countdown cancelled -- you moved!");
        }
    }
}