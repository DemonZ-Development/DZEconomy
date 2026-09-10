package org.demonz.dev.dzeconomy.listener;

import org.demonz.dev.dzeconomy.DZEconomy;
import org.demonz.dev.dzeconomy.currency.CurrencyManager;
import org.demonz.dev.dzeconomy.currency.CurrencyType;
import org.demonz.dev.dzeconomy.data.PlayerData;
import org.demonz.dev.dzeconomy.config.ConfigManager;
import org.demonz.dev.dzeconomy.util.MessagesUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final DZEconomy plugin;

    public PlayerJoinListener(DZEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncPlayerPreLogin(org.bukkit.event.player.AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        
        plugin.getCurrencyManager().loadPlayerData(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        CurrencyManager cm = plugin.getCurrencyManager();
        ConfigManager config = plugin.getConfigManager();

        cm.setPlayerOnline(uuid, true);

        PlayerData data = cm.loadPlayerData(uuid);

        if (data == null) {
            plugin.getLogger().warning("Failed to load player data for " + player.getName() + " (" + uuid + ")");
            return;
        }

        boolean isNewPlayer = data.isNewPlayer();

        data.setUsername(player.getName());
        data.setLastSeen(System.currentTimeMillis());

        if (isNewPlayer) {
            double startingBalance = config.getConfig().getDouble("currencies.money.starting-balance", 0);
            double startingMobcoins = config.getConfig().getDouble("currencies.mobcoin.starting-balance", 0);
            double startingGems = config.getConfig().getDouble("currencies.gem.starting-balance", 0);

            if (startingBalance > 0) {
                cm.addBalance(uuid, CurrencyType.MONEY, startingBalance);
            }
            if (startingMobcoins > 0) {
                cm.addBalance(uuid, CurrencyType.MOBCOIN, startingMobcoins);
            }
            if (startingGems > 0) {
                cm.addBalance(uuid, CurrencyType.GEM, startingGems);
            }

            if (config.getConfig().getBoolean("welcome-message.enabled", true)) {
                MessagesUtil.sendMessage(player, "welcome-new-player",
                        "%player%", player.getName(),
                        "%money%", cm.formatAmount(CurrencyType.MONEY, startingBalance),
                        "%mobcoins%", cm.formatAmount(CurrencyType.MOBCOIN, startingMobcoins),
                        "%gems%", cm.formatAmount(CurrencyType.GEM, startingGems));
            }

            data.setNewPlayer(false);
        } else {
            
            if (config.getConfig().getBoolean("welcome-back-message.enabled", false)) {
                double balance = cm.getBalance(uuid, CurrencyType.MONEY);
                MessagesUtil.sendMessage(player, "welcome-back",
                        "%player%", player.getName(),
                        "%balance%", cm.formatAmount(CurrencyType.MONEY, balance));
            }
        }

        if (player.hasPermission("dzeconomy.admin")) {
            if (plugin.isUpdateAvailable()) {
                String latestVersion = plugin.getLatestVersion();
                MessagesUtil.sendMessage(player, "update-available",
                        "%current%", plugin.getDescription().getVersion(),
                        "%latest%", latestVersion);
            }
        }

        cm.savePlayerDataAsync(uuid);
    }
}
