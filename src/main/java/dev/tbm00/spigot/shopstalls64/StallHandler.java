package dev.tbm00.spigot.shopstalls64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.griefdefender.api.claim.Claim;

import xzot1k.plugins.ds.api.objects.LocationClone;
import xzot1k.plugins.ds.api.objects.Shop;

import dev.tbm00.spigot.shopstalls64.data.MySQLConnection;
import dev.tbm00.spigot.shopstalls64.data.Stall;
import dev.tbm00.spigot.shopstalls64.data.StallDAO;
import dev.tbm00.spigot.shopstalls64.hook.DSHook;
import dev.tbm00.spigot.shopstalls64.hook.EcoHook;
import dev.tbm00.spigot.shopstalls64.hook.GDHook;

public class StallHandler {
    private final StallDAO dao;
    public final DSHook dsHook;
    public final GDHook gdHook;
    public final EcoHook ecoHook;

    // stored sorted in order of stalls' ids
    private final List<Stall> stalls = new ArrayList<>();

    public StallHandler(MySQLConnection db, DSHook dsHook, GDHook gdHook, EcoHook ecoHook) {
        this.dao = new StallDAO(db);
        this.dsHook = dsHook;
        this.gdHook = gdHook;
        this.ecoHook = ecoHook;
        
        reloadAll();
    }

    /**
     * Process (in order):
     *  - Clear current internal cache 
     *  - Get all stall data from sql
     *  - Get the active claim using the world and uuid
     *  - Get the active contained shops using the claim's 3d corner blocks
     *  - Load all stalls to internal cache
     */
    /** Load everything into memory. */
    public void reloadAll() {
        stalls.clear();
        List<Stall> stallsToLoad = dao.loadAll();

        // Load stalls' claims
        for (Stall stall : stallsToLoad) {
            stall.setClaim(gdHook.getClaimByUuid(stall.getWorld(), stall.getClaimUuid()));
        }

        // Load stalls' shops
        ConcurrentHashMap<String, Shop> dsMap = dsHook.pl.getManager().getShopMap();
        shopLoop:
        for (Shop shop : dsMap.values()) {
            LocationClone shopLoc = shop.getBaseLocation();

            for (Stall stall : stallsToLoad) {
                Claim claim = stall.getClaim();

                if (dsHook.isInRegion(shopLoc, gdHook.getLowerNorthWestCorner(claim), gdHook.getUpperSouthEastCorner(claim))) {
                    String locationStr = shopLoc.getWorldName() + "," + shopLoc.getX() + "," + shopLoc.getY() + "," + shopLoc.getZ();
                    stall.addShopToMap(shop, locationStr);
                    stall.addShopUuidToSet(shop.getShopId());
                    continue shopLoop;
                }
            }
        }
        stalls.addAll(stallsToLoad);
    }

    /**
     * Process (in order): 
     *  - Get the active claim using the world and uuid
     *  - Get the active contained shops using the claim's 3d corner blocks
     * 
     *  - Sets each shop's data:
     *      - Sets shop owner to all `ffffff`
     *      - Clears shop manager's
     *      - Sets shop limits to null
     *      - Sets shop itemstack to null
     *      - Sets shop buyPrice and sellPrice to -1
     *      - Sets shop stackSize to 1
     *      - Sets stored balance to 0
     *      - Sets stored stock to 0
     * 
     *  - Creates internal Stall object with relavant attributes and "empty" renter name, placed in arraylist at the id index
     *  - Adds Stall entry to SQL database
     * 
     *  - Returns true on success, false when there was an error
     * @param stallId int
     * @param initialPrice double
     * @param renewalPrice double
     * @param world String
     * @param storageLoc String in form of "x,y,z"
     * @param claimUuid UUID
     */
    public boolean createStall(int stallId, double initialPrice, double renewalPrice, String worldName, String storageLoc, UUID claimUuid) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;

        Claim claim = gdHook.getClaimByUuid(world, claimUuid);
        if (claim == null) return false;

