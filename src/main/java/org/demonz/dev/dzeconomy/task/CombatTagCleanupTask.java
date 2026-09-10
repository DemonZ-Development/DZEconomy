package org.demonz.dev.dzeconomy.task;

import org.demonz.dev.dzeconomy.manager.CombatTagManager;

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
