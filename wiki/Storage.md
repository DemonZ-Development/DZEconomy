# 🗄️ Storage Backends

Complete guide to storage backends, migration, backup, and performance optimization for DZEconomy v2.1.0.

---

## 📦 Supported Backends

| Backend | Type | Best For | Status |
|---------|------|----------|--------|
| **SQLite** | File-based | Small-to-medium servers (<50 players) | Default, recommended for most servers |
| **MySQL** | Remote database | Large servers, network/bungeecord setups | Production-ready with HikariCP pooling |
| **Flat File** | YAML files | Testing, debugging, small servers | Available but not recommended for production |

---

## 🐬 SQLite

### Overview

SQLite is the **default** storage backend. It requires zero configuration — just start the server and it works.

### Configuration

```yaml
storage:
  type: SQLITE
  sqlite:
    file: data.db
```

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `storage.sqlite.file` | String | `data.db` | Database file name inside `plugins/DZEconomy/` |

### How It Works

- The database file is created at `plugins/DZEconomy/data.db`
- Uses the bundled SQLite JDBC driver (no external dependencies)
- Data is cached in memory and periodically flushed to disk
- Auto-save interval is configurable (default: 5 minutes)

### Optimization Tips

1. **Increase auto-save interval** for large servers:
   ```yaml
   auto-save:
     interval: 600  # 10 minutes instead of 5
   ```

2. **Disable save-on-transaction** if enabled:
   ```yaml
   auto-save:
     save-on-transaction: false
   ```

3. **Keep the database file on an SSD** for faster I/O

4. **Periodically vacuum** the database (run `/economy backup` then restart):
   - SQLite databases can grow with fragmentation
   - A backup and restore will compact the file

### Limitations

- Not suitable for BungeeCord/Velocity networks (each server has its own file)
- Write concurrency is limited (one writer at a time)
- Very large databases (>100K players) may see slower queries

---

## 🐬 MySQL

### Overview

MySQL is recommended for **large servers** (50+ players) and **network setups** where multiple servers share economy data.

### Configuration

```yaml
storage:
  type: MYSQL
  mysql:
    host: localhost
    port: 3306
    database: dzeconomy
    username: dzeconomy_user
    password: "your_secure_password"
    parameters: "?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8"
    pool-size: 10
    connection-timeout: 30
```

### Settings Reference

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `host` | String | `localhost` | MySQL server hostname |
| `port` | Integer | `3306` | MySQL server port |
| `database` | String | `dzeconomy` | Database name (must exist) |
| `username` | String | `root` | Database username |
| `password` | String | `changeme` | Database password |
| `parameters` | String | `?useSSL=false&...` | JDBC URL parameters |
| `pool-size` | Integer | `10` | HikariCP connection pool size |
| `connection-timeout` | Integer | `30` | Maximum wait time (seconds) for connection |

### Setup Steps

1. **Create the database** on your MySQL server:
   ```sql
   CREATE DATABASE dzeconomy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Create a dedicated user** (recommended):
   ```sql
   CREATE USER 'dzeconomy_user'@'%' IDENTIFIED BY 'your_secure_password';
   GRANT ALL PRIVILEGES ON dzeconomy.* TO 'dzeconomy_user'@'%';
   FLUSH PRIVILEGES;
   ```

3. **Configure DZEconomy** to use MySQL in `config.yml`

4. **Restart** the server — DZEconomy will automatically create the required tables

### Connection Pooling (HikariCP)

DZEconomy uses **HikariCP** for MySQL connection pooling, providing:
- High-performance connection reuse
- Automatic connection validation
- Configurable pool size
- Connection leak detection

**Pool Size Tuning:**

| Players | Recommended Pool Size |
|---------|-----------------------|
| <20 | 5 |
| 20-50 | 10 (default) |
| 50-100 | 15 |
| 100+ | 20 |

> **Formula**: `pool-size = (core_count * 2) + effective_spindle_count`

### JDBC Parameters

Common parameters you can append to the connection URL:

| Parameter | Purpose |
|-----------|---------|
| `useSSL=false` | Disable SSL for local connections |
| `autoReconnect=true` | Auto-reconnect on connection loss |
| `useUnicode=true` | Enable Unicode support |
| `characterEncoding=UTF-8` | Force UTF-8 encoding |
| `allowPublicKeyRetrieval=true` | Needed for MySQL 8+ with caching_sha2_password |

### Remote MySQL Setup

For remote MySQL servers:

1. Ensure the MySQL server accepts remote connections
2. Configure firewall to allow port 3306
3. Use SSL for remote connections: remove `useSSL=false` and add:
   ```
   ?useSSL=true&requireSSL=true&serverCertificate=/path/to/ca.pem
   ```
4. Increase `connection-timeout` for high-latency connections

---

## 📄 Flat File Storage

### Overview

Flat file storage uses YAML files to store player data. It's included for testing and small servers but is **not recommended** for production.

### Configuration

```yaml
storage:
  type: FLATFILE
