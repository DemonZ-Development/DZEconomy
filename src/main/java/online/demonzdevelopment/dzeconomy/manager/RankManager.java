package online.demonzdevelopment.dzeconomy.manager;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.rank.Rank;
import online.demonzdevelopment.dzeconomy.rank.Rank.RankCurrencySettings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player ranks loaded from ranks.yml.
 * Resolves player rank via LuckPerms integration or config default.
 * Thread-safe via ConcurrentHashMap.
 */
public class RankManager {
    
    private final DZEconomy plugin;
    private final ConcurrentHashMap<String, Rank> ranks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Rank> playerRanks = new ConcurrentHashMap<>();
    private String defaultRankName;
    
    public RankManager(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load all ranks from ranks.yml config.
     */
    public void loadRanks() {
        ranks.clear();
        playerRanks.clear();
        
        FileConfiguration ranksConfig = plugin.getConfigManager().getRanks();
        if (ranksConfig == null) {
            plugin.getLogger().warning("ranks.yml is null, cannot load ranks!");
            return;
        }
        
        defaultRankName = ranksConfig.getString("default-rank", "default");
        
        ConfigurationSection ranksSection = ranksConfig.getConfigurationSection("ranks");
        if (ranksSection == null) {
            ranksSection = ranksConfig;
        }
        
        for (String rankName : ranksSection.getKeys(false)) {
            if (rankName.equals("default-rank") || rankName.equals("config-version")) continue;
            ConfigurationSection rankSection = ranksSection.getKeys(false).contains(rankName) ? ranksSection.getConfigurationSection(rankName) : null;
            if (rankSection == null) continue;
            
            try {
                Rank rank = loadRankFromSection(rankName, rankSection);
                ranks.put(rankName.toLowerCase(), rank);
                plugin.getLogger().info("Loaded rank: " + rankName);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load rank '" + rankName + "': " + e.getMessage());
            }
        }
        
        // Validate default rank exists
        if (!ranks.containsKey(defaultRankName.toLowerCase())) {
            plugin.getLogger().warning("Default rank '" + defaultRankName + "' not found in ranks.yml! Available: " + ranks.keySet());
        }
    }
    
    private Rank loadRankFromSection(String name, ConfigurationSection section) {
        String displayName = section.getString("display-name", name);
        int priority = section.getInt("priority", 0);
        
        Map<String, RankCurrencySettings> currencySettings = new LinkedHashMap<>();
        
        ConfigurationSection currenciesSection = section.getConfigurationSection("currencies");
        if (currenciesSection != null) {
            for (String currencyKey : currenciesSection.getKeys(false)) {
                ConfigurationSection currencySection = currenciesSection.getConfigurationSection(currencyKey);
                if (currencySection == null) continue;
                
                RankCurrencySettings settings = new RankCurrencySettings(
                    currencyKey,
                    currencySection.getDouble("transfer-tax", 0.05),
                    currencySection.getInt("cooldown", 0),
                    currencySection.getDouble("daily-limit", -1),
                    currencySection.getInt("request-cooldown", 60),
                    currencySection.getDouble("boss-kill-bonus", 0.0)
                );
                currencySettings.put(currencyKey.toLowerCase(), settings);
            }
        }
        
        Map<String, Double> multipliers = new LinkedHashMap<>();
        ConfigurationSection multipliersSection = section.getConfigurationSection("multipliers");
        if (multipliersSection != null) {
            for (String currencyKey : multipliersSection.getKeys(false)) {
                multipliers.put(currencyKey.toLowerCase(), multipliersSection.getDouble(currencyKey, 1.0));
            }
        }
        
        return new Rank(name, displayName, priority, Collections.unmodifiableMap(currencySettings), Collections.unmodifiableMap(multipliers));
    }
    
    /**
     * Reload ranks from config.
     */
    public void reloadRanks() {
        loadRanks();
        // Re-resolve online player ranks
        for (UUID uuid : playerRanks.keySet()) {
            loadPlayerRank(uuid);
        }
    }
    
    /**
     * Load and cache a player's rank based on LuckPerms group or default.
     */
    public void loadPlayerRank(UUID uuid) {
        Rank resolved = resolveRankFromLuckPerms(uuid);
        if (resolved == null) {
            resolved = getDefaultRank();
        }
        if (resolved != null) {
            playerRanks.put(uuid, resolved);
        }
    }
    
    /**
     * Resolve player rank via LuckPerms or fall back to default.
     */
    private Rank resolveRankFromLuckPerms(UUID uuid) {
        if (plugin.getLuckPermsIntegration() != null && plugin.getLuckPermsIntegration().isEnabled()) {
            String groupName = plugin.getLuckPermsIntegration().getPlayerGroup(uuid);
            if (groupName != null) {
                Rank rank = ranks.get(groupName.toLowerCase());
                if (rank != null) {
                    return rank;
                }
            }
        }
        return null;
    }
    
    /**
     * Get a player's rank. Resolves from cache or loads on demand.
     */
    public Rank getPlayerRank(UUID uuid) {
        Rank cached = playerRanks.get(uuid);
        if (cached != null) {
            return cached;
        }
        // Try to load
        loadPlayerRank(uuid);
        Rank loaded = playerRanks.get(uuid);
        return loaded != null ? loaded : getDefaultRank();
    }
    
    /**
     * Get the default rank.
     */
    public Rank getDefaultRank() {
        Rank def = ranks.get(defaultRankName.toLowerCase());
        if (def == null && !ranks.isEmpty()) {
            // Fall back to first available rank
            return ranks.values().iterator().next();
        }
        return def;
    }
    
    /**
     * Get the transfer tax rate for a player and currency.
     */
    public double getTransferTaxRate(UUID uuid, String currencyType) {
        Rank rank = getPlayerRank(uuid);
        if (rank == null) return 0.05; // default 5%
        
        RankCurrencySettings settings = rank.getCurrencySettings(currencyType.toLowerCase());
        if (settings != null) {
            return settings.getTransferTax();
        }
        return 0.05;
    }

    /**
     * Get the transfer tax rate for a player and currency type (overload accepting CurrencyType enum).
     */
    public double getTransferTaxRate(UUID uuid, CurrencyType currencyType) {
        return getTransferTaxRate(uuid, currencyType.name().toLowerCase());
    }
    
    /**
     * Get all loaded ranks.
     */
    public List<Rank> getAllRanks() {
        return new ArrayList<>(ranks.values());
    }
    
    /**
     * Get a rank by name.
     */
    public Rank getRank(String name) {
        if (name == null) return null;
        return ranks.get(name.toLowerCase());
    }
    
    /**
     * Remove a player's cached rank (on disconnect).
     */
    public void removePlayerRank(UUID uuid) {
        playerRanks.remove(uuid);
    }
    
    
    /**
     * Get reward multiplier for a player and currency.
     */
    public double getMultiplier(UUID uuid, String currencyType) {
        Rank rank = getPlayerRank(uuid);
        if (rank == null) return 1.0;
        return rank.getMultiplier(currencyType);
    }

    /**
     * Get reward multiplier for a player and currency type.
     */
    public double getMultiplier(UUID uuid, CurrencyType currencyType) {
        return getMultiplier(uuid, currencyType.name().toLowerCase());
    }

    /**
     * Get the default rank name.
     */
    public String getDefaultRankName() {
        return defaultRankName;
    }
}
