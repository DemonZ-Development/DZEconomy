package online.demonzdevelopment.dzeconomy.adapter.impl;

import online.demonzdevelopment.dzeconomy.adapter.ServerAdapter;
import online.demonzdevelopment.dzeconomy.adapter.ServerPlatform;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.logging.Logger;

public class LegacyServerAdapter implements ServerAdapter {

    private static final Logger LOGGER = Logger.getLogger("DZEconomy");

    private static final String[] DRIVER_CANDIDATES = {
        "org.sqlite.JDBC"
    };

    @Override
    public boolean loadSQLiteDriver() {
        for (String className : DRIVER_CANDIDATES) {
            try {
                Class<?> driverClass = Class.forName(className);
                LOGGER.info("Loaded SQLite JDBC driver: " + className);
                return true;
            } catch (ClassNotFoundException ignored) {
            }
        }

        LOGGER.severe("SQLite JDBC driver not found. Check that sqlite-jdbc is bundled.");
        return false;
    }

    @Override
    public ServerPlatform getPlatform() {
        return ServerPlatform.LEGACY;
    }
}
