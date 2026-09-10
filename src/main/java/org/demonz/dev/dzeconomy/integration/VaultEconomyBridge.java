package org.demonz.dev.dzeconomy.integration;

import org.demonz.dev.dzeconomy.DZEconomy;
import org.demonz.dev.dzeconomy.currency.CurrencyType;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * Exposes the MONEY currency through Vault's Economy service so shop,
 * jobs and rank plugins can hook into DZEconomy transparently.
 */
public class VaultEconomyBridge implements Economy {

    private static final String BANK_NOT_SUPPORTED = "DZEconomy does not support bank accounts";

    private final DZEconomy plugin;

    public VaultEconomyBridge(DZEconomy plugin) {
        this.plugin = plugin;
    }

    private DZEconomy economy() {
        DZEconomy active = DZEconomy.getInstance();
        if (active == null) {
            throw new IllegalStateException("DZEconomy is currently disabled!");
        }
        return active;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled() && DZEconomy.getInstance() != null;
    }

    @Override
    public String getName() {
        return "DZEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    private boolean moneyEnabled() {
        return economy().getCurrencyManager().isCurrencyEnabled(CurrencyType.MONEY);
    }

    private int moneyScale() {
        return economy().getCurrencyManager().getDecimalPlaces(CurrencyType.MONEY);
    }

    @Override
    public int fractionalDigits() {
        try {
            return Math.max(0, moneyScale());
        } catch (IllegalStateException e) {
            return 2;
        }
    }

    @Override
    public String format(double amount) {
        String symbol = economy().getConfigManager().getConfig().getString("currencies.money.symbol", "$");
        return symbol + org.demonz.dev.dzeconomy.util.MoneyUtil.formatAmount(amount, moneyScale());
    }

    @Override
    public String currencyNamePlural() {
        return "Money";
    }

    @Override
    public String currencyNameSingular() {
        return "Money";
    }

    @Override
    public boolean hasAccount(String playerName) {
        if (playerName == null) return false;
        return hasAccount(economy().getServer().getOfflinePlayer(playerName));
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        if (player == null || !moneyEnabled()) return false;
        if (player.isOnline()) return true;
        Double peeked = economy().getCurrencyManager().peekBalance(player.getUniqueId(), CurrencyType.MONEY);
        if (peeked != null) return true;
        try {
            return player.hasPlayedBefore();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        if (playerName == null) return 0.0;
        return getBalance(economy().getServer().getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player == null || !moneyEnabled()) return 0.0;
        // Never materialize records for names that never had an account.
        Double peeked = economy().getCurrencyManager().peekBalance(player.getUniqueId(), CurrencyType.MONEY);
        return peeked != null ? peeked : 0.0;
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return economy().getCurrencyManager().hasBalance(player.getUniqueId(), CurrencyType.MONEY, amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(economy().getServer().getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Invalid withdrawal amount");
        }
        if (!moneyEnabled()) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Money currency is disabled");
        }
        if (!hasAccount(player)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account not found");
        }
        boolean success = economy().getCurrencyManager().removeBalance(player.getUniqueId(), CurrencyType.MONEY, amount);
        if (!success) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(economy().getServer().getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Invalid deposit amount");
        }
        if (!moneyEnabled()) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Money currency is disabled");
        }
        if (player == null || !hasAccount(player)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account not found");
        }
        boolean success = economy().getCurrencyManager().addBalance(player.getUniqueId(), CurrencyType.MONEY, amount);
        if (!success) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Deposit failed");
        }
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        if (playerName == null) return false;
        return createPlayerAccount(economy().getServer().getOfflinePlayer(playerName));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (player == null || !moneyEnabled()) return false;
        try {
            // Explicit account creation: materialize the record so later
            // deposits succeed even for players who never joined.
            economy().getCurrencyManager().loadPlayerData(player.getUniqueId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    public Plugin getOwningPlugin() {
        return plugin;
    }
}
