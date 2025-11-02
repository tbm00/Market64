package dev.tbm00.spigot.market64.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;

import dev.tbm00.papermc.playershops64.data.structure.Shop;
import dev.tbm00.papermc.playershops64.gui.ShopManageGui;
import dev.tbm00.papermc.playershops64.gui.ShopTransactionGui;

import dev.tbm00.spigot.market64.Market64;
import dev.tbm00.spigot.market64.StallHandler;
import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.data.Stall;
import dev.tbm00.spigot.market64.hook.PSHook;

public class VillagerGui {
    Market64 javaPlugin;
    StallHandler stallHandler;
    PSHook psHook;
    PaginatedGui gui;
    String label;
    Player player;
    Stall stall;
    List<Map.Entry<UUID, Shop>> stallShops;
    int currentSortIndex = 0;
    
    @SuppressWarnings("deprecation")
    public VillagerGui(Market64 javaPlugin, StallHandler stallHandler, PSHook psHook, Stall stall, Player player) {
        if (stall==null) return;
        this.javaPlugin = javaPlugin;
        this.stallHandler = stallHandler;
        this.psHook = psHook;
        this.player = player;
        this.stall = stall;
        this.stallShops =  new ArrayList<>(stallHandler.getStallsShops(stall).entrySet());
        label = "Stall #"+stall.getId()+" - ";

        gui = new PaginatedGui(6, 45, "Stall #"+stall.getId());
        
        sortShops();
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
        for (Shop shop : stallHandler.getStallsShops(stall).values()) {
            double buyPrice = shop.getBuyPrice().doubleValue(), sellPrice = shop.getSellPrice().doubleValue(),
                    balance = shop.getMoneyStock().doubleValue();
            int stock = shop.getItemStock();
            boolean empty = false;
            ItemStack item;

            if (shop.getItemStack()==null) {
                item = new ItemStack(Material.BARRIER, 1);
                empty = true;
            } else item = shop.getItemStack().clone();

            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();    
            String name = meta.getDisplayName();

            addShopItemToGui(gui, shop, item, meta, lore, balance, buyPrice, sellPrice, stock, empty, name);
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

        StaticUtil.setAboutItemInGui(gui, item, meta, lore);

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
    public void sortShops() {
        stallShops.sort((e1, e2) -> {
            Shop s1 = e1.getValue();
            Shop s2 = e2.getValue();

            ItemStack i1 = s1.getItemStack();
            ItemStack i2 = s2.getItemStack();
            
            if (i1 == null || i1.getType() == null) {
                if (i2 == null || i2.getType() == null) return 0; // no movement
                return 1; // s1 goes after s2
            }
            if (i2 == null || i2.getType() == null) {
                return -1; // s2 goes after s1
            }
            
            String mat1 = i1.getType().toString().replace("_", " ");
            String mat2 = i2.getType().toString().replace("_", " ");
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
    public void addShopItemToGui(PaginatedGui gui, Shop shop, ItemStack item, ItemMeta meta, List<String> lore, double balance, double buyPrice, double sellPrice, int stock, boolean isEmpty, String name) {
        if (meta.getDisplayName()==null || meta.getDisplayName().isBlank())
            name = StaticUtil.formatMaterial(item.getType()) + " &7x &f" + shop.getItemStack();
        else name = meta.getDisplayName() + " &7x &f" + shop.getItemStack();
        if (isEmpty) name = "&c(no item)";
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
       
        String priceLine = "";        
        lore.add("&8-----------------------");
        lore.add("&c" + shop.getDescription());
        if (buyPrice>=0) priceLine = "&7B: &a$" + StaticUtil.formatInt(buyPrice) + " ";
        if (sellPrice>=0) priceLine += "&7S: &c$" + StaticUtil.formatInt(sellPrice);
        lore.add(priceLine);
        if (shop.hasInfiniteStock()) lore.add("&7Stock: &e∞");
            else lore.add("&7Stock: &e" + StaticUtil.formatInt(stock));
        if (shop.hasInfiniteMoney()) lore.add("&7Balance: &e$&e∞");
            else lore.add("&7Balance: &e$" + StaticUtil.formatInt(balance));
        if (shop.getOwnerName()!=null) lore.add("&7Owner: &f" + shop.getOwnerName());
        lore.add("&7"+shop.getLocation().getWorld().getName()+": &f"+(int)shop.getLocation().getX()+"&7, &f"
                    +(int)shop.getLocation().getY()+"&7, &f"+(int)shop.getLocation().getZ());
        lore.add("&8-----------------------");
        if (player.getUniqueId().equals(shop.getOwnerUuid()) || shop.isAssistant(player.getUniqueId()))
            lore.add("&6Click to manage to this shop");
        else lore.add("&6Click to open this shop");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());

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
        item.setAmount(shop.getStackSize());

        gui.addItem(ItemBuilder.from(item).asGuiItem(event -> {handleShopClick(event, player, shop);}));
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
        
        if (!sender.getUniqueId().equals(shop.getOwnerUuid()) && !shop.isAssistant(sender.getUniqueId())) {
            openShopBuyerMenu(sender, shop);
        } else openShopManageMenu(sender, shop);
    }

    /**
     * Opens the PlayerShops64' menu for that particular shop.
     * 
     * This should build the menu
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    private void openShopBuyerMenu(Player player, Shop shop) {
        new ShopTransactionGui(psHook.pl, player, false, shop.getUuid(), 1, false);
    }

    /**
     * Opens the PlayerShops64' menu for that particular shop.
     * 
     * This should build the menu, as well as set the player-shop to "currently editing"
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    private void openShopManageMenu(Player player, Shop shop) {
        new ShopManageGui(psHook.pl, player, false, shop.getUuid());
    }
}