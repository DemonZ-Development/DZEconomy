package online.demonzdevelopment.dzeconomy.storage.impl;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import online.demonzdevelopment.dzeconomy.storage.StorageProvider;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;

public class FlatFileStorageProvider implements StorageProvider {
    
    private final DZEconomy plugin;
    private File dataDir;
    
    // In-memory cache for all player balances to prevent disk scans during getTopBalances
    private final Map<UUID, Map<String, Double>> allBalancesCache = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile boolean initialLoaded = false;
    
    public FlatFileStorageProvider(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean initialize() {
        dataDir = new File(plugin.getDataFolder(), "playerdata");
        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                plugin.getLogger().severe("Failed to create playerdata directory!");
                return false;
            }
        }
        plugin.getLogger().info("FlatFile storage initialized successfully!");
        return true;
    }
    
    private void ensureInitialLoad() {
        if (initialLoaded) return;
        synchronized (this) {
            if (initialLoaded) return;
            plugin.getLogger().info("Loading flatfile player balances into cache...");
            File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    String uuidStr = fileName.substring(0, fileName.length() - 4);
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                        Map<String, Double> balances = new java.util.concurrent.ConcurrentHashMap<>();
                        balances.put("money", yaml.getDouble("balances.money", 0.0));
                        balances.put("mobcoin", yaml.getDouble("balances.mobcoin", 0.0));
                        balances.put("gem", yaml.getDouble("balances.gem", 0.0));
                        allBalancesCache.put(uuid, balances);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            initialLoaded = true;
            plugin.getLogger().info("Loaded " + allBalancesCache.size() + " players into flatfile cache.");
        }
    }
    
    private File getPlayerFile(UUID uuid) {
        return new File(dataDir, uuid.toString() + ".yml");
    }
    
    private File getTempFile(UUID uuid) {
        return new File(dataDir, uuid.toString() + "_" + Thread.currentThread().getId() + ".yml.tmp");
    }
    
    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            return null;
        }
        
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        PlayerData data = new PlayerData(uuid);
        
        data.setUsername(yaml.getString("username", null));
        data.setFirstJoin(yaml.getLong("first-join", 0L));
        data.setLastSeen(yaml.getLong("last-seen", 0L));
        data.setBalance(CurrencyType.MONEY, yaml.getDouble("balances.money", 0.0));
        data.setBalance(CurrencyType.MOBCOIN, yaml.getDouble("balances.mobcoin", 0.0));
        data.setBalance(CurrencyType.GEM, yaml.getDouble("balances.gem", 0.0));
        data.setMoneySent(CurrencyType.MONEY, yaml.getDouble("stats.money-sent", 0.0));
        data.setMoneyReceived(CurrencyType.MONEY, yaml.getDouble("stats.money-received", 0.0));
        data.setMoneySent(CurrencyType.MOBCOIN, yaml.getDouble("stats.mobcoin-sent", 0.0));
        data.setMoneyReceived(CurrencyType.MOBCOIN, yaml.getDouble("stats.mobcoin-received", 0.0));
        data.setMoneySent(CurrencyType.GEM, yaml.getDouble("stats.gem-sent", 0.0));
        data.setMoneyReceived(CurrencyType.GEM, yaml.getDouble("stats.gem-received", 0.0));
        
        // Load daily limits
        for (CurrencyType type : CurrencyType.values()) {
            String path = "daily-limits." + type.getId();
            data.setDailySendCount(type, yaml.getLong(path + ".send-count", 0L));
            data.setDailyRequestCount(type, yaml.getLong(path + ".request-count", 0L));
        }
        
        // Load cooldowns
        for (CurrencyType type : CurrencyType.values()) {
            String path = "cooldowns." + type.getId();
            data.setSendCooldown(type, yaml.getLong(path + ".send-cooldown", 0L));
            data.setRequestCooldown(type, yaml.getLong(path + ".request-cooldown", 0L));
        }
        
        data.setDirty(false);
        
        // Cache balances in memory
        Map<String, Double> balances = allBalancesCache.computeIfAbsent(uuid, k -> new java.util.concurrent.ConcurrentHashMap<>());
        balances.put("money", data.getBalance(CurrencyType.MONEY));
        balances.put("mobcoin", data.getBalance(CurrencyType.MOBCOIN));
        balances.put("gem", data.getBalance(CurrencyType.GEM));
        
        return data;
    }
    
    @Override
    public void savePlayerData(PlayerData data) {
        UUID uuid = data.getUuid();
        File file = getPlayerFile(uuid);
        File tempFile = getTempFile(uuid);
        
        YamlConfiguration yaml = new YamlConfiguration();
        
        yaml.set("username", data.getUsername());
        yaml.set("first-join", data.getFirstJoin());
        yaml.set("last-seen", data.getLastSeen());
        yaml.set("balances.money", data.getBalance(CurrencyType.MONEY));
        yaml.set("balances.mobcoin", data.getBalance(CurrencyType.MOBCOIN));
        yaml.set("balances.gem", data.getBalance(CurrencyType.GEM));
        yaml.set("stats.money-sent", data.getMoneySent(CurrencyType.MONEY));
        yaml.set("stats.money-received", data.getMoneyReceived(CurrencyType.MONEY));
        yaml.set("stats.mobcoin-sent", data.getMoneySent(CurrencyType.MOBCOIN));
        yaml.set("stats.mobcoin-received", data.getMoneyReceived(CurrencyType.MOBCOIN));
        yaml.set("stats.gem-sent", data.getMoneySent(CurrencyType.GEM));
        yaml.set("stats.gem-received", data.getMoneyReceived(CurrencyType.GEM));
        
        // Save daily limits
        for (CurrencyType type : CurrencyType.values()) {
            String path = "daily-limits." + type.getId();
            yaml.set(path + ".send-count", data.getDailySendCount(type));
            yaml.set(path + ".request-count", data.getDailyRequestCount(type));
        }
        
        // Save cooldowns
        for (CurrencyType type : CurrencyType.values()) {
            String path = "cooldowns." + type.getId();
            yaml.set(path + ".send-cooldown", data.getSendCooldown(type));
            yaml.set(path + ".request-cooldown", data.getRequestCooldown(type));
        }
        
        // Atomic write: write to .tmp file first, then atomically move into place
        try {
            yaml.save(tempFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write temp file for " + uuid, e);
            deleteTempFile(tempFile);
            return;
        }
        
        try {
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            data.setDirty(false);
        } catch (IOException e) {
            // ATOMIC_MOVE may not be supported on all filesystems; try REPLACE_EXISTING alone
            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                data.setDirty(false);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to move temp file to final location for " + uuid, ex);
                try {
                    yaml.save(file);
                    data.setDirty(false);
                } catch (IOException ex2) {
                    plugin.getLogger().log(Level.SEVERE, "Fallback save also failed for " + uuid, ex2);
                }
                deleteTempFile(tempFile);
            }
        }
        
        // Cache balances in memory
        Map<String, Double> balances = allBalancesCache.computeIfAbsent(uuid, k -> new java.util.concurrent.ConcurrentHashMap<>());
        balances.put("money", data.getBalance(CurrencyType.MONEY));
        balances.put("mobcoin", data.getBalance(CurrencyType.MOBCOIN));
        balances.put("gem", data.getBalance(CurrencyType.GEM));
    }
    
    @Override
    public boolean playerDataExists(UUID uuid) {
        return allBalancesCache.containsKey(uuid) || getPlayerFile(uuid).exists();
    }
    
    @Override
    public void deletePlayerData(UUID uuid) {
        allBalancesCache.remove(uuid);
        File file = getPlayerFile(uuid);
        if (file.exists()) {
            if (!file.delete()) {
                plugin.getLogger().log(Level.SEVERE, "Failed to delete player data file for " + uuid);
            }
        }
        // Also clean up any leftover temp file
        File tempFile = getTempFile(uuid);
        if (tempFile.exists()) {
            if (!tempFile.delete()) {
                plugin.getLogger().warning("Failed to delete temp file: " + tempFile.getAbsolutePath());
            }
        }
    }
    
    @Override
    public List<UUID> getAllPlayerUUIDs() {
        ensureInitialLoad();
        return new ArrayList<>(allBalancesCache.keySet());
    }
    
    private void deleteTempFile(File tempFile) {
        if (tempFile.exists() && !tempFile.delete()) {
            plugin.getLogger().warning("Failed to delete temp file: " + tempFile.getAbsolutePath());
        }
    }

    @Override
    public Map<String, Double> getAllBalances(UUID uuid) {
        ensureInitialLoad();
        Map<String, Double> balances = allBalancesCache.get(uuid);
        if (balances != null) {
            return new java.util.concurrent.ConcurrentHashMap<>(balances);
        }
        
        File file = getPlayerFile(uuid);
        if (!file.exists()) return Map.of();
        
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        Map<String, Double> result = new java.util.concurrent.ConcurrentHashMap<>();
        result.put("money", yaml.getDouble("balances.money", 0.0));
        result.put("mobcoin", yaml.getDouble("balances.mobcoin", 0.0));
        result.put("gem", yaml.getDouble("balances.gem", 0.0));
        
        allBalancesCache.put(uuid, result);
        return result;
    }

    @Override
    public void setBalance(UUID uuid, String currencyKey, double amount) {
        if (currencyKey == null) return;
        
        File file = getPlayerFile(uuid);
        YamlConfiguration yaml;
        if (file.exists()) {
            yaml = YamlConfiguration.loadConfiguration(file);
        } else {
            yaml = new YamlConfiguration();
        }
        String path;
        String key = currencyKey.toLowerCase();
        if (key.equals("mobcoins")) key = "mobcoin";
        if (key.equals("gems")) key = "gem";
        
        switch (key) {
            case "money": path = "balances.money"; break;
            case "mobcoin": path = "balances.mobcoin"; break;
            case "gem": path = "balances.gem"; break;
            default: return;
        }
        yaml.set(path, amount);
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save balance for " + uuid, e);
        }
        
        // Update cache
        Map<String, Double> balances = allBalancesCache.computeIfAbsent(uuid, k -> new java.util.concurrent.ConcurrentHashMap<>());
        balances.put(key, amount);
    }

    @Override
    public List<Map.Entry<UUID, Double>> getTopBalances(String currencyKey, int limit) {
        if (currencyKey == null) return new ArrayList<>();
        String key = currencyKey.toLowerCase();
        if (key.equals("mobcoins")) key = "mobcoin";
        if (key.equals("gems")) key = "gem";
        
        ensureInitialLoad();
        
        List<Map.Entry<UUID, Double>> result = new ArrayList<>();
        for (Map.Entry<UUID, Map<String, Double>> entry : allBalancesCache.entrySet()) {
            Double bal = entry.getValue().get(key);
            if (bal != null) {
                result.add(new AbstractMap.SimpleEntry<>(entry.getKey(), bal));
            }
        }
        
        result.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return getSubList(result, limit);
    }
    
    private List<Map.Entry<UUID, Double>> getSubList(List<Map.Entry<UUID, Double>> list, int limit) {
        if (list.size() <= limit) {
            return new ArrayList<>(list);
        }
        return new ArrayList<>(list.subList(0, limit));
    }

    @Override
    public void close() {
        allBalancesCache.clear();
        initialLoaded = false;
    }
}
