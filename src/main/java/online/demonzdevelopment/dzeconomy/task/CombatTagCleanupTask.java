package online.demonzdevelopment.dzeconomy.task;

import online.demonzdevelopment.dzeconomy.manager.CombatTagManager;

public class CombatTagCleanupTask implements Runnable {

    private final CombatTagManager combatTagManager;

    public CombatTagCleanupTask(CombatTagManager combatTagManager) {
        this.combatTagManager = combatTagManager;
    }

    @Override
    public void run() {
        
        combatTagManager.cleanupExpiredCombatTags();
    }
}
