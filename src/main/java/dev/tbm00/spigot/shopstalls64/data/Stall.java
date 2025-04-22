package dev.tbm00.spigot.shopstalls64.data;

import java.sql.Date;
import java.util.UUID;

import org.bukkit.World;

public class Stall {
    
    private int id;
    private UUID claimUuid; // use ShopStalls64.gdHook.getClaimByUuid(World, UUID)
    private UUID[] shopUuids; // use ShopStalls64.dsHook.getManager().getShopById(UUID)
    
    private World world;
    private int[] expiredGoodsStorageLocaiton; // x,y,z, (if it gets full, create a new barrel block underneath the current, and continue filling)
    private Double intialPrice;
    private Double renewalPrice;
    
    private boolean rented; // is shop currently rented by someone?
    private UUID renterUuid;
    private String renterName;
    private Date evictionDate;
    private Date lastShopTranscationDate;

    public Stall() {}
}
