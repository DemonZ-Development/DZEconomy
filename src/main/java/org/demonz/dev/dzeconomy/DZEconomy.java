package org.demonz.dev.dzeconomy;

import org.demonz.dev.dzeconomy.api.DZEconomyAPI;
import org.demonz.dev.dzeconomy.api.DZEconomyAPIImpl;
import org.demonz.dev.dzeconomy.command.*;
import org.demonz.dev.dzeconomy.config.ConfigManager;
import org.demonz.dev.dzeconomy.config.ConfigMigrator;
import org.demonz.dev.dzeconomy.currency.CurrencyManager;
import org.demonz.dev.dzeconomy.currency.CurrencyType;
import org.demonz.dev.dzeconomy.integration.LuckPermsIntegration;
import org.demonz.dev.dzeconomy.integration.PlaceholderAPIExpansion;
import org.demonz.dev.dzeconomy.integration.VaultEconomyBridge;
import org.demonz.dev.dzeconomy.listener.*;
import org.demonz.dev.dzeconomy.manager.CombatTagManager;
import org.demonz.dev.dzeconomy.manager.MigrationManager;
import org.demonz.dev.dzeconomy.manager.RankManager;
import org.demonz.dev.dzeconomy.rank.Rank;
import org.demonz.dev.dzeconomy.storage.StorageProvider;
import org.demonz.dev.dzeconomy.storage.StorageType;
import org.demonz.dev.dzeconomy.storage.impl.*;
import org.demonz.dev.dzeconomy.task.*;
import org.demonz.dev.dzeconomy.update.UpdateManager;
import org.demonz.dev.dzeconomy.util.FoliaAdapter;
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
    private CombatTagManager combatTagManager;
    private LuckPermsIntegration luckPermsIntegration;
    private UpdateManager updateManager;
    private MigrationManager migrationManager;
    private DZEconomyAPI api;
    private PlaceholderAPIExpansion placeholderExpansion;
    private EntityDeathListener entityDeathListener;
    private VaultEconomyBridge vaultBridge;

    private org.demonz.dev.dzeconomy.util.FoliaAdapter.FoliaTask autoSaveHandle;
    private org.demonz.dev.dzeconomy.util.FoliaAdapter.FoliaTask dailyResetHandle;
    private org.demonz.dev.dzeconomy.util.FoliaAdapter.FoliaTask requestTimeoutHandle;
    private org.demonz.dev.dzeconomy.util.FoliaAdapter.FoliaTask combatCleanupHandle;
    private org.demonz.dev.dzeconomy.util.FoliaAdapter.FoliaTask updateCheckHandle;
    private org.demonz.dev.dzeconomy.util.FoliaAdapter.FoliaTask interestHandle;

    private long startupTime;

    @Override
    public void onEnable() {
        instance = this;
        startupTime = System.currentTimeMillis();
        
        printStartupBanner();
        
        configManager = new ConfigManager(this);
        configManager.loadAll();
        
        ConfigMigrator migrator = new ConfigMigrator(this);
        migrator.migrate();
        
        if (!initializeStorage()) {
            getLogger().severe("Failed to initialize storage! Disabling plugin...");
            setEnabled(false);
            return;
        }
        
        this.currencyManager = new CurrencyManager(this);
        this.rankManager = new RankManager(this);
        this.rankManager.loadRanks();
        this.luckPermsIntegration = new LuckPermsIntegration(this);
        this.combatTagManager = new CombatTagManager(this);
        this.migrationManager = new MigrationManager(this);
        
        this.updateManager = new UpdateManager(this);
        this.api = new DZEconomyAPIImpl();
        Bukkit.getServicesManager().register(DZEconomyAPI.class, this.api, this, org.bukkit.plugin.ServicePriority.Normal);
        
        registerCommands();
        
        registerEvents();
        
        registerIntegrations();
        
        try {
            int pluginId = 31625;
            Metrics metrics = new Metrics(this, pluginId);
        } catch (Exception | LinkageError e) {
            getLogger().log(Level.WARNING, "bStats metrics failed to initialize, skipping", e);
        }
        
        scheduleTasks();
        
        updateManager.checkForUpdates();
        
        getLogger().info("DZEconomy v" + getDescription().getVersion() + " enabled on " + (FoliaAdapter.isFolia() ? "Folia" : Bukkit.getName()) + " " + Bukkit.getVersion());
    }

    @Override
    public void onDisable() {
        
        FoliaAdapter.cancelTasks(this);
        
        if (currencyManager != null) {
            try {
                currencyManager.saveAllPlayersSync();
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Error saving player data during shutdown", e);
            }
        }
        
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

        if (vaultBridge != null) {
            try {
                Bukkit.getServicesManager().unregister(net.milkbowl.vault.economy.Economy.class, vaultBridge);
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to unregister Vault economy provider", e);
            }
            vaultBridge = null;
        }

        if (luckPermsIntegration != null) {
            try {
                luckPermsIntegration.cleanup();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to cleanup LuckPerms integration", e);
            }
        }

        instance = null;
        getLogger().info("DZEconomy v" + getDescription().getVersion() + " has been disabled. Thank you for using DZEconomy!");
    }
    
    private void printStartupBanner() {
        getLogger().info("Starting DZEconomy v" + getDescription().getVersion() + " by DemonZ Development");
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
        entityDeathListener = new EntityDeathListener(this);
        pm.registerEvents(entityDeathListener, this);
        pm.registerEvents(new CombatTagListener(this), this);
    }
    
    private void registerIntegrations() {
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholderExpansion = new PlaceholderAPIExpansion(this);
            this.placeholderExpansion.register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            try {
                this.vaultBridge = new VaultEconomyBridge(this);
                Bukkit.getServicesManager().register(net.milkbowl.vault.economy.Economy.class,
                        vaultBridge, this, org.bukkit.plugin.ServicePriority.High);
                getLogger().info("Vault economy provider registered!");
            } catch (Exception | LinkageError e) {
                getLogger().log(Level.WARNING, "Failed to register Vault economy provider", e);
                this.vaultBridge = null;
            }
        }
        
        luckPermsIntegration.setup();
    }
    
    private void scheduleTasks() {
        
        long autoSaveInterval = configManager.getConfig().getLong("auto-save.interval", 300) * 20L;
        AutoSaveTask autoSaveTask = new AutoSaveTask(this);
        autoSaveHandle = FoliaAdapter.runTaskTimerAsynchronously(this, autoSaveTask, autoSaveInterval, autoSaveInterval);
        
        DailyResetTask dailyResetTask = new DailyResetTask(this);
        dailyResetHandle = FoliaAdapter.runTaskTimer(this, dailyResetTask, 1200L, 1200L);
        
        long requestTimeout = configManager.getConfig().getLong("request.timeout", 60) * 20L;
        RequestTimeoutTask requestTimeoutTask = new RequestTimeoutTask(this);
        requestTimeoutHandle = FoliaAdapter.runTaskTimer(this, requestTimeoutTask, requestTimeout, requestTimeout);
        
        combatCleanupHandle = null;
        if (configManager.getConfig().getBoolean("combat-tag.enabled", true)) {
            CombatTagCleanupTask combatTagCleanupTask = new CombatTagCleanupTask(combatTagManager);
            combatCleanupHandle = FoliaAdapter.runTaskTimer(this, combatTagCleanupTask, 100L, 100L);
        }
        
        interestHandle = null;
        if (hasInterestEnabledRank()) {
            InterestTask interestTask = new InterestTask(this);
            interestHandle = FoliaAdapter.runTaskTimerAsynchronously(this, interestTask, 1200L, 1200L);
        }

        updateCheckHandle = null;
        if (configManager.getConfig().getBoolean("updates.check-enabled", true)) {
            long updateInterval = configManager.getConfig().getLong("updates.check-interval", 21600) * 20L;
            updateCheckHandle = FoliaAdapter.runTaskTimer(this, () -> {
                if (updateManager != null) {
                    updateManager.checkForUpdates();
                }
            }, 1200L, updateInterval);
        }
    }
    
    public void reloadTasks() {
        if (autoSaveHandle != null) autoSaveHandle.cancel();
        if (dailyResetHandle != null) dailyResetHandle.cancel();
        if (requestTimeoutHandle != null) requestTimeoutHandle.cancel();
        if (combatCleanupHandle != null) combatCleanupHandle.cancel();
        if (updateCheckHandle != null) updateCheckHandle.cancel();
        if (interestHandle != null) interestHandle.cancel();
        scheduleTasks();
    }

    private boolean hasInterestEnabledRank() {
        if (rankManager == null) {
            return false;
        }
        try {
            for (Rank rank : rankManager.getAllRanks()) {
                if (rank != null && rank.getInterestSettings().isEnabled()
                        && rank.getInterestSettings().getRate() > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            getLogger().warning("Failed to check interest ranks: " + e.getMessage());
        }
        return false;
    }
    
    public static DZEconomy getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public StorageProvider getStorageProvider() { return storageProvider; }
    public CurrencyManager getCurrencyManager() { return currencyManager; }
    public RankManager getRankManager() { return rankManager; }
    public CombatTagManager getCombatTagManager() { return combatTagManager; }
    public LuckPermsIntegration getLuckPermsIntegration() { return luckPermsIntegration; }
    public MigrationManager getMigrationManager() { return migrationManager; }
    public EntityDeathListener getEntityDeathListener() { return entityDeathListener; }
    public DZEconomyAPI getAPI() { return api; }
    public PlaceholderAPIExpansion getPlaceholderExpansion() { return placeholderExpansion; }

    public long getStartupTime() { return startupTime; }

    public boolean isUpdateAvailable() {
        return updateManager != null && updateManager.isUpdateAvailable();
    }

    public String getLatestVersion() {
        return updateManager != null ? updateManager.getLatestVersionNumber() : null;
    }

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
