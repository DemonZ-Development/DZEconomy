package org.demonz.dev.dzeconomy.task;

import org.demonz.dev.dzeconomy.DZEconomy;
import org.demonz.dev.dzeconomy.currency.CurrencyManager;
import org.demonz.dev.dzeconomy.currency.CurrencyType;
import org.demonz.dev.dzeconomy.rank.Rank;
import org.demonz.dev.dzeconomy.util.MessagesUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pays rank interest on Money balances.
 * <p>
 * Runs asynchronously: every pass checks online players whose rank enables
 * {@code perks.interest}, paying {@code balance × rate / 100} (capped at
 * {@code max-balance} when set) once per {@code interval} seconds.
 */
public class InterestTask implements Runnable {

    private final DZEconomy plugin;
    private final ConcurrentHashMap<UUID, Long> lastPayout = new ConcurrentHashMap<>();

    public InterestTask(DZEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        CurrencyManager cm = plugin.getCurrencyManager();
        if (cm == null || plugin.getRankManager() == null) {
            return;
        }
        Set<UUID> players = new HashSet<>(cm.getCachedPlayerUUIDs());
        if (players.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        for (UUID uuid : players) {
            try {
                if (!cm.isPlayerOnline(uuid)) {
                    continue;
                }
                Rank rank = plugin.getRankManager().getPlayerRank(uuid);
                if (rank == null) {
                    continue;
                }
                Rank.InterestSettings interest = rank.getInterestSettings();
                if (!interest.isEnabled() || interest.getRate() <= 0) {
                    continue;
                }
                long last = lastPayout.getOrDefault(uuid, now);
                if (now - last < interest.getIntervalSeconds() * 1000L) {
                    continue;
                }
                double balance = cm.getBalance(uuid, CurrencyType.MONEY);
                double base = interest.getMaxBalance() >= 0
                        ? Math.min(balance, interest.getMaxBalance())
                        : balance;
                if (base <= 0) {
                    lastPayout.put(uuid, now);
                    continue;
                }
                double payout = org.demonz.dev.dzeconomy.util.MoneyUtil.round(
                        base * interest.getRate() / 100.0, cm.getDecimalPlaces(CurrencyType.MONEY));
                if (payout <= 0) {
                    lastPayout.put(uuid, now);
                    continue;
                }
                if (cm.addBalance(uuid, CurrencyType.MONEY, payout)) {
                    lastPayout.put(uuid, now);
                    double newBalance = cm.getBalance(uuid, CurrencyType.MONEY);
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        String symbol = plugin.getConfigManager().getConfig()
                                .getString("currencies.money.symbol", "$");
                        org.demonz.dev.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, player, () ->
                                MessagesUtil.sendMessage(player, "interest-paid",
                                        "%amount%", cm.formatAmount(CurrencyType.MONEY, payout),
                                        "%balance%", cm.formatAmount(CurrencyType.MONEY, newBalance),
                                        "%currency%", "money",
                                        "%symbol%", symbol));
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[Interest] Failed to pay interest to " + uuid + ": " + e.getMessage());
            }
        }
    }
}
