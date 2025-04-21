package dev.tbm00.spigot.shopstalls64.utils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import xzot1k.plugins.ds.api.objects.Shop;

import dev.tbm00.spigot.shopstalls64.ConfigHandler;
import dev.tbm00.spigot.shopstalls64.ShopStalls64;
import dev.tbm00.spigot.shopstalls64.gui.*;

public class ShopUtils {
    private static ShopStalls64 javaPlugin;
    private static ConfigHandler configHandler;
    public static final List<String> pendingTeleports = new CopyOnWriteArrayList<>();
    public static String clipboardWorld = null;
    public static int x1=0, y1=0, z1=0, x2=0, y2=0, z2=0, xc=0, yc=0, zc=0;

    public static void init(ShopStalls64 javaPlugin, ConfigHandler configHandler) {
        ShopUtils.javaPlugin = javaPlugin;
        ShopUtils.configHandler = configHandler;
    }

    /**
     * Handles the sub command for opening a specific category.
     * 
     * @param player the command sender
     * @param category the category passed to the command
     * @return true if command was processed successfully
     */
    public static boolean handleCategoryCmd(Player player, String category) {
        Utils.sudoCommand(player, "commandpanel "+category);
        return true;
    }

    /**
     * Handles the sub command for opening the shop gui.
     * 
     * @param player the command sender
     * @return true after creating gui instance
     */
    public static boolean handleGuiCmd(Player player) {
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        new ListAllGui(javaPlugin, dsMap, player, 1);
        return true;
    }

    /**
     * Handles the sub command for opening the shop gui.
     * 
     * @param player the command sender
     * @return true after creating gui instance
     */
    public static boolean handleGuiAdminCmd(Player player) {
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        new ListAllAdminGui(javaPlugin, dsMap, player, 1);
        return true;
    }

    /**
     * Handles searching the shops by String/player
     * 
     * @param sender the command sender
     * @param args the arguments passed to the command
     * @param queryType the type of query 0="shop", 1="buy", 2="sell"
     * @return true if task was processed successfully
     */
    public static boolean handleSearch(Player sender, String[] args, int queryType) {
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        // search shops for target player
        while (args[0].startsWith(" ")) {
            args[0] = args[0].substring(1);
        }
        String targetName = args[0];
        String targetUUID = ShopStalls64.repHook.getRepManager().getPlayerUUID(targetName);
        if (targetUUID!=null) {
            new ListResultsPlayerGui(javaPlugin, dsMap, sender, targetUUID, targetName, queryType, 1);
            return true;
        }

        // search shops for target item String
        String search = null;
        int i=0;
        for (String arg : args) {
            if (i==0) {
                search = arg;
                ++i;
            } else search = search + " " + arg;
        }

        if (search==null) return false;
        search = search.replace("_", " ");
        
        new ListResultsStringGui(javaPlugin, dsMap, sender, search, queryType, 1);
        return true;
    }

    /**
     * Handles searching the shops by String/player for admins
     * 
     * @param sender the command sender
     * @param args the arguments passed to the command
     * @return true if task was processed successfully
     */
    public static boolean handleAdminSearch(Player sender, String[] args) {
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        // search shops for target player
        while (args[0].startsWith(" ")) {
            args[0] = args[0].substring(1);
        }
        String targetName = args[0];
        if (targetName!=null) {
            new ListResultsAdminGui(javaPlugin, dsMap, sender, targetName, 1);
            return true;
        }

        // search shops for target item String
        String search = null;
        int i=0;
        for (String arg : args) {
            if (i==0) {
                search = arg;
                ++i;
            } else search = search + " " + arg;
        }

        if (search==null) return false;
        search = search.replace("_", " ");
        
        new ListResultsAdminGui(javaPlugin, dsMap, sender, search, 1);
        return true;
    }

