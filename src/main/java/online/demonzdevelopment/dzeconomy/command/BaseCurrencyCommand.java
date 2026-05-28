package online.demonzdevelopment.dzeconomy.command;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.data.CurrencyRequest;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import online.demonzdevelopment.dzeconomy.config.ConfigManager;
import online.demonzdevelopment.dzeconomy.util.MessagesUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class BaseCurrencyCommand implements TabExecutor {

    protected final DZEconomy plugin;
    protected final CurrencyType currencyType;
    protected final String commandName;

    public BaseCurrencyCommand(DZEconomy plugin, CurrencyType currencyType, String commandName) {
        this.plugin = plugin;
        this.currencyType = currencyType;
        this.commandName = commandName;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            handleBalance(sender, new String[0]);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "balance":
            case "bal":
                handleBalance(sender, shiftArgs(args));
                break;
            case "send":
            case "pay":
            case "give":
                handleSend(sender, shiftArgs(args));
                break;
            case "request":
            case "req":
                handleRequest(sender, shiftArgs(args));
                break;
            case "accept":
                handleAccept(sender, shiftArgs(args));
                break;
            case "deny":
            case "reject":
                handleDeny(sender, shiftArgs(args));
                break;
            case "add":
                handleAdd(sender, shiftArgs(args));
                break;
            case "remove":
            case "take":
                handleRemove(sender, shiftArgs(args));
                break;
            case "set":
                handleSet(sender, shiftArgs(args));
                break;
            case "top":
            case "baltop":
                handleTop(sender, shiftArgs(args));
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    // ─── Balance ───────────────────────────────────────────────────────────────

    private void handleBalance(CommandSender sender, String[] args) {
        String perm = "dzeconomy." + commandName + ".balance";
        if (!sender.hasPermission(perm)) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return;
        }

        CurrencyManager cm = plugin.getCurrencyManager();

        if (args.length >= 1) {
            if (!sender.hasPermission("dzeconomy." + commandName + ".balance.others")) {
                MessagesUtil.sendMessage(sender, "no-permission");
                return;
            }

            String targetName = args[0];
            resolveOfflinePlayer(targetName, target -> {
                if (target == null || (!target.hasPlayedBefore() && !target.isOnline() && !cm.playerDataExists(target.getUniqueId()))) {
                    MessagesUtil.sendMessage(sender, "player-not-found", "%player%", targetName);
                    return;
                }

                online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
                    double balance = cm.getBalance(target.getUniqueId(), currencyType);
                    boolean isOnline = target.isOnline();
                    if (!isOnline) {
                        cm.unloadPlayerData(target.getUniqueId());
                    }
                    Runnable notifyTask = () -> {
                        MessagesUtil.sendMessage(sender, commandName + "-balance-other",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%balance%", String.format("%,.2f", balance),
                                "%currency%", commandName);
                    };
                    if (sender instanceof Player) {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, notifyTask);
                    } else {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, notifyTask);
                    }
                });
            });
        } else {
            if (!(sender instanceof Player)) {
                MessagesUtil.sendMessage(sender, "player-only");
                return;
            }
            Player player = (Player) sender;
            double balance = cm.getBalance(player.getUniqueId(), currencyType);
            MessagesUtil.sendMessage(player, commandName + "-balance",
                    "%balance%", String.format("%,.2f", balance),
                    "%currency%", commandName);
        }
    }

    // ─── Send ─────────────────────────────────────────────────────────────────

    private void handleSend(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessagesUtil.sendMessage(sender, "player-only");
            return;
        }
        Player player = (Player) sender;
        
        String perm = "dzeconomy." + commandName + ".send";
        if (!player.hasPermission(perm)) {
            MessagesUtil.sendMessage(player, "no-permission");
            return;
        }

        if (args.length < 2) {
            MessagesUtil.sendMessage(player, "usage-" + commandName + "-send");
            return;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            MessagesUtil.sendMessage(player, "player-not-found", "%player%", targetName);
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            MessagesUtil.sendMessage(player, "cannot-send-self");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            MessagesUtil.sendMessage(player, "invalid-amount", "%input%", args[1]);
            return;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            MessagesUtil.sendMessage(player, "amount-must-be-positive");
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        String currencyPath = "currencies." + commandName;

        // Check max transaction limit
        double maxTransaction = config.getConfig().getDouble(currencyPath + ".max-transaction", -1);
        if (maxTransaction > 0 && amount > maxTransaction) {
            MessagesUtil.sendMessage(player, "max-transaction-exceeded",
                    "%max%", String.format("%,.2f", maxTransaction),
                    "%amount%", String.format("%,.2f", amount),
                    "%currency%", commandName);
            return;
        }

        // Check cooldown
        long cooldownSeconds = config.getConfig().getLong(currencyPath + ".send-cooldown", 0);
        if (cooldownSeconds > 0) {
            CurrencyManager cm = plugin.getCurrencyManager();
            PlayerData data = cm.loadPlayerData(player.getUniqueId());
            if (data != null) {
                long lastSend = data.getLastSendTime(currencyType);
                long elapsed = (System.currentTimeMillis() - lastSend) / 1000;
                if (elapsed < cooldownSeconds) {
                    long remaining = cooldownSeconds - elapsed;
                    MessagesUtil.sendMessage(player, "send-cooldown",
                            "%time%", String.valueOf(remaining),
                            "%currency%", commandName);
                    return;
                }
            }
        }

        // Check combat tag
        CurrencyManager cm = plugin.getCurrencyManager();
        if (cm.isCombatTagged(player.getUniqueId())) {
            MessagesUtil.sendMessage(player, "combat-tagged-send");
            return;
        }

        // Atomic transfer with daily limit
        double dailyLimit = config.getConfig().getDouble(currencyPath + ".daily-limit", -1);
        boolean success = cm.transfer(player.getUniqueId(), target.getUniqueId(), currencyType, amount, dailyLimit);
        if (success) {
            // Update cooldown outside the lock
            cm.executeWithPlayerLock(player.getUniqueId(), () -> {
                PlayerData senderData = cm.loadPlayerData(player.getUniqueId());
                if (senderData != null) {
                    senderData.setLastSendTime(currencyType, System.currentTimeMillis());
                }
            });

            double senderBalance = cm.getBalance(player.getUniqueId(), currencyType);
            double receiverBalance = cm.getBalance(target.getUniqueId(), currencyType);

            MessagesUtil.sendMessage(player, commandName + "-send-success",
                    "%player%", target.getName(),
                    "%amount%", String.format("%,.2f", amount),
                    "%balance%", String.format("%,.2f", senderBalance),
                    "%currency%", commandName);

            MessagesUtil.sendMessage(target, commandName + "-receive",
                    "%player%", player.getName(),
                    "%amount%", String.format("%,.2f", amount),
                    "%balance%", String.format("%,.2f", receiverBalance),
                    "%currency%", commandName);
        } else {
            MessagesUtil.sendMessage(player, commandName + "-send-failed",
                    "%player%", target.getName(),
                    "%amount%", String.format("%,.2f", amount),
                    "%currency%", commandName);
        }
    }

    // ─── Request ──────────────────────────────────────────────────────────────

    private void handleRequest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessagesUtil.sendMessage(sender, "player-only");
            return;
        }
        Player player = (Player) sender;
        
        String perm = "dzeconomy." + commandName + ".request";
        if (!player.hasPermission(perm)) {
            MessagesUtil.sendMessage(player, "no-permission");
            return;
        }

        if (args.length < 2) {
            MessagesUtil.sendMessage(player, "usage-" + commandName + "-request");
            return;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            MessagesUtil.sendMessage(player, "player-not-found", "%player%", targetName);
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            MessagesUtil.sendMessage(player, "cannot-request-self");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            MessagesUtil.sendMessage(player, "invalid-amount", "%input%", args[1]);
            return;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            MessagesUtil.sendMessage(player, "amount-must-be-positive");
            return;
        }

        CurrencyManager cm = plugin.getCurrencyManager();

        // Check if already have pending request with this player
        if (cm.hasPendingRequestWith(player.getUniqueId(), target.getUniqueId())) {
            MessagesUtil.sendMessage(player, "request-already-pending",
                    "%player%", target.getName(),
                    "%currency%", commandName);
            return;
        }

        // Check max pending requests
        ConfigManager config = plugin.getConfigManager();
        int maxRequests = config.getConfig().getInt("request.max-pending", 5);
        if (cm.getPendingRequestCount(player.getUniqueId(), currencyType) >= maxRequests) {
            MessagesUtil.sendMessage(player, "max-requests-reached",
                    "%max%", String.valueOf(maxRequests));
            return;
        }

        long timeoutSeconds = config.getConfig().getLong("request.timeout", 120);
        CurrencyRequest request = new CurrencyRequest(
                player.getUniqueId(),
                target.getUniqueId(),
                currencyType,
                amount,
                System.currentTimeMillis() + (timeoutSeconds * 1000)
        );

        cm.addRequest(request);

        MessagesUtil.sendMessage(player, commandName + "-request-sent",
                "%player%", target.getName(),
                "%amount%", String.format("%,.2f", amount),
                "%currency%", commandName);

        MessagesUtil.sendMessage(target, commandName + "-request-received",
                "%player%", player.getName(),
                "%amount%", String.format("%,.2f", amount),
                "%currency%", commandName);
    }

    // ─── Accept ───────────────────────────────────────────────────────────────

    private void handleAccept(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessagesUtil.sendMessage(sender, "player-only");
            return;
        }
        Player player = (Player) sender;
        
        String perm = "dzeconomy." + commandName + ".accept";
        if (!player.hasPermission(perm)) {
            MessagesUtil.sendMessage(player, "no-permission");
            return;
        }

        if (args.length < 1) {
            MessagesUtil.sendMessage(player, "usage-" + commandName + "-accept");
            return;
        }

        String requesterName = args[0];
        Player requester = Bukkit.getPlayerExact(requesterName);
        if (requester == null) {
            MessagesUtil.sendMessage(player, "player-not-found", "%player%", requesterName);
            return;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        CurrencyRequest request = cm.findRequest(requester.getUniqueId(), player.getUniqueId(), currencyType);

        if (request == null) {
            MessagesUtil.sendMessage(player, "no-request-found",
                    "%player%", requester.getName(),
                    "%currency%", commandName);
            return;
        }

        // Check if request expired
        if (request.isExpired()) {
            cm.removeRequest(player.getUniqueId(), request);
            MessagesUtil.sendMessage(player, "request-expired",
                    "%player%", requester.getName(),
                    "%currency%", commandName);
            return;
        }

        // Check combat tag
        if (cm.isCombatTagged(player.getUniqueId())) {
            MessagesUtil.sendMessage(player, "combat-tagged-request");
            return;
        }

        // Atomic transfer
        double amount = request.getAmount();
        boolean success = cm.transfer(player.getUniqueId(), requester.getUniqueId(), currencyType, amount);

        if (success) {
            cm.removeRequest(player.getUniqueId(), request);

            double senderBalance = cm.getBalance(player.getUniqueId(), currencyType);
            double receiverBalance = cm.getBalance(requester.getUniqueId(), currencyType);

            MessagesUtil.sendMessage(player, commandName + "-accept-sender",
                    "%player%", requester.getName(),
                    "%amount%", String.format("%,.2f", amount),
                    "%balance%", String.format("%,.2f", senderBalance),
                    "%currency%", commandName);

            MessagesUtil.sendMessage(requester, commandName + "-accept-receiver",
                    "%player%", player.getName(),
                    "%amount%", String.format("%,.2f", amount),
                    "%balance%", String.format("%,.2f", receiverBalance),
                    "%currency%", commandName);
        } else {
            MessagesUtil.sendMessage(player, commandName + "-accept-failed",
                    "%player%", requester.getName(),
                    "%amount%", String.format("%,.2f", amount),
                    "%currency%", commandName);
        }
    }

    // ─── Deny ─────────────────────────────────────────────────────────────────

    private void handleDeny(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessagesUtil.sendMessage(sender, "player-only");
            return;
        }
        Player player = (Player) sender;
        
        String perm = "dzeconomy." + commandName + ".deny";
        if (!player.hasPermission(perm)) {
            MessagesUtil.sendMessage(player, "no-permission");
            return;
        }

        if (args.length < 1) {
            MessagesUtil.sendMessage(player, "usage-" + commandName + "-deny");
            return;
        }

        String requesterName = args[0];
        Player requester = Bukkit.getPlayerExact(requesterName);
        if (requester == null) {
            MessagesUtil.sendMessage(player, "player-not-found", "%player%", requesterName);
            return;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        CurrencyRequest request = cm.findRequest(requester.getUniqueId(), player.getUniqueId(), currencyType);

        if (request == null) {
            MessagesUtil.sendMessage(player, "no-request-found",
                    "%player%", requester.getName(),
                    "%currency%", commandName);
            return;
        }

        cm.removeRequest(player.getUniqueId(), request);

        MessagesUtil.sendMessage(player, commandName + "-deny-sender",
                "%player%", requester.getName(),
                "%amount%", String.format("%,.2f", request.getAmount()),
                "%currency%", commandName);

        MessagesUtil.sendMessage(requester, commandName + "-deny-receiver",
                "%player%", player.getName(),
                "%amount%", String.format("%,.2f", request.getAmount()),
                "%currency%", commandName);
    }

    // ─── Admin: Add ───────────────────────────────────────────────────────────

    private void handleAdd(CommandSender sender, String[] args) {
        String perm = "dzeconomy." + commandName + ".add";
        if (!sender.hasPermission(perm)) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            MessagesUtil.sendMessage(sender, "usage-" + commandName + "-add");
            return;
        }

        String targetName = args[0];
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            MessagesUtil.sendMessage(sender, "invalid-amount", "%input%", args[1]);
            return;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            MessagesUtil.sendMessage(sender, "amount-must-be-positive");
            return;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        resolveOfflinePlayer(targetName, target -> {
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline() && !cm.playerDataExists(target.getUniqueId()))) {
                MessagesUtil.sendMessage(sender, "player-not-found", "%player%", targetName);
                return;
            }

            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
                boolean success = cm.addBalance(target.getUniqueId(), currencyType, amount);

                if (success) {
                    double newBalance = cm.getBalance(target.getUniqueId(), currencyType);
                    boolean isOnline = target.isOnline();
                    if (!isOnline) {
                        cm.unloadPlayerData(target.getUniqueId());
                    }
                    Runnable notifyTask = () -> {
                        MessagesUtil.sendMessage(sender, commandName + "-add-success",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%amount%", String.format("%,.2f", amount),
                                "%balance%", String.format("%,.2f", newBalance),
                                "%currency%", commandName);

                        if (isOnline && target instanceof Player) {
                            Player targetPlayer = (Player) target;
                            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, targetPlayer, () -> {
                                MessagesUtil.sendMessage(targetPlayer, commandName + "-added",
                                        "%player%", sender.getName(),
                                        "%amount%", String.format("%,.2f", amount),
                                        "%balance%", String.format("%,.2f", newBalance),
                                        "%currency%", commandName);
                            });
                        }
                    };
                    if (sender instanceof Player) {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, notifyTask);
                    } else {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, notifyTask);
                    }
                } else {
                    Runnable failTask = () -> {
                        MessagesUtil.sendMessage(sender, commandName + "-add-failed",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%amount%", String.format("%,.2f", amount),
                                "%currency%", commandName);
                    };
                    if (sender instanceof Player) {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, failTask);
                    } else {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, failTask);
                    }
                }
            });
        });
    }

    // ─── Admin: Remove ────────────────────────────────────────────────────────

    private void handleRemove(CommandSender sender, String[] args) {
        String perm = "dzeconomy." + commandName + ".remove";
        if (!sender.hasPermission(perm)) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            MessagesUtil.sendMessage(sender, "usage-" + commandName + "-remove");
            return;
        }

        String targetName = args[0];
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            MessagesUtil.sendMessage(sender, "invalid-amount", "%input%", args[1]);
            return;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            MessagesUtil.sendMessage(sender, "amount-must-be-positive");
            return;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        resolveOfflinePlayer(targetName, target -> {
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline() && !cm.playerDataExists(target.getUniqueId()))) {
                MessagesUtil.sendMessage(sender, "player-not-found", "%player%", targetName);
                return;
            }

            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
                boolean success = cm.removeBalance(target.getUniqueId(), currencyType, amount);

                if (success) {
                    double newBalance = cm.getBalance(target.getUniqueId(), currencyType);
                    boolean isOnline = target.isOnline();
                    if (!isOnline) {
                        cm.unloadPlayerData(target.getUniqueId());
                    }
                    Runnable notifyTask = () -> {
                        MessagesUtil.sendMessage(sender, commandName + "-remove-success",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%amount%", String.format("%,.2f", amount),
                                "%balance%", String.format("%,.2f", newBalance),
                                "%currency%", commandName);

                        if (isOnline && target instanceof Player) {
                            Player targetPlayer = (Player) target;
                            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, targetPlayer, () -> {
                                MessagesUtil.sendMessage(targetPlayer, commandName + "-removed",
                                        "%player%", sender.getName(),
                                        "%amount%", String.format("%,.2f", amount),
                                        "%balance%", String.format("%,.2f", newBalance),
                                        "%currency%", commandName);
                            });
                        }
                    };
                    if (sender instanceof Player) {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, notifyTask);
                    } else {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, notifyTask);
                    }
                } else {
                    Runnable failTask = () -> {
                        MessagesUtil.sendMessage(sender, commandName + "-remove-failed",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%amount%", String.format("%,.2f", amount),
                                "%currency%", commandName);
                    };
                    if (sender instanceof Player) {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, failTask);
                    } else {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, failTask);
                    }
                }
            });
        });
    }

    // ─── Admin: Set ───────────────────────────────────────────────────────────

    private void handleSet(CommandSender sender, String[] args) {
        String perm = "dzeconomy." + commandName + ".set";
        if (!sender.hasPermission(perm)) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            MessagesUtil.sendMessage(sender, "usage-" + commandName + "-set");
            return;
        }

        String targetName = args[0];
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            MessagesUtil.sendMessage(sender, "invalid-amount", "%input%", args[1]);
            return;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) {
            MessagesUtil.sendMessage(sender, "amount-must-be-positive");
            return;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        resolveOfflinePlayer(targetName, target -> {
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline() && !cm.playerDataExists(target.getUniqueId()))) {
                MessagesUtil.sendMessage(sender, "player-not-found", "%player%", targetName);
                return;
            }

            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
                boolean success = cm.setBalance(target.getUniqueId(), currencyType, amount);

                if (success) {
                    double newBalance = cm.getBalance(target.getUniqueId(), currencyType);
                    boolean isOnline = target.isOnline();
                    if (!isOnline) {
                        cm.unloadPlayerData(target.getUniqueId());
                    }
                    Runnable notifyTask = () -> {
                        MessagesUtil.sendMessage(sender, commandName + "-set-success",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%amount%", String.format("%,.2f", amount),
                                "%balance%", String.format("%,.2f", newBalance),
                                "%currency%", commandName);

                        if (isOnline && target instanceof Player) {
                            Player targetPlayer = (Player) target;
                            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, targetPlayer, () -> {
                                MessagesUtil.sendMessage(targetPlayer, commandName + "-set",
                                        "%player%", sender.getName(),
                                        "%amount%", String.format("%,.2f", amount),
                                        "%balance%", String.format("%,.2f", newBalance),
                                        "%currency%", commandName);
                            });
                        }
                    };
                    if (sender instanceof Player) {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, notifyTask);
                    } else {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, notifyTask);
                    }
                } else {
                    Runnable failTask = () -> {
                        MessagesUtil.sendMessage(sender, commandName + "-set-failed",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%amount%", String.format("%,.2f", amount),
                                "%currency%", commandName);
                    };
                    if (sender instanceof Player) {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, failTask);
                    } else {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, failTask);
                    }
                }
            });
        });
    }

    /**
     * Non-blocking resolution of OfflinePlayer objects for online or offline players.
     */
    protected void resolveOfflinePlayer(String name, java.util.function.Consumer<org.bukkit.OfflinePlayer> callback) {
        org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayerExact(name);
        if (onlinePlayer != null) {
            callback.accept(onlinePlayer);
            return;
        }

        boolean onlineMode = Bukkit.getOnlineMode();
        boolean bungeecord = false;
        try {
            Class<?> spigotConfigClass = Class.forName("org.spigotmc.SpigotConfig");
            java.lang.reflect.Field bungeeField = spigotConfigClass.getField("bungee");
            bungeecord = bungeeField.getBoolean(null);
        } catch (Exception ignored) {
            try {
                bungeecord = Bukkit.spigot().getSpigotConfig().getBoolean("settings.bungeecord", false);
            } catch (Exception ignored2) {}
        }

        if (!onlineMode && !bungeecord) {
            java.util.UUID offlineUuid = java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offlineUuid);
            callback.accept(offlinePlayer);
            return;
        }

        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer resolved = Bukkit.getOfflinePlayer(name);
            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, () -> callback.accept(resolved));
        });
    }

    // ─── Top ──────────────────────────────────────────────────────────────────

    private void handleTop(CommandSender sender, String[] args) {
        String perm = "dzeconomy." + commandName + ".top";
        if (!sender.hasPermission(perm)) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return;
        }

        int pageRaw = 1;
        if (args.length >= 1) {
            try {
                pageRaw = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                MessagesUtil.sendMessage(sender, "invalid-page", "%input%", args[0]);
                return;
            }
        }

        final int page = Math.max(pageRaw, 1);
        int perPage = 10;
        
        CurrencyManager cm = plugin.getCurrencyManager();
        
        cm.getBalanceTopAsync(currencyType, page * perPage).thenAccept(top -> {
            List<Map.Entry<String, Double>> resolvedTop = new ArrayList<>();
            int start = (page - 1) * perPage;
            if (!top.isEmpty()) {
                for (int i = start; i < Math.min(top.size(), start + perPage); i++) {
                    Map.Entry<UUID, Double> entry = top.get(i);
                    String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                    if (name == null) name = entry.getKey().toString().substring(0, 8) + "...";
                    resolvedTop.add(new java.util.AbstractMap.SimpleEntry<>(name, entry.getValue()));
                }
            }

            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, () -> {
                String separator = MessagesUtil.colorize("&8&l&m─────────────────────────────────");
                String currencyDisplayName = commandName.substring(0, 1).toUpperCase() + commandName.substring(1);

                sender.sendMessage(separator);
                sender.sendMessage(MessagesUtil.colorize("&6&l  " + currencyDisplayName + " Top &8▸ &7Page " + page));

                if (top.isEmpty()) {
                    sender.sendMessage(MessagesUtil.colorize("  &7No data available."));
                } else {
                    int rank = start + 1;
                    for (Map.Entry<String, Double> entry : resolvedTop) {
                        String rankColor = rank == 1 ? "&6" : rank == 2 ? "&7" : rank == 3 ? "&c" : "&e";
                        sender.sendMessage(MessagesUtil.colorize("  " + rankColor + rank + ". &8▸ &7" + entry.getKey() + " &8- &a" + String.format("%,.2f", entry.getValue())));
                        rank++;
                    }
                }

                sender.sendMessage(separator);
            });
        });
    }

    // ─── Help ─────────────────────────────────────────────────────────────────

    private void sendHelp(CommandSender sender) {
        String separator = MessagesUtil.colorize("&8&l&m─────────────────────────────────");
        String currencyDisplayName = commandName.substring(0, 1).toUpperCase() + commandName.substring(1);

        sender.sendMessage(separator);
        sender.sendMessage(MessagesUtil.colorize("&6&l  " + currencyDisplayName + " Commands"));
        sender.sendMessage(MessagesUtil.colorize(""));

        if (sender.hasPermission("dzeconomy." + commandName + ".balance")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/" + commandName + " balance [player] &8- &7Check balance"));
        }
        if (sender.hasPermission("dzeconomy." + commandName + ".send")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/" + commandName + " send <player> <amount> &8- &7Send " + commandName));
        }
        if (sender.hasPermission("dzeconomy." + commandName + ".request")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/" + commandName + " request <player> <amount> &8- &7Request " + commandName));
        }
        if (sender.hasPermission("dzeconomy." + commandName + ".accept")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/" + commandName + " accept <player> &8- &7Accept request"));
        }
        if (sender.hasPermission("dzeconomy." + commandName + ".deny")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/" + commandName + " deny <player> &8- &7Deny request"));
        }
        if (sender.hasPermission("dzeconomy." + commandName + ".top")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/" + commandName + " top [page] &8- &7Balance leaderboard"));
        }
        if (sender.hasPermission("dzeconomy." + commandName + ".add")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/" + commandName + " add <player> <amount> &8- &7Add " + commandName));
        }
        if (sender.hasPermission("dzeconomy." + commandName + ".remove")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/" + commandName + " remove <player> <amount> &8- &7Remove " + commandName));
        }
        if (sender.hasPermission("dzeconomy." + commandName + ".set")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/" + commandName + " set <player> <amount> &8- &7Set " + commandName));
        }

        sender.sendMessage(separator);
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

    private String[] shiftArgs(String[] args) {
        if (args.length <= 1) return new String[0];
        String[] shifted = new String[args.length - 1];
        System.arraycopy(args, 1, shifted, 0, shifted.length);
        return shifted;
    }

    // ─── Tab Completion ───────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("dzeconomy." + commandName + ".balance")) subs.add("balance");
            if (sender.hasPermission("dzeconomy." + commandName + ".send")) subs.add("send");
            if (sender.hasPermission("dzeconomy." + commandName + ".request")) subs.add("request");
            if (sender.hasPermission("dzeconomy." + commandName + ".accept")) subs.add("accept");
            if (sender.hasPermission("dzeconomy." + commandName + ".deny")) subs.add("deny");
            if (sender.hasPermission("dzeconomy." + commandName + ".add")) subs.add("add");
            if (sender.hasPermission("dzeconomy." + commandName + ".remove")) subs.add("remove");
            if (sender.hasPermission("dzeconomy." + commandName + ".set")) subs.add("set");
            if (sender.hasPermission("dzeconomy." + commandName + ".top")) subs.add("top");

            String input = args[0].toLowerCase();
            for (String sub : subs) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "balance":
                case "bal":
                case "send":
                case "pay":
                case "give":
                case "request":
                case "req":
                case "accept":
                case "deny":
                case "reject":
                case "add":
                case "remove":
                case "take":
                case "set":
                    boolean hasPerm = false;
                    switch (sub) {
                        case "balance": case "bal":
                            hasPerm = sender.hasPermission("dzeconomy." + commandName + ".balance.others");
                            break;
                        case "send": case "pay": case "give":
                            hasPerm = sender.hasPermission("dzeconomy." + commandName + ".send");
                            break;
                        case "request": case "req":
                            hasPerm = sender.hasPermission("dzeconomy." + commandName + ".request");
                            break;
                        case "accept":
                            hasPerm = sender.hasPermission("dzeconomy." + commandName + ".accept");
                            break;
                        case "deny": case "reject":
                            hasPerm = sender.hasPermission("dzeconomy." + commandName + ".deny");
                            break;
                        case "add":
                            hasPerm = sender.hasPermission("dzeconomy." + commandName + ".add");
                            break;
                        case "remove": case "take":
                            hasPerm = sender.hasPermission("dzeconomy." + commandName + ".remove");
                            break;
                        case "set":
                            hasPerm = sender.hasPermission("dzeconomy." + commandName + ".set");
                            break;
                    }
                    if (hasPerm) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (sender instanceof Player && !((Player) sender).canSee(p)) {
                                continue;
                            }
                            if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(p.getName());
                            }
                        }
                    }
                    break;
                case "top":
                case "baltop":
                    if (sender.hasPermission("dzeconomy." + commandName + ".top")) {
                        completions.add("1");
                        completions.add("2");
                        completions.add("3");
                    }
                    break;
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            boolean hasPerm = false;
            switch (sub) {
                case "send":
                case "pay":
                case "give":
                    hasPerm = sender.hasPermission("dzeconomy." + commandName + ".send");
                    break;
                case "request":
                case "req":
                    hasPerm = sender.hasPermission("dzeconomy." + commandName + ".request");
                    break;
                case "add":
                    hasPerm = sender.hasPermission("dzeconomy." + commandName + ".add");
                    break;
                case "remove":
                case "take":
                    hasPerm = sender.hasPermission("dzeconomy." + commandName + ".remove");
                    break;
                case "set":
                    hasPerm = sender.hasPermission("dzeconomy." + commandName + ".set");
                    break;
            }
            if (hasPerm) {
                completions.add("100");
                completions.add("500");
                completions.add("1000");
            }
        }

        return completions;
    }
}
