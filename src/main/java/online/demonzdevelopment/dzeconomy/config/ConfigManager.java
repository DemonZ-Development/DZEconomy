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
    
    public void loadAll() {
        
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        config = loadConfig("config.yml");
        ranks = loadConfig("ranks.yml");
        mobRewards = loadConfig("mob-rewards.yml");
        messages = loadConfig("messages.yml");
    }
    
    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        
        if (!file.exists()) {
            try {
                plugin.saveResource(fileName, false);
                plugin.getLogger().info("Created default " + fileName);
            } catch (IllegalArgumentException e) {
                
                try {
                    file.createNewFile();
                    plugin.getLogger().info("Created empty " + fileName);
                } catch (IOException ioException) {
                    plugin.getLogger().log(Level.SEVERE, "Could not create " + fileName, ioException);
                }
            }
        }
        
        FileConfiguration fileConfig;
        try (InputStreamReader reader = new InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
            fileConfig = YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            fileConfig = YamlConfiguration.loadConfiguration(file);
        }
        
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
    
    public void saveConfig() {
        if (config != null && configFile != null) {
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
            }
        }
    }
    
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
    
    public void reloadAll() {
        loadAll();
    }

    public void reload() {
        reloadAll();
    }
    
    public FileConfiguration getConfig() {
        if (config == null) {
            loadAll();
        }
        return config;
    }
    
    public FileConfiguration getRanks() {
        if (ranks == null) {
            loadAll();
        }
        return ranks;
    }
    
    public FileConfiguration getMobRewards() {
        if (mobRewards == null) {
            loadAll();
        }
        return mobRewards;
    }
    
    public FileConfiguration getMessages() {
        if (messages == null) {
            loadAll();
        }
        return messages;
    }
    
    public void setConfigValue(String path, Object value, boolean save) {
        if (config != null) {
            config.set(path, value);
            if (save) {
                saveConfig();
            }
        }
    }
}
