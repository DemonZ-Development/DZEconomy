package org.demonz.dev.dzeconomy.manager;

import org.demonz.dev.dzeconomy.DZEconomy;
import org.demonz.dev.dzeconomy.currency.CurrencyType;
import org.demonz.dev.dzeconomy.rank.Rank;
import org.demonz.dev.dzeconomy.rank.Rank.RankCurrencySettings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RankManager {

    /** Fallback transfer tax applied when a rank defines no rate. */
    public static final double DEFAULT_TRANSFER_TAX = 0.05;
    
    private final DZEconomy plugin;
    private final ConcurrentHashMap<String, Rank> ranks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Rank> playerRanks = new ConcurrentHashMap<>();
    private String defaultRankName = "default";
    
    public RankManager(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
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
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load rank '" + rankName + "': " + e.getMessage());
            }
        }
        
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

        Map<String, Object> perks = new LinkedHashMap<>();
        ConfigurationSection perksSection = section.getConfigurationSection("perks");
        if (perksSection != null) {
            for (String perkKey : perksSection.getKeys(false)) {
                if (perksSection.isConfigurationSection(perkKey)) {
                    ConfigurationSection nested = perksSection.getConfigurationSection(perkKey);
                    Map<String, Object> nestedMap = new LinkedHashMap<>();
                    for (String nestedKey : nested.getKeys(false)) {
                        nestedMap.put(nestedKey, nested.get(nestedKey));
                    }
                    perks.put(perkKey, Collections.unmodifiableMap(nestedMap));
                } else {
                    perks.put(perkKey, perksSection.get(perkKey));
                }
            }
        }

        java.util.List<String> permissions = section.isList("permissions")
                ? section.getStringList("permissions")
                : Collections.emptyList();

        return new Rank(name, displayName, priority, Collections.unmodifiableMap(currencySettings),
                Collections.unmodifiableMap(multipliers), Collections.unmodifiableMap(perks), permissions);
    }
    
    public void reloadRanks() {
        // Snapshot before loadRanks() clears the cache; the previous code
        // iterated the map after clearing it, so this loop never ran.
        java.util.Set<UUID> knownPlayers = new java.util.HashSet<>(playerRanks.keySet());
        loadRanks();
        
        for (UUID uuid : knownPlayers) {
            loadPlayerRank(uuid);
        }
    }
    
    public void loadPlayerRank(UUID uuid) {
        Rank resolved = resolveRankFromLuckPerms(uuid);
        if (resolved == null) {
            resolved = getDefaultRank();
        }
        if (resolved != null) {
            playerRanks.put(uuid, resolved);
        }
    }
    
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
    
    public Rank getPlayerRank(UUID uuid) {
        Rank cached = playerRanks.get(uuid);
        if (cached != null) {
            return cached;
        }
        
        loadPlayerRank(uuid);
        Rank loaded = playerRanks.get(uuid);
        return loaded != null ? loaded : getDefaultRank();
    }
    
    public Rank getDefaultRank() {
        String fallback = defaultRankName != null ? defaultRankName : "default";
        Rank def = ranks.get(fallback.toLowerCase());
        if (def == null && !ranks.isEmpty()) {
            
            return ranks.values().iterator().next();
        }
        return def;
    }
    
    public double getTransferTaxRate(UUID uuid, String currencyType) {
        Rank rank = getPlayerRank(uuid);
        if (rank == null) return DEFAULT_TRANSFER_TAX; 
        
        RankCurrencySettings settings = rank.getCurrencySettings(currencyType.toLowerCase());
        if (settings != null) {
            return settings.getTransferTax();
        }
        return DEFAULT_TRANSFER_TAX;
    }

    /**
     * Cooldown multiplier for the player's rank ({@code 1.0} = no reduction).
     */
    public double getCooldownMultiplier(UUID uuid) {
        Rank rank = getPlayerRank(uuid);
        return rank != null ? rank.getCooldownMultiplier() : 1.0;
    }

    /**
     * Daily-limit multiplier for the player's rank ({@code 1.0} = no bonus).
     */
    public double getDailyLimitMultiplier(UUID uuid) {
        Rank rank = getPlayerRank(uuid);
        return rank != null ? rank.getDailyLimitMultiplier() : 1.0;
    }

    /**
     * Whether the player's rank bypasses combat-tag economy restrictions.
     */
    public boolean canBypassCombatTag(UUID uuid) {
        Rank rank = getPlayerRank(uuid);
        return rank != null && rank.canBypassCombatTag();
    }

    public double getTransferTaxRate(UUID uuid, CurrencyType currencyType) {
        return getTransferTaxRate(uuid, currencyType.name().toLowerCase());
    }
    
    public List<Rank> getAllRanks() {
        return new ArrayList<>(ranks.values());
    }
    
    public Rank getRank(String name) {
        if (name == null) return null;
        return ranks.get(name.toLowerCase());
    }
    
    public void removePlayerRank(UUID uuid) {
        playerRanks.remove(uuid);
    }
    
    public double getMultiplier(UUID uuid, String currencyType) {
        Rank rank = getPlayerRank(uuid);
        if (rank == null) return 1.0;
        return rank.getMultiplier(currencyType);
    }

    public double getMultiplier(UUID uuid, CurrencyType currencyType) {
        return getMultiplier(uuid, currencyType.name().toLowerCase());
    }

    public String getDefaultRankName() {
        return defaultRankName;
    }
}
