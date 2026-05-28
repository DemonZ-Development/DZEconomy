package online.demonzdevelopment.dzeconomy.rank;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable data class representing a rank with per-currency settings.
 * Once constructed, cannot be modified.
 */
public class Rank {
    
    private final String name;
    private final String displayName;
    private final int priority;
    private final Map<String, RankCurrencySettings> currencySettings;
    private final Map<String, Double> multipliers;
    
    /**
     * Construct a new Rank.
     *
     * @param name            Internal rank name
     * @param displayName     Display name shown to players
     * @param priority        Priority for rank resolution (higher = more important)
     * @param currencySettings Map of currency key to settings (will be stored as unmodifiable)
     * @param multipliers     Map of currency key to reward multiplier (will be stored as unmodifiable)
     */
    public Rank(String name, String displayName, int priority, Map<String, RankCurrencySettings> currencySettings, Map<String, Double> multipliers) {
        this.name = name;
        this.displayName = displayName;
        this.priority = priority;
        this.currencySettings = currencySettings != null 
            ? Collections.unmodifiableMap(currencySettings) 
            : Collections.emptyMap();
        this.multipliers = multipliers != null
            ? Collections.unmodifiableMap(multipliers)
            : Collections.emptyMap();
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getPriority() {
        return priority;
    }

    public double getMultiplier(String currencyKey) {
        return multipliers.getOrDefault(currencyKey.toLowerCase(), 1.0);
    }

    public double getMultiplier(online.demonzdevelopment.dzeconomy.currency.CurrencyType type) {
        return getMultiplier(type.getId());
    }

    public Map<String, Double> getAllMultipliers() {
        return multipliers;
    }
    
    /**
     * Get currency settings for a specific currency type.
     * Returns null if no settings defined for this currency.
     */
    public RankCurrencySettings getCurrencySettings(String currencyKey) {
        return currencySettings.get(currencyKey);
    }
    
    /**
     * Get all currency settings. Returned map is unmodifiable.
     */
    public Map<String, RankCurrencySettings> getAllCurrencySettings() {
        return currencySettings;
    }
    
    @Override
    public String toString() {
        return "Rank{name='" + name + "', displayName='" + displayName + "', priority=" + priority + "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rank rank = (Rank) o;
        return name.equalsIgnoreCase(rank.name);
    }
    
    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
    
    /**
     * Immutable per-currency settings for a rank.
     */
    public static class RankCurrencySettings {
        
        private final String currencyKey;
        private final double transferTax;
        private final int cooldown;
        private final double dailyLimit;
        private final int requestCooldown;
        private final double bossKillBonus;
        
        /**
         * Construct per-currency settings.
         *
         * @param currencyKey     The currency identifier
         * @param transferTax     Tax rate applied to transfers (0.0 - 1.0)
         * @param cooldown        Cooldown between transactions in seconds
         * @param dailyLimit      Maximum daily transaction amount (-1 = unlimited)
         * @param requestCooldown Cooldown between money requests in seconds
         * @param bossKillBonus   Bonus multiplier for boss kills
         */
        public RankCurrencySettings(String currencyKey, double transferTax, int cooldown, 
                                     double dailyLimit, int requestCooldown, double bossKillBonus) {
            this.currencyKey = currencyKey;
            this.transferTax = transferTax;
            this.cooldown = cooldown;
            this.dailyLimit = dailyLimit;
            this.requestCooldown = requestCooldown;
            this.bossKillBonus = bossKillBonus;
        }
        
        public String getCurrencyKey() {
            return currencyKey;
        }
        
        public double getTransferTax() {
            return transferTax;
        }
        
        public int getCooldown() {
            return cooldown;
        }
        
        public double getDailyLimit() {
            return dailyLimit;
        }
        
        public int getRequestCooldown() {
            return requestCooldown;
        }
        
        public double getBossKillBonus() {
            return bossKillBonus;
        }
        
        @Override
        public String toString() {
            return "RankCurrencySettings{currency='" + currencyKey + "', tax=" + transferTax 
                + ", cooldown=" + cooldown + ", dailyLimit=" + dailyLimit 
                + ", requestCooldown=" + requestCooldown + ", bossKillBonus=" + bossKillBonus + "}";
        }
    }
}
