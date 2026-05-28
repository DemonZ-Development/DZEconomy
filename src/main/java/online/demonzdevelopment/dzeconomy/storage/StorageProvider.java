package online.demonzdevelopment.dzeconomy.storage;

import online.demonzdevelopment.dzeconomy.data.PlayerData;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StorageProvider {
    boolean initialize();
    PlayerData loadPlayerData(UUID uuid);
    void savePlayerData(PlayerData data);
    boolean playerDataExists(UUID uuid);
    void deletePlayerData(UUID uuid);
    List<UUID> getAllPlayerUUIDs();
    void close();

    /**
     * Get all balances for a player across all currencies.
     * Used for migration between storage backends.
     */
    default Map<String, Double> getAllBalances(UUID uuid) {
        return Map.of();
    }

    /**
     * Set a specific currency balance for a player.
     * Used for migration between storage backends.
     */
    default void setBalance(UUID uuid, String currencyKey, double amount) {
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
