package dev.tbm00.spigot.market64.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;
import xzot1k.plugins.ds.api.enums.EditType;
import xzot1k.plugins.ds.api.events.ShopEditEvent;
import xzot1k.plugins.ds.api.events.ShopTransactionEvent;
import xzot1k.plugins.ds.api.objects.Menu;
import xzot1k.plugins.ds.api.objects.Shop;

import dev.tbm00.spigot.market64.Market64;
import dev.tbm00.spigot.market64.StallHandler;
import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.data.Stall;

public class VillagerGui {
    Market64 javaPlugin;
    StallHandler stallHandler;
    PaginatedGui gui;
    String label;
    Player player;
    Stall stall;
    List<Map.Entry<String, Shop>> dsMap;
    int currentSortIndex = 0;
    
    @SuppressWarnings("deprecation")
    public VillagerGui(Market64 javaPlugin, StallHandler stallHandler, Stall stall, Player player) {
        if (stall==null) return;
        this.javaPlugin = javaPlugin;
        this.stallHandler = stallHandler;
        this.player = player;
        this.stall = stall;
        this.dsMap = new ArrayList<>(stallHandler.getShopMap(stall).entrySet());
        label = "Stall #"+stall.getId()+" - ";

        gui = new PaginatedGui(6, 45, "Stall #"+stall.getId());
        
        sortShops(this.dsMap);
        fillShops();
        setupFooter();

        gui.updateTitle(label + gui.getCurrentPageNum() + "/" + gui.getPagesNum());
        StaticUtil.disableAll(gui);
        gui.disableAllInteractions();
        gui.open(player);
    }

