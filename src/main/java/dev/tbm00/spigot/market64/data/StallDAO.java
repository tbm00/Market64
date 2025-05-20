package dev.tbm00.spigot.market64.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import dev.tbm00.spigot.market64.StaticUtil;

public class StallDAO {
    private final MySQLConnection db;

    // SQL statements formatted for readability
    private static final String SELECT_ALL_SQL =
        "SELECT * FROM market64_stalls";

    private static final String SELECT_BY_ID_SQL =
        "SELECT * FROM market64_stalls WHERE id = ?";

    private static final String INSERT_SQL = """
        INSERT INTO market64_stalls (
            id,
            claim_uuid,
            world,
            sign_coords,
            storage_coords,
            initial_price,
            renewal_price,
            rental_time,
            max_play_time,
            rented,
            renter_uuid,
            renter_name,
            eviction_date,
            last_transaction_date
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE market64_stalls
        SET
            id = ?,
            claim_uuid = ?,
            world = ?,
            sign_coords = ?,
            storage_coords = ?,
            initial_price = ?,
            renewal_price = ?,
            rental_time = ?,
            max_play_time = ?,
            rented = ?,
            renter_uuid = ?,
            renter_name = ?,
            eviction_date = ?,
            last_transaction_date = ?
        WHERE id = ?
        """;

    private static final String DELETE_SQL =
        "DELETE FROM market64_stalls WHERE id = ?";

    public StallDAO(MySQLConnection db) {
        this.db = db;
    }

    public List<Stall> loadAll() {
        List<Stall> out = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                out.add(newStallFromResultSet(rs));
            }
        } catch (SQLException e) {
            StaticUtil.log(ChatColor.RED, "Error loading stalls: " + e.getMessage());
        }
        return out;
    }

    public Stall loadById(int id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return newStallFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            StaticUtil.log(ChatColor.RED, "Error loading stall " + id + ": " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Stall s) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                prepareStatement(ps, s);
                return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            StaticUtil.log(ChatColor.RED, "Error inserting stall: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Stall s) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                prepareStatement(ps, s);
                ps.setInt(15, s.getId());
                return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            StaticUtil.log(ChatColor.RED, "Error updating stall: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
                ps.setInt(1, id);
                return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            StaticUtil.log(ChatColor.RED, "Error deleting stall: " + e.getMessage());
            return false;
        }
    }

    private Stall newStallFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        UUID claimUuid = UUID.fromString(rs.getString("claim_uuid"));
        World world = Bukkit.getWorld(rs.getString("world"));
        Location signLocation = getLocationFromCoords(world, rs.getString("sign_coords"));
        int[] storageCoords = parseCoords(rs.getString("storage_coords"));
        double initialPrice = rs.getDouble("initial_price");
        double renewalPrice  = rs.getDouble("renewal_price");
        int rentalTime  = rs.getInt("rental_time");
        int maxPlayTime  = rs.getInt("max_play_time");
        boolean rented = rs.getBoolean("rented");
        UUID renterUuid = rented ? UUID.fromString(rs.getString("renter_uuid")) : null;
        String renterName = rs.getString("renter_name");
        java.util.Date eviction = toUtilDate(rs.getTimestamp("eviction_date"));
        java.util.Date lastTransaction  = toUtilDate(rs.getTimestamp("last_transaction_date"));

        return new Stall(id, claimUuid, null, Collections.emptySet(), new ConcurrentHashMap<>(), world, signLocation, storageCoords, initialPrice, renewalPrice, 
                        rentalTime, maxPlayTime, rented, renterUuid, renterName, eviction, lastTransaction);
    }

    private void prepareStatement(PreparedStatement ps, Stall s) throws SQLException {
        ps.setInt(1, s.getId());
        ps.setString(2, s.getClaimUuid().toString());
        ps.setString(3, s.getWorld().getName());
        ps.setString(4, joinCoords(getCoordsFromLocation(s.getSignLocation())));
        ps.setString(5, joinCoords(s.getStorageCoords()));
        ps.setDouble(6, s.getInitialPrice());
        ps.setDouble(7, s.getRenewalPrice());
        ps.setInt(8, s.getRentalTimeDays());
        ps.setInt(9, s.getPlayTimeDays());
        ps.setBoolean(10, s.isRented());
        ps.setString(11, s.isRented() ? (s.getRenterUuid()!=null ? s.getRenterUuid().toString() : Bukkit.getServer().getOfflinePlayer(s.getRenterName()).getUniqueId().toString()) : null);
        ps.setString(12, s.getRenterName());
        ps.setTimestamp(13, toSqlTimestamp(s.getEvictionDate()));
        ps.setTimestamp(14, toSqlTimestamp(s.getLastTransaction()));
    }

    private int[] parseCoords(String coords) {
        if (coords == null || coords.isEmpty()) return new int[]{0,0,0};
        String[] parts = coords.split(",");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) out[i] = Integer.parseInt(parts[i].trim());
        return out;
    }

    private int[] getCoordsFromLocation(Location location) {
        if (location == null) return new int[]{0,0,0};
        return new int[]{location.getBlockX(),location.getBlockY(),location.getBlockZ()};
    }

    private Location getLocationFromCoords(World world, String coords) {
        if (world==null || coords==null || coords.isEmpty()) return null;

        int[] coordsArr = parseCoords(coords);

        return world.getBlockAt(coordsArr[0], coordsArr[1], coordsArr[2]).getLocation();
    }

    private String joinCoords(int[] coords) {
        return Arrays.stream(coords)
                     .mapToObj(String::valueOf)
                     .collect(Collectors.joining(","));
    }

    private java.sql.Timestamp toSqlTimestamp(java.util.Date d) {
        return d != null ? new java.sql.Timestamp(d.getTime()) : null;
    }

    private java.util.Date toUtilDate(java.sql.Timestamp d) {
        return d != null ? new java.util.Date(d.getTime()) : null;
    }
}