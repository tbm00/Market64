package dev.tbm00.spigot.shopstalls64.hook;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;

public class EcoHook {

    public Economy pl;
    
    public EcoHook(ShopStalls64 javaPlugin) {
        RegisteredServiceProvider<Economy> rsp = javaPlugin.getServer().getServicesManager().getRegistration(Economy.class);
        pl = rsp.getProvider();
    }

    public boolean hasMoney(OfflinePlayer player, double amount) {
        double bal = pl.getBalance(player);
        if (bal>=amount) return true;
        else return false;
    }

    public boolean removeMoney(OfflinePlayer player, double amount) {
        if (amount <= 0) return true;

        EconomyResponse r = pl.withdrawPlayer(player, amount);
        if (r.transactionSuccess()) return true;
        else return false;
    }

    public boolean giveMoney(OfflinePlayer player, double amount) {
        if (amount <= 0) return true;

        EconomyResponse r = pl.depositPlayer(player, amount);
        if (r.transactionSuccess()) return true;
        else return false;
    }
}