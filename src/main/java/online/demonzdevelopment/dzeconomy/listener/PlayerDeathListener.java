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

        // Skip PVP currency loss in blacklisted worlds
        if (config.getConfig().getStringList("pvp.world-blacklist").contains(victim.getWorld().getName())) {
            return;
        }

        UUID victimUuid = victim.getUniqueId();
        UUID killerUuid = killer.getUniqueId();

        for (CurrencyType type : CurrencyType.values()) {
            String currencyName = type.getId();

            // Config stores the loss as a PERCENTAGE (e.g. 5.0 = 5%).
            // A value of 0 or missing disables PVP loss for that currency.
            double lossPercent = config.getConfig().getDouble("pvp.loss-percent." + currencyName, 0.0);
            if (lossPercent <= 0) {
                continue;
            }
            double lossFraction = Math.max(0.0, Math.min(1.0, lossPercent / 100.0));

            // The victim retains at least this balance after PVP death
            double minimumBalance = config.getConfig().getDouble("pvp.minimum-balance." + currencyName, 0.0);

            double victimBalance = cm.getBalance(victimUuid, type);
            if (victimBalance <= 0) {
                continue;
            }

            // Calculate amount to transfer based on configurable loss percentage
            double amount = victimBalance * lossFraction;

            // Enforce minimum retained balance
            double maxTransferable = Math.max(0.0, victimBalance - minimumBalance);
            if (amount > maxTransferable) {
                amount = maxTransferable;
            }

            // Round to 2 decimal places
            amount = Math.round(amount * 100.0) / 100.0;

            if (amount <= 0) {
                continue;
            }

            // Atomic transfer via CurrencyManager
            double killerBalanceBefore = cm.getBalance(killerUuid, type);
            boolean success = cm.transfer(victimUuid, killerUuid, type, amount);

            if (success) {
                double victimNewBalance = cm.getBalance(victimUuid, type);
                double killerNewBalance = cm.getBalance(killerUuid, type);
                // Transfer tax is deducted from the loot, so the killer nets less than the victim loses
                double netReceived = Math.max(0.0, killerNewBalance - killerBalanceBefore);
                String symbol = config.getConfig().getString("currencies." + currencyName + ".symbol", currencyName);

                // Notify victim
                MessagesUtil.sendMessage(victim, "pvp-lost-" + currencyName,
                        "%killer%", killer.getName(),
                        "%amount%", String.format("%,.2f", amount),
                        "%percentage%", String.format("%.0f", lossPercent),
                        "%balance%", String.format("%,.2f", victimNewBalance),
                        "%currency%", currencyName,
                        "%symbol%", symbol);

                // Notify killer with the net amount actually received
                MessagesUtil.sendMessage(killer, "pvp-gained-" + currencyName,
                        "%victim%", victim.getName(),
                        "%amount%", String.format("%,.2f", netReceived),
                        "%percentage%", String.format("%.0f", lossPercent),
                        "%balance%", String.format("%,.2f", killerNewBalance),
                        "%currency%", currencyName,
                        "%symbol%", symbol);

                // Broadcast when the dropped amount exceeds the configured threshold
                if (config.getConfig().getBoolean("pvp.broadcast.enabled", true)) {
                    double broadcastThreshold = config.getConfig().getDouble("pvp.broadcast.threshold", 1000);
                    if (broadcastThreshold > 0 && amount >= broadcastThreshold) {
                        String broadcastMessage = MessagesUtil.getStaticMessage("pvp-broadcast",
                                "%killer%", killer.getName(),
                                "%victim%", victim.getName(),
                                "%amount%", String.format("%,.2f", amount),
                                "%currency%", currencyName,
                                "%symbol%", symbol);
                        Bukkit.broadcastMessage(broadcastMessage);
                    }
                }
            }
        }
    }
}
