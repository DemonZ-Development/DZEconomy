package online.demonzdevelopment.dzeconomy.integration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.util.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * PlaceholderAPI expansion for DZEconomy.
 * Provides placeholders for all currency types, ranks, and formatted values.
 * Uses Caffeine cache with 5-second expiry.
 */
public class PlaceholderAPIExpansion extends PlaceholderExpansion {
    
    private final DZEconomy plugin;
    
    // Caffeine Cache: Key -> "uuid:params"
    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .maximumSize(10_000)
            .build();
    
    public PlaceholderAPIExpansion(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "dz";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().isEmpty() 
            ? "DemonzDevelopment" 
            : plugin.getDescription().getAuthors().get(0);
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        
        String key = player.getUniqueId().toString() + ":" + params.toLowerCase();
        
        return cache.get(key, k -> computePlaceholder(player, params));
    }
    
    private String computePlaceholder(OfflinePlayer player, String params) {
        String lower = params.toLowerCase();
        UUID uuid = player.getUniqueId();
        
        switch (lower) {
            // Money (full and short format)
            case "money":
            case "balance": {
                double balance = plugin.getCurrencyManager().getBalance(uuid, "money");
                return NumberFormatter.formatFull(balance);
            }
            case "money_short":
            case "balance_short": {
                double balance = plugin.getCurrencyManager().getBalance(uuid, "money");
                return NumberFormatter.formatShort(balance);
            }
            
            // Mobcoin (full and short format)
            case "mobcoin":
            case "mobcoins": {
                double balance = plugin.getCurrencyManager().getBalance(uuid, "mobcoin");
                return NumberFormatter.formatFull(balance);
            }
            case "mobcoin_short":
            case "mobcoins_short": {
                double balance = plugin.getCurrencyManager().getBalance(uuid, "mobcoin");
                return NumberFormatter.formatShort(balance);
            }
            
            // Gem (full and short format)
            case "gem":
            case "gems": {
                double balance = plugin.getCurrencyManager().getBalance(uuid, "gem");
                return NumberFormatter.formatFull(balance);
            }
            case "gem_short":
            case "gems_short": {
                double balance = plugin.getCurrencyManager().getBalance(uuid, "gem");
                return NumberFormatter.formatShort(balance);
            }
            
            // Rank
            case "rank": {
                online.demonzdevelopment.dzeconomy.rank.Rank rank = plugin.getRankManager().getPlayerRank(uuid);
                return rank != null ? rank.getDisplayName() : "None";
            }
            case "rank_name": {
                online.demonzdevelopment.dzeconomy.rank.Rank rank = plugin.getRankManager().getPlayerRank(uuid);
                return rank != null ? rank.getName() : "none";
            }
            
            // Combat tag status
            case "combat":
            case "combat_tagged": {
                if (plugin.getCombatTagManager() != null) {
                    return plugin.getCombatTagManager().isInCombat(uuid) ? "Yes" : "No";
                }
                return "No";
            }
            case "combat_time":
            case "combat_remaining": {
                if (plugin.getCombatTagManager() != null) {
                    return String.valueOf(plugin.getCombatTagManager().getRemainingCombatTime(uuid));
                }
                return "0";
            }
            
            default:
                // Handle dynamic currency placeholders: %dz_currency_name% / %dz_currency_name_short%
                if (lower.endsWith("_short")) {
                    String currencyName = lower.substring(0, lower.length() - 6);
                    double balance = plugin.getCurrencyManager().getBalance(uuid, currencyName);
                    return NumberFormatter.formatShort(balance);
                }
                // Try as a currency name
                double balance = plugin.getCurrencyManager().getBalance(uuid, lower);
                return NumberFormatter.formatFull(balance);
        }
    }
    
    /**
     * Clear all cached data. Called on reload.
     */
    public void clearCache() {
        cache.invalidateAll();
    }
    
    /**
     * Remove a player's cached data. Called on disconnect.
     */
    public void removePlayerCache(java.util.UUID uuid) {
        if (uuid == null) return;
        String uuidStr = uuid.toString();
        cache.asMap().keySet().removeIf(k -> k.startsWith(uuidStr));
    }
}
