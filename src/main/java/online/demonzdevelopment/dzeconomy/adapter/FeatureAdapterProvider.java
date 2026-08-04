package online.demonzdevelopment.dzeconomy.adapter;

import online.demonzdevelopment.dzeconomy.adapter.impl.LegacyFeatureAdapter;
import online.demonzdevelopment.dzeconomy.adapter.impl.ModernFeatureAdapter;

import org.bukkit.Bukkit;

import java.util.logging.Logger;

public final class FeatureAdapterProvider {

    private static final Logger LOGGER = Logger.getLogger("DZEconomy");

    private FeatureAdapterProvider() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    static FeatureAdapter detect() {
        int[] mc = parseMinecraftVersion();
        if (mc != null && (mc[0] < 1 || (mc[0] == 1 && mc[1] < 16))) {
            return new LegacyFeatureAdapter();
        }
        return new ModernFeatureAdapter();
    }

    private static int[] parseMinecraftVersion() {
        try {
            String version = Bukkit.getBukkitVersion();
            if (version == null || version.isEmpty()) {
                return null;
            }
            String[] dash = version.split("-");
            String[] parts = dash[0].split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return new int[]{major, minor};
        } catch (Throwable t) {
            return null;
        }
    }
}
