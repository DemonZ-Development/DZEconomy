package online.demonzdevelopment.dzeconomy.api;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.rank.Rank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Public API for DZEconomy v2.0.0.
 * All methods that modify balances are thread-safe and use per-player locks.
 */
public interface DZEconomyAPI {
    
    double getBalance(@NotNull UUID uuid, @NotNull CurrencyType type);
    boolean hasBalance(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);
    boolean addCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);
    boolean removeCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);
    boolean setCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);
    
    boolean transferCurrency(@NotNull UUID from, @NotNull UUID to, @NotNull CurrencyType type, double amount);
    boolean convertCurrency(@NotNull UUID uuid, @NotNull CurrencyType from, @NotNull CurrencyType to, double amount);
    double getConversionRate(@NotNull CurrencyType from, @NotNull CurrencyType to);
    
    @Nullable Rank getPlayerRank(@NotNull UUID uuid);
    @NotNull List<Rank> getAllRanks();
    
    @NotNull String formatCurrency(double amount, @NotNull CurrencyType type);
    @NotNull String formatCurrencyShort(double amount);
    
    int getAPIVersion();
}
