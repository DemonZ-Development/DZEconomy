package online.demonzdevelopment.dzeconomy.task;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.data.PlayerData;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AutoSaveTask extends BukkitRunnable {

    private final DZEconomy plugin;

    public AutoSaveTask(DZEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        CurrencyManager cm = plugin.getCurrencyManager();
        Set<UUID> cachedPlayers = new HashSet<>(cm.getCachedPlayerUUIDs());

        if (cachedPlayers.isEmpty()) {
            plugin.getLogger().info("[AutoSave] No cached player data to save.");
            return;
        }

        long startTime = System.currentTimeMillis();
        int savedCount = 0;
        int failedCount = 0;

        for (UUID uuid : cachedPlayers) {
            try {
                PlayerData data = cm.loadPlayerData(uuid);
                if (data != null) {
                    if (data.isDirty()) {
                        cm.savePlayerData(uuid);
                        savedCount++;
                    }
                    if (org.bukkit.Bukkit.getPlayer(uuid) == null) {
                        cm.unloadPlayerData(uuid);
                    }
                }
            } catch (Exception e) {
                failedCount++;
                // Use plugin logger instead of e.printStackTrace()
                plugin.getLogger().warning("[AutoSave] Failed to save data for player " + uuid + ": " + e.getMessage());
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;

        plugin.getLogger().info("[AutoSave] Save complete: " + savedCount + " saved, "
                + failedCount + " failed, "
                + (cachedPlayers.size() - savedCount - failedCount) + " unchanged, "
                + "took " + elapsed + "ms");
    }
}
