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

public class ListResultsStringGui {
    ShopStalls64 javaPlugin;
    PaginatedGui gui;
    String query;
    int queryType; // the type of query 0="shop", 1="buy", 2="sell"
    String label;
    Player player;
    List<Map.Entry<String, Shop>> dsMap;
    int currentSortIndex = 0;
    
    public ListResultsStringGui(ShopStalls64 javaPlugin, ConcurrentHashMap<String, Shop> dsMap, Player player, String query, int queryType, int sortIndex) {
        this.javaPlugin = javaPlugin;
        this.player = player;
        this.dsMap = new ArrayList<>(dsMap.entrySet());
        this.query = query;
        this.queryType = queryType;
        currentSortIndex = sortIndex;
        label = query+" - ";
        gui = new PaginatedGui(6, 45, query);
        
        preProcessShops();
        ShopUtils.sortShops(this.dsMap, currentSortIndex);
        fillShops();
        setupFooter();
        
        gui.updateTitle(label + gui.getCurrentPageNum() + "/" + gui.getPagesNum());
        gui.disableAllInteractions();
        gui.open(player);
    }

    private void preProcessShops() {
        Iterator<Map.Entry<String, Shop>> iter = dsMap.iterator();
        while(iter.hasNext()) {
            Map.Entry<String, Shop> entry = iter.next();
            Shop shop = entry.getValue();

            if (shop.getShopItem() == null) { // if no shop item
                iter.remove();
                continue;
            }

            boolean remove = false;

            /*check if valid & active shop*/ 
                double buyPrice = shop.getBuyPrice(false), sellPrice = shop.getSellPrice(false),
                        balance = shop.getStoredBalance();
                int stock = shop.getStock(), stackSize = shop.getShopItemAmount();
                if (buyPrice<0 && sellPrice<0) remove = true; // if buy-from & sell-to are both disabled
                else if (sellPrice<0 && stock<stackSize && stock!=-1) remove = true; // if sell-to disabled & no stock to buy-from
                else if (buyPrice<0 && balance<sellPrice && balance!=-1) remove = true; // if buy-from disabled & no money to sell-to
                else if (stock==0 && balance<sellPrice && balance!=-1) remove = true; // if no stock & no money to sell-to
                //else if (queryType==1 && buyPrice<0) remove = true; // if searching for buy shops and buy-from is disabled
                //else if (queryType==2 && sellPrice<0) continue; // if searching for sell shops and sell-to is disabled

            /*check if query matches*/
                boolean include = false; 
                ItemStack item = shop.getShopItem().clone();
                String mat = item.getType().toString().replace("_", " ");
                ItemMeta meta = item.getItemMeta();
                String name = meta.getDisplayName();
                String desc = shop.getDescription();
                if (StringUtils.containsIgnoreCase(mat, query)) include = true;
                else if (name!=null && StringUtils.containsIgnoreCase(name, query)) include = true;
                else if (StringUtils.containsIgnoreCase(desc, query)) include = true;
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
     *
     * @param queryType the type of query 0="shop", 1="buy", 2="sell"
     */
    private void fillShops() {        
        Iterator<Map.Entry<String, Shop>> iter = dsMap.iterator();
        while(iter.hasNext()) {
            Map.Entry<String, Shop> entry = iter.next();
            Shop shop = entry.getValue();

            double buyPrice = shop.getBuyPrice(false), sellPrice = shop.getSellPrice(false),
                    balance = shop.getStoredBalance();
            int stock = shop.getStock();
            ItemStack item = shop.getShopItem().clone();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();    
            String name = meta.getDisplayName(), priceLine = "";
            UUID uuid = shop.getOwnerUniqueId();

            GuiUtils.addGuiItemShop(gui, shop, item, meta, lore, balance, buyPrice, sellPrice, priceLine, stock, uuid, name, player);
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
        GuiUtils.setGuiItemYourShops(gui, item, meta, lore, player);

        // All Shops
        GuiUtils.setGuiItemAllShops(gui, item, meta, lore);

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
        GuiUtils.setGuiItemSortShopsString(gui, item, meta, lore, query, queryType, currentSortIndex);

        // Search
        GuiUtils.setGuiItemSearch(gui, item, meta, lore);
        
        // Main Menu
        GuiUtils.setGuiItemMainMenu(gui, item, meta, lore);
    }
}