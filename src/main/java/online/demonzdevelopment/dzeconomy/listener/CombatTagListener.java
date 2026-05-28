package online.demonzdevelopment.dzeconomy.listener;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.config.ConfigManager;
import online.demonzdevelopment.dzeconomy.util.MessagesUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class CombatTagListener implements Listener {

    private final DZEconomy plugin;

    public CombatTagListener(DZEconomy plugin) {
        this.plugin = plugin;
    }

    /**
     * Tag players when they engage in PVP combat.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        ConfigManager config = plugin.getConfigManager();

        if (!config.getConfig().getBoolean("combat-tag.enabled", true)) {
            return;
        }

        // Ensure both entities are players
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = null;

        // Check if the damager is a player directly or a projectile shot by a player
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile) {
            org.bukkit.entity.Projectile projectile = (org.bukkit.entity.Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }

        if (attacker == null) {
            return;
        }

        // Don't tag if damage is 0 or very low (e.g., snowballs)
        if (event.getFinalDamage() <= 0) {
            return;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        long tagDuration = config.getConfig().getLong("combat-tag.duration", 30) * 1000;
        long expiryTime = System.currentTimeMillis() + tagDuration;

        UUID victimUuid = victim.getUniqueId();
        UUID attackerUuid = attacker.getUniqueId();

        // Tag both players
        boolean victimWasTagged = cm.isCombatTagged(victimUuid);
        boolean attackerWasTagged = cm.isCombatTagged(attackerUuid);

        cm.addCombatTag(victimUuid, expiryTime);
        cm.addCombatTag(attackerUuid, expiryTime);

        // Send tag notification if not already tagged
        if (!victimWasTagged) {
            MessagesUtil.sendMessage(victim, "combat-tagged",
                    "%player%", attacker.getName(),
                    "%duration%", String.valueOf(tagDuration / 1000));
        }

        if (!attackerWasTagged) {
            MessagesUtil.sendMessage(attacker, "combat-tagged",
                    "%player%", victim.getName(),
                    "%duration%", String.valueOf(tagDuration / 1000));
        }
    }

    /**
     * Remove combat tag when a player quits.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CurrencyManager cm = plugin.getCurrencyManager();
        if (cm.isCombatTagged(player.getUniqueId())) {
            player.setHealth(0.0);
            plugin.getLogger().info(player.getName() + " logged out while combat tagged and was killed.");
        }
        cm.removeCombatTag(player.getUniqueId());
    }

    /**
     * Block request GUI when the player is combat tagged.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        // Check if this is a request-related GUI using the inventory holder
        if (event.getInventory() != null && event.getInventory().getHolder() instanceof online.demonzdevelopment.dzeconomy.gui.RequestGUIManager.RequestInventoryHolder) {
            CurrencyManager cm = plugin.getCurrencyManager();
            if (cm.isCombatTagged(player.getUniqueId())) {
                event.setCancelled(true);
                MessagesUtil.sendMessage(player, "combat-tagged-gui");
            }
        }
    }
}
