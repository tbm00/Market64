package dev.tbm00.spigot.shopstalls64.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import dev.tbm00.spigot.shopstalls64.StaticUtil;

public class StallDAO {
    private final MySQLConnection db;

    // SQL statements formatted for readability
    private static final String SELECT_ALL_SQL =
        "SELECT * FROM shopstalls64_shop";

    private static final String SELECT_BY_ID_SQL =
        "SELECT * FROM shopstalls64_shop WHERE id = ?";

    private static final String INSERT_SQL = """
        INSERT INTO shopstalls64_shop (
            id,
            claim_uuid,
            world,
            storage_coords,
            initial_price,
            renewal_price,
            rented,
            renter_uuid,
            renter_name,
            eviction_date,
            last_transaction_date
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE shopstalls64_shop
        SET
            claim_uuid = ?,
            world = ?,
            storage_coords = ?,
            initial_price = ?,
            renewal_price = ?,
            rented = ?,
            renter_uuid = ?,
            renter_name = ?,
            eviction_date = ?,
            last_transaction_date = ?
        WHERE id = ?
        """;

    private static final String DELETE_SQL =
        "DELETE FROM shopstalls64_shop WHERE id = ?";

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
                ps.setInt(12, s.getId());
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
        int[] coords = parseCoords(rs.getString("storage_coords"));
        double initialPrice = rs.getDouble("initial_price");
        double renewalPrice  = rs.getDouble("renewal_price");
        boolean rented = rs.getBoolean("rented");
        String ru = rs.getString("renter_uuid");
        UUID renterUuid = ru != null ? UUID.fromString(ru) : null;
        String renterName = rs.getString("renter_name");
        java.util.Date eviction = toUtilDate(rs.getTimestamp("eviction_date"));
        java.util.Date lastTransaction  = toUtilDate(rs.getTimestamp("last_transaction_date"));

        return new Stall(id, claimUuid, null, null, null, world, coords, initialPrice, renewalPrice,
                         rented, renterUuid, renterName, eviction, lastTransaction);
    }

    private void prepareStatement(PreparedStatement ps, Stall s) throws SQLException {
        ps.setInt(1, s.getId());
        ps.setString(2, s.getClaimUuid().toString());
        ps.setString(3, s.getWorld().getName());
        ps.setString(4, joinCoords(s.getStorageCoords()));
        ps.setDouble(5, s.getInitialPrice());
        ps.setDouble(6, s.getRenewalPrice());
        ps.setBoolean(7, s.isRented());
        ps.setString(8, s.getRenterUuid() != null ? s.getRenterUuid().toString() : null);
        ps.setString(9, s.getRenterName());
        ps.setTimestamp(10, toSqlTimestamp(s.getEvictionDate()));
        ps.setTimestamp(11, toSqlTimestamp(s.getLastTransaction()));
    }

    private int[] parseCoords(String data) {
        if (data == null || data.isEmpty()) return new int[]{0,0,0};
        String[] parts = data.split(",");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) out[i] = Integer.parseInt(parts[i].trim());
        return out;
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