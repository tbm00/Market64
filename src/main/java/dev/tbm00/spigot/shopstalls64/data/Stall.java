package dev.tbm00.spigot.shopstalls64.data;

import java.sql.Date;
import java.util.UUID;

import org.bukkit.World;

public class Stall {
    private int id;
    private UUID claimUuid; // use ShopStalls64.gdHook.getClaimByUuid(World, UUID)
    private UUID[] shopUuids; // use ShopStalls64.dsHook.getManager().getShopById(UUID)
    private World world;
    private int[] expiredGoodsStorageLocaiton; // x,y,z
    private Double intialPrice;
    private Double renewalPrice;
    private boolean rented;
    private UUID renterUuid;
    private String renterName;
    private Date evictionDate;
    private Date lastShopTranscationDate;

    /**
     * Constructs a Stall with all properties initialized.
     *
     * @param id unique identifier for the stall
     * @param claimUuid UUID of the land claim
     * @param shopUuids UUIDs of shops in the stall
     * @param world world where the stall resides
     * @param expiredGoodsStorageLocaiton coordinates (x, y, z) for expired goods storage
     * @param intialPrice initial rental price
     * @param renewalPrice renewal rental price
     * @param rented whether the stall is currently rented
     * @param renterUuid UUID of the renter
     * @param renterName name of the renter
     * @param evictionDate date when the renter will be evicted if not renewed
     * @param lastShopTranscationDate date of the last shop transaction
     */
    public Stall(int id,
                 UUID claimUuid,
                 UUID[] shopUuids,
                 World world,
                 int[] expiredGoodsStorageLocaiton,
                 Double intialPrice,
                 Double renewalPrice,
                 boolean rented,
                 UUID renterUuid,
                 String renterName,
                 Date evictionDate,
                 Date lastShopTranscationDate) {
        this.id = id;
        this.claimUuid = claimUuid;
        this.shopUuids = shopUuids;
        this.world = world;
        this.expiredGoodsStorageLocaiton = expiredGoodsStorageLocaiton;
        this.intialPrice = intialPrice;
        this.renewalPrice = renewalPrice;
        this.rented = rented;
        this.renterUuid = renterUuid;
        this.renterName = renterName;
        this.evictionDate = evictionDate;
        this.lastShopTranscationDate = lastShopTranscationDate;
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

    /** @return the shop UUIDs */
    public UUID[] getShopUuids() {
        return shopUuids;
    }
    /** @param shopUuids the shop UUIDs to set */
    public void setShopUuids(UUID[] shopUuids) {
        this.shopUuids = shopUuids;
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
    public int[] getExpiredGoodsStorageLocaiton() {
        return expiredGoodsStorageLocaiton;
    }
    /** @param expiredGoodsStorageLocaiton the storage location to set */
    public void setExpiredGoodsStorageLocaiton(int[] expiredGoodsStorageLocaiton) {
        this.expiredGoodsStorageLocaiton = expiredGoodsStorageLocaiton;
    }

    /** @return the initial price */
    public Double getIntialPrice() {
        return intialPrice;
    }
    /** @param intialPrice the initial price to set */
    public void setIntialPrice(Double intialPrice) {
        this.intialPrice = intialPrice;
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
    public Date getLastShopTranscationDate() {
        return lastShopTranscationDate;
    }
    /** @param lastShopTranscationDate the last transaction date to set */
    public void setLastShopTranscationDate(Date lastShopTranscationDate) {
        this.lastShopTranscationDate = lastShopTranscationDate;
    }
}
