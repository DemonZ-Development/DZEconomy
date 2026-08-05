package online.demonzdevelopment.dzeconomy.rank;

import java.util.Collections;
import java.util.Map;

public class Rank {
    
    private final String name;
    private final String displayName;
    private final int priority;
    private final Map<String, RankCurrencySettings> currencySettings;
    private final Map<String, Double> multipliers;
    
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

    public RankCurrencySettings getCurrencySettings(String currencyKey) {
        return currencySettings.get(currencyKey);
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
    
    public static class RankCurrencySettings {
        
        private final String currencyKey;
        private final double transferTax;
        private final int cooldown;
        private final double dailyLimit;
        private final int requestCooldown;
        private final double bossKillBonus;
        
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
