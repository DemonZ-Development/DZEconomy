package online.demonzdevelopment.dzeconomy.apiexample;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.api.DZEconomyAPI;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Example plugin demonstrating how to use the DZEconomy API v2.
 * 
 * Add DZEconomy as a dependency in your plugin.yml:
 *   depend: [DZEconomy]
 * 
 * Or as a soft dependency:
 *   softdepend: [DZEconomy]
 */
public class API_EXAMPLE extends JavaPlugin {

    private DZEconomyAPI economyAPI;

    @Override
    public void onEnable() {
        // Get the DZEconomy plugin instance
        DZEconomy dzeconomy = (DZEconomy) Bukkit.getPluginManager().getPlugin("DZEconomy");
        
        if (dzeconomy == null) {
            getLogger().severe("DZEconomy not found! Disabling...");
            setEnabled(false);
            return;
        }
        
        // Get the API instance
        economyAPI = dzeconomy.getAPI();
        
        getLogger().info("DZEconomy API v" + economyAPI.getAPIVersion() + " hooked successfully!");
        
        // Example usage
        UUID playerUUID = UUID.randomUUID();
        
        // Check balance
        double money = economyAPI.getBalance(playerUUID, CurrencyType.MONEY);
        getLogger().info("Player money: " + economyAPI.formatCurrency(money, CurrencyType.MONEY));
        
        // Add currency
        boolean success = economyAPI.addCurrency(playerUUID, CurrencyType.MONEY, 100.0);
        if (success) {
            getLogger().info("Added $100 to player!");
        }
        
        // Transfer currency (atomic, thread-safe)
        UUID fromPlayer = UUID.randomUUID();
        UUID toPlayer = UUID.randomUUID();
        boolean transferred = economyAPI.transferCurrency(fromPlayer, toPlayer, CurrencyType.MONEY, 50.0);
        if (transferred) {
            getLogger().info("Transferred $50 between players!");
        }
        
        // Convert currency
        double rate = economyAPI.getConversionRate(CurrencyType.MONEY, CurrencyType.GEM);
        getLogger().info("Money to Gem conversion rate: " + rate);
        
        // Format currency
        String formatted = economyAPI.formatCurrency(1234567.89, CurrencyType.MONEY);
        String shortForm = economyAPI.formatCurrencyShort(1234567.89);
        getLogger().info("Formatted: " + formatted + " | Short: " + shortForm);
        
        // Get player rank
        online.demonzdevelopment.dzeconomy.rank.Rank rank = economyAPI.getPlayerRank(playerUUID);
        if (rank != null) {
            getLogger().info("Player rank: " + rank.getName());
        } else {
            getLogger().info("No rank found for player");
        }
    }
}
