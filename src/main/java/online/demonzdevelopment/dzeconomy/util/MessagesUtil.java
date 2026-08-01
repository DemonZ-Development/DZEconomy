package online.demonzdevelopment.dzeconomy.util;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Thread-safe utility for reading messages from config with placeholder replacement.
 * Supports & color codes.
 */
public class MessagesUtil {
    
    private final DZEconomy plugin;
    
    public MessagesUtil(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    private String resolvePath(String path) {
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
        if (lower.equals("payall-success")) return "economy.payall.success";
        if (lower.equals("payall-received")) return "economy.payall.broadcast";
        if (lower.equals("usage-economy-give")) return "economy.give.usage";
        if (lower.equals("give-success")) return "economy.give.success";
        if (lower.equals("give-target")) return "economy.give.target";
        if (lower.equals("give-failed")) return "economy.give.failed";
        if (lower.startsWith("pvp-lost-")) return "pvp.victim-loss";
        if (lower.startsWith("pvp-gained-")) return "pvp.killer-gain";
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
        String resolvedPath = resolvePath(path);
        org.bukkit.configuration.file.FileConfiguration messages = plugin.getConfigManager().getMessages();
        String message = messages.getString(resolvedPath, "&cMessage not found: " + resolvedPath);
        return ColorUtil.translate(message);
    }
    
    public String getMessage(String path, String... placeholders) {
        String resolvedPath = resolvePath(path);
        String message = getMessage(resolvedPath);
        
        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        if (placeholders != null && placeholders.length >= 2) {
            int pairs = placeholders.length / 2;
            for (int i = 0; i < pairs; i++) {
                String key = placeholders[i * 2];
                String value = placeholders[i * 2 + 1];
                if (key != null && value != null) {
                    replacements.put(key, value);
                }
            }
        }
        
        String currency = getCurrencyFromPath(path);
        if (currency != null) {
            if (!replacements.containsKey("{symbol}") && !replacements.containsKey("%symbol%")) {
                String symbol = plugin.getConfigManager().getConfig().getString("currencies." + currency + ".symbol", "$");
                replacements.put("{symbol}", symbol);
                replacements.put("%symbol%", symbol);
            }
            if (!replacements.containsKey("{currency}") && !replacements.containsKey("%currency%")) {
                replacements.put("{currency}", currency);
                replacements.put("%currency%", currency);
            }
        }
        
        java.util.Map<String, String> finalReplacements = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, String> entry : replacements.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            finalReplacements.put(key, value);
            
            switch (key.toLowerCase()) {
                case "%player%":
                    finalReplacements.put("{name}", value);
                    finalReplacements.put("{receiver}", value);
                    finalReplacements.put("{sender}", value);
                    finalReplacements.put("{target}", value);
                    break;
                case "%balance%":
                    // NOTE: must NOT fill {amount} — messages that use both {amount} and
                    // {balance} would get one placeholder clobbered by the other
                    finalReplacements.put("{balance}", value);
                    break;
                case "%amount%":
                    finalReplacements.put("{amount}", value);
                    break;
                case "%currency%":
                    finalReplacements.put("{currency}", value);
                    break;
                case "%symbol%":
                    finalReplacements.put("{symbol}", value);
                    break;
                case "%command%":
                    finalReplacements.put("{command}", value);
                    break;
                case "%money%":
                    finalReplacements.put("{money}", value);
                    break;
                case "%mobcoins%":
                    finalReplacements.put("{mobcoins}", value);
                    break;
                case "%gems%":
                    finalReplacements.put("{gems}", value);
                    break;
                case "%current%":
                    finalReplacements.put("{current}", value);
                    break;
                case "%latest%":
                    finalReplacements.put("{latest}", value);
                    break;
                case "%max%":
                    finalReplacements.put("{max}", value);
                    break;
                case "%time%":
                case "%cooldown%":
                case "%duration%":
                    finalReplacements.put("{time}", value);
                    finalReplacements.put("{cooldown}", value);
                    break;
                case "%input%":
                    finalReplacements.put("{amount}", value);
                    finalReplacements.put("{timeout}", value);
                    break;
                case "%timeout%":
                    finalReplacements.put("{timeout}", value);
                    break;
                case "%permission%":
                    finalReplacements.put("{permission}", value);
                    break;
                case "%from%":
                    finalReplacements.put("{from}", value);
                    break;
                case "%to%":
                    finalReplacements.put("{to}", value);
                    break;
                case "%from_balance%":
                    finalReplacements.put("{from_balance}", value);
                    break;
                case "%to_balance%":
                    finalReplacements.put("{to_balance}", value);
                    break;
                case "%count%":
                    finalReplacements.put("{count}", value);
                    break;
            }
        }
        
        if (resolvedPath.equals("error.usage")) {
            if (!finalReplacements.containsKey("{usage}")) {
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
                finalReplacements.put("{usage}", "/" + (currency != null ? currency : "money") + " " + sub + " <player> <amount>");
            }
        }
        
        for (java.util.Map.Entry<String, String> entry : finalReplacements.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        
        return message;
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

    /**
     * Get a message from config with placeholder replacements (static convenience method).
     */
    public static String getStaticMessage(String path, String... placeholders) {
        MessagesUtil util = new MessagesUtil(DZEconomy.getInstance());
        return util.getMessage(path, placeholders);
    }

    /**
     * Send a message to a CommandSender using a message path from config.
     * Static convenience method that uses the plugin singleton.
     */
    public static void sendMessage(CommandSender sender, String path) {
        MessagesUtil util = new MessagesUtil(DZEconomy.getInstance());
        sender.sendMessage(util.getPrefixedMessage(path));
    }

    /**
     * Send a message to a CommandSender using a message path with placeholder replacements.
     * Static convenience method that uses the plugin singleton.
     */
    public static void sendMessage(CommandSender sender, String path, String... placeholders) {
        MessagesUtil util = new MessagesUtil(DZEconomy.getInstance());
        sender.sendMessage(util.getPrefixedMessage(path, placeholders));
    }

    /**
     * Translate color codes in a string. Static convenience method.
     */
    public static String colorize(String text) {
        return ColorUtil.translate(text);
    }
}
