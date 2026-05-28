package online.demonzdevelopment.dzeconomy.data;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;

import java.util.UUID;

public class CurrencyRequest {
    
    private final UUID requesterUUID;
    private final UUID requestedPlayerUUID;
    private final CurrencyType currencyType;
    private final double amount;
    private final long creationTime;
    private final long expirationTime;
    
    public CurrencyRequest(UUID requesterUUID, UUID requestedPlayerUUID, CurrencyType currencyType, double amount, long expirationTimeMillis) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (requesterUUID.equals(requestedPlayerUUID)) throw new IllegalArgumentException("Cannot request from yourself");
        
        this.requesterUUID = requesterUUID;
        this.requestedPlayerUUID = requestedPlayerUUID;
        this.currencyType = currencyType;
        this.amount = amount;
        this.creationTime = System.currentTimeMillis();
        this.expirationTime = expirationTimeMillis;
    }
    
    public UUID getRequesterUUID() { return requesterUUID; }
    public UUID getRequestedPlayerUUID() { return requestedPlayerUUID; }

    /** Alias for getRequesterUUID() */
    public UUID getRequester() { return requesterUUID; }
    /** Alias for getRequestedPlayerUUID() */
    public UUID getRequestedPlayer() { return requestedPlayerUUID; }
    public CurrencyType getCurrencyType() { return currencyType; }
    public double getAmount() { return amount; }
    public long getCreationTime() { return creationTime; }
    public long getExpirationTime() { return expirationTime; }
    
    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }
    
    public long getRemainingTime() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }
}
