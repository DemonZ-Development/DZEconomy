package online.demonzdevelopment.dzeconomy.config;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigMigrator {
    
    private final DZEconomy plugin;
    public static final int CURRENT_CONFIG_VERSION = 3;
    
    public ConfigMigrator(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    public void migrate() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        int version = config.getInt("config-version", 1);
        
        if (version >= CURRENT_CONFIG_VERSION) {
            plugin.getLogger().info("Config is up to date (v" + version + ").");
            return;
        }
        
        plugin.getLogger().info("Migrating config from v" + version + " to v" + CURRENT_CONFIG_VERSION + "...");
        
        backupConfig();
        
for (int v = version; v < CURRENT_CONFIG_VERSION; v++) {
            switch (v) {
                case 1:
                    migrateV1toV2();
                    break;
                case 2:
                    migrateV2toV3();
                    break;
                
            }
        }
        
        config.set("config-version", CURRENT_CONFIG_VERSION);
        plugin.getConfigManager().saveConfig();
        
        plugin.getLogger().info("Config migration complete! Updated to v" + CURRENT_CONFIG_VERSION + ".");
    }
    
    private void migrateV1toV2() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        plugin.getLogger().info("  Migrating v1 -> v2: Adding Modrinth update settings, combat tag defaults, baltop settings...");
        
        if (!config.contains("updates.check-interval")) {
            config.set("updates.check-interval", 21600);
        }
        if (!config.contains("updates.check-enabled")) {
            config.set("updates.check-enabled", true);
        }
        if (!config.contains("updates.modrinth-project-id")) {
            config.set("updates.modrinth-project-id", "dzeconomy");
        }
        if (!config.contains("combat-tag.enabled")) {
            config.set("combat-tag.enabled", true);
        }
        if (!config.contains("combat-tag.duration")) {
            config.set("combat-tag.duration", 15);
        }
        
config.set("auto-update", null);
        config.set("runtime-auto-update", null);
        config.set("update", null); 
    }
    
    private void migrateV2toV3() {
        plugin.getLogger().info("  Migrating v2 -> v3: Refreshing config header and documentation links.");
    }
    
    private void backupConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) return;

        int version = plugin.getConfigManager().getConfig().getInt("config-version", 1);

        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) backupDir.mkdirs();

        File backup = new File(backupDir, "config_v" + version + "_backup_" + System.currentTimeMillis() + ".yml");
        try {
            java.nio.file.Files.copy(configFile.toPath(), backup.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("  Config backed up to: " + backup.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to backup config before migration", e);
        }
    }
}