    /**
     * Handles selling items to any applicable shop
     * 
     * @param player the seller
     * @param inv the inventory to sell items from
     * @param sell_per the minimum price to sell each item for
     * 
     * @return true if command was processed successfully
     */
    public static boolean handleSellInv(Player player, Inventory inv, Double sell_per) {
        // Deposit into shops 1 by 1
        ConcurrentHashMap<String, Shop> dsMap = ShopStalls64.dsHook.getManager().getShopMap();
        int MAX_STOCK = configHandler.getDSMaxStoredStock();
        UUID uuid = player.getUniqueId();
        double money_earned = 0;
        int items_sold = 0;

        invItemFor:
        for (ItemStack invItem : inv.getContents()) {
            if (invItem == null) continue;

            Material invMat = invItem.getType();
            MaterialData invData = invItem.getData();
            ItemMeta invMeta = invItem.getItemMeta();
            int invAmount = invItem.getAmount();

            for (Shop shop : dsMap.values()) {
                ItemStack shopItem = shop.getShopItem();
                if (shopItem==null) continue;
                if (shop.getOwnerUniqueId()!=null && shop.getOwnerUniqueId().equals(uuid)) continue; // cant sell to own shop
                if (shop.getSellPrice(false)<0) continue; // shop has selling disabled
                if ((shop.getStoredBalance() < shop.getSellPrice(false)) && shop.getStoredBalance()!=-1) continue; // not enough money for shop to buy anything

                if (shopItem.getType()==null || !shopItem.getType().equals(invMat)) continue; 
                if (shopItem.getData()==null || !shopItem.getData().equals(invData)) continue;
                if (shopItem.getItemMeta()==null || !shopItem.getItemMeta().equals(invMeta)) continue;
                if (shop.getShopItemAmount()<1) continue;
                if (shop.getStock()>=MAX_STOCK) continue;

                // calculate how much the shop can buy
                double price_per_item = shop.getSellPrice(false) / shop.getShopItemAmount();
                if (price_per_item<sell_per) continue;
                double shop_balance = shop.getStoredBalance();
                int max_sellable = (int) Math.floor(shop_balance / price_per_item);
                if (max_sellable<1 && shop_balance>=0) continue;
                
                int sell_amount;
                if (max_sellable > invAmount) sell_amount = invAmount;
                else sell_amount = max_sellable;

                // calculate how much the shop can store
                int stock = shop.getStock(), new_stock;
                if (stock+sell_amount > MAX_STOCK) {
                    new_stock = MAX_STOCK;
                    sell_amount = MAX_STOCK - stock;
                } else new_stock = stock + sell_amount;

                double sell_price = sell_amount * price_per_item;
                double new_balance = shop_balance - sell_price;

                if (Utils.addMoney(player, sell_price)) {
                    shop.setStoredBalance(new_balance);
                    if (stock!=-1) shop.setStock(new_stock);

                    int newInvAmount = invAmount-sell_amount;
                    invItem.setAmount(newInvAmount);

                    items_sold += sell_amount;
                    money_earned += sell_price;

                    if (newInvAmount==0) continue invItemFor;
                }
            }

            if (!(inv.getHolder() instanceof Player) && invItem.getAmount()>0) {
                Utils.giveItem(player, invItem);
            }
        }

        if (items_sold<1) Utils.sendMessage(player, "&cCouldn't find any applicable shops for your items!");
        else Utils.sendMessage(player, "&aSold " + items_sold + " items for a total of $" + Utils.formatInt(money_earned));
        return true;
    }

    /**
     * Teleports the player to the given shop's location.
     * 
     * @param player the player to teleport
     * @param shop the shop whose location the player will be teleported to
     */
    public static void teleportPlayerToShop(Player player, Shop shop) {
        double x=shop.getBaseLocation().getX(), y=shop.getBaseLocation().getY(), z=shop.getBaseLocation().getZ();
        String world=shop.getBaseLocation().getWorldName();

        Utils.teleportPlayer(player, world, x, y+1, z);
    }

    /**
     * Counts the number of shops owned by a specific player.
     *
     * @param uuid  the unique identifier of the player whose shops are being counted
     * @param dsMap the map of all shops keyed by their unique IDs
     * @return the total number of shops that belong to the specified player
     */
    public static int countPlayerShops(ConcurrentHashMap<String, Shop> dsMap, UUID uuid) {
        int i = 0;
        for (Shop shop : dsMap.values()) {
            if (shop.getShopItem()==null) continue;
            if (shop.getOwnerUniqueId()==null || !shop.getOwnerUniqueId().equals(uuid)) continue;

            ++i;
        } return i;
    }

