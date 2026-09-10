package org.demonz.dev.dzeconomy.storage.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.demonz.dev.dzeconomy.DZEconomy;
import org.demonz.dev.dzeconomy.currency.CurrencyType;
import org.demonz.dev.dzeconomy.data.PlayerData;
import org.demonz.dev.dzeconomy.storage.StorageProvider;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class MySQLStorageProvider implements StorageProvider {
    
    private final DZEconomy plugin;
    private HikariDataSource dataSource;
    
    public MySQLStorageProvider(DZEconomy plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean initialize() {
        try {
            FileConfiguration config = plugin.getConfigManager().getConfig();
            String host = org.demonz.dev.dzeconomy.util.EnvResolver.resolve(config.getString("storage.mysql.host", "localhost"));
            int port = config.getInt("storage.mysql.port", 3306);
            String database = org.demonz.dev.dzeconomy.util.EnvResolver.resolve(config.getString("storage.mysql.database", "dzeconomy"));
            String username = org.demonz.dev.dzeconomy.util.EnvResolver.resolve(config.getString("storage.mysql.username", "root"));
            String password = org.demonz.dev.dzeconomy.util.EnvResolver.resolve(config.getString("storage.mysql.password", "password"));
            boolean useSSL = config.getBoolean("storage.mysql.use-ssl", false);
            int poolSize = config.getInt("storage.mysql.pool-size", 10);

            HikariConfig hc = new HikariConfig();
            hc.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&autoReconnect=true&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8",
                host, port, database, useSSL));
            hc.setUsername(username);
            hc.setPassword(password);
            hc.setMaximumPoolSize(poolSize);
            hc.setMinimumIdle(2);
            hc.setConnectionTimeout(10000);
            hc.setIdleTimeout(300000);
            hc.setMaxLifetime(600000);
            hc.setPoolName("DZEconomy-MySQL");
            hc.setConnectionInitSql("SET NAMES utf8mb4");

            dataSource = new HikariDataSource(hc);

            try (Connection conn = dataSource.getConnection()) {
                createTables(conn);
            }
            plugin.getLogger().info("MySQL storage initialized successfully!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize MySQL storage", e);
            close();
            return false;
        }
    }
    
    private void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS dze_players (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "username VARCHAR(16), " +
                "first_join BIGINT, " +
                "last_seen BIGINT, " +
                "money_balance DECIMAL(19,4) DEFAULT 0, " +
                "mobcoin_balance DECIMAL(19,4) DEFAULT 0, " +
                "gem_balance DECIMAL(19,4) DEFAULT 0, " +
                "money_sent DOUBLE DEFAULT 0, " +
                "money_received DOUBLE DEFAULT 0, " +
                "mobcoin_sent DOUBLE DEFAULT 0, " +
                "mobcoin_received DOUBLE DEFAULT 0, " +
                "gem_sent DOUBLE DEFAULT 0, " +
                "gem_received DOUBLE DEFAULT 0)"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS dze_daily_limits (" +
                "uuid VARCHAR(36), " +
                "currency_type VARCHAR(16), " +
                "send_count BIGINT DEFAULT 0, " +
                "request_count BIGINT DEFAULT 0, " +
                "sent_amount DOUBLE DEFAULT 0, " +
                "PRIMARY KEY (uuid, currency_type), " +
                "FOREIGN KEY (uuid) REFERENCES dze_players(uuid) ON DELETE CASCADE)"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS dze_cooldowns (" +
                "uuid VARCHAR(36), " +
                "currency_type VARCHAR(16), " +
                "send_cooldown BIGINT DEFAULT 0, " +
                "request_cooldown BIGINT DEFAULT 0, " +
                "send_time BIGINT DEFAULT 0, " +
                "PRIMARY KEY (uuid, currency_type), " +
                "FOREIGN KEY (uuid) REFERENCES dze_players(uuid) ON DELETE CASCADE)"
            );

            addIndexIfMissing(conn, "dze_players", "idx_dze_players_money", "money_balance");
            addIndexIfMissing(conn, "dze_players", "idx_dze_players_mobcoin", "mobcoin_balance");
            addIndexIfMissing(conn, "dze_players", "idx_dze_players_gem", "gem_balance");

            addColumnIfMissing(conn, "dze_daily_limits", "sent_amount", "DOUBLE DEFAULT 0");
            addColumnIfMissing(conn, "dze_cooldowns", "send_time", "BIGINT DEFAULT 0");
        }
    }

    private void addColumnIfMissing(Connection conn, String table, String column, String definition) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (SQLException e) {
            if (e.getErrorCode() != 1060) {
                plugin.getLogger().log(Level.WARNING,
                        "Could not add column " + column + " to " + table, e);
            }
        }
    }

    private void addIndexIfMissing(Connection conn, String table, String indexName, String column) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?")) {
            ps.setString(1, table);
            ps.setString(2, indexName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return;
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE INDEX " + indexName + " ON " + table + " (" + column + ")");
        } catch (SQLException e) {
            if (e.getErrorCode() != 1061) {
                plugin.getLogger().log(Level.WARNING,
                        "Could not create index " + indexName + " on " + table, e);
            }
        }
    }
    
    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        try (Connection conn = dataSource.getConnection()) {
            PlayerData data = new PlayerData(uuid);
            boolean found = false;
            
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM dze_players WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        found = true;
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
            
            if (!found) {
                return null;
            }
            
            loadDailyLimits(conn, data);
            loadCooldowns(conn, data);
            data.setDirty(false);
            return data;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + uuid, e);
            return null;
        }
    }
    
    private void loadDailyLimits(Connection conn, PlayerData data) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT currency_type, send_count, request_count, sent_amount FROM dze_daily_limits WHERE uuid = ?")) {
            stmt.setString(1, data.getUuid().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CurrencyType type = CurrencyType.fromString(rs.getString("currency_type"));
                    if (type != null) {
                        data.setDailySendCount(type, rs.getLong("send_count"));
                        data.setDailyRequestCount(type, rs.getLong("request_count"));
                        data.setDailySent(type, rs.getDouble("sent_amount"));
                    }
                }
            }
        }
    }
    
    private void loadCooldowns(Connection conn, PlayerData data) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT currency_type, send_cooldown, request_cooldown, send_time FROM dze_cooldowns WHERE uuid = ?")) {
            stmt.setString(1, data.getUuid().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CurrencyType type = CurrencyType.fromString(rs.getString("currency_type"));
                    if (type != null) {
                        data.setSendCooldown(type, rs.getLong("send_cooldown"));
                        data.setRequestCooldown(type, rs.getLong("request_cooldown"));
                        data.setLastSendTime(type, rs.getLong("send_time"));
                    }
                }
            }
        }
    }
    
    @Override
    public boolean savePlayerData(PlayerData data) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                savePlayerMain(conn, data);
                saveDailyLimits(conn, data);
                saveCooldowns(conn, data);
                conn.commit();
                data.setDirty(false);
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to rollback transaction", ex);
                }
                plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + data.getUuid(), e);
                return false;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Safe to ignore: the connection is closed by try-with-resources right after.
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + data.getUuid(), e);
            return false;
        }
    }
    
    private void savePlayerMain(Connection conn, PlayerData data) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO dze_players (uuid, username, first_join, last_seen, " +
                "money_balance, mobcoin_balance, gem_balance, " +
                "money_sent, money_received, mobcoin_sent, mobcoin_received, gem_sent, gem_received) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "username=VALUES(username), first_join=VALUES(first_join), last_seen=VALUES(last_seen), " +
                "money_balance=VALUES(money_balance), mobcoin_balance=VALUES(mobcoin_balance), gem_balance=VALUES(gem_balance), " +
                "money_sent=VALUES(money_sent), money_received=VALUES(money_received), " +
                "mobcoin_sent=VALUES(mobcoin_sent), mobcoin_received=VALUES(mobcoin_received), " +
                "gem_sent=VALUES(gem_sent), gem_received=VALUES(gem_received)")) {
            
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
    
    private void saveDailyLimits(Connection conn, PlayerData data) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO dze_daily_limits (uuid, currency_type, send_count, request_count, sent_amount) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "send_count=VALUES(send_count), request_count=VALUES(request_count), sent_amount=VALUES(sent_amount)")) {
            for (CurrencyType type : CurrencyType.values()) {
                stmt.setString(1, data.getUuid().toString());
                stmt.setString(2, type.getId());
                stmt.setLong(3, data.getDailySendCount(type));
                stmt.setLong(4, data.getDailyRequestCount(type));
                stmt.setDouble(5, data.getDailySent(type));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    private void saveCooldowns(Connection conn, PlayerData data) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO dze_cooldowns (uuid, currency_type, send_cooldown, request_cooldown, send_time) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "send_cooldown=VALUES(send_cooldown), request_cooldown=VALUES(request_cooldown), send_time=VALUES(send_time)")) {
            for (CurrencyType type : CurrencyType.values()) {
                stmt.setString(1, data.getUuid().toString());
                stmt.setString(2, type.getId());
                stmt.setLong(3, data.getSendCooldown(type));
                stmt.setLong(4, data.getRequestCooldown(type));
                stmt.setLong(5, data.getLastSendTime(type));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    @Override
    public boolean playerDataExists(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM dze_players WHERE uuid = ?")) {
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM dze_players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete player data for " + uuid, e);
        }
    }
    
    @Override
    public List<UUID> getAllPlayerUUIDs() {
        List<UUID> uuids = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT uuid FROM dze_players");
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
    public List<Map.Entry<UUID, Double>> getTopBalances(String currencyKey, int limit) {
        String column = getBalanceColumn(currencyKey);
        if (column == null) return Collections.emptyList();
        List<Map.Entry<UUID, Double>> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
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
    public Double getTotalBalance(String currencyKey) {
        String column = getBalanceColumn(currencyKey);
        if (column == null) return null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COALESCE(SUM(" + column + "), 0) FROM dze_players")) {
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to sum balances for " + currencyKey, e);
            return null;
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
