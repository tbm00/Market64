package dev.tbm00.spigot.shopstalls64.utils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;

import xzot1k.plugins.ds.api.objects.Shop;
import xzot1k.plugins.ds.api.enums.EditType;
import xzot1k.plugins.ds.api.events.ShopEditEvent;

import dev.tbm00.spigot.shopstalls64.ShopStalls64;
import dev.tbm00.spigot.shopstalls64.ConfigHandler;
import dev.tbm00.spigot.shopstalls64.gui.*;

public class GuiUtils {
    private static ShopStalls64 javaPlugin;
    public static final List<String> pendingTeleports = new CopyOnWriteArrayList<>();
    private static boolean editPrevention;
    public final static String[] SORT_TYPES = {"Unsorted", "Material", "Buy Price", "Sell Price", "Balance", "Stock"};

    public static void init(ShopStalls64 javaPlugin, ConfigHandler configHandler) {
        GuiUtils.javaPlugin = javaPlugin;
        editPrevention = configHandler.isDsEditorPrevented();
    }

    /**
     * Handles the event when sort shops admin results button is clicked.
     * 
     * @param event the inventory click event
     * @param sortIndex integer for sort alg
     * @param query the string for open gui
     */
    public static void handleSortShopsAdminQueryClick(InventoryClickEvent event, String query, int sortIndex) {
        event.setCancelled(true);
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        new ListResultsAdminGui(javaPlugin, dsMap, (Player) event.getWhoClicked(), query, sortIndex);
    }

    /**
     * Handles the event when sort shops admin results button is clicked.
     * 
     * @param event the inventory click event
     * @param sortIndex integer for sort alg
     * @param query the string for open gui
     */
    public static void handleSortShopsAdminAllClick(InventoryClickEvent event, int sortIndex) {
        event.setCancelled(true);
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        new ListAllAdminGui(javaPlugin, dsMap, (Player) event.getWhoClicked(), sortIndex);
    }

    /**
     * Handles the event when sort shops player results button is clicked.
     * 
     */
    public static void handleSortShopsPlayerClick(InventoryClickEvent event, String targetUUID, String targetName, int queryType, int sortIndex) {
        event.setCancelled(true);
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        new ListResultsPlayerGui(javaPlugin, dsMap, (Player) event.getWhoClicked(), targetUUID, targetName, queryType, sortIndex);
    }

    /**
     * Handles the event when sort shops string results button is clicked.
     * 
     */
    public static void handleSortShopsStringClick(InventoryClickEvent event, String query, int queryType, int sortIndex) {
        event.setCancelled(true);
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        new ListResultsStringGui(javaPlugin, dsMap, (Player) event.getWhoClicked(), query, queryType, sortIndex);
    }

    /**
     * Handles the event when sort shops all button is clicked.
     * 
     */
    public static void handleSortShopsAllClick(InventoryClickEvent event, int sortIndex) {
        event.setCancelled(true);
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        new ListAllGui(javaPlugin, dsMap, (Player) event.getWhoClicked(), sortIndex);
    }

    /**
     * Handles the event when search button is clicked.
     * 
     * @param event the inventory click event
     */
    public static void handleSearchClick(InventoryClickEvent event) {
        event.setCancelled(true);
        new SearchGui(javaPlugin, (Player) event.getWhoClicked());
    }

    /**
     * Handles the event when admin search button is clicked.
     * 
     * @param event the inventory click event
     */
    public static void handleAdminSearchClick(InventoryClickEvent event) {
        event.setCancelled(true);
        new SearchAdminGui(javaPlugin, (Player) event.getWhoClicked());
    }

    /**
     * Handles the event when a category selector is clicked.
     * 
     * @param event the inventory click event
     * @param command the command to execute for changing the category
     */
    public static void handleCategoryClick(InventoryClickEvent event, String command) {
        event.setCancelled(true);
        Utils.sudoCommand(event.getWhoClicked(), command);
    }

