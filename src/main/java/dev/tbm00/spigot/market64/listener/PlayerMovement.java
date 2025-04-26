package dev.tbm00.spigot.market64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import dev.tbm00.spigot.market64.StaticUtil;

public class PlayerMovement implements Listener {

    /**
     * Handles the player movement event.
     * Cancels pending teleports if any.
     *
     * @param event the PlayerMoveEvent
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (StaticUtil.pendingTeleports.contains(event.getPlayer().getName())) {
            StaticUtil.pendingTeleports.remove(event.getPlayer().getName());
            StaticUtil.sendMessage(event.getPlayer(), "&cTeleport countdown cancelled -- you moved!");
        }
    }
}