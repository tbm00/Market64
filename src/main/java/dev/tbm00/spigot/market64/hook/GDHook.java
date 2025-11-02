package dev.tbm00.spigot.market64.hook;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;

public class GDHook {

	public GDHook() {}

	public Set<Claim> getClaims(UUID playerUuid) {
		return new HashSet<>(GriefDefender.getCore().getAllPlayerClaims(playerUuid));
	}
	
	public String getRegionID(Location location) {
		final Vector3i vector = Vector3i.from(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaimManager(location.getWorld().getUID()).getClaimAt(vector);
		return !claim.isWilderness() ? claim.getUniqueId().toString() : null;
	}

	public String getClaimOwner(Location location) {
		final Vector3i vector = Vector3i.from(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaimManager(location.getWorld().getUID()).getClaimAt(vector);
		return !claim.isWilderness() ? claim.getOwnerName() : null;
	}

	public Claim getClaimByLocation(Location location) {
		final Vector3i vector = Vector3i.from(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		final Core gd = GriefDefender.getCore();
		return gd.getClaimManager(location.getWorld().getUID()).getClaimAt(vector);
	}

	public Claim getClaimByUuid(World world, UUID id) {
		final Core gd = GriefDefender.getCore();
		return gd.getClaimManager(world.getUID()).getClaimByUUID(id);
	}

	public Vector3i getLowerNorthWestCorner(Claim claim) {
		return claim.getLesserBoundaryCorner();
	}

	public Vector3i getUpperSouthEastCorner(Claim claim) {
		return claim.getGreaterBoundaryCorner();
	}

	public boolean hasBuilderTrust(OfflinePlayer player, String regionID) {
		if (regionID == null || regionID.isEmpty()) return true;
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(regionID));
		if (claim == null) return true;
		if (claim.getUserTrusts(TrustTypes.MANAGER).contains(player.getUniqueId())
		|| claim.getUserTrusts(TrustTypes.BUILDER).contains(player.getUniqueId()))
			return true;
		return player.getUniqueId().toString().equals(claim.getOwnerUniqueId().toString());
	}

	public boolean hasPvPEnabled(String regionID) {
		if (regionID == null || regionID.isEmpty()) return true;
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(regionID));
		if (claim == null) return true;
		return claim.isPvpAllowed();	
	}
}