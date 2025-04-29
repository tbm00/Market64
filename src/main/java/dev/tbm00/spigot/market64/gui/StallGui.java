package dev.tbm00.spigot.market64.gui;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.tbm00.spigot.market64.Market64;
import dev.tbm00.spigot.market64.StallHandler;
import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.data.Stall;

public class StallGui {
    Market64 javaPlugin;
    StallHandler stallHandler;
    Gui gui;
    String label;
    Player player;
    Stall stall;
    
    @SuppressWarnings("deprecation")
    public StallGui(Market64 javaPlugin, StallHandler stallHandler, Stall stall, Player player) {
        if (stall==null) return;
        this.javaPlugin = javaPlugin;
        this.stallHandler = stallHandler;
        this.player = player;
        this.stall = stall;
        label = "Stall #"+stall.getId();
        gui = new Gui(6, label);
        
        setupContent();
        setupFooter();

        StaticUtil.disableAll(gui);
        gui.disableAllInteractions();
        gui.open(player);
    }

    /**
     * Sets up the content of the GUI with main buttons.
     */
    private void setupContent() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        setInfoItemInGui(gui, item, meta, lore);
        setTPItemInGui(gui, item, meta, lore);

        if (stall.isRented() && player.getUniqueId().equals(stall.getRenterUuid())) {
            setRenewItemInGui(gui, item, meta, lore);
            setAbandonItemInGui(gui, item, meta, lore);
        } else if (!stall.isRented()) {
            setRentItemInGui(gui, item, meta, lore);
        }
    }

    /**
     * Sets up the footer of the GUI with info and page buttons.
     */
    @SuppressWarnings("deprecation")
    private void setupFooter() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        gui.setItem(6, 1, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 2, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 3, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 4, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        StaticUtil.setAboutItemInGui(gui, item, meta, lore);

        gui.setItem(6, 6, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 7, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 8, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        setMenuItemInGui(gui, item, meta, lore);
    }

    /**
     * Formats and sets the stall's GUI's footer's menu button.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param label holder for gui's title
     */
    public void setMenuItemInGui(Gui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.clear();
        lore.add("&8-----------------------");
        lore.add("&6Click to go back to main stall GUI");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dMain Stall GUI"));
        item.setItemMeta(meta);
        item.setType(Material.STONE_BUTTON);
        gui.setItem(6, 9, ItemBuilder.from(item).asGuiItem(event -> handleMenuClick(event)));
        lore.clear();
    }

    public void setInfoItemInGui(BaseGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        item.setType(Material.PAPER);
        lore.add("&8-----------------------");
        lore.add("&7Initial Cost: &f$"+StaticUtil.formatInt(stall.getInitialPrice()));
        lore.add("&7Renewal Cost: &f$"+StaticUtil.formatInt(stall.getRenewalPrice()));
        lore.add("&7Rental Length: &f"+stall.getRentalTimeDays()+" days");
        if (stall.getPlayTimeDays()==-1) lore.add("&7Max Playtime: &f∞");
        else lore.add("&7Max Playtime: &f"+stall.getPlayTimeDays()+" days");
        lore.add("&7DisplayShops: &f"+stall.getShopUuids().size());

        lore.add(" ");
        if (stall.isRented()) {
            lore.add("&7Rented By: &f"+stall.getRenterName());
            LocalDate ld = stall.getEvictionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            lore.add("&7Renewal Date: &f"+ld.getMonth().toString()+ " "+ld.getDayOfMonth());
            LocalDate ld2 = stall.getLastTransaction().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            lore.add("&7Last Transaction: &f"+ld2.getMonth().toString()+ " "+ld2.getDayOfMonth());
        } else {
            lore.add("&aAvailable to rent!");
        }

        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&5Stall #" + stall.getId()));
        item.setItemMeta(meta);
        gui.setItem(2, 5, ItemBuilder.from(item).asGuiItem(event -> {event.setCancelled(true);}));
        lore.clear();
    }

    public void setTPItemInGui(BaseGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        item.setType(Material.COMPASS);
        lore.add("&8-----------------------");
        lore.add("&6Click to teleport to this stall");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dTeleport to Stall #" + stall.getId()));
        item.setItemMeta(meta);
        gui.setItem(3, 5, ItemBuilder.from(item).asGuiItem(event -> {handleTPClick(event);}));
        lore.clear();
    }

    public void setRentItemInGui(BaseGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        item.setType(Material.GOLDEN_SHOVEL);
        lore.add("&8-----------------------");
        lore.add("&6Click to rent to this stall for $"+StaticUtil.formatInt(stall.getInitialPrice()));
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dRent Stall #" + stall.getId()));
        item.setItemMeta(meta);
        gui.setItem(4, 5, ItemBuilder.from(item).asGuiItem(event -> {handleRentClick(event);}));
        lore.clear();
    }

    public void setRenewItemInGui(BaseGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        item.setType(Material.GOLD_INGOT);
        lore.add("&8-----------------------");
        lore.add("&6Click to renew to this stall for $"+StaticUtil.formatInt(stall.getRenewalPrice()));
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dRenew Stall #" + stall.getId()));
        item.setItemMeta(meta);
        gui.setItem(4, 4, ItemBuilder.from(item).asGuiItem(event -> {handleRenewClick(event);}));
        lore.clear();
    }

    public void setAbandonItemInGui(BaseGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        item.setType(Material.BARRIER);
        lore.add("&8-----------------------");
        lore.add("&6Click to abandon to this stall");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dAbandon Stall #" + stall.getId()));
        item.setItemMeta(meta);
        gui.setItem(4, 6, ItemBuilder.from(item).asGuiItem(event -> {handleAbandonClick(event);}));
        lore.clear();
    }

    /**
     * Handles the event when a main stall gui button is clicked.
     * 
     * @param event the inventory click event
     */
    private void handleMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        new MainGui(javaPlugin, stallHandler, stallHandler.getStalls(), player);
    }

    /**
     * Handles the event when a TP button is clicked.
     * 
     * @param event the inventory click event
     */
    private void handleTPClick(InventoryClickEvent event) {
        event.setCancelled(true);
        StaticUtil.teleportPlayer(player, stall.getWorld(), stall.getSignLocation().getBlockX(), stall.getSignLocation().getBlockY(), stall.getSignLocation().getBlockZ());
    }

    /**
     * Handles the event when a rent button is clicked.
     * 
     * @param event the inventory click event
     */
    private void handleRentClick(InventoryClickEvent event) {
        event.setCancelled(true);
        gui.close(player);
        stallHandler.fillStall(stall.getId(), player);
    }

    /**
     * Handles the event when a renew button is clicked.
     * 
     * @param event the inventory click event
     */
    private void handleRenewClick(InventoryClickEvent event) {
        event.setCancelled(true);
        gui.close(player);
        stallHandler.renewStall(stall.getId(), false);
    }

    /**
     * Handles the event when a abandon button is clicked.
     * 
     * @param event the inventory click event
     */
    private void handleAbandonClick(InventoryClickEvent event) {
        event.setCancelled(true);
        gui.close(player);
        stallHandler.clearStall(stall.getId(), "player abandoned", false);
    }
}