        int[] coords = Arrays.stream(storageLoc.split(","))
                             .mapToInt(Integer::parseInt)
                             .toArray();

        ConcurrentHashMap<String, Shop> dsMap = dsHook.pl.getManager().getShopMap();
        ConcurrentHashMap<String, Shop> stallShops = new ConcurrentHashMap<>();
        Set<UUID> shopUuids = new HashSet<>();
        shopLoop:
        for (Shop shop : dsMap.values()) {
            LocationClone shopLoc = shop.getBaseLocation();

            if (dsHook.isInRegion(shopLoc, gdHook.getLowerNorthWestCorner(claim), gdHook.getUpperSouthEastCorner(claim))) {
                String locationStr = shopLoc.getWorldName() + "," + shopLoc.getX() + "," + shopLoc.getY() + "," + shopLoc.getZ();
                stallShops.put(locationStr, shop);
                shopUuids.add(shop.getShopId());
                continue shopLoop;
            }
        }
        
        Stall newStall = new Stall(stallId, claimUuid, claim, shopUuids, stallShops, world, coords, initialPrice, renewalPrice,
                            false, null, null, null, null);
            
        if (!dao.insert(newStall)) return false;
        ensureCapacity(stallId);
        stalls.set(stallId, newStall);
        return true;
    }

    /**
     * Process (in order): 
     *  - Deletes internal Stall object (sets the object to null in ArrayList, so it stays in same order)
     *  - Deletes Stall entry from SQL database (using updateStall())
     * 
     *  - Returns true on success, false when there was an error
     * @param stallId int
     */
    public boolean deleteStall(int stallId) {
        return false;
    }

    /**
     * Process (in order): 
     *  - Charges the player the initial price
     * 
     *  - Resets the each shop's data:
     *      - Sets shop owner to player.getUniqueId()
     *      - Sets shop buyPrice to 960 and sellPrice to 360
     * 
     *  - Adds player to claim's trustlist
     * 
     *  - Sets stalls' 'rented' attribute to true
     *  - Sets stalls' eviction date to 7 days from the present
     * 
     *  - Updates internal Stall object & other attributes accordingly
     *  - Updates Stall entry in SQL database
     *  
     *  - Returns true on success, false when there was an error
     */
    public boolean rentStall(int stallId, OfflinePlayer player) {
        return false;
    }

    /**
     * Process (in order): 
     *  - Charges the player the renewal price
     * 
     *  - Increases stalls' eviction date by 7 days
     * 
     *  - Updates internal Stall object & other attributes accordingly
     *  - Updates Stall entry in SQL database
     *  
     *  - Returns true on success, false when there was an error
     */
    public boolean renewStall(int stallId, OfflinePlayer player) {
        return false;
    }

    /**
     * Process (in order): 
     *  - Creates PAPER itemstack named "Stall #<stallId> Eviction"
     *  - Adds lore to PAPER itemstack: "Renter: <renter's name>"
     *  - Adds lore to PAPER itemstack: "Reason: <reason>" 
     *  - Adds lore to PAPER itemstack: "Date: <eviction date (i.e. current date)>" 
     *  - Adds lore to PAPER itemstack: "Last Payment: <lastPaymentDate>" 
     *  - Clears stored balance ($) from each of the stall's shops, temporarily stores total cleared as double moneyCleared
     *  - Adds lore to PAPER itemstack: "Money: <moneyCleared>"
     *  - Creates SHULKER itemstack(s) named "Stall #<stallId> Eviction Stock #<shulkerId>"
     *  - Adds lore to SHULKER itemstack: "Renter: <renter's name>"
     *  - Adds lore to SHULKER itemstack: "Date: <eviction date (i.e. current date)>" 
     *  - Moves stored stock from stall's shops into shulker(s), temporarily stores total created as int totalCleared
     *  - Stores the SHULKER into the barrel at the Stall's storageCoords (if it doesnt exist, create it, if its full create new barrel 1 block below)
     *  - Adds lore to PAPER itemstack: "Shulkers: <totalCleared>"
     *  - Stores the PAPER into the barrel at the Stall's storageCoords (if it doesnt exist, create it, if its full create new barrel 1 block below)
     *  - Confirms each shop's balance and stock was successfully cleared from the Shop object
     * 
     *  - Resets each shop's data:
     *      - Sets shop owner to all `ffffff`
     *      - Clears shop manager's
     *      - Sets shop limits to null
     *      - Sets shop itemstack to null
     *      - Sets shop buyPrice and sellPrice to -1
     *      - Sets shop stackSize to 1
     *      - Sets stored balance to 0
     *      - Sets stored stock to 0
     * 
     *  - Removes all trusted members from the claim
     * 
     *  - Sets stalls' 'rented' attribute to false
     * 
     *  - Updates internal Stall object & other attributes accordingly
     *  - Updates Stall entry in SQL database
     *  
     *  - Returns true on success, false when there was an error
     */
    public boolean evictStall(int stallId, String reason) {
        return false;
    }

    /**
     * Process (in order): 
     *  - Clears stored balance ($) from each of the stall's shops and sends total to player's vault balance
     *  - Creates SHULKER itemstack(s) named "Stall #<stallId> Abandoned Stock #<shulkerId>"
     *  - Adds lore to SHULKER itemstack: "Renter: <renter's name>"
     *  - Adds lore to SHULKER itemstack: "Date: <abandon date (i.e. current date)>" 
     *  - Moves stored stock from stall's shops into shulker(s), temporarily stores total created as int totalCleared
     *  - Stores the SHULKER into player's inventory
     *  - Confirms each shop's balance and stock was successfully cleared from the Shop object
     * 
     *  - Resets each shop's data:
     *      - Sets shop owner to all `ffffff`
     *      - Clears shop manager's
     *      - Sets shop limits to null
     *      - Sets shop itemstack to null
     *      - Sets shop buyPrice and sellPrice to -1
     *      - Sets shop stackSize to 1
     *      - Sets stored balance to 0
     *      - Sets stored stock to 0
     * 
     *  - Removes all trusted members from the claim
     * 
     *  - Sets stalls' 'rented' attribute to false
     * 
     *  - Updates internal Stall object & other attributes accordingly
     *  - Updates Stall entry in SQL database
     *  
     *  - Returns true on success, false when there was an error
     */
    public boolean abandonStall(int stallId, Player player) {
        return false;
    }

    /**
     * Process (in order, for each stall): 
     *  - If stall is active, check if the renewal date has passed.
     *     - If the renewal date has passed, remove the renewal price from OfflinePlayer's vault balance
     *        - If that fails (ie they don't have enough money), evict the stall
     *     - If the renewal date hasn't passed, check if the stall has had a transaction in the last week (using last transaction date)
     *        - If it has been a week since the transaction date, evict the stall
     *        - If it hasn'tbeen a week since the transaction date, check if the player has more than 3 weeks of playtime
     *           - If renter has more than 3 weeks of playtime, evict the stall
     * Then returns the amount of shops that were evicted
     */
    public int dailyTask() {
        return 0;
    }

    /** Get a stall by ID (from cache or from DB if absent). */
    public Stall getStall(int id) {
        if (id < 0) return null;
        if (id < stalls.size() && stalls.get(id) != null) {
            return stalls.get(id);
        }
        Stall s = dao.loadById(id);
        if (s != null) {
            ensureCapacity(id);
            stalls.set(id, s);
        }
        return s;
    }

    /** Return unmodifiable view of all internal cached stalls. */
    public List<Stall> getStalls() {
        return Collections.unmodifiableList(stalls);
    }

    /** Return dao object for accessing database. */
    public StallDAO getStallDao() {
        return dao;
    }

    private void ensureCapacity(int id) {
        while (stalls.size() <= id) stalls.add(null);
    }
}