package dev.tbm00.spigot.shopstalls64;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import dev.tbm00.spigot.rep64.Rep64;
import dev.tbm00.spigot.shopstalls64.data.ConfigHandler;
import dev.tbm00.spigot.shopstalls64.data.MySQLConnection;
import dev.tbm00.spigot.shopstalls64.hook.*;
import dev.tbm00.spigot.shopstalls64.command.*;
import dev.tbm00.spigot.shopstalls64.listener.ShopTransaction;

public class ShopStalls64 extends JavaPlugin {
    private ConfigHandler configHandler;
    private MySQLConnection mysqlConnection;
    private StallHandler stallHandler;
    public static DSHook dsHook;
    public static GDHook gdHook;
    public static EcoHook ecoHook;
    public static Rep64 repHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        final PluginDescriptionFile pdf = this.getDescription();

        if (getConfig().contains("enabled") && getConfig().getBoolean("enabled")) {
            configHandler = new ConfigHandler(this);

            StaticUtils.init(this, configHandler);

            // Connect to MySQL
            try {
                mysqlConnection = new MySQLConnection(this);
            } catch (Exception e) {
                getLogger().severe("Failed to connect to MySQL. Disabling plugin.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            StaticUtils.log(ChatColor.LIGHT_PURPLE,
                    ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-",
                    pdf.getName() + " v" + pdf.getVersion() + " created by tbm00",
                    ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
            );

            setupHooks();
            if (configHandler.isFeatureEnabled()) {
                stallHandler = new StallHandler(configHandler, mysqlConnection, dsHook, gdHook, ecoHook);

                // Register Listener
                getServer().getPluginManager().registerEvents(new ShopTransaction(stallHandler), this);
                
                // Register Commands
                getCommand("teststall").setExecutor(new StallCmd(stallHandler));
                getCommand("teststalladmin").setExecutor(new AdminCmd(stallHandler));
            }
        }
    }

    /**
     * Sets up the required hooks for plugin integration.
     * Disables the plugin if any required hook fails.
     */
    private void setupHooks() {
        if (!setupDisplayShops()) {
            getLogger().severe("DisplayShops hook failed -- disabling plugin!");
            disablePlugin();
            return;
        }

        if (!setupGriefDefender()) {
            getLogger().severe("GriefDefender hook failed -- disabling plugin!");
            disablePlugin();
            return;
        }

        if (!setupVault()) {
            getLogger().severe("Vault hook failed -- disabling plugin!");
            disablePlugin();
            return;
        }

        if (!setupRep64()) {
            getLogger().severe("Rep64 hook failed -- disabling plugin!");
            disablePlugin();
            return;
        }
    }

    /**
     * Attempts to hook into the DisplayShops plugin.
     *
     * @return true if the hook was successful, false otherwise.
     */
    private boolean setupDisplayShops() {
        if (!isPluginAvailable("DisplayShops")) return false;

        dsHook = new DSHook(this);

        if (dsHook==null || dsHook.pl==null) {
            return false;
        }

        StaticUtils.log(ChatColor.GREEN, "DisplayShops hooked.");
        return true;
    }

    /**
     * Attempts to hook into the GriefDefender plugin.
     *
     * @return true if the hook was successful, false otherwise.
     */
    private boolean setupGriefDefender() {
        if (!isPluginAvailable("GriefDefender")) return false;

        gdHook = new GDHook();

        StaticUtils.log(ChatColor.GREEN, "GriefDefender hooked.");
        return true;
    }

    /**
     * Attempts to hook into the Vault plugin.
     *
     * @return true if the hook was successful, false otherwise.
     */
    private boolean setupVault() {
        if (!isPluginAvailable("Vault")) return false;

        ecoHook = new EcoHook(this);

        if (ecoHook==null || ecoHook.pl==null) {
            return false;
        }

        StaticUtils.log(ChatColor.GREEN, "Vault hooked.");
        return true;
    }

    /**
     * Attempts to hook into the Rep64 plugin.
     *
     * @return true if the hook was successful, false otherwise.
     */
    private boolean setupRep64() {
        if (!isPluginAvailable("Rep64")) return false;

        Plugin rep64 = Bukkit.getPluginManager().getPlugin("Rep64");
        if (rep64.isEnabled() && rep64 instanceof Rep64)
            repHook = (Rep64) rep64;
        else return false;

        StaticUtils.log(ChatColor.GREEN, "Rep64 hooked.");
        return true;
    }

    /**
     * Checks if the specified plugin is available and enabled on the server.
     *
     * @param pluginName the name of the plugin to check
     * @return true if the plugin is available and enabled, false otherwise.
     */
    private boolean isPluginAvailable(String pluginName) {
		final Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
		return plugin != null && plugin.isEnabled();
	}

    /**
     * Disables the plugin.
     */
    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        mysqlConnection.closeConnection();
        getLogger().info("ShopStalls64 disabled..! ");
    }

    /**
     * Attempts to grab the MySQL database connection
     *
     * @return current MySQL database connection
     */
    public MySQLConnection getDatabase() {
        return mysqlConnection;
    }
}