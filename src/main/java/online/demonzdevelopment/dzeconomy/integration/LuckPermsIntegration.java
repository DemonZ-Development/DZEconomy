package online.demonzdevelopment.dzeconomy.integration;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LuckPermsIntegration {
    
    private final DZEconomy plugin;
    private LuckPerms luckPermsApi;
    private boolean enabled = false;
    private net.luckperms.api.event.EventSubscription<net.luckperms.api.event.user.UserDataRecalculateEvent> subscription;
    
    private final ConcurrentHashMap<UUID, CachedGroup> groupCache = new ConcurrentHashMap<>();
    
    private static final long CACHE_DURATION_MS = 30_000; 
    
    public LuckPermsIntegration(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().info("LuckPerms not found, rank resolution will use defaults.");
            return false;
        }
        
        try {
            RegisteredServiceProvider<LuckPerms> provider = 
                Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            
            if (provider == null) {
                plugin.getLogger().warning("LuckPerms service provider not found!");
                return false;
            }
            
            luckPermsApi = provider.getProvider();
            if (luckPermsApi == null) {
                plugin.getLogger().warning("LuckPerms API instance is null!");
                return false;
            }
            
            enabled = true;
            registerEventBus();
            plugin.getLogger().info("Successfully hooked into LuckPerms!");
            return true;
            
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().info("LuckPerms classes not found. Rank resolution will use config defaults.");
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into LuckPerms: " + e.getMessage());
            return false;
        }
    }
    
    public String getPlayerGroup(UUID uuid) {
        if (!enabled || luckPermsApi == null) return null;
        
        CachedGroup cached = groupCache.get(uuid);
        if (cached != null && !cached.isExpired()) {
            return cached.groupName;
        }
        
        try {
            
            User user = luckPermsApi.getUserManager().getUser(uuid);
            if (user == null) {
                
                loadUserAsync(uuid);
                return cached != null ? cached.groupName : null;
            }
            
            String groupName = user.getPrimaryGroup();
            
            groupCache.put(uuid, new CachedGroup(groupName));
            
            return groupName;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting LuckPerms group for " + uuid + ": " + e.getMessage());
            return cached != null ? cached.groupName : null;
        }
    }
    
    private void loadUserAsync(UUID uuid) {
        if (luckPermsApi == null) return;
        
        luckPermsApi.getUserManager().loadUser(uuid).thenAccept(user -> {
            if (user != null) {
                String groupName = user.getPrimaryGroup();
                groupCache.put(uuid, new CachedGroup(groupName));
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().warning("Failed to load LuckPerms user " + uuid + ": " + throwable.getMessage());
            return null;
        });
    }
    
    public void invalidateCache(UUID uuid) {
        groupCache.remove(uuid);
    }
    
    public void removePlayer(UUID uuid) {
        groupCache.remove(uuid);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public LuckPerms getApi() {
        return luckPermsApi;
    }
    
    private void registerEventBus() {
        if (luckPermsApi == null) return;
        this.subscription = luckPermsApi.getEventBus().subscribe(plugin, net.luckperms.api.event.user.UserDataRecalculateEvent.class, event -> {
            UUID uuid = event.getUser().getUniqueId();
            invalidateCache(uuid);
            if (plugin.getRankManager() != null) {
                plugin.getRankManager().removePlayerRank(uuid);
            }
        });
    }
    
    public void cleanup() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
        enabled = false;
        luckPermsApi = null;
    }
    
    private static class CachedGroup {
        final String groupName;
        final long timestamp;
        
        CachedGroup(String groupName) {
            this.groupName = groupName;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
}
