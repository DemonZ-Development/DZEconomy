package org.demonz.dev.dzeconomy.manager;

import org.demonz.dev.dzeconomy.DZEconomy;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatTagManager {

    private final DZEconomy plugin;
    private final ConcurrentHashMap<UUID, Long> tagExpiries = new ConcurrentHashMap<>();
    private volatile long combatTagDurationMillis = 30_000L;

    public CombatTagManager(DZEconomy plugin) {
        this.plugin = plugin;
        loadSettings();
    }

    public void loadSettings() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (config == null) return;
        combatTagDurationMillis = config.getLong("combat-tag.duration", 30) * 1000L;
    }

    public void tagPlayer(UUID uuid) {
        addCombatTag(uuid, System.currentTimeMillis() + combatTagDurationMillis);
    }

    public void addCombatTag(UUID uuid, long expiryTimeMillis) {
        long now = System.currentTimeMillis();
        long expiry = Math.max(expiryTimeMillis, now + 1L);
        tagExpiries.put(uuid, expiry);
    }

    public void removeTag(UUID uuid) {
        tagExpiries.remove(uuid);
    }

    public boolean isInCombat(UUID uuid) {
        return getRemainingMillis(uuid) > 0;
    }

    public int getRemainingCombatTime(UUID uuid) {
        long remaining = getRemainingMillis(uuid);
        return remaining > 0 ? (int) Math.ceil(remaining / 1000.0) : 0;
    }

    private long getRemainingMillis(UUID uuid) {
        Long expiry = tagExpiries.get(uuid);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        if (remaining <= 0) {
            tagExpiries.remove(uuid, expiry);
            return 0;
        }
        return remaining;
    }

    public void cleanExpiredTags() {
        long now = System.currentTimeMillis();
        tagExpiries.values().removeIf(expiry -> expiry <= now);
    }

    public void reload() {
        loadSettings();
    }

    public int getTaggedCount() {
        cleanExpiredTags();
        return tagExpiries.size();
    }

    public void removeCombatTag(UUID uuid) { removeTag(uuid); }

    public boolean isCombatTagged(UUID uuid) { return isInCombat(uuid); }

    public void cleanupExpiredCombatTags() { cleanExpiredTags(); }
}
