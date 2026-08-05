package online.demonzdevelopment.dzeconomy.storage;

import online.demonzdevelopment.dzeconomy.data.PlayerData;
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

    default void shutdown() {
        close();
    }
}
