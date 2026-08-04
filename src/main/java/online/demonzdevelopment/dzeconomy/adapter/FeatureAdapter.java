package online.demonzdevelopment.dzeconomy.adapter;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class FeatureAdapter {

    private static volatile FeatureAdapter instance;

    public static synchronized FeatureAdapter get() {
        if (instance == null) {
            instance = FeatureAdapterProvider.detect();
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    public abstract List<Player> getOnlinePlayers();

    public abstract String translateColors(String text);

    public boolean isBungeeCord() {
        try {
            Class<?> spigotConfigClass = Class.forName("org.spigotmc.SpigotConfig");
            java.lang.reflect.Field bungeeField = spigotConfigClass.getField("bungee");
            return bungeeField.getBoolean(null);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