    /**
     * Handles the event when a shop item in the GUI is clicked.
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    public static void handleAllClick(InventoryClickEvent event, Player sender) {
        event.setCancelled(true);
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        new ListAllGui(javaPlugin, dsMap, sender, 1);
    }

    /**
     * Handles the event when a shop item in the GUI is clicked.
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    public static void handleAllAdminClick(InventoryClickEvent event, Player sender) {
        event.setCancelled(true);
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        new ListAllAdminGui(javaPlugin, dsMap, sender, 1);
    }

    /**
     * Handles the event when a page button is clicked.
     * 
     * @param event the inventory click event
     * @param next true to go to the next page; false to go to the previous page
     */
    public static void handlePageClick(InventoryClickEvent event, PaginatedGui gui, boolean next, String label) {
        event.setCancelled(true);
        if (next) gui.next();
        else gui.previous();
        gui.updateTitle(label + gui.getCurrentPageNum() + "/" + gui.getPagesNum());
    }

    /**
     * Handles the event when a shop item in the GUI is clicked.
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    public static void handleShopClick(InventoryClickEvent event, Player sender, Shop shop) {
        event.setCancelled(true);
        
        if (event.isShiftClick() && sender.getUniqueId().equals(shop.getOwnerUniqueId())) {
            openShopMenu(sender, shop);
        } else ShopUtils.teleportPlayerToShop(sender, shop);
    }

    /**
     * Handles the event when a shop item in the admin GUI is clicked.
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    public static void handleAdminShopClick(InventoryClickEvent event, Player sender, Shop shop) {
        event.setCancelled(true);
        
        if (event.isShiftClick()) {
            openShopMenu(sender, shop);
        } else ShopUtils.teleportPlayerToShop(sender, shop);
    }

    /**
     * Handles the event when sort players button is clicked.
     * 
     * @param event the inventory click event
     */
    public static void handleMainMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Utils.sudoCommand(event.getWhoClicked(), "commandpanel menu");
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
    private static void openShopMenu(Player player, Shop shop) {
        if (editPrevention && shop.getCurrentEditor()!=null && !shop.getCurrentEditor().toString().equals(player.getUniqueId().toString())) {
            if (javaPlugin.getServer().getOfflinePlayer(shop.getCurrentEditor()).isOnline()) {
                Utils.sendMessage(player, "&cShop currently under going edits by " + javaPlugin.getServer().getOfflinePlayer(shop.getCurrentEditor()).getName());
                return;
            }
        }

        ShopEditEvent shopEditEvent = new ShopEditEvent(player, shop, EditType.OPEN_EDIT_MENU);
        javaPlugin.getServer().getPluginManager().callEvent(shopEditEvent);
        if (shopEditEvent.isCancelled()) {
            Utils.sendMessage(player, "&cShop edit event canceled somewhere along the way..!");
            return;
        }

        if (editPrevention) shop.setCurrentEditor(player.getUniqueId());

        ShopStalls64.dsHook.getManager().getDataPack(player).setSelectedShop(shop);

        if (ShopStalls64.dsHook.getManager().getDataPack(player)==null) {
            Utils.sendMessage(player, "&cYour DS data pack is null");
            return;
        }

        ShopStalls64.dsHook.getMenu("edit").build(player);
        ShopStalls64.dsHook.runEventCommands("shop-edit", player);

        Bukkit.getScheduler().runTaskLater(javaPlugin, () -> {
            ShopStalls64.dsHook.getManager().getDataPack(player).setSelectedShop(shop);
        }, 1);
    }

