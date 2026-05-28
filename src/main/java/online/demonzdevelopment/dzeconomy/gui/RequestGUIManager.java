package online.demonzdevelopment.dzeconomy.gui;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.util.ColorUtil;
import online.demonzdevelopment.dzeconomy.util.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI manager for money requests.
 * Features combat tag check, currency-specific materials,
 * timer indicators, and accept/deny handling.
 */
public class RequestGUIManager implements Listener {
    
    private final DZEconomy plugin;
    
    // Currency-specific materials
    private static final Map<String, Material> CURRENCY_MATERIALS = new LinkedHashMap<>();
    static {
        CURRENCY_MATERIALS.put("money", Material.GOLD_INGOT);
        CURRENCY_MATERIALS.put("mobcoin", Material.EMERALD);
        CURRENCY_MATERIALS.put("gem", Material.DIAMOND);
    }
    
    // Accept/Deny materials
    private static final Material ACCEPT_MATERIAL = Material.LIME_CONCRETE;
    private static final Material DENY_MATERIAL = Material.RED_CONCRETE;
    
    // Active request GUIs: target UUID -> request data
    private final Map<UUID, PendingRequest> pendingRequests = new java.util.concurrent.ConcurrentHashMap<>();
    
    public RequestGUIManager(DZEconomy plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Open a request GUI for the target player.
     */
    public void openRequestGUI(Player target, UUID requesterUUID, String currency, double amount) {
        // Combat tag check
        if (plugin.getCombatTagManager() != null && plugin.getCombatTagManager().isInCombat(target.getUniqueId())) {
            int remaining = plugin.getCombatTagManager().getRemainingCombatTime(target.getUniqueId());
            target.sendMessage(ColorUtil.translate(
                plugin.getMessagesUtil().getPrefixedMessage("request.combat-tagged",
                    "%time%", String.valueOf(remaining))
            ));
            return;
        }
        
        // Get requester name with null check for offline players
        String requesterName = "Unknown";
        Player requesterPlayer = Bukkit.getPlayer(requesterUUID);
        if (requesterPlayer != null) {
            requesterName = requesterPlayer.getName();
        } else {
            // Try offline player
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(requesterUUID);
            if (offlinePlayer.getName() != null) {
                requesterName = offlinePlayer.getName();
            }
        }
        
        // Store pending request
        UUID requestId = UUID.randomUUID();
        PendingRequest request = new PendingRequest(requesterUUID, requesterName, target.getUniqueId(), currency, amount, System.currentTimeMillis());
        pendingRequests.put(requestId, request);
        
        // Build inventory
        String title = ColorUtil.translate("&8Money Request");
        Inventory inv = Bukkit.createInventory(new RequestInventoryHolder(requestId), 27, title);
        
        // Currency item in center
        Material currencyMat = CURRENCY_MATERIALS.getOrDefault(currency.toLowerCase(), Material.PAPER);
        ItemStack currencyItem = new ItemStack(currencyMat);
        ItemMeta currencyMeta = currencyItem.getItemMeta();
        if (currencyMeta != null) {
            currencyMeta.setDisplayName(ColorUtil.translate("&6&l" + currency.substring(0, 1).toUpperCase() + currency.substring(1) + " Request"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtil.translate("&7From: &e" + requesterName));
            lore.add(ColorUtil.translate("&7Amount: &e" + NumberFormatter.formatFull(amount)));
            lore.add(ColorUtil.translate("&7Currency: &e" + currency));
            // Timer indicator
            lore.add(ColorUtil.translate("&7Expires in: &c30 seconds"));
            lore.add("");
            lore.add(ColorUtil.translate("&aClick &2Accept &aor &cDeny"));
            currencyMeta.setLore(lore);
            currencyItem.setItemMeta(currencyMeta);
        }
        inv.setItem(13, currencyItem);
        
        // Accept button (slot 11)
        ItemStack acceptItem = new ItemStack(ACCEPT_MATERIAL);
        ItemMeta acceptMeta = acceptItem.getItemMeta();
        if (acceptMeta != null) {
            acceptMeta.setDisplayName(ColorUtil.translate("&a&l✔ Accept Request"));
            List<String> acceptLore = new ArrayList<>();
            acceptLore.add(ColorUtil.translate("&7Click to accept the"));
            acceptLore.add(ColorUtil.translate("&7request from &e" + requesterName));
            acceptMeta.setLore(acceptLore);
            acceptItem.setItemMeta(acceptMeta);
        }
        inv.setItem(11, acceptItem);
        
        // Deny button (slot 15)
        ItemStack denyItem = new ItemStack(DENY_MATERIAL);
        ItemMeta denyMeta = denyItem.getItemMeta();
        if (denyMeta != null) {
            denyMeta.setDisplayName(ColorUtil.translate("&c&l✘ Deny Request"));
            List<String> denyLore = new ArrayList<>();
            denyLore.add(ColorUtil.translate("&7Click to deny the"));
            denyLore.add(ColorUtil.translate("&7request from &e" + requesterName));
            denyMeta.setLore(denyLore);
            denyItem.setItemMeta(denyMeta);
        }
        inv.setItem(15, denyItem);
        
        // Fill borders with gray glass
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(ColorUtil.translate("&r"));
            fillerMeta.setLore(Collections.emptyList());
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
        
        target.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory() == null || !(event.getInventory().getHolder() instanceof RequestInventoryHolder)) return;
        
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        
        org.bukkit.event.inventory.ClickType clickType = event.getClick();
        if (clickType == org.bukkit.event.inventory.ClickType.SHIFT_LEFT || 
            clickType == org.bukkit.event.inventory.ClickType.SHIFT_RIGHT || 
            clickType == org.bukkit.event.inventory.ClickType.DOUBLE_CLICK ||
            event.getAction() == org.bukkit.event.inventory.InventoryAction.COLLECT_TO_CURSOR ||
            event.getAction() == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            return;
        }
        
        RequestInventoryHolder holder = (RequestInventoryHolder) event.getInventory().getHolder();
        UUID requestId = holder.getRequestId();
        
        PendingRequest request = pendingRequests.get(requestId);
        if (request == null) {
            player.closeInventory();
            return;
        }
        
        if (!player.getUniqueId().equals(request.getTargetUUID())) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }
        
        // Check expiration (30 seconds)
        if (System.currentTimeMillis() - request.getTimestamp() > 30_000) {
            pendingRequests.remove(requestId);
            player.closeInventory();
            player.sendMessage(ColorUtil.translate(
                plugin.getMessagesUtil().getPrefixedMessage("request.expired")
            ));
            return;
        }
        
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == ACCEPT_MATERIAL) {
            pendingRequests.remove(requestId);
            handleAccept(player, request);
        } else if (clicked.getType() == DENY_MATERIAL) {
            pendingRequests.remove(requestId);
            handleDeny(player, request);
        }
    }
    
    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (event.getInventory() != null && event.getInventory().getHolder() instanceof RequestInventoryHolder) {
            RequestInventoryHolder holder = (RequestInventoryHolder) event.getInventory().getHolder();
            pendingRequests.remove(holder.getRequestId());
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getInventory() != null && event.getInventory().getHolder() instanceof RequestInventoryHolder) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        pendingRequests.values().removeIf(req -> req.getRequesterUUID().equals(playerUuid));
    }
    
    private void handleAccept(Player target, PendingRequest request) {
        target.closeInventory();
        
        // Process the request through the economy system
        CurrencyType currencyType = CurrencyType.fromString(request.getCurrency());
        if (currencyType == null) return;
        boolean success = plugin.getCurrencyManager().transfer(
            target.getUniqueId(), request.getRequesterUUID(), 
            currencyType, request.getAmount()
        );
        
        if (success) {
            target.sendMessage(ColorUtil.translate(
                plugin.getMessagesUtil().getPrefixedMessage("request.accepted-target",
                    "%player%", request.getRequesterName(),
                    "%amount%", NumberFormatter.formatFull(request.getAmount()),
                    "%currency%", request.getCurrency())
            ));
            
            // Notify requester
            Player requester = Bukkit.getPlayer(request.getRequesterUUID());
            if (requester != null) {
                requester.sendMessage(ColorUtil.translate(
                    plugin.getMessagesUtil().getPrefixedMessage("request.accepted-requester",
                        "%player%", target.getName(),
                        "%amount%", NumberFormatter.formatFull(request.getAmount()),
                        "%currency%", request.getCurrency())
                ));
            }
        } else {
            target.sendMessage(ColorUtil.translate(
                plugin.getMessagesUtil().getPrefixedMessage("request.accept-failed")
            ));
        }

        // If the requester is offline, unload their player data from cache asynchronously
        if (Bukkit.getPlayer(request.getRequesterUUID()) == null) {
            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
                plugin.getCurrencyManager().unloadPlayerData(request.getRequesterUUID());
            });
        }
    }
    
    private void handleDeny(Player target, PendingRequest request) {
        target.closeInventory();
        
        target.sendMessage(ColorUtil.translate(
            plugin.getMessagesUtil().getPrefixedMessage("request.denied-target",
                "%player%", request.getRequesterName())
        ));
        
        // Notify requester
        Player requester = Bukkit.getPlayer(request.getRequesterUUID());
        if (requester != null) {
            requester.sendMessage(ColorUtil.translate(
                plugin.getMessagesUtil().getPrefixedMessage("request.denied-requester",
                    "%player%", target.getName())
            ));
        }
    }
    
    /**
     * Remove a player's pending request (on disconnect, etc).
     */
    public void removePendingRequest(UUID uuid) {
        pendingRequests.values().removeIf(req -> req.getTargetUUID().equals(uuid) || req.getRequesterUUID().equals(uuid));
    }
    
    /**
     * Simple data class for pending requests.
     */
    private static class PendingRequest {
        private final UUID requesterUUID;
        private final String requesterName;
        private final UUID targetUUID;
        private final String currency;
        private final double amount;
        private final long timestamp;
        
        PendingRequest(UUID requesterUUID, String requesterName, UUID targetUUID, String currency, double amount, long timestamp) {
            this.requesterUUID = requesterUUID;
            this.requesterName = requesterName;
            this.targetUUID = targetUUID;
            this.currency = currency;
            this.amount = amount;
            this.timestamp = timestamp;
        }
        
        UUID getRequesterUUID() { return requesterUUID; }
        String getRequesterName() { return requesterName; }
        UUID getTargetUUID() { return targetUUID; }
        String getCurrency() { return currency; }
        double getAmount() { return amount; }
        long getTimestamp() { return timestamp; }
    }

    public static class RequestInventoryHolder implements org.bukkit.inventory.InventoryHolder {
        private final UUID requestId;
        public RequestInventoryHolder(UUID requestId) {
            this.requestId = requestId;
        }
        public UUID getRequestId() {
            return requestId;
        }
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
