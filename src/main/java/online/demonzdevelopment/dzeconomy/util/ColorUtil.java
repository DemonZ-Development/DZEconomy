package online.demonzdevelopment.dzeconomy.util;

import org.bukkit.ChatColor;

/**
 * Utility for translating color codes.
 */
public class ColorUtil {
    
    public static String translate(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
