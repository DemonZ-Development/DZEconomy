package online.demonzdevelopment.dzeconomy.config;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Manages all YAML configuration files for DZEconomy.
 * Handles: config.yml, ranks.yml, mob-rewards.yml, messages.yml
 * 
 * Properly loads defaults from JAR for ALL files,
 * uses try-with-resources for InputStream closing,
 * and null-checks all file operations.
 */
public class ConfigManager {
    
    private final DZEconomy plugin;
    
    private FileConfiguration config;
    private FileConfiguration ranks;
    private FileConfiguration mobRewards;
    private FileConfiguration messages;
    
    private File configFile;
    private File ranksFile;
    private File mobRewardsFile;
    private File messagesFile;
    
    public ConfigManager(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load all configuration files.
     * Creates files from JAR defaults if they don't exist.
     */
    public void loadAll() {
        // Ensure data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        config = loadConfig("config.yml");
        ranks = loadConfig("ranks.yml");
        mobRewards = loadConfig("mob-rewards.yml");
        messages = loadConfig("messages.yml");
    }
    
    /**
     * Load a single configuration file with defaults from JAR.
     * Uses try-with-resources for proper InputStream closing.
     */
    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        
        // Create file from JAR default if it doesn't exist
        if (!file.exists()) {
            try {
                plugin.saveResource(fileName, false);
                plugin.getLogger().info("Created default " + fileName);
            } catch (IllegalArgumentException e) {
                // Resource doesn't exist in JAR, create empty file
                try {
                    file.createNewFile();
                    plugin.getLogger().info("Created empty " + fileName);
                } catch (IOException ioException) {
                    plugin.getLogger().log(Level.SEVERE, "Could not create " + fileName, ioException);
                }
            }
        }
        
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
        
        // Load defaults from JAR with proper InputStream closing
        try (InputStream defaultStream = plugin.getResource(fileName)) {
            if (defaultStream != null) {
                try (InputStreamReader reader = new InputStreamReader(defaultStream, StandardCharsets.UTF_8)) {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                    fileConfig.setDefaults(defaultConfig);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load defaults for " + fileName, e);
        }
        
        // Store file references for saving
        switch (fileName) {
            case "config.yml":
                configFile = file;
                break;
            case "ranks.yml":
                ranksFile = file;
                break;
            case "mob-rewards.yml":
                mobRewardsFile = file;
                break;
            case "messages.yml":
                messagesFile = file;
                break;
        }
        
        return fileConfig;
    }
    
    /**
     * Save config.yml back to disk.
     */
    public void saveConfig() {
        if (config != null && configFile != null) {
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
            }
        }
    }
    
    /**
     * Save a specific config file.
     */
    public void save(String fileName) {
        try {
            switch (fileName) {
                case "config.yml":
                    if (config != null && configFile != null) {
                        config.save(configFile);
                    }
                    break;
                case "ranks.yml":
                    if (ranks != null && ranksFile != null) {
                        ranks.save(ranksFile);
                    }
                    break;
                case "mob-rewards.yml":
                    if (mobRewards != null && mobRewardsFile != null) {
                        mobRewards.save(mobRewardsFile);
                    }
                    break;
                case "messages.yml":
                    if (messages != null && messagesFile != null) {
                        messages.save(messagesFile);
                    }
                    break;
                default:
                    plugin.getLogger().warning("Unknown config file: " + fileName);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + fileName, e);
        }
    }
    
    /**
     * Reload all configuration files from disk.
     */
    public void reloadAll() {
        loadAll();
    }

    /**
     * Alias for reloadAll().
     */
    public void reload() {
        reloadAll();
    }
    
    // ===== Getters with null safety =====
    
    /**
     * Get the main config.yml configuration.
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            loadAll();
        }
        return config;
    }
    
    /**
     * Get the ranks.yml configuration.
     */
    public FileConfiguration getRanks() {
        if (ranks == null) {
            loadAll();
        }
        return ranks;
    }
    
    /**
     * Get the mob-rewards.yml configuration.
     */
    public FileConfiguration getMobRewards() {
        if (mobRewards == null) {
            loadAll();
        }
        return mobRewards;
    }
    
    /**
     * Get the messages.yml configuration.
     */
    public FileConfiguration getMessages() {
        if (messages == null) {
            loadAll();
        }
        return messages;
    }
    
    // ===== Setters for runtime modifications =====
    
    /**
     * Set a value in config.yml and optionally save.
     */
    public void setConfigValue(String path, Object value, boolean save) {
        if (config != null) {
            config.set(path, value);
            if (save) {
                saveConfig();
            }
        }
    }
}
