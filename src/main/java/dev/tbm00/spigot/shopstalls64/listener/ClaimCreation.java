package dev.tbm00.spigot.shopstalls64.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.event.CreateClaimEvent;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.tbm00.spigot.shopstalls64.StaticUtil;

public class ClaimCreation implements Listener {

    /**
     * Cancels the creation event if the player has X claims within the region already
     *
     * @param event the CreateClaimEvent
     */
    public ClaimCreation(CreateClaimEvent event, ProtectedRegion wgRegion, String MARKET_WORLD) {

        Claim claim = event.getClaim();

        if (!claim.getWorldName().equalsIgnoreCase(MARKET_WORLD)) return;
        if (!StaticUtil.isClaimContained(wgRegion, claim)) return;
    
        if (StaticUtil.hasMaxContainedClaims(wgRegion, event.getSourceUser().getPlayerData().getClaims())) {
            Player player = (Player) event.getSourceUser().getOnlinePlayer();

            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &fYou cannot have more than "+StaticUtil.MAX_CONTAINED_CLAIMS+" claims in our market!");
                event.cancelled(true);
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimCreation listener!");
            }
        }
    }
}