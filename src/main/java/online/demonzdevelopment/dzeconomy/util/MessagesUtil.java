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
        if (lower.equals("combat-tagged-request")) return "error.combat-tagged";
        if (lower.equals("combat-tagged-send")) return "error.combat-tagged";
        if (lower.equals("player-only")) return "error.console-only-player";
        if (lower.equals("invalid-page")) return "error.invalid-amount";
        if (lower.equals("request-cancelled-quit")) return "request.cancelled-quit";
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
                    finalReplacements.put("{balance}", value);
                    finalReplacements.put("{amount}", value);
                    break;
                case "%amount%":
                    finalReplacements.put("{amount}", value);
                    break;
                case "%currency%":
                    finalReplacements.put("{currency}", value);
                    break;
                case "%max%":
                    finalReplacements.put("{max}", value);
                    break;
                case "%time%":
                case "%cooldown%":
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
