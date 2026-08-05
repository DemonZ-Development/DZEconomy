package online.demonzdevelopment.dzeconomy.update;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.adapter.FeatureAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class UpdateManager {
    
    private final DZEconomy plugin;
    private final ModrinthAPIClient apiClient;
    private volatile ModrinthVersion latestVersion;
    private volatile boolean updateAvailable = false;
    
    public UpdateManager(DZEconomy plugin) {
        this.plugin = plugin;
        String projectId = plugin.getConfigManager().getConfig().getString("updates.modrinth-project-id", "dzeconomy");
        this.apiClient = new ModrinthAPIClient(projectId, plugin.getDescription().getVersion());
    }
    
    public CompletableFuture<Boolean> checkForUpdates() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                latestVersion = apiClient.fetchLatestVersion();
                if (latestVersion == null) return false;
                
                SemanticVersion current = new SemanticVersion(plugin.getDescription().getVersion());
                SemanticVersion latest = new SemanticVersion(latestVersion.getVersionNumber());
                
                updateAvailable = latest.isNewerThan(current);
                return updateAvailable;
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to check for updates via Modrinth", e);
                return false;
            }
        }).thenApplyAsync(available -> {
            
            if (available) {
                online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, () -> notifyAdmins());
            }
            return available;
        });
    }
    
    private void notifyAdmins() {
        if (latestVersion == null) return;
        
        String msg = "\u00a7a[DZEconomy] \u00a7eA new version is available! \u00a7fv" + latestVersion.getVersionNumber();
        String msg2 = "\u00a7a[DZEconomy] \u00a77Download: \u00a7bhttps://modrinth.com/plugin/dzeconomy/versions";
        
        for (Player player : FeatureAdapter.get().getOnlinePlayers()) {
            if (player.isOp() || player.hasPermission("dzeconomy.admin.update")) {
                online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, player, () -> {
                    player.sendMessage(msg);
                    player.sendMessage(msg2);
                });
            }
        }
        
        plugin.getLogger().info("A new version is available: v" + latestVersion.getVersionNumber());
        plugin.getLogger().info("Download at: https://modrinth.com/plugin/dzeconomy/versions");
    }
    
    public boolean isUpdateAvailable() { return updateAvailable; }
    public ModrinthVersion getLatestVersion() { return latestVersion; }

    public String getLatestVersionNumber() {
        return latestVersion != null ? latestVersion.getVersionNumber() : null;
    }
}