    /**
     * Sorts the shop map by the current index.
     * 
     * Used by GUIs
     */
    public static void sortShops(List<Map.Entry<String, Shop>> dsMap, int index) {
        switch (index) {
            case 0: // Unsorted
                break;

            case 1: // Material
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
                break;

            case 2: // Buy Price per Item
                dsMap.sort((e1, e2) -> {
                    Shop s1 = e1.getValue();
                    Shop s2 = e2.getValue();
                    double buy1 = s1.getBuyPrice(false);
                    double buy2 = s2.getBuyPrice(false);

                    if (buy1 == -1 && buy2 == -1) return 0; // no movement
                    if (buy1 == -1) return 1;  // s1 goes after s2
                    if (buy2 == -1) return -1; // s2 goes after s1

                    double unit1 = buy1 / s1.getShopItemAmount();
                    double unit2 = buy2 / s2.getShopItemAmount();
                    return Double.compare(unit1, unit2);
                });
                break;

            case 3: // Sell Price per Item
                dsMap.sort((e1, e2) -> {
                    Shop s1 = e1.getValue();
                    Shop s2 = e2.getValue();
                    double sell1 = s1.getSellPrice(false);
                    double sell2 = s2.getSellPrice(false);

                    if (sell1 == -1 && sell2 == -1) return 0; // no movement
                    if (sell1 == -1) return 1;  // s1 goes after s2
                    if (sell2 == -1) return -1; // s2 goes after s1

                    double unit1 = sell1 / s1.getShopItemAmount();
                    double unit2 = sell2 / s2.getShopItemAmount();
                    return Double.compare(unit2, unit1);
                });
                break;

            case 4: // Stored Balance
                dsMap.sort((e1, e2) -> {
                    Shop s1 = e1.getValue();
                    Shop s2 = e2.getValue();
                    double bal1 = s1.getStoredBalance();
                    double bal2 = s2.getStoredBalance();

                    if (s1.isAdminShop() && s2.isAdminShop()) return 0;  // s1 goes after s2
                    if (s1.isAdminShop()) return -1;  // s2 goes after s1
                    if (s2.isAdminShop()) return 1;   // s1 goes after s2

                    if (bal1 == -1 && bal2 == -1) return 0; // no movement
                    if (bal1 == -1) return -1;  // s2 goes after s1
                    if (bal2 == -1) return 1;   // s1 goes after s2
                    return Double.compare(bal2, bal1);
                });
                break;

            case 5: // Stored Stock
                dsMap.sort((e1, e2) -> {
                    Shop s1 = e1.getValue();
                    Shop s2 = e2.getValue();
                    int stock1 = s1.getStock();
                    int stock2 = s2.getStock();

                    if (stock1 == -1 && stock2 == -1) return 0; // no movement
                    if (stock1 == -1) return -1; // s2 goes after s1
                    if (stock2 == -1) return 1;  // s1 goes after s2

                    return Integer.compare(stock2, stock1);
                });
                break;

            default:
                break;
        }
    }

    /**
     * Sorts the shop map by the current index.
     * 
     * Used by GUIs
     */
    public static void sortShopsAdmin(List<Map.Entry<String, Shop>> dsMap, int index) {
        switch (index) {
            case 0: // Unsorted
                break;

            case 1: // Material
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
                break;

            case 2: // Buy Price per Item
                dsMap.sort((e1, e2) -> {
                    Shop s1 = e1.getValue();
                    Shop s2 = e2.getValue();
                    double buy1 = s1.getBuyPrice(false);
                    double buy2 = s2.getBuyPrice(false);

                    if (buy1 == -1 && buy2 == -1) return 0; // no movement
                    if (buy1 == -1) return 1;  // s1 goes after s2
                    if (buy2 == -1) return -1; // s2 goes after s1

                    double unit1 = buy1 / s1.getShopItemAmount();
                    double unit2 = buy2 / s2.getShopItemAmount();
                    return Double.compare(unit1, unit2);
                });
                break;

            case 3: // Sell Price per Item
                dsMap.sort((e1, e2) -> {
                    Shop s1 = e1.getValue();
                    Shop s2 = e2.getValue();
                    double sell1 = s1.getSellPrice(false);
                    double sell2 = s2.getSellPrice(false);

                    if (sell1 == -1 && sell2 == -1) return 0; // no movement
                    if (sell1 == -1) return 1;  // s1 goes after s2
                    if (sell2 == -1) return -1; // s2 goes after s1

                    double unit1 = sell1 / s1.getShopItemAmount();
                    double unit2 = sell2 / s2.getShopItemAmount();
                    return Double.compare(unit2, unit1);
                });
                break;

            case 4: // Stored Balance
                dsMap.sort((e1, e2) -> {
                    Shop s1 = e1.getValue();
                    Shop s2 = e2.getValue();
                    double bal1 = s1.getStoredBalance();
                    double bal2 = s2.getStoredBalance();

                    if (s1.isAdminShop() && s2.isAdminShop()) return 0;  // s1 goes after s2
                    if (s1.isAdminShop()) return -1;  // s2 goes after s1
                    if (s2.isAdminShop()) return 1;   // s1 goes after s2

                    if (bal1 == -1 && bal2 == -1) return 0; // no movement
                    if (bal1 == -1) return -1;  // s2 goes after s1
                    if (bal2 == -1) return 1;   // s1 goes after s2
                    return Double.compare(bal2, bal1);
                });
                break;

            case 5: // Stored Stock
                dsMap.sort((e1, e2) -> {
                    Shop s1 = e1.getValue();
                    Shop s2 = e2.getValue();
                    int stock1 = s1.getStock();
                    int stock2 = s2.getStock();

                    if (stock1 == -1 && stock2 == -1) return 0; // no movement
                    if (stock1 == -1) return -1; // s2 goes after s1
                    if (stock2 == -1) return 1;  // s1 goes after s2

                    return Integer.compare(stock2, stock1);
                });
                break;

            default:
                break;
        }
    }
}