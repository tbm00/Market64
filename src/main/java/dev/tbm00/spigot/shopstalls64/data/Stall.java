package dev.tbm00.spigot.shopstalls64.data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.World;

import com.griefdefender.api.claim.Claim;

import xzot1k.plugins.ds.api.objects.Shop;

public class Stall {
    private int id;
    private UUID claimUuid;
    private Claim claim;                                                       // not stored in mysql
    private Set<UUID> shopUuids = new HashSet<>();                             // not stored in mysql
    private ConcurrentHashMap<String, Shop> shops = new ConcurrentHashMap<>(); // not stored in mysql
    private World world;
    private int[] storageCoords; // x,y,z
    private Double initialPrice;
    private Double renewalPrice;
    private boolean rented;
    private UUID renterUuid;
    private String renterName;
    private Date evictionDate;
    private Date lastTransaction;

    /**
     * Constructs a Stall with all properties initialized.
     *
     * @param id unique identifier for the stall
     * @param claimUuid UUID of the land claim
     * @param claim the land claim for the stall
     * @param shops Map of shops in the stall
     * @param world world where the stall resides
     * @param storageCoords coordinates (x, y, z) for expired goods storage
     * @param initialPrice initial rental price
     * @param renewalPrice renewal rental price
     * @param rented whether the stall is currently rented
     * @param renterUuid UUID of the renter
     * @param renterName name of the renter
     * @param evictionDate date when the renter will be evicted if not renewed
     * @param lastTransaction date of the last shop transaction
     */
    public Stall(int id,
                 UUID claimUuid,
                 Claim claim,
                 Set<UUID> shopUuids,
                 ConcurrentHashMap<String, Shop> shops,
                 World world,
                 int[] storageCoords,
                 Double initialPrice,
                 Double renewalPrice,
                 boolean rented,
                 UUID renterUuid,
                 String renterName,
                 Date evictionDate,
                 Date lastTransaction) {
        this.id = id;
        this.claimUuid = claimUuid;
        this.claim = claim;
        this.shopUuids = new HashSet<>(shopUuids);
        this.shops = shops;
        this.world = world;
        this.storageCoords = storageCoords;
        this.initialPrice = initialPrice;
        this.renewalPrice = renewalPrice;
        this.rented = rented;
        this.renterUuid = renterUuid;
        this.renterName = renterName;
        this.evictionDate = evictionDate;
        this.lastTransaction = lastTransaction;
    }

    /** @return the stall's id */
    public int getId() {
        return id;
    }
    /** @param id the id to set */
    public void setId(int id) {
        this.id = id;
    }

    /** @return the claim UUID */
    public UUID getClaimUuid() {
        return claimUuid;
    }
    /** @param claimUuid the claim UUID to set */
    public void setClaimUuid(UUID claimUuid) {
        this.claimUuid = claimUuid;
    }

    /** @return the claim */
    public Claim getClaim() {
        return claim;
    }
    /** @param claim the claim to set */
    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    /** @return the shop UUIDs */
    public Set<UUID> getShopUuids() {
        return shopUuids;
    }
    /** @param shopUuids the shop UUIDs to set */
    public void setShopUuids(Set<UUID> shopUuids) {
        this.shopUuids = shopUuids;
    }
    /** @param uuid the shop UUID to add */
    public void addShopUuidToSet(UUID uuid) {
        shopUuids.add(uuid);
    }

    /** @return the shop map */
    public ConcurrentHashMap<String, Shop> getShopMap() {
        return shops;
    }
    /** @param shops the shop map to set */
    public void setShopMap(ConcurrentHashMap<String, Shop> shops) {
        this.shops = shops;
    }
    /** @return the shop map */
    public void addShopToMap(Shop shop, String locationStr) {
        shops.put(locationStr, shop);
    }

    /** @return the world */
    public World getWorld() {
        return world;
    }
    /** @param world the world to set */
    public void setWorld(World world) {
        this.world = world;
    }

    /** @return the expired goods storage location */
    public int[] getStorageCoords() {
        return storageCoords;
    }
    /** @param storageCoords the storage location to set */
    public void setStorageCoords(int[] storageCoords) {
        this.storageCoords = storageCoords;
    }

    /** @return the initial price */
    public Double getInitialPrice() {
        return initialPrice;
    }
    /** @param initialPrice the initial price to set */
    public void setInitialPrice(Double initialPrice) {
        this.initialPrice = initialPrice;
    }

    /** @return the renewal price */
    public Double getRenewalPrice() {
        return renewalPrice;
    }
    /** @param renewalPrice the renewal price to set */
    public void setRenewalPrice(Double renewalPrice) {
        this.renewalPrice = renewalPrice;
    }

    /** @return whether the stall is rented */
    public boolean isRented() {
        return rented;
    }
    /** @param rented set whether the stall is rented */
    public void setRented(boolean rented) {
        this.rented = rented;
    }

    /** @return the renter's UUID */
    public UUID getRenterUuid() {
        return renterUuid;
    }
    /** @param renterUuid the renter UUID to set */
    public void setRenterUuid(UUID renterUuid) {
        this.renterUuid = renterUuid;
    }

    /** @return the renter's name */
    public String getRenterName() {
        return renterName;
    }
    /** @param renterName the renter name to set */
    public void setRenterName(String renterName) {
        this.renterName = renterName;
    }

    /** @return the eviction date */
    public Date getEvictionDate() {
        return evictionDate;
    }
    /** @param evictionDate the eviction date to set */
    public void setEvictionDate(Date evictionDate) {
        this.evictionDate = evictionDate;
    }

    /** @return the last shop transaction date */
    public Date getLastTransaction() {
        return lastTransaction;
    }
    /** @param lastTransaction the last transaction date to set */
    public void setLastTransaction(Date lastTransaction) {
        this.lastTransaction = lastTransaction;
    }
}