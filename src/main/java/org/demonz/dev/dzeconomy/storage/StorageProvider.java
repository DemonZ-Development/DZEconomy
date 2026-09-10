package org.demonz.dev.dzeconomy.storage;

import org.demonz.dev.dzeconomy.data.PlayerData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StorageProvider {
    boolean initialize();
    PlayerData loadPlayerData(UUID uuid);

    boolean savePlayerData(PlayerData data);
    boolean playerDataExists(UUID uuid);
    void deletePlayerData(UUID uuid);
    List<UUID> getAllPlayerUUIDs();
    void close();

    default void checkpoint() {
    }

    default List<Map.Entry<UUID, Double>> getTopBalances(String currencyKey, int limit) {
        return Collections.emptyList();
    }

    /**
     * Sums one currency across all known accounts.
     *
     * @return the total, or {@code null} if the backend cannot answer
     */
    default Double getTotalBalance(String currencyKey) {
        return null;
    }

    default void shutdown() {
        close();
    }
}
