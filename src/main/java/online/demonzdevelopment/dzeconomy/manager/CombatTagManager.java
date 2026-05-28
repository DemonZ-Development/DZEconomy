package online.demonzdevelopment.dzeconomy.manager;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages combat tagging for players.
 * Thread-safe via ConcurrentHashMap.
 * Compatible with CombatTagCleanupTask.
 */
public class CombatTagManager {
    
    private final DZEconomy plugin;
    private final ConcurrentHashMap<UUID, Long> combatTags = new ConcurrentHashMap<>();
    private final Set<EntityType> dangerousMobs = ConcurrentHashMap.newKeySet();
    private int combatTagDurationSeconds = 30;
    
    public CombatTagManager(DZEconomy plugin) {
        this.plugin = plugin;
        loadDangerousMobs();
    }
    
    /**
     * Load dangerous mobs and settings from config.
     */
    public void loadDangerousMobs() {
        dangerousMobs.clear();
        
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (config == null) return;
        
        combatTagDurationSeconds = config.getInt("combat-tag.duration", 15);
        
        List<String> mobList = config.getStringList("combat-tag.dangerous-mobs");
        for (String mobName : mobList) {
            try {
                EntityType type = EntityType.valueOf(mobName.toUpperCase());
                dangerousMobs.add(type);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type in combat-tag dangerous-mobs: " + mobName);
            }
        }
        
        plugin.getLogger().info("Loaded " + dangerousMobs.size() + " dangerous mob types for combat tagging.");
    }
    
    /**
     * Tag a player as being in combat.
     */
    public void tagPlayer(UUID uuid) {
        combatTags.put(uuid, System.currentTimeMillis());
    }
    
    /**
     * Remove a player's combat tag.
     */
    public void removeTag(UUID uuid) {
        combatTags.remove(uuid);
    }
    
    /**
     * Check if a player is currently in combat.
     * Query-only, no side effects.
     */
    public boolean isInCombat(UUID uuid) {
        Long tagTime = combatTags.get(uuid);
        if (tagTime == null) return false;
        long elapsed = (System.currentTimeMillis() - tagTime) / 1000;
        return elapsed < combatTagDurationSeconds;
    }
    
    /**
     * Get remaining combat time in seconds.
     * Returns 0 if not in combat.
     */
    public int getRemainingCombatTime(UUID uuid) {
        Long tagTime = combatTags.get(uuid);
        if (tagTime == null) return 0;
        long elapsed = (System.currentTimeMillis() - tagTime) / 1000;
        int remaining = combatTagDurationSeconds - (int) elapsed;
        return Math.max(0, remaining);
    }
    
    /**
     * Check if an entity type is a dangerous mob.
     */
    public boolean isDangerousMob(EntityType type) {
        return dangerousMobs.contains(type);
    }
    
    /**
     * Get the combat tag duration in seconds.
     */
    public int getCombatTagDurationSeconds() {
        return combatTagDurationSeconds;
    }
    
    /**
     * Clean expired combat tags.
     * Called by CombatTagCleanupTask.
     */
    public void cleanExpiredTags() {
        long now = System.currentTimeMillis();
        long durationMillis = combatTagDurationSeconds * 1000L;
        
        Iterator<Map.Entry<UUID, Long>> iterator = combatTags.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (now - entry.getValue() >= durationMillis) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Reload settings from config.
     */
    public void reload() {
        loadDangerousMobs();
    }
    
    /**
     * Get the number of currently tagged players (for debugging/admin).
     */
    public int getTaggedCount() {
        return combatTags.size();
    }

    // ━━ Alias methods for compatibility ━━

    /** Alias for tagPlayer(UUID) */
    public void addCombatTag(UUID uuid, long durationMillis) { tagPlayer(uuid); }
    /** Alias for removeTag(UUID) */
    public void removeCombatTag(UUID uuid) { removeTag(uuid); }
    /** Alias for isInCombat(UUID) */
    public boolean isCombatTagged(UUID uuid) { return isInCombat(uuid); }
    /** Alias for cleanExpiredTags() */
    public void cleanupExpiredCombatTags() { cleanExpiredTags(); }
}
