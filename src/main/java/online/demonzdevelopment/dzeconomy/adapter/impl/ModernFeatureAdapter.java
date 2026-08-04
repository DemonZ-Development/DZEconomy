package online.demonzdevelopment.dzeconomy.adapter.impl;

import online.demonzdevelopment.dzeconomy.adapter.FeatureAdapter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModernFeatureAdapter extends FeatureAdapter {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    @Override
    public List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    @Override
    public String translateColors(String text) {
        if (text == null) {
            return "";
        }
        if (text.indexOf("&#") != -1) {
            text = toSectionHex(text);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String toSectionHex(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            StringBuilder hex = new StringBuilder("§x");
            for (char c : matcher.group(1).toLowerCase().toCharArray()) {
                hex.append('§').append(c);
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(hex.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
