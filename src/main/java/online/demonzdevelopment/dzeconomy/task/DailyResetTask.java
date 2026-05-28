package online.demonzdevelopment.dzeconomy.task;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.data.PlayerData;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DailyResetTask implements Runnable {

    private final DZEconomy plugin;

    // FIX: Use date-based comparison instead of a boolean flag.
    // Store lastResetDate and compare against current date to determine
    // whether a reset is needed. This prevents missed resets if the server
    // was offline at the scheduled time, and avoids double-resets.
    // Load and persist lastResetDate from data.yml to survive daily server restarts.
    private LocalDate lastResetDate;

    public DailyResetTask(DZEconomy plugin) {
        this.plugin = plugin;
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (dataFile.exists()) {
            YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            String lastResetStr = dataConfig.getString("last-reset-date");
            if (lastResetStr != null) {
                try {
                    this.lastResetDate = LocalDate.parse(lastResetStr);
                } catch (Exception e) {
                    this.lastResetDate = LocalDate.now();
                }
            } else {
                this.lastResetDate = LocalDate.now().minusDays(1);
            }
        } else {
            this.lastResetDate = LocalDate.now().minusDays(1);
        }
    }

    @Override
    public void run() {
        LocalDate today = LocalDate.now();

        // Only reset if the date has changed since last reset
        if (!today.isAfter(lastResetDate)) {
            return;
        }

        plugin.getLogger().info("[DailyReset] Performing daily reset for " + today + "...");

        CurrencyManager cm = plugin.getCurrencyManager();
        Set<UUID> cachedPlayers = new HashSet<>(cm.getCachedPlayerUUIDs());

        int resetCount = 0;

        for (UUID uuid : cachedPlayers) {
            try {
                PlayerData data = cm.loadPlayerData(uuid);
                if (data != null) {
                    for (CurrencyType type : CurrencyType.values()) {
                        data.resetDailyTransactions(type);
                    }
                    resetCount++;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[DailyReset] Failed to reset daily data for player " + uuid + ": " + e.getMessage());
            }
        }

        // Update lastResetDate to today
        lastResetDate = today;

        // Persist to data.yml
        try {
            File dataFile = new File(plugin.getDataFolder(), "data.yml");
            YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            dataConfig.set("last-reset-date", today.toString());
            dataConfig.save(dataFile);
        } catch (Exception e) {
            plugin.getLogger().warning("[DailyReset] Failed to save last reset date: " + e.getMessage());
        }

        plugin.getLogger().info("[DailyReset] Daily reset complete: " + resetCount + " players reset for " + today);
    }

    /**
     * Get the last reset date. Useful for debugging or status commands.
     */
    public LocalDate getLastResetDate() {
        return lastResetDate;
    }
}
