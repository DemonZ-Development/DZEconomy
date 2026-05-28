package online.demonzdevelopment.dzeconomy.currency;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.data.CurrencyRequest;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import online.demonzdevelopment.dzeconomy.data.TransactionLogEntry;
import online.demonzdevelopment.dzeconomy.storage.StorageProvider;
import online.demonzdevelopment.dzeconomy.util.MessagesUtil;
import online.demonzdevelopment.dzeconomy.util.MoneyUtil;

import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class CurrencyManager {
    
    private final DZEconomy plugin;
    private final ConcurrentHashMap<UUID, PlayerData> playerDataCache;
    private final ConcurrentHashMap<UUID, List<CurrencyRequest>> pendingRequests;
    private final ConcurrentHashMap<UUID, ReentrantLock> playerLocks;
    private final Set<UUID> onlinePlayers = ConcurrentHashMap.newKeySet();
    
    public CurrencyManager(DZEconomy plugin) {
        this.plugin = plugin;
        this.playerDataCache = new ConcurrentHashMap<>();
        this.pendingRequests = new ConcurrentHashMap<>();
        this.playerLocks = new ConcurrentHashMap<>();
    }

    public void setPlayerOnline(UUID uuid, boolean online) {
        if (online) {
            onlinePlayers.add(uuid);
        } else {
            onlinePlayers.remove(uuid);
        }
    }

    public boolean isPlayerOnline(UUID uuid) {
        return onlinePlayers.contains(uuid);
    }
    
    // ━━ Per-Player Lock System ━━
    private ReentrantLock getLock(UUID uuid) {
        return playerLocks.computeIfAbsent(uuid, k -> new ReentrantLock());
    }
    
    private void withLock(UUID uuid, Runnable action) {
        ReentrantLock lock = getLock(uuid);
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }
    
    // cleanupLock is removed to fix race conditions; locks are cleaned in unloadPlayerData

    public void executeWithPlayerLock(UUID uuid, Runnable action) {
        withLock(uuid, action);
    }
    
    // ━━ Player Data Management ━━
    public PlayerData loadPlayerData(UUID uuid) {
        ReentrantLock lock = getLock(uuid);
        lock.lock();
        try {
            PlayerData cached = playerDataCache.get(uuid);
            if (cached != null) {
                return cached;
            }
            
            StorageProvider storage = plugin.getStorageProvider();
            PlayerData data = storage.loadPlayerData(uuid);
            if (data == null) {
                data = new PlayerData(uuid);
            } else {
                long lastSeen = data.getLastSeen();
                if (lastSeen > 0) {
                    java.time.LocalDate lastSeenDate = java.time.Instant.ofEpochMilli(lastSeen)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                    java.time.LocalDate today = java.time.LocalDate.now();
                    if (today.isAfter(lastSeenDate)) {
                        for (CurrencyType type : CurrencyType.values()) {
                            data.resetDailyTransactions(type);
                        }
                    }
                }
            }
            
            playerDataCache.put(uuid, data);
            return data;
        } finally {
            lock.unlock();
        }
    }
    
    public void unloadPlayerData(UUID uuid) {
        withLock(uuid, () -> {
            PlayerData data = playerDataCache.get(uuid);
            if (data != null) {
                savePlayerData(uuid);
                playerDataCache.remove(uuid);
            }
            if (plugin.getLuckPermsIntegration() != null) {
                plugin.getLuckPermsIntegration().removePlayer(uuid);
            }
            if (plugin.getRankManager() != null) {
                plugin.getRankManager().removePlayerRank(uuid);
            }
        });
    }
    
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }
    
    public boolean playerDataExists(UUID uuid) {
        return playerDataCache.containsKey(uuid) || plugin.getStorageProvider().playerDataExists(uuid);
    }
    
    // ━━ Balance Operations (All use per-player locks + MoneyUtil precision) ━━
    public boolean addBalance(UUID uuid, CurrencyType type, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) return false;
        withLock(uuid, () -> {
            PlayerData data = loadPlayerData(uuid);
            data.setBalance(type, MoneyUtil.add(data.getBalance(type), amount));
        });
        return true;
    }
    
    public boolean removeBalance(UUID uuid, CurrencyType type, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) return false;
        final boolean[] result = {false};
        withLock(uuid, () -> {
            PlayerData data = loadPlayerData(uuid);
            double balance = data.getBalance(type);
            if (MoneyUtil.compare(balance, amount) >= 0) {
                data.setBalance(type, MoneyUtil.subtract(balance, amount));
                result[0] = true;
            }
        });
        return result[0];
    }
    
    public boolean setBalance(UUID uuid, CurrencyType type, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) return false;
        withLock(uuid, () -> {
            PlayerData data = loadPlayerData(uuid);
            data.setBalance(type, MoneyUtil.round(amount));
        });
        return true;
    }
    
    public double getBalance(UUID uuid, CurrencyType type) {
        PlayerData data = loadPlayerData(uuid);
        return data.getBalance(type);
    }

    /**
     * Get balance by currency name string.
     */
    public double getBalance(UUID uuid, String currencyName) {
        CurrencyType type = CurrencyType.fromString(currencyName);
        if (type == null) return 0.0;
        return getBalance(uuid, type);
    }
    
    public boolean hasBalance(UUID uuid, CurrencyType type, double amount) {
        return getBalance(uuid, type) >= amount;
    }
    
    // ━━ Transfer (Atomic with dual locks) ━━
    public boolean transfer(UUID from, UUID to, CurrencyType type, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0 || from.equals(to)) return false;
        
        // Lock ordering to prevent deadlock: always lock lower UUID first
        UUID first = from.compareTo(to) < 0 ? from : to;
        UUID second = from.compareTo(to) < 0 ? to : from;
        
        ReentrantLock lock1 = getLock(first);
        ReentrantLock lock2 = getLock(second);
        
        lock1.lock();
        try {
            lock2.lock();
            try {
                PlayerData fromData = loadPlayerData(from);
                PlayerData toData = loadPlayerData(to);
                
                if (MoneyUtil.compare(fromData.getBalance(type), amount) < 0) return false;
                
                // Apply transfer tax from rank settings and clamp it between 0 and 1
                double taxRate = plugin.getRankManager().getTransferTaxRate(from, type);
                taxRate = Math.max(0.0, Math.min(1.0, taxRate));
                double tax = MoneyUtil.multiply(amount, taxRate);
                double received = MoneyUtil.subtract(amount, tax);
                
                fromData.setBalance(type, MoneyUtil.subtract(fromData.getBalance(type), amount));
                toData.setBalance(type, MoneyUtil.add(toData.getBalance(type), received));
                fromData.addMoneySent(type, amount);
                toData.addMoneyReceived(type, received);
                
                return true;
            } finally {
                lock2.unlock();
            }
        } finally {
            lock1.unlock();
        }
    }
    
    // ━━ Transfer with daily limit (Atomic with dual locks) ━━
    public boolean transfer(UUID from, UUID to, CurrencyType type, double amount, double dailyLimit) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0 || from.equals(to)) return false;
        
        UUID first = from.compareTo(to) < 0 ? from : to;
        UUID second = from.compareTo(to) < 0 ? to : from;
        
        ReentrantLock lock1 = getLock(first);
        ReentrantLock lock2 = getLock(second);
        
        lock1.lock();
        try {
            lock2.lock();
            try {
                PlayerData fromData = loadPlayerData(from);
                PlayerData toData = loadPlayerData(to);
                
                if (MoneyUtil.compare(fromData.getBalance(type), amount) < 0) return false;
                
                if (dailyLimit > 0) {
                    double dailySent = fromData.getDailySent(type);
                    if (dailySent + amount > dailyLimit) return false;
                }
                
                double taxRate = plugin.getRankManager().getTransferTaxRate(from, type);
                taxRate = Math.max(0.0, Math.min(1.0, taxRate));
                double tax = MoneyUtil.multiply(amount, taxRate);
                double received = MoneyUtil.subtract(amount, tax);
                
                fromData.setBalance(type, MoneyUtil.subtract(fromData.getBalance(type), amount));
                toData.setBalance(type, MoneyUtil.add(toData.getBalance(type), received));
                fromData.addMoneySent(type, amount);
                toData.addMoneyReceived(type, received);
                
                if (dailyLimit > 0) {
                    fromData.addDailySent(type, amount);
                }
                
                return true;
            } finally {
                lock2.unlock();
            }
        } finally {
            lock1.unlock();
        }
    }
    
    // ━━ Convert (Atomic) ━━
    public boolean convert(UUID uuid, CurrencyType from, CurrencyType to, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0 || from == to) return false;
        
        final boolean[] result = {false};
        withLock(uuid, () -> {
            PlayerData data = loadPlayerData(uuid);
            if (MoneyUtil.compare(data.getBalance(from), amount) < 0) return;
            
            double rate = plugin.getConfigManager().getConfig().getDouble(
                "conversion.rates." + from.getId() + "-to-" + to.getId(), 1.0
            );
            if (rate <= 0) return;
            
            double feePercent = plugin.getConfigManager().getConfig().getDouble("conversion.fee-percent", 0.0);
            double converted = MoneyUtil.multiply(amount, rate);
            double fee = MoneyUtil.multiply(converted, feePercent / 100.0);
            double received = MoneyUtil.subtract(converted, fee);
            if (received <= 0) return;
            
            data.setBalance(from, MoneyUtil.subtract(data.getBalance(from), amount));
            data.setBalance(to, MoneyUtil.add(data.getBalance(to), received));
            result[0] = true;
        });
        return result[0];
    }
    
    // ━━ Request Management (Thread-safe with CopyOnWriteArrayList) ━━
    public void addRequest(CurrencyRequest request) {
        pendingRequests.computeIfAbsent(request.getRequestedPlayerUUID(), k -> new CopyOnWriteArrayList<>())
            .add(request);
    }
    
    public boolean removeRequest(UUID requestedPlayer, CurrencyRequest request) {
        final boolean[] removed = {false};
        pendingRequests.computeIfPresent(requestedPlayer, (uuid, list) -> {
            removed[0] = list.remove(request);
            return list.isEmpty() ? null : list;
        });
        return removed[0];
    }
    
    public List<CurrencyRequest> getRequestsForPlayer(UUID uuid) {
        return new ArrayList<>(pendingRequests.getOrDefault(uuid, Collections.emptyList()));
    }
    
    public CurrencyRequest findRequest(UUID requester, UUID requested, CurrencyType type) {
        List<CurrencyRequest> requests = pendingRequests.get(requested);
        if (requests != null) {
            for (CurrencyRequest req : requests) {
                if (req.getRequesterUUID().equals(requester) && req.getCurrencyType() == type) {
                    return req;
                }
            }
        }
        return null;
    }
    
    public boolean hasPendingRequestWith(UUID player1, UUID player2) {
        // Check if player1 has a pending request to player2
        List<CurrencyRequest> requests1 = pendingRequests.get(player2);
        if (requests1 != null) {
            for (CurrencyRequest req : requests1) {
                if (req.getRequesterUUID().equals(player1)) return true;
            }
        }
        // Check if player2 has a pending request to player1
        List<CurrencyRequest> requests2 = pendingRequests.get(player1);
        if (requests2 != null) {
            for (CurrencyRequest req : requests2) {
                if (req.getRequesterUUID().equals(player2)) return true;
            }
        }
        return false;
    }
    
    // ━━ Save Operations ━━
    public void savePlayerData(UUID uuid) {
        withLock(uuid, () -> {
            PlayerData data = playerDataCache.get(uuid);
            if (data != null && data.isDirty()) {
                try {
                    plugin.getStorageProvider().savePlayerData(data);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to save player data for " + uuid + ": " + e.getMessage());
                }
            }
        });
    }
    
    public void savePlayerDataAsync(UUID uuid) {
        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> savePlayerData(uuid));
    }
    
    public void saveAllPlayersSync() {
        for (UUID uuid : new HashSet<>(playerDataCache.keySet())) {
            savePlayerData(uuid);
        }
    }
    
    public void saveAllPlayersAsync() {
        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, this::saveAllPlayersSync);
    }
    
    // ━━ Leaderboard ━━
    public java.util.concurrent.CompletableFuture<List<Map.Entry<UUID, Double>>> getBalanceTopAsync(CurrencyType type, int limit) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            return plugin.getStorageProvider().getTopBalances(type.getId(), limit);
        });
    }
    
    // ━━ Utility ━━
    public Collection<PlayerData> getAllPlayerData() {
        return Collections.unmodifiableCollection(playerDataCache.values());
    }
    
    public Collection<UUID> getAllLoadedPlayers() {
        return Collections.unmodifiableSet(playerDataCache.keySet());
    }
    
    public int getLoadedPlayerCount() {
        return playerDataCache.size();
    }

    // ━━ Stub methods used by EconomyCommand ━━

    public int getPendingRequestCount() {
        int count = 0;
        for (List<CurrencyRequest> requests : pendingRequests.values()) {
            count += requests.size();
        }
        return count;
    }

    public int getActiveCombatTagCount() {
        if (plugin.getCombatTagManager() != null) {
            return plugin.getCombatTagManager().getTaggedCount();
        }
        return 0;
    }

    public double getTotalCurrency(CurrencyType type) {
        double total = 0;
        for (PlayerData data : playerDataCache.values()) {
            total += data.getBalance(type);
        }
        return total;
    }

    public boolean migrate(String fromType, String toType) {
        // Migration is handled by MigrationManager
        return false;
    }

    // ━━ Additional accessor methods ━━

    public int getCachedPlayerCount() { return playerDataCache.size(); }
    public java.util.Collection<UUID> getCachedPlayerUUIDs() { return playerDataCache.keySet(); }

    public List<CurrencyRequest> getRequestsFrom(UUID requester) {
        List<CurrencyRequest> result = new ArrayList<>();
        for (Map.Entry<UUID, List<CurrencyRequest>> entry : pendingRequests.entrySet()) {
            for (CurrencyRequest req : entry.getValue()) {
                if (req.getRequesterUUID().equals(requester)) {
                    result.add(req);
                }
            }
        }
        return result;
    }

    public List<CurrencyRequest> getRequestsTo(UUID requested) {
        return new ArrayList<>(pendingRequests.getOrDefault(requested, Collections.emptyList()));
    }

    public java.util.Set<UUID> getRequestHolders() {
        return new java.util.HashSet<>(pendingRequests.keySet());
    }

    public CurrencyManager getRequestManager() { return this; }

    public online.demonzdevelopment.dzeconomy.util.MessagesUtil getMessagesUtil() {
        return new online.demonzdevelopment.dzeconomy.util.MessagesUtil(plugin);
    }

    public void reloadConfig() {
        // No-op: config is managed by ConfigManager
    }

    // ━━ Combat tag delegate methods (delegated to CombatTagManager) ━━

    public boolean isCombatTagged(UUID uuid) {
        if (plugin.getCombatTagManager() != null) {
            return plugin.getCombatTagManager().isCombatTagged(uuid);
        }
        return false;
    }

    public void addCombatTag(UUID uuid, long expiryTime) {
        if (plugin.getCombatTagManager() != null) {
            plugin.getCombatTagManager().addCombatTag(uuid, expiryTime);
        }
    }

    public void removeCombatTag(UUID uuid) {
        if (plugin.getCombatTagManager() != null) {
            plugin.getCombatTagManager().removeCombatTag(uuid);
        }
    }

    public void cleanupExpiredCombatTags() {
        if (plugin.getCombatTagManager() != null) {
            plugin.getCombatTagManager().cleanupExpiredCombatTags();
        }
    }

    public int getPendingRequestCount(UUID requester, CurrencyType type) {
        int count = 0;
        for (List<CurrencyRequest> requests : pendingRequests.values()) {
            for (CurrencyRequest req : requests) {
                if (req.getRequesterUUID().equals(requester) && req.getCurrencyType() == type) {
                    count++;
                }
            }
        }
        return count;
    }
}
