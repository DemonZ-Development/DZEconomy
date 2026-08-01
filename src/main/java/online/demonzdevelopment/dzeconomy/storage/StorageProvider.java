package online.demonzdevelopment.dzeconomy.storage;

import online.demonzdevelopment.dzeconomy.data.PlayerData;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StorageProvider {
    boolean initialize();
    PlayerData loadPlayerData(UUID uuid);

    /**
     * Persist player data to storage.
     *
     * @param data the player data to save
     * @return true if the data was written successfully, false otherwise
     */
    boolean savePlayerData(PlayerData data);
    boolean playerDataExists(UUID uuid);
    void deletePlayerData(UUID uuid);
    List<UUID> getAllPlayerUUIDs();
    void close();

    /**
     * Flush any pending writes so the underlying files can be safely
     * copied for backup (e.g. WAL checkpoint for SQLite).
     * No-op for storage backends that don't need it.
     */
    default void checkpoint() {
    }

    /**
     * Get the top balances for a given currency across all players.
     * Used for leaderboard display.
     */
    default List<Map.Entry<UUID, Double>> getTopBalances(String currencyKey, int limit) {
        return List.of();
    }

    /**
     * Shutdown the storage provider (alias for close with cleanup).
     * Used for migration between storage backends.
     */
    default void shutdown() {
        close();
    }
}
