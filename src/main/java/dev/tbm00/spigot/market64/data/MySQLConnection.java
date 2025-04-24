package dev.tbm00.spigot.market64.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.plugin.java.JavaPlugin;

public class MySQLConnection {
    private HikariDataSource dataSource;
    private JavaPlugin javaPlugin;
    private HikariConfig config;

    public MySQLConnection(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        
        loadSQLConfig();
        setupConnectionPool();
        initializeDatabase();
    }

    private void loadSQLConfig() {
        config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + javaPlugin.getConfig().getString("mysql.host") + 
                        ":" + javaPlugin.getConfig().getInt("mysql.port") + 
                        "/" + javaPlugin.getConfig().getString("mysql.database") +
                        "?useSSL=" + javaPlugin.getConfig().getBoolean("mysql.useSSL", false));
        config.setUsername(javaPlugin.getConfig().getString("mysql.username"));
        config.setPassword(javaPlugin.getConfig().getString("mysql.password"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "100");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(javaPlugin.getConfig().getInt("mysql.hikari.maximumPoolSize"));
        config.setMinimumIdle(javaPlugin.getConfig().getInt("mysql.hikari.minimumPoolSize"));
        config.setIdleTimeout(javaPlugin.getConfig().getInt("mysql.hikari.idleTimeout")*1000);
        config.setConnectionTimeout(javaPlugin.getConfig().getInt("mysql.hikari.connectionTimeout")*1000);
        config.setMaxLifetime(javaPlugin.getConfig().getInt("mysql.hikari.maxLifetime")*1000);
        if (javaPlugin.getConfig().getBoolean("mysql.hikari.leakDetection.enabled"))
            config.setLeakDetectionThreshold(javaPlugin.getConfig().getInt("mysql.hikari.leakDetection.threshold")*1000);
    }

    private void setupConnectionPool() {
        dataSource = new HikariDataSource(config);
        javaPlugin.getLogger().info("Initialized Hikari connection pool.");

        try (Connection connection = getConnection()) {
            if (connection.isValid(2))
                javaPlugin.getLogger().info("MySQL database connection is valid!");
        } catch (SQLException e) {
            javaPlugin.getLogger().severe("Failed to establish connection to MySQL database: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed())
            dataSource.close();
    }

    private void initializeDatabase() {
        String playerTable = "CREATE TABLE IF NOT EXISTS market64_shop (" +
                "id INT PRIMARY KEY NOT NULL, " +
                "claim_uuid VARCHAR(36) NOT NULL, " +
                "world TEXT NOT NULL, " +
                "storage_coords TEXT NOT NULL, " +
                "initial_price DOUBLE NOT NULL, " +
                "renewal_price DOUBLE NOT NULL, " +
                "rental_time INT NOT NULL, " +
                "max_play_time INT NOT NULL, " +
                "rented BOOLEAN NOT NULL, " +
                "renter_uuid VARCHAR(36), " +
                "renter_name VARCHAR(20), " +
                "eviction_date TIMESTAMP, " +
                "last_transaction_date TIMESTAMP);";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(playerTable);
        } catch (SQLException e) {
            javaPlugin.getLogger().severe("Error initializing database: " + e.getMessage());
        }
    }
}