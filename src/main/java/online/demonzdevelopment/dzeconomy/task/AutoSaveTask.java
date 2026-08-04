package online.demonzdevelopment.dzeconomy.task;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.data.PlayerData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AutoSaveTask implements Runnable {

    private final DZEconomy plugin;

    public AutoSaveTask(DZEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        CurrencyManager cm = plugin.getCurrencyManager();
        Set<UUID> cachedPlayers = new HashSet<>(cm.getCachedPlayerUUIDs());

        if (cachedPlayers.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        int savedCount = 0;
        int failedCount = 0;

        for (UUID uuid : cachedPlayers) {
            try {
                PlayerData data = cm.getPlayerData(uuid); // Use getPlayerData to avoid loading if not already loaded
                if (data != null) {
                    if (data.isDirty()) {
                        cm.savePlayerData(uuid);
                        savedCount++;
                    }
                    if (!cm.isPlayerOnline(uuid)) {
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

        if (savedCount > 0 || failedCount > 0) {
            plugin.getLogger().info("[AutoSave] " + savedCount + " saved, " + failedCount + " failed (" + elapsed + "ms)");
        }
    }
}
