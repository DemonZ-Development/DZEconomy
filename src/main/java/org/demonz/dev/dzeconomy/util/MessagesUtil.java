package org.demonz.dev.dzeconomy.util;

import org.demonz.dev.dzeconomy.DZEconomy;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class MessagesUtil {
    
    private static final java.util.concurrent.ConcurrentHashMap<String, String> PATH_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

    private final DZEconomy plugin;
    
    public MessagesUtil(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    private static String resolvePath(String path) {
        if (path == null) return "";
        String lower = path.toLowerCase();
        
        if (lower.equals("no-permission")) return "error.no-permission";
        if (lower.equals("player-not-found")) return "error.player-not-found";
        if (lower.equals("invalid-amount")) return "error.invalid-amount";
        if (lower.equals("amount-must-be-positive")) return "error.invalid-amount";
        if (lower.equals("cannot-send-self")) return "error.cannot-send-self";
        if (lower.equals("cannot-request-self")) return "error.cannot-send-self";
        if (lower.equals("send-cooldown")) return "error.cooldown";
        if (lower.equals("max-requests-reached")) return "request.max-pending";
        if (lower.equals("request-already-pending")) return "request.max-pending";
        if (lower.equals("no-request-found")) return "request.not-found";
        if (lower.equals("request-expired")) return "request.expired";
        if (lower.equals("request-expired-timeout")) return "request.expired";
        if (lower.equals("request-expired-notify")) return "request.expired-target";
        if (lower.equals("combat-tagged-request")) return "error.combat-tagged";
        if (lower.equals("combat-tagged-send")) return "error.combat-tagged";
        if (lower.equals("combat-tagged")) return "combat-tag.tagged";
        if (lower.equals("player-only")) return "error.console-only-player";
        if (lower.equals("invalid-page")) return "error.invalid-amount";
        if (lower.equals("invalid-currency-type")) return "error.invalid-currency-type";
        if (lower.equals("invalid-currency-or-page")) return "error.invalid-currency-or-page";
        if (lower.equals("request-cancelled-quit")) return "request.cancelled-quit";
        if (lower.equals("max-transaction-exceeded")) return "error.above-maximum";
        if (lower.equals("welcome-new-player")) return "welcome.first-join";
        if (lower.equals("welcome-back")) return "welcome.returning";
        if (lower.equals("update-available")) return "update.notification";
        if (lower.equals("unknown-subcommand")) return "error.unknown-subcommand";
        if (lower.equals("reload-success")) return "economy.reload.success";
        if (lower.equals("reload-failed")) return "economy.reload.failed";
        if (lower.equals("same-currency-type")) return "economy.convert.same-currency";
        if (lower.equals("convert-success")) return "economy.convert.success";
        if (lower.equals("convert-failed")) return "economy.convert.insufficient";
        if (lower.equals("usage-economy-convert")) return "economy.convert.usage";
        if (lower.equals("usage-economy-migrate")) return "economy.migrate.usage";
        if (lower.equals("usage-economy-payall")) return "economy.payall.usage";
        if (lower.equals("migrate-same-storage")) return "economy.migrate.same-backend";
        if (lower.equals("migrate-invalid-storage")) return "economy.migrate.invalid-storage";
        if (lower.equals("migrate-start")) return "economy.migrate.start";
        if (lower.equals("min-transaction")) return "error.below-minimum";
        if (lower.equals("daily-limit-reached")) return "error.daily-limit-reached";
        if (lower.equals("currency-disabled")) return "error.currency-disabled";
        if (lower.equals("interest-paid")) return "interest.paid";
        if (lower.equals("payall-success")) return "economy.payall.success";
        if (lower.equals("payall-received")) return "economy.payall.broadcast";
        if (lower.equals("usage-economy-give")) return "economy.give.usage";
        if (lower.equals("give-success")) return "economy.give.success";
        if (lower.equals("give-target")) return "economy.give.target";
        if (lower.equals("give-failed")) return "economy.give.failed";
        if (lower.startsWith("pvp-lost-")) return "pvp.victim-loss";
        if (lower.startsWith("pvp-gained-")) return "pvp.killer-gain";
        if (lower.equals("pvp-broadcast")) return "pvp.broadcast";
        if (lower.endsWith("-earned")) return "mob-rewards.reward";
        
        String base = lower;
        if (lower.startsWith("money-")) {
            base = lower.substring(6);
        } else if (lower.startsWith("mobcoin-")) {
            base = lower.substring(8);
        } else if (lower.startsWith("gem-")) {
            base = lower.substring(4);
        } else {
            return path;
        }
        
        if (base.startsWith("usage-")) {
            return "error.usage";
        }
        
        switch (base) {
            case "balance": return "balance.self";
            case "balance-other": return "balance.others";
            case "send-success": return "send.sender";
            case "receive": return "send.receiver";
            case "send-failed": return "error.insufficient-funds";
            case "request-sent": return "request.sent";
            case "request-received": return "request.received";
            case "accept-sender": return "request.accepted-target";
            case "accept-receiver": return "request.accepted-sender";
            case "accept-failed": return "error.insufficient-funds";
            case "deny-sender": return "request.denied-target";
            case "deny-receiver": return "request.denied-sender";
            case "add-success": return "admin.add.sender";
            case "added": return "admin.add.target";
            case "add-failed": return "error.invalid-amount";
            case "remove-success": return "admin.remove.sender";
            case "removed": return "admin.remove.target";
            case "remove-failed": return "error.insufficient-funds";
            case "set-success": return "admin.set.sender";
            case "set": return "admin.set.target";
            case "set-failed": return "error.invalid-amount";
            default: return path;
        }
    }

    private String getCurrencyFromPath(String path) {
        if (path == null) return null;
        String lower = path.toLowerCase();
        if (lower.startsWith("money-")) return "money";
        if (lower.startsWith("mobcoin-")) return "mobcoin";
        if (lower.startsWith("gem-")) return "gem";
        return null;
    }

    public String getMessage(String path) {
        String resolvedPath = resolvePathCached(path);
        return getRawMessage(resolvedPath);
    }

    private String getRawMessage(String resolvedPath) {
        org.bukkit.configuration.file.FileConfiguration messages = plugin.getConfigManager().getMessages();
        String message = messages.getString(resolvedPath);
        if (message == null) {
            
            message = "&cMessage not found: " + resolvedPath;
        }
        return ColorUtil.translate(message);
    }

    private static String resolvePathCached(String path) {
        if (path == null) return "";
        return PATH_CACHE.computeIfAbsent(path.toLowerCase(), MessagesUtil::resolvePath);
    }
    
    public String getMessage(String path, String... placeholders) {
        String resolvedPath = resolvePathCached(path);
        String message = getRawMessage(resolvedPath);
        
        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        if (placeholders != null && placeholders.length >= 2) {
            int pairs = placeholders.length / 2;
            for (int i = 0; i < pairs; i++) {
                String key = placeholders[i * 2];
                String value = placeholders[i * 2 + 1];
                if (key != null && value != null) {
                    putReplacement(replacements, key, value);
                }
            }
        }
        
        String currency = getCurrencyFromPath(path);
        if (currency != null) {
            if (!replacements.containsKey("{symbol}") && !replacements.containsKey("%symbol%")) {
                String symbol = plugin.getConfigManager().getConfig().getString("currencies." + currency + ".symbol", "$");
                putReplacement(replacements, "{symbol}", symbol);
                putReplacement(replacements, "%symbol%", symbol);
            }
            if (!replacements.containsKey("{currency}") && !replacements.containsKey("%currency%")) {
                putReplacement(replacements, "{currency}", currency);
                putReplacement(replacements, "%currency%", currency);
            }
        }
        
        if (resolvedPath.equals("error.usage")) {
            if (!replacements.containsKey("{usage}")) {
                String lower = path.toLowerCase();
                String sub = "help";
                if (lower.contains("-")) {
                    String base = lower.substring(lower.indexOf("-") + 1);
                    if (base.startsWith("usage-")) {
                        sub = base.substring(6);
                    } else if (lower.startsWith("usage-")) {
                        sub = lower.substring(6);
                    }
                }
                putReplacement(replacements, "{usage}", "/" + (currency != null ? currency : "money") + " " + sub + " <player> <amount>");
            }
        }
        
        for (java.util.Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        
        return message;
    }

    private static void putReplacement(java.util.Map<String, String> map, String key, String value) {
        map.put(key, value);
        
        switch (key.toLowerCase()) {
            case "%player%":
                map.put("{name}", value);
                map.put("{receiver}", value);
                map.put("{sender}", value);
                map.put("{target}", value);
                break;
            case "%balance%":
                map.put("{balance}", value);
                break;
            case "%amount%":
                map.put("{amount}", value);
                break;
            case "%currency%":
                map.put("{currency}", value);
                break;
            case "%symbol%":
                map.put("{symbol}", value);
                break;
            case "%command%":
                map.put("{command}", value);
                break;
            case "%money%":
                map.put("{money}", value);
                break;
            case "%mobcoins%":
                map.put("{mobcoins}", value);
                break;
            case "%gems%":
                map.put("{gems}", value);
                break;
            case "%current%":
                map.put("{current}", value);
                break;
            case "%latest%":
                map.put("{latest}", value);
                break;
            case "%max%":
                map.put("{max}", value);
                break;
            case "%time%":
            case "%cooldown%":
            case "%duration%":
                map.put("{time}", value);
                map.put("{cooldown}", value);
                break;
            case "%input%":
                map.put("{amount}", value);
                map.put("{timeout}", value);
                break;
            case "%timeout%":
                map.put("{timeout}", value);
                break;
            case "%permission%":
                map.put("{permission}", value);
                break;
            case "%from%":
                map.put("{from}", value);
                break;
            case "%to%":
                map.put("{to}", value);
                break;
            case "%from_balance%":
                map.put("{from_balance}", value);
                break;
            case "%to_balance%":
                map.put("{to_balance}", value);
                break;
            case "%to_amount%":
                map.put("{to_amount}", value);
                break;
            case "%killer%":
                map.put("{killer}", value);
                break;
            case "%victim%":
                map.put("{victim}", value);
                break;
            case "%mob%":
                map.put("{mob}", value);
                break;
            case "%count%":
                map.put("{count}", value);
                break;
            case "%min%":
                map.put("{min}", value);
                break;
            case "%limit%":
                map.put("{limit}", value);
                break;
            case "%remaining%":
                map.put("{remaining}", value);
                break;
            case "%percentage%":
                map.put("{percentage}", value);
                break;
        }
    }
    
    public String getPrefixedMessage(String path) {
        String prefix = getMessage("prefix");
        String message = getMessage(path);
        return prefix + message;
    }
    
    public String getPrefixedMessage(String path, String... placeholders) {
        String prefix = getMessage("prefix");
        String message = getMessage(path, placeholders);
        return prefix + message;
    }

    public static String getStaticMessage(String path, String... placeholders) {
        MessagesUtil util = new MessagesUtil(DZEconomy.getInstance());
        return util.getMessage(path, placeholders);
    }

    public static void sendMessage(CommandSender sender, String path) {
        MessagesUtil util = new MessagesUtil(DZEconomy.getInstance());
        sender.sendMessage(util.getPrefixedMessage(path));
    }

    public static void sendMessage(CommandSender sender, String path, String... placeholders) {
        MessagesUtil util = new MessagesUtil(DZEconomy.getInstance());
        sender.sendMessage(util.getPrefixedMessage(path, placeholders));
    }

    public static String colorize(String text) {
        return ColorUtil.translate(text);
    }
}
