package online.demonzdevelopment.dzeconomy;

import online.demonzdevelopment.dzeconomy.api.DZEconomyAPI;
import online.demonzdevelopment.dzeconomy.api.DZEconomyAPIImpl;
import online.demonzdevelopment.dzeconomy.command.*;
import online.demonzdevelopment.dzeconomy.config.ConfigManager;
import online.demonzdevelopment.dzeconomy.config.ConfigMigrator;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import online.demonzdevelopment.dzeconomy.gui.RequestGUIManager;
import online.demonzdevelopment.dzeconomy.integration.LuckPermsIntegration;
import online.demonzdevelopment.dzeconomy.integration.PlaceholderAPIExpansion;
import online.demonzdevelopment.dzeconomy.listener.*;
import online.demonzdevelopment.dzeconomy.manager.CombatTagManager;
import online.demonzdevelopment.dzeconomy.manager.MigrationManager;
import online.demonzdevelopment.dzeconomy.manager.RankManager;
import online.demonzdevelopment.dzeconomy.storage.StorageProvider;
import online.demonzdevelopment.dzeconomy.storage.StorageType;
import online.demonzdevelopment.dzeconomy.storage.impl.*;
import online.demonzdevelopment.dzeconomy.task.*;
import online.demonzdevelopment.dzeconomy.update.UpdateManager;
import online.demonzdevelopment.dzeconomy.util.ColorUtil;
import online.demonzdevelopment.dzeconomy.util.FoliaAdapter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class DZEconomy extends JavaPlugin {

    private static volatile DZEconomy instance;
    
    private ConfigManager configManager;
    private StorageProvider storageProvider;
    private CurrencyManager currencyManager;
    private RankManager rankManager;
    private RequestGUIManager requestGUIManager;
    private CombatTagManager combatTagManager;
    private LuckPermsIntegration luckPermsIntegration;
    private UpdateManager updateManager;
    private MigrationManager migrationManager;
    private DZEconomyAPI api;
    private online.demonzdevelopment.dzeconomy.util.MessagesUtil messagesUtil;
    private EntityDeathListener entityDeathListener;
    private PlaceholderAPIExpansion placeholderExpansion;

    private long startupTime;

    @Override
    public void onEnable() {
        instance = this;
        startupTime = System.currentTimeMillis();
        
        // Print beautiful startup banner
        printStartupBanner();
        
        // Initialize configuration with migration
        configManager = new ConfigManager(this);
        configManager.loadAll();
        
        // Run config migration if needed
        ConfigMigrator migrator = new ConfigMigrator(this);
        migrator.migrate();
        
        // Initialize storage
        if (!initializeStorage()) {
            getLogger().severe("Failed to initialize storage! Disabling plugin...");
            setEnabled(false);
            return;
        }
        
        // Initialize managers
        this.currencyManager = new CurrencyManager(this);
        this.rankManager = new RankManager(this);
        this.luckPermsIntegration = new LuckPermsIntegration(this);
        this.requestGUIManager = new RequestGUIManager(this);
        this.combatTagManager = new CombatTagManager(this);
        this.migrationManager = new MigrationManager(this);
        
        // Initialize API
        this.api = new DZEconomyAPIImpl();
        Bukkit.getServicesManager().register(DZEconomyAPI.class, this.api, this, org.bukkit.plugin.ServicePriority.Normal);
        
        // Initialize cached utilities
        this.messagesUtil = new online.demonzdevelopment.dzeconomy.util.MessagesUtil(this);
        
        // Register commands
        registerCommands();
        
        // Register events
        registerEvents();
        
        // Register integrations
        registerIntegrations();
        
        // Initialize bStats (wrapped in try-catch to allow MockBukkit tests to pass without shadow relocation)
        try {
            int pluginId = 24898;
            Metrics metrics = new Metrics(this, pluginId);
            getLogger().info("bStats metrics enabled.");
        } catch (IllegalStateException e) {
            getLogger().info("bStats metrics skipped (likely running in a test environment).");
        }
        
        // Schedule tasks
        scheduleTasks();
        
        // Initialize update checker (Modrinth, checks every 6 hours)
        this.updateManager = new UpdateManager(this);
        updateManager.checkForUpdates();
        
        getLogger().info("DZEconomy v2.1.0 has been successfully enabled!");
        getLogger().info("Running on " + (FoliaAdapter.isFolia() ? "Folia" : Bukkit.getName()) + " " + Bukkit.getVersion());
        getLogger().info("Support & Wiki: https://wiki.demonzdevelopment.online/dzeconomy");
        getLogger().info("Thank you for choosing DZEconomy!");
    }

    @Override
    public void onDisable() {
        // Cancel all tasks first to prevent any new storage access
        FoliaAdapter.cancelTasks(this);
        
        // Save all player data before shutdown
        if (currencyManager != null) {
            try {
                currencyManager.saveAllPlayersSync();
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Error saving player data during shutdown", e);
            }
        }
        
        // Close storage
        if (storageProvider != null) {
            try {
                storageProvider.close();
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Error closing storage provider", e);
            }
        }
        
        if (placeholderExpansion != null) {
            try {
                placeholderExpansion.unregister();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to unregister PlaceholderAPI expansion", e);
            }
            placeholderExpansion = null;
        }

        if (this.api != null) {
            try {
                Bukkit.getServicesManager().unregister(DZEconomyAPI.class, this.api);
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to unregister API service", e);
            }
            this.api = null;
        }

        if (luckPermsIntegration != null) {
            try {
                luckPermsIntegration.cleanup();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to cleanup LuckPerms integration", e);
            }
        }

        instance = null;
        getLogger().info("DZEconomy v2.1.0 has been disabled. Thank you for using DZEconomy!");
    }
    
    private void printStartupBanner() {
        getLogger().info("Starting DZEconomy v2.1.0 by DemonZ Development");
    }
    
    private boolean initializeStorage() {
        String storageType = configManager.getConfig().getString("storage.type", "sqlite").toLowerCase();
        
        switch (storageType) {
            case "mysql":
                storageProvider = new MySQLStorageProvider(this);
                break;
            case "sqlite":
                storageProvider = new SQLiteStorageProvider(this);
                break;
            case "flatfile":
                storageProvider = new FlatFileStorageProvider(this);
                break;
            default:
                getLogger().warning("Unknown storage type: " + storageType + ". Defaulting to SQLite.");
                storageProvider = new SQLiteStorageProvider(this);
                break;
        }
        
        try {
            return storageProvider.initialize();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize " + storageType + " storage", e);
            return false;
        }
    }
    
    private void registerCommands() {
        safeRegisterCommand("money", new MoneyCommand(this));
        safeRegisterCommand("mobcoin", new MobCoinCommand(this));
        safeRegisterCommand("gem", new GemCommand(this));
        safeRegisterCommand("economy", new EconomyCommand(this));
    }
    
    private void safeRegisterCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            if (executor instanceof org.bukkit.command.TabCompleter) {
                cmd.setTabCompleter((org.bukkit.command.TabCompleter) executor);
            }
        } else {
            getLogger().warning("Command /" + name + " not found in plugin.yml! Skipping registration.");
        }
    }
    
    private void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        this.entityDeathListener = new EntityDeathListener(this);
        pm.registerEvents(entityDeathListener, this);
        pm.registerEvents(new CombatTagListener(this), this);
        pm.registerEvents(requestGUIManager, this);
    }
    
    private void registerIntegrations() {
        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholderExpansion = new PlaceholderAPIExpansion(this);
            this.placeholderExpansion.register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }
        
        // LuckPerms
        if (luckPermsIntegration.setup()) {
            getLogger().info("LuckPerms integration enabled!");
        } else {
            getLogger().warning("LuckPerms not found. Rank detection will use config-based defaults.");
        }
    }
    
    private void scheduleTasks() {
        // Detect Folia and log accordingly
        if (FoliaAdapter.isFolia()) {
            getLogger().info("Folia detected! Using region-based scheduling.");
        }
        
        // Auto-save (default 5 minutes)
        long autoSaveInterval = configManager.getConfig().getLong("auto-save.interval", 300) * 20L;
        AutoSaveTask autoSaveTask = new AutoSaveTask(this);
        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskTimerAsynchronously(this, autoSaveTask, autoSaveInterval, autoSaveInterval);
        
        // Daily reset (check every minute)
        DailyResetTask dailyResetTask = new DailyResetTask(this);
        FoliaAdapter.runTaskTimer(this, dailyResetTask, 1200L, 1200L);
        
        // Request timeout
        long requestTimeout = configManager.getConfig().getLong("request.timeout", 60) * 20L;
        RequestTimeoutTask requestTimeoutTask = new RequestTimeoutTask(this);
        FoliaAdapter.runTaskTimer(this, requestTimeoutTask, requestTimeout, requestTimeout);
        
        // Combat tag cleanup
        if (configManager.getConfig().getBoolean("combat-tag.enabled", true)) {
            CombatTagCleanupTask combatTagCleanupTask = new CombatTagCleanupTask(combatTagManager);
            FoliaAdapter.runTaskTimer(this, combatTagCleanupTask, 100L, 100L);
        }
        
        // Modrinth update check (every 6 hours = 4320000 ticks)
        long updateInterval = configManager.getConfig().getLong("updates.check-interval", 21600) * 20L;
        FoliaAdapter.runTaskTimer(this, () -> updateManager.checkForUpdates(), 1200L, updateInterval);
    }
    
    public void reload() {
        configManager.reloadAll();
        if (rankManager != null) {
            rankManager.reloadRanks();
        }
        if (combatTagManager != null) {
            combatTagManager.reload();
        }
        if (entityDeathListener != null) {
            entityDeathListener.reload();
        }
    }
    
    // Getters
    public static DZEconomy getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public StorageProvider getStorageProvider() { return storageProvider; }
    public CurrencyManager getCurrencyManager() { return currencyManager; }
    public RankManager getRankManager() { return rankManager; }
    public RequestGUIManager getRequestGUIManager() { return requestGUIManager; }
    public CombatTagManager getCombatTagManager() { return combatTagManager; }
    public LuckPermsIntegration getLuckPermsIntegration() { return luckPermsIntegration; }
    public UpdateManager getUpdateManager() { return updateManager; }
    public MigrationManager getMigrationManager() { return migrationManager; }
    public DZEconomyAPI getAPI() { return api; }
    public PlaceholderAPIExpansion getPlaceholderExpansion() { return placeholderExpansion; }

    public long getStartupTime() { return startupTime; }

    public online.demonzdevelopment.dzeconomy.util.MessagesUtil getMessagesUtil() {
        return messagesUtil;
    }

    public boolean isUpdateAvailable() {
        return updateManager != null && updateManager.isUpdateAvailable();
    }

    public String getLatestVersion() {
        return updateManager != null ? updateManager.getLatestVersionNumber() : null;
    }

    /**
     * Create a storage provider for the given type.
     * Used by MigrationManager.
     */
    public StorageProvider createStorageProvider(StorageType type) {
        switch (type) {
            case SQLITE:
                return new SQLiteStorageProvider(this);
            case MYSQL:
                return new MySQLStorageProvider(this);
            case FLATFILE:
                return new FlatFileStorageProvider(this);
            default:
                throw new IllegalArgumentException("Unknown storage type: " + type);
        }
    }
}
