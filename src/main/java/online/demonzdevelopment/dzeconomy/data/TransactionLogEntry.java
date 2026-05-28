package online.demonzdevelopment.dzeconomy.data;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single economy transaction for auditing/history.
 */
public class TransactionLogEntry {
    
    public enum TransactionType {
        ADD, REMOVE, SET, TRANSFER_SEND, TRANSFER_RECEIVE, CONVERT, PAYALL
    }
    
    private final long timestamp;
    private final UUID playerUUID;
    private final TransactionType type;
    private final CurrencyType currency;
    private final double amount;
    private final double balanceAfter;
    private final UUID targetUUID; // null for non-transfer operations
    private final String description;
    
    public TransactionLogEntry(UUID playerUUID, TransactionType type, CurrencyType currency,
                                double amount, double balanceAfter, UUID targetUUID, String description) {
        this.timestamp = Instant.now().toEpochMilli();
        this.playerUUID = playerUUID;
        this.type = type;
        this.currency = currency;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.targetUUID = targetUUID;
        this.description = description;
    }
    
    // Getters
    public long getTimestamp() { return timestamp; }
    public UUID getPlayerUUID() { return playerUUID; }
    public TransactionType getType() { return type; }
    public CurrencyType getCurrency() { return currency; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public UUID getTargetUUID() { return targetUUID; }
    public String getDescription() { return description; }
    
    /**
     * Returns a formatted string representation for display.
     */
    public String toDisplayString() {
        String time = Instant.ofEpochMilli(timestamp).toString();
        String target = targetUUID != null ? " -> " + targetUUID.toString().substring(0, 8) + "..." : "";
        return String.format("[%s] %s %s %.2f (bal: %.2f)%s %s",
            time, type.name(), currency.name(), amount, balanceAfter, target,
            description != null ? "- " + description : "");
    }
}
