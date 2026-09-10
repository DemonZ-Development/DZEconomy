package org.demonz.dev.dzeconomy.command;

import org.demonz.dev.dzeconomy.DZEconomy;
import org.demonz.dev.dzeconomy.adapter.FeatureAdapter;
import org.demonz.dev.dzeconomy.currency.CurrencyManager;
import org.demonz.dev.dzeconomy.currency.CurrencyType;
import org.demonz.dev.dzeconomy.config.ConfigManager;
import org.demonz.dev.dzeconomy.util.MessagesUtil;
import org.demonz.dev.dzeconomy.util.MoneyUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class EconomyCommand implements TabExecutor {

    private final DZEconomy plugin;

    public EconomyCommand(DZEconomy plugin) {
        this.plugin = plugin;
    }

    private String fmt(CurrencyType type, double amount) {
        return plugin.getCurrencyManager().formatAmount(type, amount);
    }

    private boolean requireCurrencyEnabled(CommandSender sender, CurrencyType... types) {
        for (CurrencyType type : types) {
            if (type != null && !plugin.getCurrencyManager().isCurrencyEnabled(type)) {
                MessagesUtil.sendMessage(sender, "currency-disabled",
                        "%currency%", type.name().toLowerCase());
                return false;
            }
        }
        return true;
    }

    private String activeStorageName() {
        org.demonz.dev.dzeconomy.storage.StorageProvider provider = plugin.getStorageProvider();
        if (provider == null) return "none";
        String simple = provider.getClass().getSimpleName().toLowerCase();
        if (simple.contains("mysql")) return "mysql";
        if (simple.contains("sqlite")) return "sqlite";
        if (simple.contains("flatfile")) return "flatfile";
        return simple;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "info":
                return handleInfo(sender);
            case "credits":
                return handleCredits(sender);
            case "reload":
                return handleReload(sender);
            case "version":
                return handleVersion(sender);
            case "status":
                return handleStatus(sender);
            case "convert":
                return handleConvert(sender, args);
            case "migrate":
                return handleMigrate(sender, args);
            case "baltop":
                return handleBaltop(sender, args);
            case "payall":
                return handlePayall(sender, args);
            case "give":
                return handleGive(sender, args);
            case "backup":
                return handleBackup(sender);
            default:
                MessagesUtil.sendMessage(sender, "unknown-subcommand", "%subcommand%", sub);
                return true;
        }
    }

    private boolean handleInfo(CommandSender sender) {
        if (sender instanceof Player && !sender.hasPermission("dzeconomy.economy.info")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        ConfigManager config = plugin.getConfigManager();
        String separator = MessagesUtil.colorize("&8&l&m─────────────────────────────────");
        sender.sendMessage(separator);
        sender.sendMessage(MessagesUtil.colorize("&6&l  DZEconomy &7v" + plugin.getDescription().getVersion()));
        sender.sendMessage(MessagesUtil.colorize(""));
        sender.sendMessage(MessagesUtil.colorize("  &eCurrencies&8:"));
        for (CurrencyType type : CurrencyType.values()) {
            String name = type.name().toLowerCase();
            boolean enabled = config.getConfig().getBoolean("currencies." + name + ".enabled", true);
            String symbol = config.getConfig().getString("currencies." + name + ".symbol", "$");
            sender.sendMessage(MessagesUtil.colorize("    &8▸ &7" + name + " &8- " + (enabled ? "&aEnabled" : "&cDisabled") + " &8(" + symbol + ")"));
        }
        sender.sendMessage(MessagesUtil.colorize(""));
        sender.sendMessage(MessagesUtil.colorize("  &eStorage&8: &7" + config.getConfig().getString("storage.type", "sqlite")));
        sender.sendMessage(MessagesUtil.colorize("  &eLanguage&8: &7" + config.getConfig().getString("language", "en")));
        sender.sendMessage(separator);
        return true;
    }

    private boolean handleCredits(CommandSender sender) {
        String separator = MessagesUtil.colorize("&8&l&m─────────────────────────────────");
        sender.sendMessage(separator);
        sender.sendMessage(MessagesUtil.colorize("&6&l  DZEconomy &7Credits"));
        sender.sendMessage(MessagesUtil.colorize(""));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eAuthor&8: &7DemonzDevelopment"));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eVersion&8: &7" + plugin.getDescription().getVersion()));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eWebsite&8: &7demonzdevelopment.online"));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eContributors&8: &7The community"));
        sender.sendMessage(MessagesUtil.colorize(""));
        sender.sendMessage(MessagesUtil.colorize("  &7Thank you for using DZEconomy!"));
        sender.sendMessage(separator);
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("dzeconomy.admin.reload")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        try {
            plugin.getConfigManager().reload();
            plugin.getRankManager().reloadRanks();
            plugin.getCombatTagManager().reload();
            if (plugin.getEntityDeathListener() != null) {
                plugin.getEntityDeathListener().reload();
            }
            plugin.reloadTasks();
            if (plugin.getPlaceholderExpansion() != null) {
                plugin.getPlaceholderExpansion().clearCache();
            }
            String configured = plugin.getConfigManager().getConfig().getString("storage.type", "sqlite");
            String active = activeStorageName();
            if (configured != null && !configured.equalsIgnoreCase(active)) {
                sender.sendMessage(MessagesUtil.colorize("&e[DZEconomy] &7storage.type is &e" + configured
                        + " &7but the active backend is &e" + active
                        + "&7. Use &e/economy migrate &7to switch backends; a reload alone does not move data."));
            }
            MessagesUtil.sendMessage(sender, "reload-success");
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Reload failed", e);
            MessagesUtil.sendMessage(sender, "reload-failed");
        }
        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        if (!sender.hasPermission("dzeconomy.admin.version")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        String currentVersion = plugin.getDescription().getVersion();
        String separator = MessagesUtil.colorize("&8&l&m─────────────────────────────────");
        sender.sendMessage(separator);
        sender.sendMessage(MessagesUtil.colorize("&6&l  DZEconomy Version Info"));
        sender.sendMessage(MessagesUtil.colorize(""));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eInstalled&8: &7v" + currentVersion));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eServer&8: &7" + Bukkit.getVersion()));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eBukkit API&8: &7" + Bukkit.getBukkitVersion()));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eJava&8: &7" + System.getProperty("java.version")));
        sender.sendMessage(separator);
        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        if (!sender.hasPermission("dzeconomy.admin.status")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        ConfigManager config = plugin.getConfigManager();
        String storageType = activeStorageName();
        String configuredType = config.getConfig().getString("storage.type", "sqlite");
        int cachedPlayers = cm.getCachedPlayerCount();
        int onlinePlayers = FeatureAdapter.get().getOnlinePlayers().size();
        long uptimeMillis = System.currentTimeMillis() - plugin.getStartupTime();
        long uptimeSeconds = uptimeMillis / 1000;
        long uptimeMinutes = uptimeSeconds / 60;
        long uptimeHours = uptimeMinutes / 60;

        String separator = MessagesUtil.colorize("&8&l&m─────────────────────────────────");
        sender.sendMessage(separator);
        sender.sendMessage(MessagesUtil.colorize("&6&l  DZEconomy Status"));
        sender.sendMessage(MessagesUtil.colorize(""));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &ePlugin&8: &aRunning"));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eVersion&8: &7v" + plugin.getDescription().getVersion()));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eStorage&8: &7" + storageType
                + (configuredType != null && !configuredType.equalsIgnoreCase(storageType)
                        ? " &8(config: " + configuredType + ")" : "")));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eCached Players&8: &7" + cachedPlayers));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eOnline Players&8: &7" + onlinePlayers));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eUptime&8: &7" + uptimeHours + "h " + (uptimeMinutes % 60) + "m " + (uptimeSeconds % 60) + "s"));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &ePending Requests&8: &7" + cm.getPendingRequestCount()));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eCombat Tags&8: &7" + cm.getActiveCombatTagCount()));

        org.demonz.dev.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
            List<String> totalLines = new ArrayList<>();
            for (CurrencyType type : CurrencyType.values()) {
                double total = cm.getTotalCurrency(type);
                totalLines.add(MessagesUtil.colorize("  &8▸ &eTotal " + type.name().toLowerCase() + "&8: &7"
                        + fmt(type, total)));
            }
            Runnable sendTotals = () -> {
                for (String line : totalLines) {
                    sender.sendMessage(line);
                }
                sender.sendMessage(separator);
            };
            if (sender instanceof Player) {
                org.demonz.dev.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, sendTotals);
            } else {
                org.demonz.dev.dzeconomy.util.FoliaAdapter.runTask(plugin, sendTotals);
            }
        });
        return true;
    }

    private boolean handleBackup(CommandSender sender) {
        if (!sender.hasPermission("dzeconomy.admin.backup")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        sender.sendMessage(MessagesUtil.colorize("&a[DZEconomy] &7Creating backup..."));
        org.demonz.dev.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
            boolean ok = plugin.getMigrationManager().createBackup();
            Runnable notifyTask = () -> sender.sendMessage(MessagesUtil.colorize(ok
                    ? "&a[DZEconomy] &7Backup created successfully."
                    : "&c[DZEconomy] &7Backup failed - check console for details."));
            if (sender instanceof Player) {
                org.demonz.dev.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, notifyTask);
            } else {
                org.demonz.dev.dzeconomy.util.FoliaAdapter.runTask(plugin, notifyTask);
            }
        });
        return true;
    }

    private boolean handleConvert(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dzeconomy.admin.convert")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 5) {
            MessagesUtil.sendMessage(sender, "usage-economy-convert");
            return true;
        }

        String targetName = args[1];
        CurrencyType fromType = parseCurrencyType(args[2]);
        CurrencyType toType = parseCurrencyType(args[3]);
        if (fromType == null || toType == null) {
            MessagesUtil.sendMessage(sender, "invalid-currency-type");
            return true;
        }
        if (fromType == toType) {
            MessagesUtil.sendMessage(sender, "same-currency-type");
            return true;
        }
        if (!requireCurrencyEnabled(sender, fromType, toType)) {
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[4]);
        } catch (NumberFormatException e) {
            MessagesUtil.sendMessage(sender, "invalid-amount", "%input%", args[4]);
            return true;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            MessagesUtil.sendMessage(sender, "amount-must-be-positive");
            return true;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        resolveOfflinePlayer(targetName, target -> {
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline() && !cm.playerDataExists(target.getUniqueId()))) {
                MessagesUtil.sendMessage(sender, "player-not-found", "%player%", targetName);
                return;
            }

            org.demonz.dev.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
                UUID uuid = target.getUniqueId();
                double oldToBalance = cm.getBalance(uuid, toType);
                boolean success = cm.convert(uuid, fromType, toType, amount);
                if (success) {
                    boolean isOnline = target.isOnline();
                    if (!isOnline) {
                        cm.unloadPlayerData(uuid);
                    }
                    double newFromBalance = cm.getBalance(uuid, fromType);
                    double newToBalance = cm.getBalance(uuid, toType);
                    double received = MoneyUtil.subtract(newToBalance, oldToBalance, cm.getDecimalPlaces(toType));
                    Runnable notifyTask = () -> {
                        MessagesUtil.sendMessage(sender, "convert-success",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%from%", fromType.name().toLowerCase(),
                                "%to%", toType.name().toLowerCase(),
                                "%amount%", fmt(fromType, amount),
                                "%to_amount%", fmt(toType, received),
                                "%from_balance%", fmt(fromType, newFromBalance),
                                "%to_balance%", fmt(toType, newToBalance));
                    };
                    if (sender instanceof Player) {
                        org.demonz.dev.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, notifyTask);
                    } else {
                        org.demonz.dev.dzeconomy.util.FoliaAdapter.runTask(plugin, notifyTask);
                    }
                } else {
                    Runnable notifyTask = () -> {
                        MessagesUtil.sendMessage(sender, "convert-failed",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%from%", fromType.name().toLowerCase(),
                                "%to%", toType.name().toLowerCase(),
                                "%amount%", fmt(fromType, amount));
                    };
                    if (sender instanceof Player) {
                        org.demonz.dev.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, notifyTask);
                    } else {
                        org.demonz.dev.dzeconomy.util.FoliaAdapter.runTask(plugin, notifyTask);
                    }
                }
            });
        });
        return true;
    }

    private void resolveOfflinePlayer(String name, java.util.function.Consumer<org.bukkit.OfflinePlayer> callback) {
        org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayerExact(name);
        if (onlinePlayer != null) {
            callback.accept(onlinePlayer);
            return;
        }

        boolean onlineMode = Bukkit.getOnlineMode();
        boolean bungeecord = FeatureAdapter.get().isBungeeCord();

        if (!onlineMode && !bungeecord) {
            java.util.UUID offlineUuid = java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offlineUuid);
            callback.accept(offlinePlayer);
            return;
        }

        org.demonz.dev.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer resolved = Bukkit.getOfflinePlayer(name);
            org.demonz.dev.dzeconomy.util.FoliaAdapter.runTask(plugin, () -> callback.accept(resolved));
        });
    }

    private boolean handleMigrate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dzeconomy.admin.migrate")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 3) {
            MessagesUtil.sendMessage(sender, "usage-economy-migrate");
            return true;
        }

        String fromStorage = args[1].toLowerCase();
        String toStorage = args[2].toLowerCase();

        if (fromStorage.equals(toStorage)) {
            MessagesUtil.sendMessage(sender, "migrate-same-storage");
            return true;
        }

        List<String> validStorages = Arrays.asList("sqlite", "mysql", "flatfile", "yaml");
        if (!validStorages.contains(fromStorage) || !validStorages.contains(toStorage)) {
            MessagesUtil.sendMessage(sender, "migrate-invalid-storage",
                    "%from%", fromStorage,
                    "%to%", toStorage);
            return true;
        }

        MessagesUtil.sendMessage(sender, "migrate-start",
                "%from%", fromStorage,
                "%to%", toStorage);

        org.demonz.dev.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
            plugin.getMigrationManager().migrate(fromStorage, toStorage, sender);
        });
        return true;
    }

    private boolean handleBaltop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dzeconomy.admin.baltop")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }
        CurrencyType type = CurrencyType.MONEY;
        int page = 1;

        if (args.length >= 2) {
            CurrencyType parsed = parseCurrencyType(args[1]);
            if (parsed != null) {
                type = parsed;
            } else {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    MessagesUtil.sendMessage(sender, "invalid-currency-or-page", "%input%", args[1]);
                    return true;
                }
            }
        }

        if (args.length >= 3) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                MessagesUtil.sendMessage(sender, "invalid-page", "%input%", args[2]);
                return true;
            }
        }

        if (page < 1) page = 1;
        if (page > 100) page = 100;

        if (!requireCurrencyEnabled(sender, type)) {
            return true;
        }

        final int finalPage = page;
        final CurrencyType finalType = type;
        int perPage = 10;
        CurrencyManager cm = plugin.getCurrencyManager();

        final String finalCurrencyName = type.name().toLowerCase();
        cm.getBalanceTopAsync(type, finalPage * perPage).thenAccept(top -> {
            List<Map.Entry<String, Double>> resolvedTop = new ArrayList<>();
            int start = (finalPage - 1) * perPage;
            for (int i = start; i < Math.min(top.size(), start + perPage); i++) {
                Map.Entry<UUID, Double> entry = top.get(i);
                String name = resolvePlayerName(entry.getKey());
                resolvedTop.add(new java.util.AbstractMap.SimpleEntry<>(name, entry.getValue()));
            }

            Runnable sendBaltopTask = () -> {
                String separator = MessagesUtil.colorize("&8&l&m─────────────────────────────────");

                sender.sendMessage(separator);
                sender.sendMessage(MessagesUtil.colorize("&6&l  Balance Top &8- &e" + finalCurrencyName.substring(0, 1).toUpperCase() + finalCurrencyName.substring(1) + " &8▸ &7Page " + finalPage));

                if (top.isEmpty()) {
                    sender.sendMessage(MessagesUtil.colorize("  &7No data available."));
                } else {
                    int rank = start + 1;
                    for (Map.Entry<String, Double> entry : resolvedTop) {
                        String rankColor = rank == 1 ? "&6" : rank == 2 ? "&7" : rank == 3 ? "&c" : "&e";
                        sender.sendMessage(MessagesUtil.colorize("  " + rankColor + rank + ". &8▸ &7" + entry.getKey() + " &8- &a" + fmt(finalType, entry.getValue())));
                        rank++;
                    }
                }

                sender.sendMessage(separator);
            };

            if (sender instanceof Player) {
                org.demonz.dev.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, sendBaltopTask);
            } else {
                org.demonz.dev.dzeconomy.util.FoliaAdapter.runTask(plugin, sendBaltopTask);
            }
        });

        return true;
    }

    private String resolvePlayerName(UUID uuid) {
        org.demonz.dev.dzeconomy.data.PlayerData cachedData =
                plugin.getCurrencyManager().getPlayerData(uuid);
        String cachedName = cachedData != null ? cachedData.getUsername() : null;
        if (cachedName != null && !cachedName.equals(uuid.toString())) {
            return cachedName;
        }
        @SuppressWarnings("deprecation")
        String offlineName = Bukkit.getOfflinePlayer(uuid).getName();
        if (offlineName != null) return offlineName;
        return uuid.toString().substring(0, 8) + "...";
    }

    private boolean handlePayall(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dzeconomy.admin.payall")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 3) {
            MessagesUtil.sendMessage(sender, "usage-economy-payall");
            return true;
        }

        CurrencyType type = parseCurrencyType(args[1]);
        if (type == null) {
            MessagesUtil.sendMessage(sender, "invalid-currency-type");
            return true;
        }
        if (!requireCurrencyEnabled(sender, type)) {
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            MessagesUtil.sendMessage(sender, "invalid-amount", "%input%", args[2]);
            return true;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            MessagesUtil.sendMessage(sender, "amount-must-be-positive");
            return true;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        final int[] count = {0};
        String symbol = plugin.getConfigManager().getConfig().getString("currencies." + type.name().toLowerCase() + ".symbol", "$");

        for (Player player : FeatureAdapter.get().getOnlinePlayers()) {
            boolean success = cm.addBalance(player.getUniqueId(), type, amount);
            if (success) {
                count[0]++;
                org.demonz.dev.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, player, () ->
                        MessagesUtil.sendMessage(player, "payall-received",
                                "%currency%", type.name().toLowerCase(),
                                "%amount%", fmt(type, amount),
                                "%symbol%", symbol));
            }
        }

        MessagesUtil.sendMessage(sender, "payall-success",
                "%count%", String.valueOf(count[0]),
                "%currency%", type.name().toLowerCase(),
                "%amount%", fmt(type, amount),
                "%symbol%", symbol);
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dzeconomy.admin.give")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 3) {
            MessagesUtil.sendMessage(sender, "usage-economy-give");
            return true;
        }

        String targetName = args[1];
        CurrencyType type = CurrencyType.MONEY;
        if (args.length >= 4) {
            type = parseCurrencyType(args[3]);
            if (type == null) {
                MessagesUtil.sendMessage(sender, "invalid-currency-type");
                return true;
            }
        }
        if (!requireCurrencyEnabled(sender, type)) {
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            MessagesUtil.sendMessage(sender, "invalid-amount", "%input%", args[2]);
            return true;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            MessagesUtil.sendMessage(sender, "amount-must-be-positive");
            return true;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        final CurrencyType finalType = type;
        resolveOfflinePlayer(targetName, target -> {
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline() && !cm.playerDataExists(target.getUniqueId()))) {
                MessagesUtil.sendMessage(sender, "player-not-found", "%player%", targetName);
                return;
            }

            org.demonz.dev.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
                boolean success = cm.addBalance(target.getUniqueId(), finalType, amount);

                if (success) {
                    double newBalance = cm.getBalance(target.getUniqueId(), finalType);
                    boolean isOnline = target.isOnline();
                    if (!isOnline) {
                        cm.unloadPlayerData(target.getUniqueId());
                    }
                    String currencyName = finalType.name().toLowerCase();
                    String symbol = plugin.getConfigManager().getConfig().getString("currencies." + currencyName + ".symbol", "$");
                    Runnable notifyTask = () -> {
                        MessagesUtil.sendMessage(sender, "give-success",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%amount%", fmt(finalType, amount),
                                "%balance%", fmt(finalType, newBalance),
                                "%currency%", currencyName,
                                "%symbol%", symbol);

                        if (isOnline && target instanceof Player) {
                            Player targetPlayer = (Player) target;
                            org.demonz.dev.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, targetPlayer, () -> {
                                MessagesUtil.sendMessage(targetPlayer, "give-target",
                                        "%player%", sender.getName(),
                                        "%amount%", fmt(finalType, amount),
                                        "%balance%", fmt(finalType, newBalance),
                                        "%currency%", currencyName,
                                        "%symbol%", symbol);
                            });
                        }
                    };
                    if (sender instanceof Player) {
                        org.demonz.dev.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, notifyTask);
                    } else {
                        org.demonz.dev.dzeconomy.util.FoliaAdapter.runTask(plugin, notifyTask);
                    }
                } else {
                    Runnable failTask = () -> {
                        MessagesUtil.sendMessage(sender, "give-failed",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%amount%", fmt(finalType, amount),
                                "%currency%", finalType.name().toLowerCase());
                    };
                    if (sender instanceof Player) {
                        org.demonz.dev.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, failTask);
                    } else {
                        org.demonz.dev.dzeconomy.util.FoliaAdapter.runTask(plugin, failTask);
                    }
                }
            });
        });
        return true;
    }

    private void sendHelp(CommandSender sender) {
        String separator = MessagesUtil.colorize("&8&l&m─────────────────────────────────");
        sender.sendMessage(separator);
        sender.sendMessage(MessagesUtil.colorize("&6&l  DZEconomy Commands"));
        sender.sendMessage(MessagesUtil.colorize(""));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy info &8- &7View plugin information"));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy credits &8- &7View credits"));
        if (sender.hasPermission("dzeconomy.admin.reload")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy reload &8- &7Reload configuration"));
        }
        if (sender.hasPermission("dzeconomy.admin.version")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy version &8- &7Version information"));
        }
        if (sender.hasPermission("dzeconomy.admin.status")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy status &8- &7Plugin status"));
        }
        if (sender.hasPermission("dzeconomy.admin.convert")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy convert <player> <from> <to> <amount> &8- &7Convert currency"));
        }
        if (sender.hasPermission("dzeconomy.admin.migrate")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy migrate <from> <to> &8- &7Migrate storage"));
        }
        if (sender.hasPermission("dzeconomy.admin.backup")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy backup &8- &7Create a data backup"));
        }
        if (sender.hasPermission("dzeconomy.admin.baltop")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy baltop [currency] [page] &8- &7Balance leaderboard"));
        }
        if (sender.hasPermission("dzeconomy.admin.payall")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy payall <currency> <amount> &8- &7Pay all online players"));
        }
        if (sender.hasPermission("dzeconomy.admin.give")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy give <player> <amount> [currency] &8- &7Give any currency"));
        }
        sender.sendMessage(separator);
    }

    private CurrencyType parseCurrencyType(String input) {
        if (input == null) return null;
        switch (input.toLowerCase()) {
            case "money":
            case "coins":
                return CurrencyType.MONEY;
            case "mobcoin":
            case "mobcoins":
            case "mob_coin":
            case "mob_coins":
                return CurrencyType.MOBCOIN;
            case "gem":
            case "gems":
                return CurrencyType.GEM;
            default:
                return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("info");
            subs.add("credits");
            if (sender.hasPermission("dzeconomy.admin.reload")) subs.add("reload");
            if (sender.hasPermission("dzeconomy.admin.version")) subs.add("version");
            if (sender.hasPermission("dzeconomy.admin.status")) subs.add("status");
            if (sender.hasPermission("dzeconomy.admin.convert")) subs.add("convert");
            if (sender.hasPermission("dzeconomy.admin.migrate")) subs.add("migrate");
            if (sender.hasPermission("dzeconomy.admin.baltop")) subs.add("baltop");
            if (sender.hasPermission("dzeconomy.admin.payall")) subs.add("payall");
            if (sender.hasPermission("dzeconomy.admin.give")) subs.add("give");
            if (sender.hasPermission("dzeconomy.admin.backup")) subs.add("backup");
            String input = args[0].toLowerCase();
            for (String sub : subs) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "convert":
                    if (!sender.hasPermission("dzeconomy.admin.convert")) break;
                    for (Player p : FeatureAdapter.get().getOnlinePlayers()) {
                        if (sender instanceof Player && !((Player) sender).canSee(p)) {
                            continue;
                        }
                        if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(p.getName());
                        }
                    }
                    break;
                case "baltop":
                    if (!sender.hasPermission("dzeconomy.admin.baltop")) break;
                    for (CurrencyType type : CurrencyType.values()) {
                        String name = type.name().toLowerCase();
                        if (name.startsWith(args[1].toLowerCase())) {
                            completions.add(name);
                        }
                    }
                    break;
                case "payall":
                    if (!sender.hasPermission("dzeconomy.admin.payall")) break;
                    for (CurrencyType type : CurrencyType.values()) {
                        String name = type.name().toLowerCase();
                        if (name.startsWith(args[1].toLowerCase())) {
                            completions.add(name);
                        }
                    }
                    break;
                case "give":
                    if (!sender.hasPermission("dzeconomy.admin.give")) break;
                    for (Player p : FeatureAdapter.get().getOnlinePlayers()) {
                        if (sender instanceof Player && !((Player) sender).canSee(p)) {
                            continue;
                        }
                        if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(p.getName());
                        }
                    }
                    break;
                case "migrate":
                    if (!sender.hasPermission("dzeconomy.admin.migrate")) break;
                    List<String> storages = Arrays.asList("sqlite", "mysql", "flatfile");
                    for (String s : storages) {
                        if (s.startsWith(args[1].toLowerCase())) {
                            completions.add(s);
                        }
                    }
                    break;
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "convert":
                    if (!sender.hasPermission("dzeconomy.admin.convert")) break;
                    for (CurrencyType type : CurrencyType.values()) {
                        String name = type.name().toLowerCase();
                        if (name.startsWith(args[2].toLowerCase())) {
                            completions.add(name);
                        }
                    }
                    break;
                case "migrate":
                    if (!sender.hasPermission("dzeconomy.admin.migrate")) break;
                    List<String> storages = Arrays.asList("sqlite", "mysql", "flatfile");
                    for (String s : storages) {
                        if (s.startsWith(args[2].toLowerCase())) {
                            completions.add(s);
                        }
                    }
                    break;
                case "baltop":
                    if (!sender.hasPermission("dzeconomy.admin.baltop")) break;
                    completions.add("1");
                    completions.add("2");
                    completions.add("3");
                    break;
                case "payall":
                    if (!sender.hasPermission("dzeconomy.admin.payall")) break;
                    completions.add("100");
                    completions.add("500");
                    completions.add("1000");
                    break;
                case "give":
                    if (!sender.hasPermission("dzeconomy.admin.give")) break;
                    completions.add("100");
                    completions.add("500");
                    completions.add("1000");
                    break;
            }
        } else if (args.length == 4) {
            String sub = args[0].toLowerCase();
            if (sub.equals("convert")) {
                if (!sender.hasPermission("dzeconomy.admin.convert")) return completions;
                for (CurrencyType type : CurrencyType.values()) {
                    String name = type.name().toLowerCase();
                    if (name.startsWith(args[3].toLowerCase())) {
                        completions.add(name);
                    }
                }
            } else if (sub.equals("give")) {
                if (!sender.hasPermission("dzeconomy.admin.give")) return completions;
                for (CurrencyType type : CurrencyType.values()) {
                    String name = type.name().toLowerCase();
                    if (name.startsWith(args[3].toLowerCase())) {
                        completions.add(name);
                    }
                }
            }
        } else if (args.length == 5) {
            String sub = args[0].toLowerCase();
            if (sub.equals("convert")) {
                if (!sender.hasPermission("dzeconomy.admin.convert")) return completions;
                completions.add("100");
                completions.add("500");
                completions.add("1000");
            }
        }

        return completions;
    }
}
