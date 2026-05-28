package online.demonzdevelopment.dzeconomy.listener;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import online.demonzdevelopment.dzeconomy.config.ConfigManager;
import online.demonzdevelopment.dzeconomy.util.MessagesUtil;

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLogin(org.bukkit.event.player.AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        // Pre-load player data asynchronously on join to prevent main thread blocking
        plugin.getCurrencyManager().loadPlayerData(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        CurrencyManager cm = plugin.getCurrencyManager();
        ConfigManager config = plugin.getConfigManager();

        // Mark player as online in track
        cm.setPlayerOnline(uuid, true);

        // Load player data
        PlayerData data = cm.loadPlayerData(uuid);

        // Null check on loadPlayerData result
        if (data == null) {
            plugin.getLogger().warning("Failed to load player data for " + player.getName() + " (" + uuid + ")");
            return;
        }

        // Check if this is a new player (no previous data)
        boolean isNewPlayer = data.isNewPlayer();

        // Update username and lastSeen
        data.setUsername(player.getName());
        data.setLastSeen(System.currentTimeMillis());

        // Send welcome message for new players
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
                        "%money%", String.format("%,.2f", startingBalance),
                        "%mobcoins%", String.format("%,.2f", startingMobcoins),
                        "%gems%", String.format("%,.2f", startingGems));
            }
        } else {
            // Welcome back message
            if (config.getConfig().getBoolean("welcome-back-message.enabled", false)) {
                double balance = cm.getBalance(uuid, CurrencyType.MONEY);
                MessagesUtil.sendMessage(player, "welcome-back",
                        "%player%", player.getName(),
                        "%balance%", String.format("%,.2f", balance));
            }
        }

        // Check for updates (notify admin)
        if (player.hasPermission("dzeconomy.admin")) {
            if (plugin.isUpdateAvailable()) {
                String latestVersion = plugin.getLatestVersion();
                MessagesUtil.sendMessage(player, "update-available",
                        "%current%", plugin.getDescription().getVersion(),
                        "%latest%", latestVersion);
            }
        }

        // Save updated data
        cm.savePlayerDataAsync(uuid);
    }
}
