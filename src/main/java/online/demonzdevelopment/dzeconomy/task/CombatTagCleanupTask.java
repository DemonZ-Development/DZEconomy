package online.demonzdevelopment.dzeconomy.task;

import online.demonzdevelopment.dzeconomy.manager.CombatTagManager;

import org.bukkit.scheduler.BukkitRunnable;

public class CombatTagCleanupTask extends BukkitRunnable {

    private final CombatTagManager combatTagManager;

    public CombatTagCleanupTask(CombatTagManager combatTagManager) {
        this.combatTagManager = combatTagManager;
    }

    @Override
    public void run() {
        // Simple cleanup: remove all expired combat tags
        combatTagManager.cleanupExpiredCombatTags();
    }
}
