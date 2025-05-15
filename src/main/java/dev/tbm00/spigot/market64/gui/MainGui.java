package dev.tbm00.spigot.market64.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;

import dev.tbm00.spigot.market64.Market64;
import dev.tbm00.spigot.market64.StallHandler;
import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.data.Stall;

public class MainGui {
    Market64 javaPlugin;
    StallHandler stallHandler;
    PaginatedGui gui;
    String label;
    Player player;
    ArrayList<Stall> stalls;
    
    @SuppressWarnings("deprecation")
    public MainGui(Market64 javaPlugin, StallHandler stallHandler, List<Stall> stalls, Player player) {
        this.javaPlugin = javaPlugin;
        this.stallHandler = stallHandler;
        this.player = player;
        this.stalls = new ArrayList<>(stalls);
        label = "Market Stalls - ";
        gui = new PaginatedGui(6, 45, "Market Stalls");
        
        setupFooter();
        fillStalls();

        gui.updateTitle(label + gui.getCurrentPageNum() + "/" + gui.getPagesNum());
        StaticUtil.disableAll(gui);
        gui.disableAllInteractions();
        gui.open(player);
    }

    /**
     * Fills the GUI with items from the stall list.
     */
    private void fillStalls() {
        for (Stall stall : stalls) {
            if (stall == null) continue;
            addStallItemToGui(gui, player, stall);
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

        // Previous Page
        if (gui.getPagesNum()>=2) StaticUtil.setGuiItemPageBack(gui, item, meta, lore, label);
        else gui.setItem(6, 4, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        StaticUtil.setAboutItemInGui(gui, item, meta, lore);

        // Next Page
        if (gui.getPagesNum()>=2) StaticUtil.setGuiItemPageNext(gui, item, meta, lore, label);
        else gui.setItem(6, 6, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        gui.setItem(6, 7, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 8, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 9, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
    }

    /**
     * Formats and adds a stall ItemStack button to the main GUI.
     *
     * @param gui the paginated GUI to which the item will be added
     * @param sender the player viewing the GUI
     * @param stall the stall to create a button for
     * @param item the item to be displayed in the GUI
     * @param meta the metadata of the item
     * @param lore the list of lore descriptions to be displayed
     */
    public void addStallItemToGui(PaginatedGui gui, Player sender, Stall stall) {
        ItemStack item = new ItemStack(Material.BEDROCK);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        meta.setLore(null);
        
        int id;
        if (stall == null) {
            lore.add("&8-----------------------");
            lore.add("&7null stall");
            id = 99999;
        } else {
            id = stall.getId();
            lore.add("&8-----------------------");
            if (stall.isRented()) {
                lore.add("&7Rented By: &a"+stall.getRenterName());
                item.setType(Material.OBSIDIAN);
            } else {
                lore.add("&aAvailable to rent!");
                lore.add("&7Initial Cost: &f$"+StaticUtil.formatInt(stall.getInitialPrice()));
                lore.add("&7Renewal Cost: &f$"+StaticUtil.formatInt(stall.getRenewalPrice()));
                lore.add("&7Rental Length: &f"+stall.getRentalTimeDays()+" days");
                if (stall.getPlayTimeDays()==-1) lore.add("&7Max Playtime: &f∞");
                else lore.add("&7Max Playtime: &f"+stall.getPlayTimeDays()+" days");
                item.setType(Material.GLASS);
            }
            lore.add("&8-----------------------");
            lore.add("&6Click to teleport to this stall");
            if (!stall.isRented() || stall.getRenterUuid().equals(sender.getUniqueId()))
                lore.add("&eShift-click to open this stall's GUI");
        }

        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bStall #"+id));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_DYE);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        item.setItemMeta(meta);
        if (stall==null || stall.getId()==0) item.setAmount(1);
        else item.setAmount(stall.getId());

        gui.addItem(ItemBuilder.from(item).asGuiItem(event -> handleStallClick(event, sender, stall)));
    }

    /**
     * Handles the event when a stall item in the main GUI is clicked.
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the stall item
     * @param stall the stall associated with the clicked item
     */
    private void handleStallClick(InventoryClickEvent event, Player player, Stall stall) {
        event.setCancelled(true);
        
        if (event.isShiftClick() && (!stall.isRented() || stall.getRenterUuid().equals(player.getUniqueId()))) {
            new StallGui(javaPlugin, stallHandler, stall, player);
        } else {
            StaticUtil.teleportPlayer(player, stall.getWorld(), stall.getSignLocation().getBlockX(), stall.getSignLocation().getBlockY(), stall.getSignLocation().getBlockZ());
        }
    }
}