    /**
     * Fills the GUI with items from the shop map.
     * Each shop that has a valid shop item and pricing information is converted into a clickable GUI item.
     *
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

            addShopItemToGui(gui, shop, item, meta, lore, balance, buyPrice, sellPrice, priceLine, stock, uuid, name, player, empty);
        }
    }

    /**
     * Sets up the footer of the GUI with categories & all other buttons.
     */
    @SuppressWarnings("deprecation")
    private void setupFooter() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        gui.setItem(6, 1, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 2, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 3, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Empty
        gui.setItem(6, 4, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Previous Page
        if (gui.getPagesNum()>=2) StaticUtil.setGuiItemPageBack(gui, item, meta, lore, label);
        else gui.setItem(6, 5, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        StaticUtil.setAboutStallItemInGui(gui, item, meta, lore, stall);

        // Next Page
        if (gui.getPagesNum()>=2) StaticUtil.setGuiItemPageNext(gui, item, meta, lore, label);
        else gui.setItem(6, 6, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        gui.setItem(6, 7, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 8, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 9, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
    }

    /**
     * Sorts the shop map by the material name.
     */
    public static void sortShops(List<Map.Entry<String, Shop>> dsMap) {
        dsMap.sort((e1, e2) -> {
            Shop s1 = e1.getValue();
            Shop s2 = e2.getValue();
            
            if (s1.getShopItem() == null || s1.getShopItem().getType() == null) {
                if (s2.getShopItem() == null || s2.getShopItem().getType() == null) return 0; // no movement
                return 1; // s1 goes after s2
            }
            if (s2.getShopItem() == null || s2.getShopItem().getType() == null) {
                return -1; // s2 goes after s1
            }
            
            String mat1 = s1.getShopItem().getType().toString().replace("_", " ");
            String mat2 = s2.getShopItem().getType().toString().replace("_", " ");
            return mat1.compareToIgnoreCase(mat2);
        });
    }

    /**
     * Formats and adds an item to the shop villager GUI.
     *
     * @param gui the paginated GUI to which the item will be added
     * @param shop the shop associated with the item
     * @param item the item to be displayed in the GUI
     * @param meta the metadata of the item
     * @param lore the list of lore descriptions to be displayed
     * @param balance the shop's current balance
     * @param buyPrice the item's buy price
     * @param sellPrice the item's sell price
     * @param priceLine the formatted price string
     * @param stock the current stock of the item
     * @param uuid the unique identifier of the shop owner
     * @param name the formatted display name of the item
     * @param sender the player viewing the shop
     * @param isEmpty is the shop item empty
     */
    public void addShopItemToGui(PaginatedGui gui, Shop shop, ItemStack item, ItemMeta meta, List<String> lore, double balance, double buyPrice, double sellPrice, String priceLine, int stock, UUID uuid, String name, Player sender, boolean isEmpty) {
        meta.setLore(null);
        lore.add("&8-----------------------");
        lore.add("&c" + shop.getDescription());
        if (buyPrice>=0) priceLine = "&7B: &a$" + StaticUtil.formatInt(buyPrice) + " ";
        if (sellPrice>=0) priceLine += "&7S: &c$" + StaticUtil.formatInt(sellPrice);
        lore.add(priceLine);
        if (stock<0) lore.add("&7Stock: &e∞");
            else lore.add("&7Stock: &e" + stock);
        if (shop.isAdminShop()) lore.add("&7Balance: &e$&e∞");
            else lore.add("&7Balance: &e$" + StaticUtil.formatInt(balance));
        if (uuid!=null) lore.add("&7Owner: &f" + javaPlugin.getServer().getOfflinePlayer(uuid).getName());
        lore.add("&7"+shop.getBaseLocation().getWorldName()+": &f"+(int)shop.getBaseLocation().getX()+"&7, &f"
                    +(int)shop.getBaseLocation().getY()+"&7, &f"+(int)shop.getBaseLocation().getZ());
        lore.add("&8-----------------------");
        if (sender.getUniqueId().equals(uuid))lore.add("&6Click to manage to this shop");
        else lore.add("&6Click to open this shop");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        if (meta.getDisplayName()==null || meta.getDisplayName().isBlank())
            name = StaticUtil.formatMaterial(item.getType()) + " &7x &f" + shop.getShopItemAmount();
        else name = meta.getDisplayName() + " &7x &f" + shop.getShopItemAmount();
        if (isEmpty) name = "&c(no item)";
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_DYE);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        //for (Enchantment enchant : new HashSet<>(meta.getEnchants().keySet()))
        //    meta.removeEnchant(enchant);

        item.setItemMeta(meta);
        item.setAmount(shop.getShopItemAmount());

        gui.addItem(ItemBuilder.from(item).asGuiItem(event -> {handleShopClick(event, sender, shop);}));
    }

    /**
     * Handles the event when a shop item in the GUI is clicked.
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    private void handleShopClick(InventoryClickEvent event, Player sender, Shop shop) {
        event.setCancelled(true);
        
        if (!sender.getUniqueId().equals(shop.getOwnerUniqueId())) {
            openShopBuyerMenu(sender, shop);
        } else openShopManageMenu(sender, shop);
    }

    /**
     * Opens the DisplayShops' menu for that particular shop.
     * 
     * This should build the menu
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    private void openShopBuyerMenu(Player player, Shop shop) {
        if (StaticUtil.EDITOR_PREVENTION && shop.getCurrentEditor()!=null && !shop.getCurrentEditor().toString().equals(player.getUniqueId().toString())) {
            if (javaPlugin.getServer().getOfflinePlayer(shop.getCurrentEditor()).isOnline()) {
                StaticUtil.sendMessage(player, "&cShop currently under going edits by " + javaPlugin.getServer().getOfflinePlayer(shop.getCurrentEditor()).getName());
                return;
            }
        }

        ShopTransactionEvent shopTransactionEvent = new ShopTransactionEvent(player, shop);
        javaPlugin.getServer().getPluginManager().callEvent(shopTransactionEvent);
        if (shopTransactionEvent.isCancelled()) {
            StaticUtil.sendMessage(player, "&cShop open event canceled somewhere along the way..!");
            return;
        }

        stallHandler.dsHook.pl.getManager().getDataPack(player).setSelectedShop(shop);
        if (stallHandler.dsHook.pl.getManager().getDataPack(player)==null) {
            StaticUtil.sendMessage(player, "&cYour DS data pack is null");
            return;
        }

        if (StaticUtil.EDITOR_PREVENTION) shop.setCurrentEditor(player.getUniqueId());
        
        stallHandler.dsHook.pl.runEventCommands("shop-open", player);
        Menu transactionMenu = stallHandler.dsHook.pl.getMenu("transaction");
        if (transactionMenu != null) {
            transactionMenu.build(player);
        }

        Bukkit.getScheduler().runTaskLater(javaPlugin, () -> {
            stallHandler.dsHook.pl.getManager().getDataPack(player).setSelectedShop(shop);
        }, 1);
    }

    /**
     * Opens the DisplayShops' menu for that particular shop.
     * 
     * This should build the menu, as well as set the player-shop to "currently editing"
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    private void openShopManageMenu(Player player, Shop shop) {
        if (StaticUtil.EDITOR_PREVENTION && shop.getCurrentEditor()!=null && !shop.getCurrentEditor().toString().equals(player.getUniqueId().toString())) {
            if (javaPlugin.getServer().getOfflinePlayer(shop.getCurrentEditor()).isOnline()) {
                StaticUtil.sendMessage(player, "&cShop currently under going edits by " + javaPlugin.getServer().getOfflinePlayer(shop.getCurrentEditor()).getName());
                return;
            }
        }

        ShopEditEvent shopEditEvent = new ShopEditEvent(player, shop, EditType.OPEN_EDIT_MENU);
        javaPlugin.getServer().getPluginManager().callEvent(shopEditEvent);
        if (shopEditEvent.isCancelled()) {
            StaticUtil.sendMessage(player, "&cShop edit event canceled somewhere along the way..!");
            return;
        }

        stallHandler.dsHook.pl.getManager().getDataPack(player).setSelectedShop(shop);
        if (stallHandler.dsHook.pl.getManager().getDataPack(player)==null) {
            StaticUtil.sendMessage(player, "&cYour DS data pack is null");
            return;
        }

        if (StaticUtil.EDITOR_PREVENTION) shop.setCurrentEditor(player.getUniqueId());

        stallHandler.dsHook.pl.runEventCommands("shop-edit", player);
        Menu editMenu = stallHandler.dsHook.pl.getMenu("edit");
        if (editMenu != null) {
            editMenu.build(player);
        }

        Bukkit.getScheduler().runTaskLater(javaPlugin, () -> {
            stallHandler.dsHook.pl.getManager().getDataPack(player).setSelectedShop(shop);
        }, 1);
    }
}