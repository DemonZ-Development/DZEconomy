package online.demonzdevelopment.dzeconomy.api;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import online.demonzdevelopment.dzeconomy.manager.RankManager;
import online.demonzdevelopment.dzeconomy.rank.Rank;
import online.demonzdevelopment.dzeconomy.util.NumberFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DZEconomyAPIImpl implements DZEconomyAPI {
    
    public DZEconomyAPIImpl() {
    }
    
    private DZEconomy getPlugin() {
        DZEconomy plugin = DZEconomy.getInstance();
        if (plugin == null) {
            throw new IllegalStateException("DZEconomy is currently disabled!");
        }
        return plugin;
    }
    
    @Override
    public double getBalance(@NotNull UUID uuid, @NotNull CurrencyType type) {
        CurrencyManager cm = getPlugin().getCurrencyManager();
        PlayerData data = cm.loadPlayerData(uuid);
        return data != null ? data.getBalance(type) : 0.0;
    }
    
    @Override
    public boolean hasBalance(@NotNull UUID uuid, @NotNull CurrencyType type, double amount) {
        if (amount < 0) return false;
        return online.demonzdevelopment.dzeconomy.util.MoneyUtil.compare(getBalance(uuid, type), amount) >= 0;
    }
    
    @Override
    public boolean addCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount) {
        if (amount < 0) return false;
        CurrencyManager cm = getPlugin().getCurrencyManager();
        return cm.addBalance(uuid, type, amount);
    }
    
    @Override
    public boolean removeCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount) {
        if (amount < 0) return false;
        CurrencyManager cm = getPlugin().getCurrencyManager();
        return cm.removeBalance(uuid, type, amount);
    }
    
    @Override
    public boolean setCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount) {
        if (amount < 0) return false;
        CurrencyManager cm = getPlugin().getCurrencyManager();
        return cm.setBalance(uuid, type, amount);
    }
    
    @Override
    public boolean transferCurrency(@NotNull UUID from, @NotNull UUID to, @NotNull CurrencyType type, double amount) {
        if (amount <= 0 || from.equals(to)) return false;
        CurrencyManager cm = getPlugin().getCurrencyManager();
        return cm.transfer(from, to, type, amount);
    }
    
    @Override
    public boolean convertCurrency(@NotNull UUID uuid, @NotNull CurrencyType from, @NotNull CurrencyType to, double amount) {
        if (amount <= 0 || from == to) return false;
        CurrencyManager cm = getPlugin().getCurrencyManager();
        return cm.convert(uuid, from, to, amount);
    }
    
    @Override
    public double getConversionRate(@NotNull CurrencyType from, @NotNull CurrencyType to) {
        if (from == to) return 1.0;
        return getPlugin().getConfigManager().getConfig().getDouble(
            "conversion.rates." + from.name().toLowerCase() + "-to-" + to.name().toLowerCase(), 1.0
        );
    }
    
    @Override
    public @Nullable Rank getPlayerRank(@NotNull UUID uuid) {
        RankManager rm = getPlugin().getRankManager();
        return rm != null ? rm.getPlayerRank(uuid) : null;
    }
    
    @Override
    public @NotNull List<Rank> getAllRanks() {
        RankManager rm = getPlugin().getRankManager();
        return rm != null ? rm.getAllRanks() : List.of();
    }
    
    @Override
    public @NotNull String formatCurrency(double amount, @NotNull CurrencyType type) {
        return type.getDefaultSymbol() + NumberFormatter.formatFull(amount);
    }
    
    @Override
    public @NotNull String formatCurrencyShort(double amount) {
        return NumberFormatter.formatShort(amount);
    }
    
    @Override
    public int getAPIVersion() {
        return 2;
    }
}