```

### How It Works

- Each player's data is stored in a separate YAML file
- Files are located at `plugins/DZEconomy/playerdata/<uuid>.yml`
- All operations are synchronous (can cause lag on large servers)

### When to Use

- Testing and development
- Very small servers (<10 players)
- Debugging data issues

### When NOT to Use

- Production servers with 10+ players
- Servers with frequent economy transactions
- BungeeCord/Velocity networks

---

## 🔄 Migration Between Backends

DZEconomy supports live migration between storage backends using the `/economy migrate` command.

### Migration Command

```
/economy migrate <from> <to>
```

**Valid backends:** `sqlite`, `mysql`, `flatfile`

### Migration Examples

```bash
# SQLite → MySQL (most common)
/economy migrate sqlite mysql

# MySQL → SQLite (downgrading)
/economy migrate mysql sqlite

# Flat File → MySQL
/economy migrate flatfile mysql

# SQLite → Flat File (for debugging)
/economy migrate sqlite flatfile
```

### Migration Process

1. **Pre-check**: Verifies the target backend is different from the current one
2. **Initialization**: Creates a temporary storage provider for the target backend
3. **Reading**: Loads all player data from the source backend
4. **Writing**: Saves all player data to the target backend
5. **Verification**: Confirms the migration was successful
6. **Completion**: Updates `config.yml` with the new storage type

### Migration Best Practices

> ⚠️ **Always back up your data before migrating!**

1. **Back up** your `plugins/DZEconomy/` folder
2. **Notify players** that the server may lag briefly during migration
3. **Run the migrate command** from console or in-game
4. **Wait for completion** — migration runs asynchronously
5. **Restart** the server to ensure clean state
6. **Verify** player data is intact with `/economy status`

### Troubleshooting Migration

| Issue | Solution |
|-------|----------|
| "Already using X storage!" | Change `storage.type` in `config.yml` first, or you're migrating to the same backend |
| "Migration failed!" | Check console for errors; verify MySQL credentials |
| Data missing after migration | Check the source backend data; restore from backup |
| Server lag during migration | Normal for large databases; migration is async and should not block gameplay |

---

## 💾 Backup and Restore

### Creating Backups

#### Automatic Backups

DZEconomy creates automatic saves based on the `auto-save.interval` setting. For additional safety:

1. **Enable transaction logging**:
   ```yaml
   misc:
     transaction-log:
       enabled: true
       file: transactions.log
       max-size: 10
       max-files: 5
   ```

#### Manual Backups

Use the `/economy backup` command:
```
/economy backup    → Creates a timestamped backup
```

Backups are stored in `plugins/DZEconomy/backups/`.

#### File-Level Backups

For complete backups, copy the entire `plugins/DZEconomy/` folder:
```bash
cp -r plugins/DZEconomy/ backups/DZEconomy_$(date +%Y%m%d_%H%M%S)/
```

### Restoring from Backup

1. **Stop the server**
2. **Back up** the current `plugins/DZEconomy/` folder (just in case)
3. **Replace** the current data files with the backup
4. **Start the server**

For SQLite:
```bash
# Replace the database file
cp backups/DZEconomy_20250101/data.db plugins/DZEconomy/data.db
```

For MySQL:
```bash
# Restore from MySQL dump
mysql -u dzeconomy_user -p dzeconomy < backup.sql
```

---

## ⚡ Performance Tips

### General Tips

| Tip | Impact | Details |
|-----|--------|---------|
| Use MySQL for 50+ players | High | Better write concurrency and query performance |
| Increase auto-save interval | Medium | Reduces disk I/O at the cost of potential data loss |
| Disable save-on-transaction | High | Only enable if crash protection is critical |
| Enable baltop caching | Medium | `baltop.refresh-interval: 300` avoids per-query calculations |
| Use Paper or Folia | High | Async chunk loading improves overall server performance |

### SQLite Optimization

```yaml
auto-save:
  interval: 600          # 10 minutes (default: 5)
  save-on-transaction: false  # Never enable this with SQLite
```

- Keep `data.db` on an SSD
- For databases >50MB, consider migrating to MySQL
- Periodically back up and recreate the database to reduce file size

### MySQL Optimization

```yaml
storage:
  mysql:
    pool-size: 15           # Adjust based on player count
    connection-timeout: 30  # Increase for remote MySQL

auto-save:
  interval: 300             # 5 minutes is fine with MySQL
  save-on-transaction: false # MySQL handles durability better
```

**MySQL Server Tuning** (in `my.cnf`):
```ini
[mysqld]
innodb_buffer_pool_size = 256M   # Increase for larger databases
innodb_log_file_size = 64M
max_connections = 100
```

### Caching

DZEconomy caches player data in memory for fast access:
- **Online players**: Always cached
- **Offline players**: Loaded on demand, not cached by default
- **PlaceholderAPI**: 3-second placeholder cache with automatic eviction

### Baltop Performance

```yaml
baltop:
  refresh-interval: 300     # Cache leaderboard for 5 minutes
  include-offline: true     # Includes all players (slower but complete)
  entries-per-page: 10      # Standard page size
```

For very large databases (>10K players):
- Increase `refresh-interval` to 600 or more
- Consider disabling `include-offline` if only online players matter

---

<p align="center">
  See <a href="Configuration.md">Configuration</a> for storage-related config options.
</p>

---
### 📖 Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***
