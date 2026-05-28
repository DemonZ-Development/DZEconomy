package online.demonzdevelopment.dzeconomy.command;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.config.ConfigManager;
import online.demonzdevelopment.dzeconomy.util.MessagesUtil;

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
            default:
                MessagesUtil.sendMessage(sender, "unknown-subcommand", "%subcommand%", sub);
                return true;
        }
    }

    // ─── Public info command (no admin permission required) ───────────────────

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

    // ─── Credits (no permission required) ─────────────────────────────────────

    private boolean handleCredits(CommandSender sender) {
        String separator = MessagesUtil.colorize("&8&l&m─────────────────────────────────");
        sender.sendMessage(separator);
        sender.sendMessage(MessagesUtil.colorize("&6&l  DZEconomy &7Credits"));
        sender.sendMessage(MessagesUtil.colorize(""));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eAuthor&8: &7DemonzDevelopment"));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eVersion&8: &7" + plugin.getDescription().getVersion()));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eWebsite&8: &7online.demonzdevelopment"));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eContributors&8: &7The community"));
        sender.sendMessage(MessagesUtil.colorize(""));
        sender.sendMessage(MessagesUtil.colorize("  &7Thank you for using DZEconomy!"));
        sender.sendMessage(separator);
        return true;
    }

    // ─── Admin commands ───────────────────────────────────────────────────────

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("dzeconomy.admin")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        plugin.getConfigManager().reload();
        plugin.getCurrencyManager().reloadConfig();
        MessagesUtil.sendMessage(sender, "reload-success");
        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        if (!sender.hasPermission("dzeconomy.admin")) {
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
        if (!sender.hasPermission("dzeconomy.admin")) {
            MessagesUtil.sendMessage(sender, "no-permission");
            return true;
        }

        CurrencyManager cm = plugin.getCurrencyManager();
        ConfigManager config = plugin.getConfigManager();
        String storageType = config.getConfig().getString("storage.type", "sqlite");
        int cachedPlayers = cm.getCachedPlayerCount();
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
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
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eStorage&8: &7" + storageType));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eCached Players&8: &7" + cachedPlayers));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eOnline Players&8: &7" + onlinePlayers));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eUptime&8: &7" + uptimeHours + "h " + (uptimeMinutes % 60) + "m " + (uptimeSeconds % 60) + "s"));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &ePending Requests&8: &7" + cm.getPendingRequestCount()));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &eCombat Tags&8: &7" + cm.getActiveCombatTagCount()));
        for (CurrencyType type : CurrencyType.values()) {
            double total = cm.getTotalCurrency(type);
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &eTotal " + type.name().toLowerCase() + "&8: &7" + String.format("%,.2f", total)));
        }
        sender.sendMessage(separator);
        return true;
    }

    private boolean handleConvert(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dzeconomy.admin")) {
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

            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
                UUID uuid = target.getUniqueId();
                boolean success = cm.convert(uuid, fromType, toType, amount);
                if (success) {
                    boolean isOnline = target.isOnline();
                    if (!isOnline) {
                        cm.unloadPlayerData(uuid);
                    }
                    double newFromBalance = cm.getBalance(uuid, fromType);
                    double newToBalance = cm.getBalance(uuid, toType);
                    Runnable notifyTask = () -> {
                        MessagesUtil.sendMessage(sender, "convert-success",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%from%", fromType.name().toLowerCase(),
                                "%to%", toType.name().toLowerCase(),
                                "%amount%", String.format("%,.2f", amount),
                                "%from_balance%", String.format("%,.2f", newFromBalance),
                                "%to_balance%", String.format("%,.2f", newToBalance));
                    };
                    if (sender instanceof Player) {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, notifyTask);
                    } else {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, notifyTask);
                    }
                } else {
                    Runnable notifyTask = () -> {
                        MessagesUtil.sendMessage(sender, "convert-failed",
                                "%player%", target.getName() != null ? target.getName() : targetName,
                                "%from%", fromType.name().toLowerCase(),
                                "%to%", toType.name().toLowerCase(),
                                "%amount%", String.format("%,.2f", amount));
                    };
                    if (sender instanceof Player) {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, notifyTask);
                    } else {
                        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, notifyTask);
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

    private boolean handleMigrate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dzeconomy.admin")) {
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

        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
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

        final int finalPage = page;
        int perPage = 10;
        CurrencyManager cm = plugin.getCurrencyManager();
        
        final String finalCurrencyName = type.name().toLowerCase();
        cm.getBalanceTopAsync(type, page * perPage).thenAccept(top -> {
            List<Map.Entry<String, Double>> resolvedTop = new ArrayList<>();
            int start = (finalPage - 1) * perPage;
            if (!top.isEmpty()) {
                for (int i = start; i < Math.min(top.size(), start + perPage); i++) {
                    Map.Entry<UUID, Double> entry = top.get(i);
                    String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                    if (name == null) name = entry.getKey().toString().substring(0, 8) + "...";
                    resolvedTop.add(new java.util.AbstractMap.SimpleEntry<>(name, entry.getValue()));
                }
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
                        sender.sendMessage(MessagesUtil.colorize("  " + rankColor + rank + ". &8▸ &7" + entry.getKey() + " &8- &a" + String.format("%,.2f", entry.getValue())));
                        rank++;
                    }
                }

                sender.sendMessage(separator);
            };

            if (sender instanceof Player) {
                online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, (Player) sender, sendBaltopTask);
            } else {
                online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTask(plugin, sendBaltopTask);
            }
        });

        return true;
    }

    private boolean handlePayall(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dzeconomy.admin")) {
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
        int count = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean success = cm.addBalance(player.getUniqueId(), type, amount);
            if (success) {
                count++;
                MessagesUtil.sendMessage(player, "payall-received",
                        "%currency%", type.name().toLowerCase(),
                        "%amount%", String.format("%,.2f", amount));
            }
        }

        MessagesUtil.sendMessage(sender, "payall-success",
                "%count%", String.valueOf(count),
                "%currency%", type.name().toLowerCase(),
                "%amount%", String.format("%,.2f", amount));
        return true;
    }

    // ─── Help ─────────────────────────────────────────────────────────────────

    private void sendHelp(CommandSender sender) {
        String separator = MessagesUtil.colorize("&8&l&m─────────────────────────────────");
        sender.sendMessage(separator);
        sender.sendMessage(MessagesUtil.colorize("&6&l  DZEconomy Commands"));
        sender.sendMessage(MessagesUtil.colorize(""));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy info &8- &7View plugin information"));
        sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy credits &8- &7View credits"));
        if (sender.hasPermission("dzeconomy.admin")) {
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy reload &8- &7Reload configuration"));
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy version &8- &7Version information"));
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy status &8- &7Plugin status"));
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy convert <player> <from> <to> <amount> &8- &7Convert currency"));
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy migrate <from> <to> &8- &7Migrate storage"));
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy baltop [currency] [page] &8- &7Balance leaderboard"));
            sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy payall <currency> <amount> &8- &7Pay all online players"));
        } else {
            if (sender.hasPermission("dzeconomy.admin.baltop")) {
                sender.sendMessage(MessagesUtil.colorize("  &8▸ &e/economy baltop [currency] [page] &8- &7Balance leaderboard"));
            }
        }
        sender.sendMessage(separator);
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

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

    // ─── Tab Completion ───────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("info");
            subs.add("credits");
            if (sender.hasPermission("dzeconomy.admin")) {
                subs.add("reload");
                subs.add("version");
                subs.add("status");
                subs.add("convert");
                subs.add("migrate");
                subs.add("baltop");
                subs.add("payall");
            } else {
                if (sender.hasPermission("dzeconomy.admin.baltop")) {
                    subs.add("baltop");
                }
            }
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
                    if (!sender.hasPermission("dzeconomy.admin")) break;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (sender instanceof Player && !((Player) sender).canSee(p)) {
                            continue;
                        }
                        if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(p.getName());
                        }
                    }
                    break;
                case "baltop":
                    if (!sender.hasPermission("dzeconomy.admin") && !sender.hasPermission("dzeconomy.admin.baltop")) break;
                    for (CurrencyType type : CurrencyType.values()) {
                        String name = type.name().toLowerCase();
                        if (name.startsWith(args[1].toLowerCase())) {
                            completions.add(name);
                        }
                    }
                    break;
                case "payall":
                    if (!sender.hasPermission("dzeconomy.admin")) break;
                    for (CurrencyType type : CurrencyType.values()) {
                        String name = type.name().toLowerCase();
                        if (name.startsWith(args[1].toLowerCase())) {
                            completions.add(name);
                        }
                    }
                    break;
                case "migrate":
                    if (!sender.hasPermission("dzeconomy.admin")) break;
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
                    if (!sender.hasPermission("dzeconomy.admin")) break;
                    for (CurrencyType type : CurrencyType.values()) {
                        String name = type.name().toLowerCase();
                        if (name.startsWith(args[2].toLowerCase())) {
                            completions.add(name);
                        }
                    }
                    break;
                case "migrate":
                    if (!sender.hasPermission("dzeconomy.admin")) break;
                    List<String> storages = Arrays.asList("sqlite", "mysql", "flatfile");
                    for (String s : storages) {
                        if (s.startsWith(args[2].toLowerCase())) {
                            completions.add(s);
                        }
                    }
                    break;
                case "baltop":
                    if (!sender.hasPermission("dzeconomy.admin") && !sender.hasPermission("dzeconomy.admin.baltop")) break;
                    completions.add("1");
                    completions.add("2");
                    completions.add("3");
                    break;
                case "payall":
                    if (!sender.hasPermission("dzeconomy.admin")) break;
                    completions.add("100");
                    completions.add("500");
                    completions.add("1000");
                    break;
            }
        } else if (args.length == 4) {
            String sub = args[0].toLowerCase();
            if (sub.equals("convert")) {
                if (!sender.hasPermission("dzeconomy.admin")) return completions;
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
                if (!sender.hasPermission("dzeconomy.admin")) return completions;
                completions.add("100");
                completions.add("500");
                completions.add("1000");
            }
        }

        return completions;
    }
}
