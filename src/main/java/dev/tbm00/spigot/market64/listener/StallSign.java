package dev.tbm00.spigot.market64.listener;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.griefdefender.api.claim.Claim;

import dev.tbm00.spigot.market64.StallHandler;
import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.data.Stall;

public class StallSign implements Listener {
    private final StallHandler stallHandler;

    public StallSign(StallHandler stallHandler) {
        this.stallHandler = stallHandler;
    }

    /**
     * Opens the stall's GUI.
     *
     * @param event the PlayerInteractEvent
     */
    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getState() instanceof Sign) {
            Sign clickedSign = (Sign) event.getClickedBlock().getState();
            if (clickedSign.getSide(Side.BACK).getLine(0).contains("[Stall") ||
                clickedSign.getSide(Side.FRONT).getLine(0).contains("[Stall") ) {
                if (!StaticUtil.hasPermission(event.getPlayer(), StaticUtil.PLAYER_PERM)) {
                    StaticUtil.sendMessage(event.getPlayer(), "&cNo permission!");
                    return;
                }
                for (Stall stall : stallHandler.getStalls()) {
                    if (stall.getSignLocation().equals(event.getClickedBlock().getLocation())) {
                        // trigger gui opening
                        return;
                    }
                }
            }
        }
    }

    /**
     * Create a stall using a sign.
     *
     * @param event the SignChangeEvent
     */
    @EventHandler
    public void onSignEdit(SignChangeEvent event) {
        if (!StaticUtil.hasPermission(event.getPlayer(), StaticUtil.ADMIN_PERM)) {
            return;
        }

        int id;
        int rentalTimeDays, maxPlayTimeDays;
        double initialPrice, renewalPrice;
        Block signBlock = event.getBlock();
        String storageLocCoords;
        UUID claimUuid;

        String line1 = event.getLine(0).trim();
        if (!line1.contains("stall") && !line1.contains("Stall")) return;
        String[] parts1 = line1.split("\\s+");
        if (parts1.length < 2) {
            StaticUtil.sendMessage(event.getPlayer(), "&cInvalid format on line 1, expecting 'Stall <id>'");
            return;
        } try {
            id = Integer.parseInt(parts1[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(event.getPlayer(), "&cInvalid stall ID: " + parts1[1]);
            return;
        } if (stallHandler.getStall(id)!=null) {
            StaticUtil.sendMessage(event.getPlayer(), "&cThere is already a stall with that ID!");
            return;
        }

        String line2 = event.getLine(1).trim();
        String[] parts2 = line2.split("\\s+");
        if (parts2.length < 2) {
            StaticUtil.sendMessage(event.getPlayer(), "&cInvalid format on line 2, expecting <rentalDays> <maxPlayDays>");
            return;
        } try {
            rentalTimeDays    = Integer.parseInt(parts2[0]);
            maxPlayTimeDays   = Integer.parseInt(parts2[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(event.getPlayer(), "&cCould not parse numbers on line 2: " + line2);
            return;
        }

        String line3 = event.getLine(2).trim();
        String[] parts3 = line3.split("\\s+");
        if (parts3.length < 2) {
            StaticUtil.sendMessage(event.getPlayer(), "&cInvalid format on line 3, expecting <initialPrice> <renewalPrice>");
            return;
        } try {
            initialPrice = Double.parseDouble(parts3[0]);
            renewalPrice = Double.parseDouble(parts3[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(event.getPlayer(), "&cCould not parse doubles on line 3: " + line3);
            return;
        }

        storageLocCoords = event.getLine(3).trim();
        if (!storageLocCoords.matches("-?\\d+,-?\\d+,-?\\d+")) {
            StaticUtil.sendMessage(event.getPlayer(), "&cInvalid storage coords: " + storageLocCoords);
            return;
        }

        Claim claim = stallHandler.gdHook.getClaimByLocation(signBlock.getLocation());
        if (claim == null || claim.isWilderness() || !claim.isAdminClaim()) {
            StaticUtil.sendMessage(event.getPlayer(), "&cSign must be inside an admin claim!");
            return;
        } claimUuid = claim.getUniqueId();

        if (!stallHandler.createStall(id, rentalTimeDays, maxPlayTimeDays, initialPrice, renewalPrice, signBlock.getWorld().getName(), signBlock.getLocation(), storageLocCoords, claimUuid)) {
            StaticUtil.sendMessage(event.getPlayer(), "&cStall creation failed!");
            return;
        }

        Sign sign = (Sign) signBlock.getState();
        try {
            sign.getSide(Side.FRONT).setLine(0, "&1[Stall " + id +"]");
            sign.getSide(Side.BACK).setLine(0, "&1[Stall " + id +"]");
            sign.update();
        } catch (Exception e) {
            StaticUtil.log(ChatColor.RED, "Caught exception setting sign text!" + e.getMessage());
        }
    }
}