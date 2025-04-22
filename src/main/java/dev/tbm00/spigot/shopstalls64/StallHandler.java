package dev.tbm00.spigot.shopstalls64;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.shopstalls64.data.MySQLConnection;
import dev.tbm00.spigot.shopstalls64.data.Stall;
import dev.tbm00.spigot.shopstalls64.hook.DSHook;
import dev.tbm00.spigot.shopstalls64.hook.EcoHook;
import dev.tbm00.spigot.shopstalls64.hook.GDHook;

public class StallHandler {
    private final MySQLConnection mysqlConnection;
    public final DSHook dsHook;
    public final GDHook gdHook;
    public final EcoHook ecoHook;

    // stored sorted in order of stalls' ids
    public ArrayList<Stall> stalls = new ArrayList<Stall>();

    public StallHandler(MySQLConnection mysqlConnection, DSHook dsHook, GDHook gdHook, EcoHook ecoHook) {
        this.mysqlConnection = mysqlConnection;
        this.dsHook = dsHook;
        this.gdHook = gdHook;
        this.ecoHook = ecoHook;

        loadStallEntries();
    }

    // loads stalls from mysqlConnection
    private void loadStallEntries() {

    }

    // creates stall on mysqlConnection
    private void createEntryInSQL(Stall stall) {

    }

    // updates stall on mysqlConnection
    private void updateEntryInSQL(Stall stall) {

    }

    // deletes stall on mysqlConnection
    private void deleteEntryFromSQL(int id) {

    }

    // get stall from internal list
    // if it doesn't exist, try to get it from mysqlconnection, then populate internal list with query result
    // return the stall or null
    public Stall getStall(int id) {
        return null;
    }

    // get internal stalls
    public ArrayList<Stall> getStalls() {
        return stalls;
    }

    /**
     * Process (in order): 
     *  - Gets the claim using the world and uuid
     *  - Gets the contained shops using the claim's 3d corner blocks
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
     * @param storedGoodLocation String in form of "x,y,z"
     * @param claimUuid UUID
     */
    private boolean createStall(int stallId, double initialPrice, double renewalPrice, String world, String storedGoodLocation, UUID claimUuid) {
        return false;
    }

    /**
     * Process (in order): 
     *  - Deletes internal Stall object (sets the object to null in ArrayList, so it stays in same order)
     *  - Deletes Stall entry from SQL database (using updateStall())
     * 
     *  - Returns true on success, false when there was an error
     * @param stallId int
     */
    private boolean deleteStall(int stallId) {
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
    private boolean rentStall(int stallId, OfflinePlayer player) {
        player.getUniqueId();
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
    private boolean renewStall(int stallId, OfflinePlayer player) {
        player.getUniqueId();
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
     *  - Stores the SHULKER into the barrel at the Stall's expiredGoodsStorageLocaiton (if it doesnt exist, create it, if its full create new barrel 1 block below)
     *  - Adds lore to PAPER itemstack: "Shulkers: <totalCleared>"
     *  - Stores the PAPER into the barrel at the Stall's expiredGoodsStorageLocaiton (if it doesnt exist, create it, if its full create new barrel 1 block below)
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
    private boolean evictStall(int stallId, String reason) {
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
    private boolean abandonStall(int stallId, Player player) {
        return false;
    }

    /**
     * Process (in order, for each stall): 
     *  - If stall is active, check if the renewal date has passed.
     *     - If the renewal date has passed, remove the renewal price from OfflinePlayer's vault balance
     *        - If that fails (ie they don't have enough money), evict them from the stall
     * Then returns the amount of shops that were evicted
     */
    private int dailyTask() {
        return 0;
    }
}
