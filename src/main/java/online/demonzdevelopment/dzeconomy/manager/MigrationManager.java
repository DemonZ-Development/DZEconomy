package online.demonzdevelopment.dzeconomy.manager;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.storage.StorageProvider;
import online.demonzdevelopment.dzeconomy.storage.StorageType;
import online.demonzdevelopment.dzeconomy.storage.impl.*;
import online.demonzdevelopment.dzeconomy.util.ColorUtil;
import online.demonzdevelopment.dzeconomy.util.FoliaAdapter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Handles data migration between storage backends.
 * Supports: flatfile <-> sqlite <-> mysql
 * 
 * Fixes applied:
 * - getAllPlayerUUIDs queries DB for ALL players, not just online
 * - All sendMessage calls use Bukkit.getScheduler().runTask()
 * - Storage providers closed in finally blocks
 * - Migration type strings validated against whitelist
 * - Backup created before migration
 */
public class MigrationManager {
    
    private final DZEconomy plugin;
    
    private static final Set<String> VALID_TYPES = new HashSet<>(Arrays.asList(
        "flatfile", "sqlite", "mysql"
    ));
    
    public MigrationManager(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Validate that a migration type string is whitelisted.
     */
    private boolean validateMigrationType(String type) {
        if (type == null) return false;
        return VALID_TYPES.contains(type.toLowerCase());
    }
    
    /**
     * Create a backup of the current data before migration.
     */
    public boolean createBackup() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        Path backupDir = plugin.getDataFolder().toPath().resolve("backups");
        
        try {
            Files.createDirectories(backupDir);
            Path backupFile = backupDir.resolve("backup_" + timestamp + ".zip");
            
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(backupFile))) {
                Path dataFolder = plugin.getDataFolder().toPath();
                backupDirectory(dataFolder, dataFolder, zos);
            }
            
            plugin.getLogger().info("Backup created: " + backupFile.getFileName());
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
            return false;
        }
    }
    
    private void backupDirectory(Path root, Path current, ZipOutputStream zos) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(current)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    // Skip backups folder to avoid recursive backup
                    if (path.getFileName().toString().equals("backups")) continue;
                    backupDirectory(root, path, zos);
                } else {
                    String entryName = root.relativize(path).toString().replace('\\', '/');
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(path, zos);
                    zos.closeEntry();
                }
            }
        }
    }
    
    /**
     * Migrate data from one storage type to another.
     * 
     * @param fromType Source storage type (must be whitelisted)
     * @param toType   Target storage type (must be whitelisted)
     * @param sender   CommandSender to receive progress messages
     * @return true if migration succeeded
     */
    public boolean migrate(String fromType, String toType, CommandSender sender) {
        // Validate migration types against whitelist
        if (!validateMigrationType(fromType)) {
            sendMessageSafe(sender, "&cInvalid source storage type: " + fromType + ". Valid types: " + VALID_TYPES);
            return false;
        }
        if (!validateMigrationType(toType)) {
            sendMessageSafe(sender, "&cInvalid target storage type: " + toType + ". Valid types: " + VALID_TYPES);
            return false;
        }
        
        if (fromType.equalsIgnoreCase(toType)) {
            sendMessageSafe(sender, "&cSource and target storage types are the same!");
            return false;
        }
        
        sendMessageSafe(sender, "&aStarting migration from &e" + fromType + " &ato &e" + toType + "&a...");
        
        // Create backup first
        sendMessageSafe(sender, "&7Creating backup...");
        if (!createBackup()) {
            sendMessageSafe(sender, "&cBackup failed! Migration aborted for safety.");
            return false;
        }
        sendMessageSafe(sender, "&aBackup created successfully.");
        
        StorageProvider sourceProvider = null;
        StorageProvider targetProvider = null;
        
        try {
            // Create source provider
            sourceProvider = createStorageProvider(fromType);
            if (sourceProvider == null) {
                sendMessageSafe(sender, "&cFailed to create source storage provider: " + fromType);
                return false;
            }
            
            // Create target provider
            targetProvider = createStorageProvider(toType);
            if (targetProvider == null) {
                sendMessageSafe(sender, "&cFailed to create target storage provider: " + toType);
                return false;
            }
            
            // Initialize both providers
            sourceProvider.initialize();
            targetProvider.initialize();
            
            // Get ALL player UUIDs from source database (not just online players)
            sendMessageSafe(sender, "&7Querying all player data from source...");
            Set<UUID> allUUIDs = new HashSet<>(sourceProvider.getAllPlayerUUIDs());
            
            if (allUUIDs == null || allUUIDs.isEmpty()) {
                sendMessageSafe(sender, "&eNo player data found to migrate.");
                return true;
            }
            
            sendMessageSafe(sender, "&aFound &e" + allUUIDs.size() + " &aplayers to migrate.");
            
            int migrated = 0;
            int failed = 0;
            
            for (UUID uuid : allUUIDs) {
                try {
                    // Migrate full player data record (balances, limit states, cooldowns, statistics, usernames, join dates)
                    online.demonzdevelopment.dzeconomy.data.PlayerData data = sourceProvider.loadPlayerData(uuid);
                    if (data != null) {
                        data.setDirty(true); // force saving all related components in SQLite/MySQL Providers
                        targetProvider.savePlayerData(data);
                    }
                    migrated++;
                    
                    if (migrated % 50 == 0) {
                        sendMessageSafe(sender, "&7Migrated &e" + migrated + "&7/" + allUUIDs.size() + " players...");
                    }
                } catch (Exception e) {
                    failed++;
                    plugin.getLogger().warning("Failed to migrate player " + uuid + ": " + e.getMessage());
                }
            }
            
            sendMessageSafe(sender, "&aMigration complete! &e" + migrated + " &aplayers migrated, &e" + failed + " &cfailed.");
            
            if (failed == 0) {
                sendMessageSafe(sender, "&aYou can now update your storage type in config.yml to &e" + toType);
            } else {
                sendMessageSafe(sender, "&eSome players failed to migrate. Check console for details.");
            }
            
            return failed == 0;
            
        } catch (Exception e) {
            sendMessageSafe(sender, "&cMigration failed with error: " + e.getMessage());
            plugin.getLogger().log(Level.SEVERE, "Migration failed: " + e.getMessage(), e);
            return false;
        } finally {
            // Close storage providers in finally blocks
            if (sourceProvider != null) {
                try {
                    sourceProvider.shutdown();
                } catch (Exception e) {
                    plugin.getLogger().warning("Error closing source provider: " + e.getMessage());
                }
            }
            if (targetProvider != null) {
                try {
                    targetProvider.shutdown();
                } catch (Exception e) {
                    plugin.getLogger().warning("Error closing target provider: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Create a storage provider for the given type.
     */
    private StorageProvider createStorageProvider(String type) {
        try {
            String normalized = type.toUpperCase();
            if (normalized.equals("YAML")) normalized = "FLATFILE";
            StorageType storageType = StorageType.valueOf(normalized);
            return plugin.createStorageProvider(storageType);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("Unknown storage type: " + type);
            return null;
        }
    }
    
    /**
     * Send a message to a CommandSender on the main thread.
     * Uses Bukkit.getScheduler().runTask() for all sendMessage calls.
     */
    private void sendMessageSafe(CommandSender sender, String message) {
        if (sender == null) return;
        String translated = online.demonzdevelopment.dzeconomy.util.ColorUtil.translate(message);
        if (Bukkit.isPrimaryThread()) {
            sender.sendMessage(translated);
        } else {
            FoliaAdapter.runTask(plugin, () -> sender.sendMessage(translated));
        }
    }
}
