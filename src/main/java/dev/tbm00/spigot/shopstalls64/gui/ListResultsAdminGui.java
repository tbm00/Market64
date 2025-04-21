package dev.tbm00.spigot.shopstalls64.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;

import xzot1k.plugins.ds.api.objects.Shop;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;
import dev.tbm00.spigot.shopstalls64.utils.*;

public class ListResultsAdminGui {
    ShopStalls64 javaPlugin;
    PaginatedGui gui;
    String query;
    String label;
    Player sender;
    List<Map.Entry<String, Shop>> dsMap;
    int currentSortIndex = 0;
    
    public ListResultsAdminGui(ShopStalls64 javaPlugin, ConcurrentHashMap<String, Shop> dsMap, Player sender, String query, int sortIndex) {
        this.javaPlugin = javaPlugin;
        this.sender = sender;
        this.dsMap = new ArrayList<>(dsMap.entrySet());;
        this.query = query;
        currentSortIndex = sortIndex;

        String name = ShopStalls64.repHook.getRepManager().getPlayerUsername(query);
        if (name!=null) label = name+" - ";
        else label = query+" - ";
        
        gui = new PaginatedGui(6, 45, query);
        
        preProcessShops();
        ShopUtils.sortShops(this.dsMap, currentSortIndex);
        fillShops();
        setupFooter();
        
        gui.updateTitle(label + gui.getCurrentPageNum() + "/" + gui.getPagesNum());
        gui.disableAllInteractions();
        gui.open(sender);
    }

    private void preProcessShops() {
        Iterator<Map.Entry<String, Shop>> iter = dsMap.iterator();
        while(iter.hasNext()) {
            Map.Entry<String, Shop> entry = iter.next();
            Shop shop = entry.getValue();

            boolean remove = false;

            /*check if query matches*/ 
                ItemStack item;
                if (shop.getShopItem()==null) item = new ItemStack(Material.BARRIER);
                else item = shop.getShopItem().clone();
                
                boolean include = false;
                String mat = item.getType().toString().replace("_", " ");
                ItemMeta meta = item.getItemMeta();
                String name = meta.getDisplayName();
                String desc = shop.getDescription();
                if (mat != null && StringUtils.containsIgnoreCase(mat, query)) include = true;
                else if (name!=null && StringUtils.containsIgnoreCase(name, query)) include = true;
                else if (desc != null && StringUtils.containsIgnoreCase(desc, query)) include = true;
                else if (shop.getOwnerUniqueId()!=null) {
                    String owner = ShopStalls64.repHook.getRepManager().getPlayerUsername(shop.getOwnerUniqueId().toString());
                    if (owner!=null && StringUtils.containsIgnoreCase(owner, query)) include = true;
                } if (!include) remove = true;
                
            if (remove) iter.remove();
        }
    }
    /**
     * Fills the GUI with items from the shop map.
     * Each shop that has a valid shop item and pricing information is converted into a clickable GUI item.
     */
    private void fillShops() {        
        Iterator<Map.Entry<String, Shop>> iter = dsMap.iterator();
        while(iter.hasNext()) {
            Map.Entry<String, Shop> entry = iter.next();
            Shop shop = entry.getValue();

            double buyPrice = shop.getBuyPrice(false), sellPrice = shop.getSellPrice(false),
                    balance = shop.getStoredBalance();
            int stock = shop.getStock();
            boolean empty = false;
            ItemStack item;

            if (shop.getShopItem()==null) {
                item = new ItemStack(Material.BARRIER, 1);
                empty = true;
            } else item = shop.getShopItem().clone();

            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();    
            String name = meta.getDisplayName(), priceLine = "";
            UUID uuid = shop.getOwnerUniqueId();

            GuiUtils.addGuiAdminItemShop(gui, shop, item, meta, lore, balance, buyPrice, sellPrice, priceLine, stock, uuid, name, sender, empty);
        }
    }

    /**
     * Sets up the footer of the GUI with categories & all other buttons.
     */
    private void setupFooter() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        // Your Shops
        GuiUtils.setGuiItemYourShops(gui, item, meta, lore, sender);

        // All Shops
        GuiUtils.setGuiItemAllShopsAdmin(gui, item, meta, lore);

        // Category
        GuiUtils.setGuiItemCat(gui, item, meta, lore);

        // Empty
        gui.setItem(6, 4, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Previous Page
        if (gui.getPagesNum()>=2) GuiUtils.setGuiItemPageBack(gui, item, meta, lore, label);
        else gui.setItem(6, 5, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Next Page
        if (gui.getPagesNum()>=2)  GuiUtils.setGuiItemPageNext(gui, item, meta, lore, label);
        else gui.setItem(6, 6, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Sort
        GuiUtils.setGuiItemSortShopsAdminQuery(gui, item, meta, lore, currentSortIndex, query);

        // Search
        GuiUtils.setGuiAdminItemSearch(gui, item, meta, lore);
        
        // Main Menu
        GuiUtils.setGuiItemMainMenu(gui, item, meta, lore);
    }
}