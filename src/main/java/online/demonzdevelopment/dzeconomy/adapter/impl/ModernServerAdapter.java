package online.demonzdevelopment.dzeconomy.adapter.impl;

import online.demonzdevelopment.dzeconomy.adapter.ServerAdapter;
import online.demonzdevelopment.dzeconomy.adapter.ServerPlatform;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.logging.Logger;

public class ModernServerAdapter implements ServerAdapter {

    private static final Logger LOGGER = Logger.getLogger("DZEconomy");

    private static final String[] DRIVER_CANDIDATES = {
        "org.sqlite.JDBC"
    };

    @Override
    public boolean loadSQLiteDriver() {
        if (isSQLiteDriverRegistered()) {
            return true;
        }

        for (String className : DRIVER_CANDIDATES) {
            try {
                Class.forName(className);
                LOGGER.fine("Loaded SQLite JDBC driver via Class.forName: " + className);
                if (isSQLiteDriverRegistered()) {
                    return true;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        LOGGER.severe("SQLite JDBC driver not found. Check that sqlite-jdbc is bundled.");
        return false;
    }

    private boolean isSQLiteDriverRegistered() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                if (driver.acceptsURL("jdbc:sqlite:")) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @Override
    public ServerPlatform getPlatform() {
        return ServerPlatform.MODERN;
    }
}
