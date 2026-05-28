package online.demonzdevelopment.dzeconomy.listener;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.config.ConfigManager;
import online.demonzdevelopment.dzeconomy.util.MessagesUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final DZEconomy plugin;

    public PlayerDeathListener(DZEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory()) {
            return;
        }
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) {
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        CurrencyManager cm = plugin.getCurrencyManager();

        if (!config.getConfig().getBoolean("pvp.enabled", false)) {
            return;
        }

        UUID victimUuid = victim.getUniqueId();
        UUID killerUuid = killer.getUniqueId();

        for (CurrencyType type : CurrencyType.values()) {
            String currencyName = type.name().toLowerCase();
            String path = "pvp." + currencyName;

            if (!config.getConfig().getBoolean(path + ".enabled", false)) {
                continue;
            }

            // Get the loss percentage (0.0 to 1.0, not always 100%)
            double lossPercentage = config.getConfig().getDouble(path + ".loss-percentage", 1.0);
            lossPercentage = Math.max(0.0, Math.min(1.0, lossPercentage));

            if (lossPercentage <= 0) {
                continue;
            }

            double victimBalance = cm.getBalance(victimUuid, type);
            if (victimBalance <= 0) {
                continue;
            }

            // Calculate amount to transfer based on configurable loss percentage
            double amount = victimBalance * lossPercentage;

            // Round to 2 decimal places
            amount = Math.round(amount * 100.0) / 100.0;

            if (amount <= 0) {
                continue;
            }

            // Atomic transfer via CurrencyManager
            boolean success = cm.transfer(victimUuid, killerUuid, type, amount);

            if (success) {
                double victimNewBalance = cm.getBalance(victimUuid, type);
                double killerNewBalance = cm.getBalance(killerUuid, type);

                // Notify victim
                MessagesUtil.sendMessage(victim, "pvp-lost-" + currencyName,
                        "%killer%", killer.getName(),
                        "%amount%", String.format("%,.2f", amount),
                        "%percentage%", String.format("%.0f", lossPercentage * 100),
                        "%balance%", String.format("%,.2f", victimNewBalance));

                // Notify killer
                MessagesUtil.sendMessage(killer, "pvp-gained-" + currencyName,
                        "%victim%", victim.getName(),
                        "%amount%", String.format("%,.2f", amount),
                        "%percentage%", String.format("%.0f", lossPercentage * 100),
                        "%balance%", String.format("%,.2f", killerNewBalance));

                // Broadcast with threshold
                double broadcastThreshold = config.getConfig().getDouble(path + ".broadcast-threshold", 1000);
                if (amount >= broadcastThreshold && broadcastThreshold > 0) {
                    String broadcastMessage = MessagesUtil.getStaticMessage("pvp-broadcast",
                            "%killer%", killer.getName(),
                            "%victim%", victim.getName(),
                            "%amount%", String.format("%,.2f", amount),
                            "%currency%", currencyName);
                    Bukkit.broadcastMessage(broadcastMessage);
                }
            }
        }
    }
}
