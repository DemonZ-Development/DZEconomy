package online.demonzdevelopment.dzeconomy.manager;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CombatTagManager {
    
    private final DZEconomy plugin;
    private final ConcurrentHashMap<UUID, Long> combatTags = new ConcurrentHashMap<>();
    private int combatTagDurationSeconds = 30;
    
    public CombatTagManager(DZEconomy plugin) {
        this.plugin = plugin;
        loadSettings();
    }
    
    public void loadSettings() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (config == null) return;
        
        combatTagDurationSeconds = config.getInt("combat-tag.duration", 15);
    }
    
    public void tagPlayer(UUID uuid) {
        combatTags.put(uuid, System.currentTimeMillis());
    }
    
    public void removeTag(UUID uuid) {
        combatTags.remove(uuid);
    }
    
    public boolean isInCombat(UUID uuid) {
        Long tagTime = combatTags.get(uuid);
        if (tagTime == null) return false;
        long elapsed = (System.currentTimeMillis() - tagTime) / 1000;
        return elapsed < combatTagDurationSeconds;
    }
    
    public int getRemainingCombatTime(UUID uuid) {
        Long tagTime = combatTags.get(uuid);
        if (tagTime == null) return 0;
        long elapsed = (System.currentTimeMillis() - tagTime) / 1000;
        int remaining = combatTagDurationSeconds - (int) elapsed;
        return Math.max(0, remaining);
    }
    
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
    
    public void reload() {
        loadSettings();
    }
    
    public int getTaggedCount() {
        return combatTags.size();
    }

    public void addCombatTag(UUID uuid, long durationMillis) { tagPlayer(uuid); }
    
    public void removeCombatTag(UUID uuid) { removeTag(uuid); }
    
    public boolean isCombatTagged(UUID uuid) { return isInCombat(uuid); }
    
    public void cleanupExpiredCombatTags() { cleanExpiredTags(); }
}
