package online.demonzdevelopment.dzeconomy.currency;

import org.jetbrains.annotations.Nullable;

public enum CurrencyType {
    MONEY("money", "Money", "$", 0.0),
    MOBCOIN("mobcoin", "MobCoin", "\u2605", 0.0),
    GEM("gem", "Gem", "\u2666", 0.0);
    
    private final String id;
    private final String displayName;
    private final String defaultSymbol;
    private final double defaultBalance;
    
    CurrencyType(String id, String displayName, String defaultSymbol, double defaultBalance) {
        this.id = id;
        this.displayName = displayName;
        this.defaultSymbol = defaultSymbol;
        this.defaultBalance = defaultBalance;
    }
    
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDefaultSymbol() { return defaultSymbol; }
    public double getDefaultBalance() { return defaultBalance; }
    
    @Nullable
    public static CurrencyType fromString(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase().replace(" ", "").replace("-", "").replace("_", "");
        // Support plurals and common aliases
        switch (lower) {
            case "money": case "cash": case "dollars": case "balance":
                return MONEY;
            case "mobcoin": case "mobcoins":
                return MOBCOIN;
            case "gem": case "gems":
                return GEM;
            default:
                for (CurrencyType type : values()) {
                    if (type.id.equalsIgnoreCase(lower)) return type;
                }
                return null;
        }
    }
}
