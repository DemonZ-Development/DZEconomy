package online.demonzdevelopment.dzeconomy.adapter.impl;

import online.demonzdevelopment.dzeconomy.adapter.FeatureAdapter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyFeatureAdapter extends FeatureAdapter {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final int[][] LEGACY_COLORS = {
        {0x00, 0x00, 0x00}, {0x00, 0x00, 0xAA}, {0x00, 0xAA, 0x00}, {0x00, 0xAA, 0xAA},
        {0xAA, 0x00, 0x00}, {0xAA, 0x00, 0xAA}, {0xFF, 0xAA, 0x00}, {0xAA, 0xAA, 0xAA},
        {0x55, 0x55, 0x55}, {0x55, 0x55, 0xFF}, {0x55, 0xFF, 0x55}, {0x55, 0xFF, 0xFF},
        {0xFF, 0x55, 0x55}, {0xFF, 0x55, 0xFF}, {0xFF, 0xFF, 0x55}, {0xFF, 0xFF, 0xFF}
    };
    private static final String LEGACY_CODE_CHARS = "0123456789abcdef";

    @Override
    public List<Player> getOnlinePlayers() {
        try {
            Method method = Bukkit.class.getMethod("getOnlinePlayers");
            Object result = method.invoke(null);
            if (result instanceof Collection<?>) {
                List<Player> players = new ArrayList<>();
                for (Object entry : (Collection<?>) result) {
                    players.add((Player) entry);
                }
                return players;
            }
            if (result instanceof Player[]) {
                return new ArrayList<>(Arrays.asList((Player[]) result));
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Collections.emptyList();
    }

    @Override
    public String translateColors(String text) {
        if (text == null) {
            return "";
        }
        if (text.indexOf("&#") != -1) {
            text = toNearestLegacy(text);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String toNearestLegacy(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            int codeIndex = nearestLegacyIndex(hex);
            matcher.appendReplacement(buffer, "&" + LEGACY_CODE_CHARS.charAt(codeIndex));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static int nearestLegacyIndex(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        int best = 0;
        long bestDistance = Long.MAX_VALUE;
        for (int i = 0; i < LEGACY_COLORS.length; i++) {
            int[] color = LEGACY_COLORS[i];
            long dr = r - color[0];
            long dg = g - color[1];
            long db = b - color[2];
            long distance = dr * dr + dg * dg + db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }
}
