package online.demonzdevelopment.dzeconomy.task;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.data.CurrencyRequest;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import online.demonzdevelopment.dzeconomy.util.MessagesUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class RequestTimeoutTask implements Runnable {

    private final DZEconomy plugin;

    public RequestTimeoutTask(DZEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        CurrencyManager cm = plugin.getCurrencyManager();
        long now = System.currentTimeMillis();

        List<CurrencyRequest> expiredRequests = new ArrayList<>();
        List<UUID> expiredRequestedPlayers = new ArrayList<>();

        for (UUID requestedPlayer : cm.getRequestHolders()) {
            List<CurrencyRequest> requests = cm.getRequestsTo(requestedPlayer);
            if (requests == null) continue;

            for (CurrencyRequest request : requests) {
                if (request.isExpired()) {
                    
                    expiredRequests.add(request);
                    expiredRequestedPlayers.add(requestedPlayer);
                }
            }
        }

        for (int i = 0; i < expiredRequests.size(); i++) {
            CurrencyRequest expiredRequest = expiredRequests.get(i);
            UUID requestedPlayer = expiredRequestedPlayers.get(i);

            cm.removeRequest(requestedPlayer, expiredRequest);

            Player requester = Bukkit.getPlayer(expiredRequest.getRequester());
            if (requester != null && requester.isOnline()) {
                online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, requester, () -> {
                    MessagesUtil.sendMessage(requester, "request-expired-timeout",
                            "%player%", getPlayerName(expiredRequest.getRequestedPlayer()),
                            "%amount%", String.format("%,.2f", expiredRequest.getAmount()),
                            "%currency%", expiredRequest.getCurrencyType().name().toLowerCase());
                });
            }

            Player requested = Bukkit.getPlayer(requestedPlayer);
            if (requested != null && requested.isOnline()) {
                online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, requested, () -> {
                    MessagesUtil.sendMessage(requested, "request-expired-notify",
                            "%player%", getPlayerName(expiredRequest.getRequester()),
                            "%amount%", String.format("%,.2f", expiredRequest.getAmount()),
                            "%currency%", expiredRequest.getCurrencyType().name().toLowerCase());

                    closeRequestGUI(requested);
                });
            }
        }

        if (!expiredRequests.isEmpty()) {
            plugin.getLogger().info("[RequestTimeout] Cleaned up " + expiredRequests.size() + " expired request(s).");
        }
    }

    private void closeRequestGUI(Player player) {
        try {
            if (player.getOpenInventory() != null && player.getOpenInventory().getTitle() != null) {
                String title = player.getOpenInventory().getTitle();
                if (title.contains("Request")) {
                    player.closeInventory();
                }
            }
        } catch (Exception e) {
            
            plugin.getLogger().warning("[RequestTimeout] Failed to close GUI for " + player.getName() + ": " + e.getMessage());
        }
    }

    private String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        
        PlayerData cached = plugin.getCurrencyManager().getPlayerData(uuid);
        if (cached != null && cached.getUsername() != null) {
            return cached.getUsername();
        }
        
        String offlineName = Bukkit.getOfflinePlayer(uuid).getName();
        return offlineName != null ? offlineName : uuid.toString().substring(0, 8) + "...";
    }
}
