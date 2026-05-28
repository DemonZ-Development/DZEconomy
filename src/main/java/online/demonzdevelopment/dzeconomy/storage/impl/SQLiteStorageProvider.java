package online.demonzdevelopment.dzeconomy.storage.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.adapter.ServerAdapterProvider;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.data.PlayerData;
import online.demonzdevelopment.dzeconomy.storage.StorageProvider;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class SQLiteStorageProvider implements StorageProvider {
    
    private final DZEconomy plugin;
    private String url;
    private HikariDataSource dataSource;
    
    public SQLiteStorageProvider(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean initialize() {
        try {
            if (!ServerAdapterProvider.getAdapter().loadSQLiteDriver()) {
                return false;
            }

            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File dbFile = new File(dataFolder, "dzeconomy.db");
            this.url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
            config.setPoolName("DZEconomy-SQLite");
            config.setConnectionInitSql("PRAGMA journal_mode = WAL;");
            config.setConnectionTestQuery("SELECT 1");
            config.addDataSourceProperty("foreign_keys", "on");
            config.addDataSourceProperty("synchronous", "NORMAL");
            config.addDataSourceProperty("busy_timeout", "5000");
            
            this.dataSource = new HikariDataSource(config);
            
            try (Connection connection = dataSource.getConnection()) {
                createTables(connection);
            }
            
            plugin.getLogger().info("SQLite storage initialized successfully!");
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite storage", e);
            return false;
        }
    }
    
    private Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }
    
    private void createTables(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS dze_players (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "username VARCHAR(16), " +
                "first_join INTEGER, " +
                "last_seen INTEGER, " +
                "money_balance REAL DEFAULT 0, " +
                "mobcoin_balance REAL DEFAULT 0, " +
                "gem_balance REAL DEFAULT 0, " +
                "money_sent REAL DEFAULT 0, " +
                "money_received REAL DEFAULT 0, " +
                "mobcoin_sent REAL DEFAULT 0, " +
                "mobcoin_received REAL DEFAULT 0, " +
                "gem_sent REAL DEFAULT 0, " +
                "gem_received REAL DEFAULT 0)"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS dze_daily_limits (" +
                "uuid VARCHAR(36) NOT NULL, " +
                "currency_type VARCHAR(16) NOT NULL, " +
                "send_count INTEGER DEFAULT 0, " +
                "request_count INTEGER DEFAULT 0, " +
                "PRIMARY KEY (uuid, currency_type), " +
                "FOREIGN KEY (uuid) REFERENCES dze_players(uuid) ON DELETE CASCADE)"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS dze_cooldowns (" +
                "uuid VARCHAR(36) NOT NULL, " +
                "currency_type VARCHAR(16) NOT NULL, " +
                "send_cooldown INTEGER DEFAULT 0, " +
                "request_cooldown INTEGER DEFAULT 0, " +
                "PRIMARY KEY (uuid, currency_type), " +
                "FOREIGN KEY (uuid) REFERENCES dze_players(uuid) ON DELETE CASCADE)"
            );
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create SQLite tables", e);
        }
    }
    
    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        try (Connection connection = getConnection()) {
            PlayerData data = new PlayerData(uuid);
            
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM dze_players WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        data.setUsername(rs.getString("username"));
                        data.setFirstJoin(rs.getLong("first_join"));
                        data.setLastSeen(rs.getLong("last_seen"));
                        data.setBalance(CurrencyType.MONEY, rs.getDouble("money_balance"));
                        data.setBalance(CurrencyType.MOBCOIN, rs.getDouble("mobcoin_balance"));
                        data.setBalance(CurrencyType.GEM, rs.getDouble("gem_balance"));
                        data.setMoneySent(CurrencyType.MONEY, rs.getDouble("money_sent"));
                        data.setMoneyReceived(CurrencyType.MONEY, rs.getDouble("money_received"));
                        data.setMoneySent(CurrencyType.MOBCOIN, rs.getDouble("mobcoin_sent"));
                        data.setMoneyReceived(CurrencyType.MOBCOIN, rs.getDouble("mobcoin_received"));
                        data.setMoneySent(CurrencyType.GEM, rs.getDouble("gem_sent"));
                        data.setMoneyReceived(CurrencyType.GEM, rs.getDouble("gem_received"));
                    }
                }
            }
            
            loadDailyLimits(connection, data);
            loadCooldowns(connection, data);
            data.setDirty(false);
            return data;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + uuid, e);
            return null;
        }
    }
    
    private void loadDailyLimits(Connection connection, PlayerData data) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT currency_type, send_count, request_count FROM dze_daily_limits WHERE uuid = ?")) {
            stmt.setString(1, data.getUuid().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CurrencyType type = CurrencyType.fromString(rs.getString("currency_type"));
                    if (type != null) {
                        data.setDailySendCount(type, rs.getLong("send_count"));
                        data.setDailyRequestCount(type, rs.getLong("request_count"));
                    }
                }
            }
        }
    }
    
    private void loadCooldowns(Connection connection, PlayerData data) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT currency_type, send_cooldown, request_cooldown FROM dze_cooldowns WHERE uuid = ?")) {
            stmt.setString(1, data.getUuid().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CurrencyType type = CurrencyType.fromString(rs.getString("currency_type"));
                    if (type != null) {
                        data.setSendCooldown(type, rs.getLong("send_cooldown"));
                        data.setRequestCooldown(type, rs.getLong("request_cooldown"));
                    }
                }
            }
        }
    }
    
    @Override
    public void savePlayerData(PlayerData data) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                savePlayerMain(connection, data);
                saveDailyLimits(connection, data);
                saveCooldowns(connection, data);
                connection.commit();
                data.setDirty(false);
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to rollback SQLite transaction", ex);
                }
                throw e;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + data.getUuid(), e);
        }
    }
    
    private void savePlayerMain(Connection connection, PlayerData data) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO dze_players (uuid, username, first_join, last_seen, " +
                "money_balance, mobcoin_balance, gem_balance, " +
                "money_sent, money_received, mobcoin_sent, mobcoin_received, gem_sent, gem_received) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, data.getUuid().toString());
            stmt.setString(2, data.getUsername());
            stmt.setLong(3, data.getFirstJoin());
            stmt.setLong(4, data.getLastSeen());
            stmt.setDouble(5, data.getBalance(CurrencyType.MONEY));
            stmt.setDouble(6, data.getBalance(CurrencyType.MOBCOIN));
            stmt.setDouble(7, data.getBalance(CurrencyType.GEM));
            stmt.setDouble(8, data.getMoneySent(CurrencyType.MONEY));
            stmt.setDouble(9, data.getMoneyReceived(CurrencyType.MONEY));
            stmt.setDouble(10, data.getMoneySent(CurrencyType.MOBCOIN));
            stmt.setDouble(11, data.getMoneyReceived(CurrencyType.MOBCOIN));
            stmt.setDouble(12, data.getMoneySent(CurrencyType.GEM));
            stmt.setDouble(13, data.getMoneyReceived(CurrencyType.GEM));
            stmt.executeUpdate();
        }
    }
    
    private void saveDailyLimits(Connection connection, PlayerData data) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO dze_daily_limits (uuid, currency_type, send_count, request_count) " +
                "VALUES (?, ?, ?, ?)")) {
            for (CurrencyType type : CurrencyType.values()) {
                stmt.setString(1, data.getUuid().toString());
                stmt.setString(2, type.getId());
                stmt.setLong(3, data.getDailySendCount(type));
                stmt.setLong(4, data.getDailyRequestCount(type));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    private void saveCooldowns(Connection connection, PlayerData data) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO dze_cooldowns (uuid, currency_type, send_cooldown, request_cooldown) " +
                "VALUES (?, ?, ?, ?)")) {
            for (CurrencyType type : CurrencyType.values()) {
                stmt.setString(1, data.getUuid().toString());
                stmt.setString(2, type.getId());
                stmt.setLong(3, data.getSendCooldown(type));
                stmt.setLong(4, data.getRequestCooldown(type));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    @Override
    public boolean playerDataExists(UUID uuid) {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM dze_players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check player data for " + uuid, e);
            return false;
        }
    }
    
    @Override
    public void deletePlayerData(UUID uuid) {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement("DELETE FROM dze_players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete player data for " + uuid, e);
        }
    }
    
    @Override
    public List<UUID> getAllPlayerUUIDs() {
        List<UUID> uuids = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT uuid FROM dze_players");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                uuids.add(UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get all player UUIDs", e);
        }
        return uuids;
    }
    
    @Override
    public Map<String, Double> getAllBalances(UUID uuid) {
        Map<String, Double> balances = new HashMap<>();
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                "SELECT money_balance, mobcoin_balance, gem_balance FROM dze_players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    balances.put("money", rs.getDouble("money_balance"));
                    balances.put("mobcoin", rs.getDouble("mobcoin_balance"));
                    balances.put("gem", rs.getDouble("gem_balance"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get all balances for " + uuid, e);
        }
        return balances;
    }

    @Override
    public void setBalance(UUID uuid, String currencyKey, double amount) {
        String column = getBalanceColumn(currencyKey);
        if (column == null) return;
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO dze_players (uuid, " + column + ") VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET " + column + " = excluded." + column)) {
            stmt.setString(1, uuid.toString());
            stmt.setDouble(2, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to set balance for " + uuid, e);
        }
    }

    @Override
    public List<Map.Entry<UUID, Double>> getTopBalances(String currencyKey, int limit) {
        String column = getBalanceColumn(currencyKey);
        if (column == null) return List.of();
        List<Map.Entry<UUID, Double>> result = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                "SELECT uuid, " + column + " FROM dze_players ORDER BY " + column + " DESC LIMIT ?")) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    double balance = rs.getDouble(column);
                    result.add(new AbstractMap.SimpleEntry<>(uuid, balance));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get top balances", e);
        }
        return result;
    }

    private String getBalanceColumn(String currencyKey) {
        if (currencyKey == null) return null;
        switch (currencyKey.toLowerCase()) {
            case "money": return "money_balance";
            case "mobcoin": case "mobcoins": return "mobcoin_balance";
            case "gem": case "gems": return "gem_balance";
            default: return null;
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
