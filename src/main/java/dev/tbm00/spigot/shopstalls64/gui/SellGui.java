package dev.tbm00.spigot.shopstalls64.gui;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Bukkit;

import dev.triumphteam.gui.guis.Gui;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;
import dev.tbm00.spigot.shopstalls64.utils.*;

public class SellGui {

    /**
     * Empty gui that try to sell all items inside once closed.
     */
    public SellGui(ShopStalls64 javaPlugin, Player player, Double sell_per) {
        Gui gui = new Gui(6, "$"+Utils.formatInt(sell_per)+" per item");

        gui.setCloseGuiAction(event -> {
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTaskAsynchronously(javaPlugin, () -> {
                if (!player.isDead()) ShopUtils.handleSellInv(player, event.getInventory(), sell_per);
            });
        });
        gui.enableAllInteractions();
        gui.open(player);
    }
}