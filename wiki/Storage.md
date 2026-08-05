![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# Storage Backends

Storage backends, migration, backup, and performance tuning for DZEconomy v2.1.2.

---

## Supported Backends

| Backend | Type | Best For | Status |
|---------|------|----------|--------|
| **SQLite** | File-based | Small-to-medium servers (<50 players) | Default. Recommended for most servers |
| **MySQL** | Remote database | Large servers, network setups | Production-ready with HikariCP pooling |

SQLite works out of the box. Nothing to configure, nothing to install.

---

## SQLite

### Overview

SQLite is the default backend. Start the server, and the plugin creates `plugins/DZEconomy/data.db` automatically.

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

- Uses the bundled SQLite JDBC driver. No external dependencies.
- Data is cached in memory and flushed periodically.
- The auto-save interval is configurable (default: 5 minutes).

### Optimization Tips

1. Increase the auto-save interval for large servers:

   ```yaml
   auto-save:
     interval: 600  # 10 minutes instead of 5
   ```

2. Keep the database file on an SSD for faster I/O.

### Limitations

- Not suitable for BungeeCord or Velocity networks. Each server has its own file.
- Write concurrency is limited. One writer at a time.
- Very large databases (100K+ players) may see slower queries.

---

## MySQL

### Overview

MySQL suits large servers (50+ players) and network setups where multiple servers share one economy database.

### Configuration

```yaml
storage:
  type: MYSQL
  mysql:
    host: localhost
    port: 3306
    database: dzeconomy
    username: root
    password: "your_secure_password"
    use-ssl: false
    pool-size: 10
```

### Settings Reference

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `storage.mysql.host` | String | `localhost` | MySQL server hostname |
| `storage.mysql.port` | Integer | `3306` | MySQL server port |
| `storage.mysql.database` | String | `dzeconomy` | Database name (must exist) |
| `storage.mysql.username` | String | `root` | Database username |
| `storage.mysql.password` | String | `changeme` | Database password |
| `storage.mysql.use-ssl` | Boolean | `false` | Whether to connect with SSL |
| `storage.mysql.pool-size` | Integer | `10` | HikariCP connection pool size |

### Setup Steps

1. Create the database on your MySQL server:

   ```sql
   CREATE DATABASE dzeconomy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Create a dedicated user (recommended):

   ```sql
   CREATE USER 'dzeconomy_user'@'%' IDENTIFIED BY 'your_secure_password';
   GRANT ALL PRIVILEGES ON dzeconomy.* TO 'dzeconomy_user'@'%';
   FLUSH PRIVILEGES;
   ```

3. Set `storage.type: MYSQL` in `config.yml`.
4. Restart the server. DZEconomy creates the required tables automatically.

### Connection Pooling (HikariCP)

DZEconomy pools MySQL connections with HikariCP:

- High-performance connection reuse
- Automatic connection validation
- Configurable pool size

**Pool Size Tuning:**

| Players | Recommended Pool Size |
|---------|-----------------------|
| <20 | 5 |
| 20-50 | 10 (default) |
| 50-100 | 15 |
| 100+ | 20 |

### SSL Connections

Set `storage.mysql.use-ssl: true` to connect over SSL. Use this on remote connections or anything crossing an untrusted network. The JDBC driver handles certificate negotiation.

### Remote MySQL Setup

1. Make sure the MySQL server accepts remote connections.
2. Allow port 3306 through the firewall.
3. Use SSL for remote connections (`use-ssl: true`).
4. Increase `pool-size` if the server handles many concurrent players.

---

## Migration Between Backends

Migrate live between storage backends with `/economy migrate`.

### Migration Command

```
/economy migrate <from> <to>
```

**Valid backends:** `sqlite`, `mysql` (and `flatfile`/`yaml` for the legacy file backend).

### Migration Examples

```bash
# SQLite to MySQL (most common)
/economy migrate sqlite mysql

# MySQL to SQLite (downgrading)
/economy migrate mysql sqlite
```

### Migration Process

1. **Pre-check** — verifies the target backend differs from the current one
2. **Initialization** — creates a temporary storage provider for the target backend
3. **Reading** — loads all player data from the source backend
4. **Writing** — saves all player data to the target backend
5. **Verification** — confirms the migration succeeded
6. **Completion** — updates `config.yml` with the new storage type

### Best Practices

Back up your data before migrating. Warn players the server may lag briefly. Run the command from console or in-game, wait for completion, then restart to confirm a clean state.

### Troubleshooting

| Issue | Solution |
|-------|----------|
| "Already using X storage!" | You are migrating to the backend already in use |
| "Migration failed!" | Check the console for errors. Verify MySQL credentials |
| Data missing after migration | Check the source backend data. Restore from backup |
| Server lag during migration | Normal for large databases. Migration is async |

---

## Backup and Restore

### Manual Backups

Run `/economy backup` to create a timestamped backup in `plugins/DZEconomy/backups/`.

### File-Level Backups

Copy the whole data folder for a complete backup:

```bash
cp -r plugins/DZEconomy/ backups/DZEconomy_$(date +%Y%m%d_%H%M%S)/
```

### Restoring from Backup

1. Stop the server.
2. Back up the current `plugins/DZEconomy/` folder, just in case.
3. Replace the data files with the backup.
4. Start the server.

For SQLite:

```bash
cp backups/DZEconomy_20250101/data.db plugins/DZEconomy/data.db
```

For MySQL:

```bash
mysql -u dzeconomy_user -p dzeconomy < backup.sql
```

---

## Performance Tips

| Tip | Impact | Details |
|-----|--------|---------|
| Use MySQL for 50+ players | High | Better write concurrency and query performance |
| Increase auto-save interval | Medium | Less disk I/O. Risk of losing recent data on crash |
| Keep SQLite on an SSD | Medium | Faster writes for file-based storage |
| Use Paper or Folia | High | Async chunk loading improves overall performance |

### SQLite Tuning

```yaml
auto-save:
  interval: 600          # 10 minutes instead of 5
```

- Keep `data.db` on an SSD.
- For databases over 50MB, consider migrating to MySQL.
- Periodically back up and recreate the database to shrink the file.

### MySQL Tuning

```yaml
storage:
  mysql:
    pool-size: 15        # Adjust based on player count
```

MySQL server tuning (in `my.cnf`):

```ini
[mysqld]
innodb_buffer_pool_size = 256M
innodb_log_file_size = 64M
max_connections = 100
```

### Caching

DZEconomy caches player data in memory for fast access:

- **Online players** — always cached
- **Offline players** — loaded on demand, not cached by default
- **PlaceholderAPI** — 3-second placeholder cache with automatic eviction

---

<p align="center">
  See <a href="Configuration.md">Configuration</a> for storage-related config options.
</p>

---
### Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***