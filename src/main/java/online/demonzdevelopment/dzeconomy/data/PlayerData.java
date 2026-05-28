package online.demonzdevelopment.dzeconomy.data;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {
    
    private final UUID uuid;
    private volatile String username;
    private volatile long firstJoin;
    private volatile long lastSeen;
    
    private final ConcurrentHashMap<CurrencyType, Double> balances;
    private final ConcurrentHashMap<CurrencyType, Long> dailySendCounts;
    private final ConcurrentHashMap<CurrencyType, Long> dailyRequestCounts;
    private final ConcurrentHashMap<CurrencyType, Long> sendCooldowns;
    private final ConcurrentHashMap<CurrencyType, Long> requestCooldowns;
    private final ConcurrentHashMap<CurrencyType, Double> moneySent;
    private final ConcurrentHashMap<CurrencyType, Double> moneyReceived;
    private final ConcurrentHashMap<CurrencyType, Double> dailySentAmount;
    
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.firstJoin = System.currentTimeMillis();
        this.lastSeen = System.currentTimeMillis();
        this.balances = new ConcurrentHashMap<>();
        this.dailySendCounts = new ConcurrentHashMap<>();
        this.dailyRequestCounts = new ConcurrentHashMap<>();
        this.sendCooldowns = new ConcurrentHashMap<>();
        this.requestCooldowns = new ConcurrentHashMap<>();
        this.moneySent = new ConcurrentHashMap<>();
        this.moneyReceived = new ConcurrentHashMap<>();
        this.dailySentAmount = new ConcurrentHashMap<>();
        
        // Initialize default balances
        for (CurrencyType type : CurrencyType.values()) {
            balances.put(type, type.getDefaultBalance());
            dailySendCounts.put(type, 0L);
            dailyRequestCounts.put(type, 0L);
            sendCooldowns.put(type, 0L);
            requestCooldowns.put(type, 0L);
            moneySent.put(type, 0.0);
            moneyReceived.put(type, 0.0);
            dailySentAmount.put(type, 0.0);
        }
        
        // Use UUID as placeholder username until loaded from DB or player joins
        this.username = uuid.toString();
    }
    
    // Getters
    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public long getFirstJoin() { return firstJoin; }
    public long getLastSeen() { return lastSeen; }
    
    public double getBalance(CurrencyType type) { return balances.getOrDefault(type, 0.0); }
    public long getDailySendCount(CurrencyType type) { return dailySendCounts.getOrDefault(type, 0L); }
    public long getDailyRequestCount(CurrencyType type) { return dailyRequestCounts.getOrDefault(type, 0L); }
    public long getSendCooldown(CurrencyType type) { return sendCooldowns.getOrDefault(type, 0L); }
    public long getRequestCooldown(CurrencyType type) { return requestCooldowns.getOrDefault(type, 0L); }
    public double getMoneySent(CurrencyType type) { return moneySent.getOrDefault(type, 0.0); }
    public double getMoneyReceived(CurrencyType type) { return moneyReceived.getOrDefault(type, 0.0); }
    
    // Thread-safe atomic operations using compute/merge
    public void setBalance(CurrencyType type, double amount) {
        if (amount < 0) amount = 0.0;
        balances.put(type, amount);
        dirty = true;
    }
    
    public double addBalance(CurrencyType type, double amount) {
        if (amount < 0) return balances.getOrDefault(type, 0.0);
        double result = balances.merge(type, amount, Double::sum);
        dirty = true;
        return result;
    }
    
    public double removeBalance(CurrencyType type, double amount) {
        if (amount < 0) return balances.getOrDefault(type, 0.0);
        final double deducted = amount;
        double result = balances.compute(type, (k, current) -> {
            double val = current != null ? current : 0.0;
            double r = val - deducted;
            return Math.max(0.0, r);
        });
        dirty = true;
        return result;
    }
    
    public long incrementDailySendCount(CurrencyType type) {
        long result = dailySendCounts.merge(type, 1L, Long::sum);
        dirty = true;
        return result;
    }
    
    public long incrementDailyRequestCount(CurrencyType type) {
        long result = dailyRequestCounts.merge(type, 1L, Long::sum);
        dirty = true;
        return result;
    }
    
    public void setSendCooldown(CurrencyType type, long timestamp) { sendCooldowns.put(type, timestamp); dirty = true; }
    public void setRequestCooldown(CurrencyType type, long timestamp) { requestCooldowns.put(type, timestamp); dirty = true; }
    
    public void addMoneySent(CurrencyType type, double amount) { moneySent.merge(type, amount, Double::sum); dirty = true; }
    public void addMoneyReceived(CurrencyType type, double amount) { moneyReceived.merge(type, amount, Double::sum); dirty = true; }
    
    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setFirstJoin(long firstJoin) { this.firstJoin = firstJoin; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; dirty = true; }
    
    public void resetDailyCounts() {
        for (CurrencyType type : CurrencyType.values()) {
            dailySendCounts.put(type, 0L);
            dailyRequestCounts.put(type, 0L);
        }
        dirty = true;
    }
    
    // Return unmodifiable views
    public Map<CurrencyType, Double> getBalances() { return Collections.unmodifiableMap(balances); }
    public Map<CurrencyType, Long> getDailySendCounts() { return Collections.unmodifiableMap(dailySendCounts); }
    public Map<CurrencyType, Long> getDailyRequestCounts() { return Collections.unmodifiableMap(dailyRequestCounts); }
    public Map<CurrencyType, Long> getSendCooldowns() { return Collections.unmodifiableMap(sendCooldowns); }
    public Map<CurrencyType, Long> getRequestCooldowns() { return Collections.unmodifiableMap(requestCooldowns); }
    public Map<CurrencyType, Double> getMoneySent() { return Collections.unmodifiableMap(moneySent); }
    public Map<CurrencyType, Double> getMoneyReceived() { return Collections.unmodifiableMap(moneyReceived); }
    
    // Setters for loading from storage (do NOT set dirty — called during load)
    public void setDailySendCount(CurrencyType type, long count) { dailySendCounts.put(type, count); }
    public void setDailyRequestCount(CurrencyType type, long count) { dailyRequestCounts.put(type, count); }
    public void setMoneySent(CurrencyType type, double amount) { moneySent.put(type, amount); }
    public void setMoneyReceived(CurrencyType type, double amount) { moneyReceived.put(type, amount); }

    // ━━ Additional methods used by commands and tasks ━━

    public void addDailySent(CurrencyType type, double amount) { dailySentAmount.merge(type, amount, Double::sum); dirty = true; }
    public double getDailySent(CurrencyType type) { return dailySentAmount.getOrDefault(type, 0.0); }

    private final ConcurrentHashMap<CurrencyType, Long> lastSendTimes = new ConcurrentHashMap<>();
    public long getLastSendTime(CurrencyType type) { return lastSendTimes.getOrDefault(type, 0L); }
    public void setLastSendTime(CurrencyType type, long timestamp) { lastSendTimes.put(type, timestamp); dirty = true; }

    public void resetDailySent(CurrencyType type) { dailySendCounts.put(type, 0L); dailySentAmount.put(type, 0.0); dirty = true; }
    public void resetDailyReceived(CurrencyType type) { dailyRequestCounts.put(type, 0L); moneyReceived.put(type, 0.0); dirty = true; }
    public void resetDailyTransactions(CurrencyType type) { resetDailySent(type); resetDailyReceived(type); }

    private volatile boolean dirty = false;
    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }

    private volatile boolean newPlayer = false;
    public boolean isNewPlayer() { return newPlayer; }
    public void setNewPlayer(boolean newPlayer) { this.newPlayer = newPlayer; }
}
