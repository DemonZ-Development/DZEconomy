package online.demonzdevelopment.dzeconomy.task;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.update.UpdateManager;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periodic update check task. Runs every 6 hours by default.
 * Only checks Modrinth for new versions and notifies admins.
 * No auto-download or auto-install.
 */
public class UpdateCheckTask extends BukkitRunnable {
    
    private final DZEconomy plugin;
    private final UpdateManager updateManager;
    
    public UpdateCheckTask(DZEconomy plugin, UpdateManager updateManager) {
        this.plugin = plugin;
        this.updateManager = updateManager;
    }
    
    @Override
    public void run() {
        updateManager.checkForUpdates().thenAccept(available -> {
            if (available) {
                plugin.getLogger().info("A new version of DZEconomy is available on Modrinth!");
            }
        });
    }
}
