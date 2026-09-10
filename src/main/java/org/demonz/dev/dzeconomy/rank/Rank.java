package org.demonz.dev.dzeconomy.rank;

import java.util.Collections;
import java.util.Map;

public class Rank {
    
    private final String name;
    private final String displayName;
    private final int priority;
    private final Map<String, RankCurrencySettings> currencySettings;
    private final Map<String, Double> multipliers;
    private final Map<String, Object> perks;
    private final java.util.List<String> permissions;
    
    public Rank(String name, String displayName, int priority, Map<String, RankCurrencySettings> currencySettings, Map<String, Double> multipliers) {
        this(name, displayName, priority, currencySettings, multipliers, null, null);
    }

    public Rank(String name, String displayName, int priority, Map<String, RankCurrencySettings> currencySettings,
                Map<String, Double> multipliers, Map<String, Object> perks, java.util.List<String> permissions) {
        this.name = name;
        this.displayName = displayName;
        this.priority = priority;
        this.currencySettings = currencySettings != null 
            ? Collections.unmodifiableMap(currencySettings) 
            : Collections.emptyMap();
        this.multipliers = multipliers != null
            ? Collections.unmodifiableMap(multipliers)
            : Collections.emptyMap();
        this.perks = perks != null
            ? Collections.unmodifiableMap(perks)
            : Collections.emptyMap();
        this.permissions = permissions != null
            ? Collections.unmodifiableList(new java.util.ArrayList<>(permissions))
            : Collections.emptyList();
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

    public double getMultiplier(org.demonz.dev.dzeconomy.currency.CurrencyType type) {
        return getMultiplier(type.getId());
    }

    public RankCurrencySettings getCurrencySettings(String currencyKey) {
        return currencyKey != null ? currencySettings.get(currencyKey.toLowerCase()) : null;
    }

    /**
     * Whether this rank bypasses combat-tag restrictions on economy actions
     * ({@code perks.bypass-combat-tag}).
     */
    public boolean canBypassCombatTag() {
        return getPerkBoolean("bypass-combat-tag", false);
    }

    /**
     * Multiplier applied to transfer cooldowns ({@code perks.cooldown-reduction},
     * honoured only when {@code perks.reduced-cooldown} is true). {@code 1.0}
     * means no reduction.
     */
    public double getCooldownMultiplier() {
        if (!getPerkBoolean("reduced-cooldown", false)) {
            return 1.0;
        }
        double factor = getPerkDouble("cooldown-reduction", 1.0);
        return factor > 0 ? factor : 1.0;
    }

    /**
     * Multiplier applied to daily transfer limits ({@code perks.limit-multiplier},
     * honoured only when {@code perks.increased-daily-limit} is true).
     */
    public double getDailyLimitMultiplier() {
        if (!getPerkBoolean("increased-daily-limit", false)) {
            return 1.0;
        }
        double factor = getPerkDouble("limit-multiplier", 1.0);
        return factor > 0 ? factor : 1.0;
    }

    /**
     * Interest payout settings for this rank ({@code perks.interest}).
     */
    public InterestSettings getInterestSettings() {
        Object raw = perks.get("interest");
        if (!(raw instanceof Map)) {
            return new InterestSettings(false, 0.0, 86400, -1);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> section = (Map<String, Object>) raw;
        boolean enabled = toBoolean(section.get("enabled"), false);
        double rate = toDouble(section.get("rate"), 0.0);
        int interval = (int) Math.max(1, toDouble(section.get("interval"), 86400));
        double maxBalance = toDouble(section.get("max-balance"), -1);
        return new InterestSettings(enabled, Math.max(0.0, rate), interval, maxBalance);
    }

    /**
     * Extra permission nodes documented for this rank. Granting is handled by
     * the permissions plugin; DZEconomy does not attach these automatically.
     */
    public java.util.List<String> getPermissions() {
        return permissions;
    }

    public boolean getPerkBoolean(String key, boolean fallback) {
        return toBoolean(perks.get(key), fallback);
    }

    public double getPerkDouble(String key, double fallback) {
        return toDouble(perks.get(key), fallback);
    }

    private static boolean toBoolean(Object value, boolean fallback) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) {
            String s = ((String) value).trim();
            if (s.equalsIgnoreCase("true")) return true;
            if (s.equalsIgnoreCase("false")) return false;
        }
        return fallback;
    }

    private static double toDouble(Object value, double fallback) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public static final class InterestSettings {
        private final boolean enabled;
        private final double rate;
        private final int intervalSeconds;
        private final double maxBalance;

        public InterestSettings(boolean enabled, double rate, int intervalSeconds, double maxBalance) {
            this.enabled = enabled;
            this.rate = rate;
            this.intervalSeconds = intervalSeconds;
            this.maxBalance = maxBalance;
        }

        public boolean isEnabled() { return enabled; }
        public double getRate() { return rate; }
        public int getIntervalSeconds() { return intervalSeconds; }
        public double getMaxBalance() { return maxBalance; }
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
