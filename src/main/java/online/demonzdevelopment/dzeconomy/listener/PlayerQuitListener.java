package online.demonzdevelopment.dzeconomy.listener;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.data.CurrencyRequest;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import online.demonzdevelopment.dzeconomy.util.MessagesUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

    private final DZEconomy plugin;

    public PlayerQuitListener(DZEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        CurrencyManager cm = plugin.getCurrencyManager();

        // Update lastSeen
        PlayerData data = cm.loadPlayerData(uuid);
        if (data != null) {
            data.setLastSeen(System.currentTimeMillis());
        }

        // Save player data (sync to ensure data is persisted before unload)
        cm.savePlayerData(uuid);

        // Clean up combat tags, rank, and LuckPerms integration cache
        cm.removeCombatTag(uuid);
        plugin.getRankManager().removePlayerRank(uuid);
        if (plugin.getLuckPermsIntegration() != null) {
            plugin.getLuckPermsIntegration().removePlayer(uuid);
        }
        if (plugin.getPlaceholderExpansion() != null) {
            plugin.getPlaceholderExpansion().removePlayerCache(uuid);
        }

        // Close request GUI if open
        if (player.getOpenInventory() != null && player.getOpenInventory().getTitle() != null) {
            String title = player.getOpenInventory().getTitle();
            if (title.contains("Request") || title.contains(MessagesUtil.colorize("&6Request"))) {
                player.closeInventory();
            }
        }

        // Deny all pending requests involving this player and notify the other party
        java.util.List<CurrencyRequest> requestsFrom = cm.getRequestsFrom(uuid);
        for (CurrencyRequest request : requestsFrom) {
            cm.removeRequest(request.getRequestedPlayer(), request);
            Player targetPlayer = plugin.getServer().getPlayer(request.getRequestedPlayer());
            if (targetPlayer != null && targetPlayer.isOnline()) {
                MessagesUtil.sendMessage(targetPlayer, "request-cancelled-quit",
                        "%player%", player.getName(),
                        "%currency%", request.getCurrencyType().name().toLowerCase());
            }
        }

        java.util.List<CurrencyRequest> requestsTo = cm.getRequestsTo(uuid);
        for (CurrencyRequest request : requestsTo) {
            cm.removeRequest(request.getRequestedPlayer(), request);
            Player requester = plugin.getServer().getPlayer(request.getRequester());
            if (requester != null && requester.isOnline()) {
                MessagesUtil.sendMessage(requester, "request-cancelled-quit",
                        "%player%", player.getName(),
                        "%currency%", request.getCurrencyType().name().toLowerCase());
            }
        }

        // Unload player data from cache
        cm.unloadPlayerData(uuid);
    }
}
