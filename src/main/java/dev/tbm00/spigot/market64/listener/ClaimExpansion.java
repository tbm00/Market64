package dev.tbm00.spigot.market64.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.event.ChangeClaimEvent;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.tbm00.spigot.market64.StaticUtil;

public class ClaimExpansion implements Listener {

    /**
     * Cancels the change event if the the claim is not completely inside the market
     *
     * @param event the ClaimExpansion
     */
    public ClaimExpansion(ChangeClaimEvent event, ProtectedRegion wgRegion) {

        Claim claim = event.getClaim();

        if (!claim.getWorldName().equalsIgnoreCase(StaticUtil.MARKET_WORLD)) return;
        if (!StaticUtil.isClaimContained(wgRegion, claim)) return;

        if (StaticUtil.isClaimPartlyContained(wgRegion, claim)) {
            Player player = (Player) event.getSourceUser().getOnlinePlayer();

            if (player != null && player instanceof Player) {
                StaticUtil.sendMessage(player, "&4Error: &fYou cannot have expand a claim into our market!");
                event.cancelled(true);
            } else {
                StaticUtil.log(ChatColor.RED, "Could not find player from event.getSourceUser().getOnlinePlayer() in ClaimExpansion listener!");
            }
        }
    }
}