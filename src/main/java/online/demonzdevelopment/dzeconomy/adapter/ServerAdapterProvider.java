package online.demonzdevelopment.dzeconomy.adapter;

import online.demonzdevelopment.dzeconomy.adapter.impl.LegacyServerAdapter;
import online.demonzdevelopment.dzeconomy.adapter.impl.ModernServerAdapter;

import java.util.logging.Logger;

public final class ServerAdapterProvider {

    private static final Logger LOGGER = Logger.getLogger("DZEconomy");

    private static ServerAdapter adapter;

    private ServerAdapterProvider() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    public static synchronized ServerAdapter getAdapter() {
        if (adapter == null) {
            adapter = detectAdapter();
            LOGGER.info("Detected Java runtime platform: " + adapter.getPlatform());
        }
        return adapter;
    }

    public static synchronized void reset() {
        adapter = null;
    }

    private static ServerAdapter detectAdapter() {
        ServerPlatform platform = detectPlatform();
        switch (platform) {
            case LEGACY:
                return new LegacyServerAdapter();
            case MODERN:
                return new ModernServerAdapter();
            default:
                LOGGER.warning("Unknown server platform, defaulting to MODERN");
                return new ModernServerAdapter();
        }
    }

    static ServerPlatform detectPlatform() {
        try {
            String javaVersion = System.getProperty("java.version");
            if (javaVersion == null || javaVersion.isEmpty()) {
                LOGGER.warning("Java version property is null or empty, defaulting to LEGACY");
                return ServerPlatform.LEGACY;
            }
            if (javaVersion.startsWith("1.")) {
                return ServerPlatform.LEGACY;
            }
            String[] parts = javaVersion.split("\\.");
            int major = Integer.parseInt(parts[0]);
            return major >= 17 ? ServerPlatform.MODERN : ServerPlatform.LEGACY;
        } catch (RuntimeException e) {
            LOGGER.warning("Could not detect Java version, defaulting to LEGACY: " + e.getMessage());
            return ServerPlatform.LEGACY;
        }
    }
}