    /**
     * Sets the shop GUI's footer's category button: ores.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param player player whose shops to open
     */
    public static void setGuiItemYourShops(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, Player player) {
        item.setType(Material.ENDER_CHEST);
        lore.add("&8-----------------------");
        lore.add("&6Click to view/manage your shops");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dYour Shops"));
        item.setItemMeta(meta);
        gui.setItem(6, 1, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleCategoryClick(event, "testshop list")));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's category button: ores.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemAllShops(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to view all shops");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dAll Shops"));
        item.setItemMeta(meta);
        item.setType(Material.CHEST);
        gui.setItem(6, 2, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleAllClick(event, (Player) event.getWhoClicked())));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's category button: ores.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemAllShopsAdmin(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to view all shops &c(ADMIN)");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dAll Shops"));
        item.setItemMeta(meta);
        item.setType(Material.CHEST);
        gui.setItem(6, 2, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleAllAdminClick(event, (Player) event.getWhoClicked())));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's category button: pog.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemCat(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to open category selector");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dShop Categories"));
        item.setItemMeta(meta);
        item.setType(Material.NETHER_STAR);
        gui.setItem(6, 3, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleCategoryClick(event, "commandpanel shopgui")));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's previous page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param label holder for gui's title
     */
    public static void setGuiItemPageBack(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, String label) {
        lore.add("&8-----------------------");
        lore.add("&6Click to go to the previous page");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fPrevious Page"));
        item.setItemMeta(meta);
        item.setType(Material.STONE_BUTTON);
        gui.setItem(6, 5, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handlePageClick(event, gui, false, label)));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's next page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param label holder for gui's title
     */
    public static void setGuiItemPageNext(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, String label) {
        lore.add("&8-----------------------");
        lore.add("&6Click to go to the next page");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fNext Page"));
        item.setItemMeta(meta);
        item.setType(Material.STONE_BUTTON);
        gui.setItem(6, 6, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handlePageClick(event, gui, true, label)));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's sort page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param query String of the currently opened gui
     */
    public static void setGuiItemSortShopsPlayer(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, String targetUUID, String targetName, int queryType, int sortIndex) {
        int next = (sortIndex==5) ? 0 : sortIndex+1;

        lore.add("&8-----------------------");
        lore.add("&6Click to change sort order");
        lore.add("&6("+ SORT_TYPES[sortIndex] + " -> " + SORT_TYPES[next] + ")");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fSort Shops"));
        item.setItemMeta(meta);
        item.setType(Material.HOPPER);
        gui.setItem(6, 7, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleSortShopsPlayerClick(event, targetUUID, targetName, queryType, next)));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's sort page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param query String of the currently opened gui
     */
    public static void setGuiItemSortShopsString(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, String query, int queryType, int sortIndex) {
        int next = (sortIndex==5) ? 0 : sortIndex+1;

        lore.add("&8-----------------------");
        lore.add("&6Click to change sort order");
        lore.add("&6("+ SORT_TYPES[sortIndex] + " -> " + SORT_TYPES[next] + ")");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fSort Shops"));
        item.setItemMeta(meta);
        item.setType(Material.HOPPER);
        gui.setItem(6, 7, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleSortShopsStringClick(event, query, queryType, next)));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's sort page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemSortShopsAll(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, int sortIndex) {
        int next = (sortIndex==5) ? 0 : sortIndex+1;

        lore.add("&8-----------------------");
        lore.add("&6Click to change sort order");
        lore.add("&6("+ SORT_TYPES[sortIndex] + " -> " + SORT_TYPES[next] + ")");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fSort Shops"));
        item.setItemMeta(meta);
        item.setType(Material.HOPPER);
        gui.setItem(6, 7, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleSortShopsAllClick(event, next)));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's sort page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param type integer of the sort alg
     * @param query String of the currently opened gui
     */
    public static void setGuiItemSortShopsAdminQuery(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, int type, String query) {
        int next = (type==5) ? 0 : type+1;

        lore.add("&8-----------------------");
        lore.add("&6Click to change sort order &c(ADMIN)");
        lore.add("&6("+ SORT_TYPES[type] + " -> " + SORT_TYPES[next] + ")");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fSort Shops"));
        item.setItemMeta(meta);
        item.setType(Material.HOPPER);
        gui.setItem(6, 7, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleSortShopsAdminQueryClick(event, query, next)));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's sort page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param type integer of the sort alg
     */
    public static void setGuiItemSortShopsAdminAll(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, int type) {
        int next = (type==5) ? 0 : type+1;

        lore.add("&8-----------------------");
        lore.add("&6Click to change sort order &c(ADMIN)");
        lore.add("&6("+ SORT_TYPES[type] + " -> " + SORT_TYPES[next] + ")");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fSort Shops"));
        item.setItemMeta(meta);
        item.setType(Material.HOPPER);
        gui.setItem(6, 7, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleSortShopsAdminAllClick(event, next)));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's search page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemSearch(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to search for a specific shop");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dSearch Shops"));
        item.setItemMeta(meta);
        item.setType(Material.WRITABLE_BOOK);
        gui.setItem(6, 8, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleSearchClick(event)));
        lore.clear();
    }

    /**
     * Sets the admin shop GUI's footer's search page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiAdminItemSearch(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to search for a specific shop");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dSearch Shops"));
        item.setItemMeta(meta);
        item.setType(Material.WRITABLE_BOOK);
        gui.setItem(6, 8, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleAdminSearchClick(event)));
        lore.clear();
    }

    /**
     * Sets the GUI's footer's main menu button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemMainMenu(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.clear();
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fGo to Main Menu"));
        item.setItemMeta(meta);
        item.setType(Material.STONE_BUTTON);
        gui.setItem(6, 9, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleMainMenuClick(event)));
        lore.clear();
    } public static void setGuiItemMainMenu(Gui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.clear();
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fGo to Main Menu"));
        item.setItemMeta(meta);
        item.setType(Material.STONE_BUTTON);
        gui.setItem(6, 9, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleMainMenuClick(event)));
        lore.clear();
    }

    /**
     * Formats and adds an item to the shop GUI.
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
     */
    public static void addGuiItemShop(PaginatedGui gui, Shop shop, ItemStack item, ItemMeta meta, List<String> lore, double balance, double buyPrice, double sellPrice, String priceLine, int stock, UUID uuid, String name, Player sender) {
        meta.setLore(null);
        lore.add("&8-----------------------");
        lore.add("&c" + shop.getDescription());
        if (buyPrice>=0) priceLine = "&7B: &a$" + Utils.formatInt(buyPrice) + " ";
        if (sellPrice>=0) priceLine += "&7S: &c$" + Utils.formatInt(sellPrice);
        lore.add(priceLine);
        if (stock<0) lore.add("&7Stock: &e∞");
            else lore.add("&7Stock: &e" + stock);
        if (shop.isAdminShop()) lore.add("&7Balance: &e$&e∞");
            else lore.add("&7Balance: &e$" + Utils.formatInt(balance));
        if (uuid!=null) lore.add("&7Owner: &f" + ShopStalls64.repHook.getRepManager().getPlayerUsername(uuid.toString()));
        lore.add("&7"+shop.getBaseLocation().getWorldName()+": &f"+(int)shop.getBaseLocation().getX()+"&7, &f"
                    +(int)shop.getBaseLocation().getY()+"&7, &f"+(int)shop.getBaseLocation().getZ());
        lore.add("&8-----------------------");
        lore.add("&6Click to TP to this shop");
        if (uuid!=null && sender.getUniqueId().equals(uuid))
            lore.add("&eShift-click to edit this shop");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        if (meta.getDisplayName()==null || meta.getDisplayName().isBlank())
            name = Utils.formatMaterial(item.getType()) + " &7x &f" + shop.getShopItemAmount();
        else name = meta.getDisplayName() + " &7x &f" + shop.getShopItemAmount();
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

        gui.addItem(ItemBuilder.from(item).asGuiItem(event -> handleShopClick(event, sender, shop)));
    }

    /**
     * Formats and adds an item to the shop admin GUI.
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
    public static void addGuiAdminItemShop(PaginatedGui gui, Shop shop, ItemStack item, ItemMeta meta, List<String> lore, double balance, double buyPrice, double sellPrice, String priceLine, int stock, UUID uuid, String name, Player sender, boolean isEmpty) {
        meta.setLore(null);
        lore.add("&8-----------------------");
        lore.add("&c" + shop.getDescription());
        if (buyPrice>=0) priceLine = "&7B: &a$" + Utils.formatInt(buyPrice) + " ";
        if (sellPrice>=0) priceLine += "&7S: &c$" + Utils.formatInt(sellPrice);
        lore.add(priceLine);
        if (stock<0) lore.add("&7Stock: &e∞");
            else lore.add("&7Stock: &e" + stock);
        if (shop.isAdminShop()) lore.add("&7Balance: &e$&e∞");
            else lore.add("&7Balance: &e$" + Utils.formatInt(balance));
        if (uuid!=null) lore.add("&7Owner: &f" + ShopStalls64.repHook.getRepManager().getPlayerUsername(uuid.toString()));
        lore.add("&7"+shop.getBaseLocation().getWorldName()+": &f"+(int)shop.getBaseLocation().getX()+"&7, &f"
                    +(int)shop.getBaseLocation().getY()+"&7, &f"+(int)shop.getBaseLocation().getZ());
        lore.add("&8-----------------------");
        lore.add("&6Click to TP to this shop");
        lore.add("&eShift-click to manage this shop");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        if (meta.getDisplayName()==null || meta.getDisplayName().isBlank())
            name = Utils.formatMaterial(item.getType()) + " &7x &f" + shop.getShopItemAmount();
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

        gui.addItem(ItemBuilder.from(item).asGuiItem(event -> handleAdminShopClick(event, sender, shop)));
    }
}