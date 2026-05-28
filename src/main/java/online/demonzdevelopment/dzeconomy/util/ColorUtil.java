package online.demonzdevelopment.dzeconomy.util;

import org.bukkit.ChatColor;

/**
 * Utility for translating color codes and stripping colors.
 */
public class ColorUtil {
    
    public static String translate(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(text);
    }
}
