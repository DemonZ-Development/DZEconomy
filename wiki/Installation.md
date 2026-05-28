# 📦 Installation Guide

Complete setup instructions for DZEconomy v2.0.0 on all supported server platforms.

---

## 📋 Requirements

| Requirement | Details |
|-------------|---------|
| **Java** | 17+ required, **21 recommended** |
| **Minecraft** | 1.16+ (varies by server software) |
| **Server Software** | Bukkit, Spigot, Paper, Folia, or Purpur |

---

## 🔧 Installation Steps

### Step 1: Download

Download the latest `DZEconomy-2.0.0.jar` from [Modrinth](https://modrinth.com/plugin/dzeconomy).

### Step 2: Install the Plugin

Place the jar file in your server's `plugins/` directory:

```
server/
├── plugins/
│   └── DZEconomy-2.0.0.jar   ← Place here
├── server.jar
└── ...
```

### Step 3: Start the Server

Start (or restart) your server. DZEconomy will generate its default configuration files on first run.

### Step 4: Verify

Look for the following in your console:

```
  ____             _______  __
 |  _ \ _ __ ___ |__ / _ \|  \/  |
 | | | | '__/ _ \  | | | | |\/| |
 | |_| | | | (_) | | |_| | |  | |
 |____/|_|  \___/   \___/|_|  |_|

  Version 2.0.0 | By DemonZ Development
[INFO] DZEconomy v2.0.0 has been successfully enabled!
[INFO] Running on Paper 1.21.4-...
```

### Step 5: Configure

Edit `plugins/DZEconomy/config.yml` to match your server's needs. See the [Configuration](Configuration.md) page for all options.

---

## 🖥️ Platform-Specific Instructions

### Bukkit

| Item | Details |
|------|---------|
| Supported Versions | 1.16 – 1.21.5 |
| API Version | `1.20` |
| Notes | Basic support; no async chunk loading |

1. Place the jar in `plugins/`
2. Restart the server
3. No additional configuration needed

### Spigot

| Item | Details |
|------|---------|
| Supported Versions | 1.16 – 1.21.5 |
| API Version | `1.20` |
| Notes | Full support |

1. Place the jar in `plugins/`
2. Restart the server
3. No additional configuration needed

### Paper

| Item | Details |
|------|---------|
| Supported Versions | 1.16.5 – 1.21.5 |
| API Version | `1.20` |
| Notes | **Recommended** — best performance |

1. Place the jar in `plugins/`
2. Restart the server
3. Paper's async chunk loading improves performance

### Folia

| Item | Details |
|------|---------|
| Supported Versions | 1.19.4 – 1.21.5 |
| API Version | `1.20` |
| Notes | Full region-based scheduling support |

1. Place the jar in `plugins/`
2. Restart the server
3. DZEconomy automatically detects Folia and uses region-based schedulers
4. See [Folia Support](Folia-Support.md) for details

### Purpur

| Item | Details |
|------|---------|
| Supported Versions | 1.16.5 – 1.21.5 |
| API Version | `1.20` |
| Notes | Full support (inherits Paper features) |

1. Place the jar in `plugins/`
2. Restart the server
3. No additional configuration needed

---

## 📚 Optional Dependencies

### PlaceholderAPI

| | |
|---|---|
| **Required?** | No (optional) |
| **Purpose** | Provides placeholders for scoreboards, chat, tab lists, etc. |
| **Download** | [SpigotMC](https://www.spigotmc.org/resources/placeholderapi.6245/) |

When PlaceholderAPI is installed, DZEconomy automatically registers the `dz` expansion:

```
%dz_money%          → Player's Money balance
%dz_mobcoin%        → Player's MobCoin balance
%dz_gem%            → Player's Gem balance
%dz_money_short%    → Short-form Money balance (e.g. 1.5K)
%dz_rank%           → Player's rank display name
%dz_combat_tagged%  → "Yes" or "No"
%dz_combat_time%    → Remaining combat tag seconds
```

See [API](API.md) for the full placeholder list.

### LuckPerms

| | |
|---|---|
| **Required?** | No (optional) |
| **Purpose** | Enables rank detection for the multiplier system |
| **Download** | [LuckPerms](https://luckperms.net/) |

When LuckPerms is installed:
- DZEconomy detects the player's LuckPerms group
- Applies rank multipliers from `ranks.yml`
- Grants rank-specific perks (reduced cooldowns, increased limits, etc.)

Without LuckPerms:
- All players use the `default` rank
- Rank multipliers still work for the default rank

---

## 🔄 First-Time Setup

After installation, DZEconomy creates the following file structure:

```
plugins/DZEconomy/
├── config.yml          → Main configuration
├── messages.yml        → All translatable messages
├── ranks.yml           → Rank definitions and multipliers
├── mob-rewards.yml     → Mob kill reward configuration
├── data.db             → SQLite database (default storage)
└── transactions.log    → Transaction log (if enabled)
```

### Recommended First Steps

1. **Set your storage backend** — Edit `storage.type` in `config.yml` (default: `SQLITE`)
2. **Configure currencies** — Adjust starting balances, symbols, and limits
3. **Set up ranks** — Edit `ranks.yml` to match your LuckPerms groups
4. **Configure mob rewards** — Edit `mob-rewards.yml` for your server's economy
5. **Customize messages** — Edit `messages.yml` for your server's language/theme
6. **Reload** — Run `/economy reload` to apply changes without restarting

---

## ⬆️ Updating from v1 to v2

DZEconomy v2.0.0 includes an **automatic configuration migration system**. When you update:

### Automatic Migration

1. **Back up your `plugins/DZEconomy/` folder** before updating
2. Replace the old jar with the new `DZEconomy-2.0.0.jar`
3. Start the server — the `ConfigMigrator` will automatically:
   - Detect the old `config-version: 1`
   - Migrate all settings to the new v2 format
   - Preserve your existing values
   - Update `config-version` to `2`

### Key Changes in v2

| Feature | v1 | v2 |
|---------|----|----|
| Currencies | Single currency | 3 currencies (Money, MobCoins, Gems) |
| Storage | SQLite only | SQLite, MySQL, Flat File |
| Ranks | None | LuckPerms-based rank system |
| Combat Tag | None | Full combat tag system |
| Mob Rewards | None | Configurable mob rewards with kill streaks |
| PVP Loot | None | Percentage-based PVP loot |
| Folia | Not supported | Full Folia support |
| API | None | Public API with JitPack |
| Messages | Hardcoded | Fully customizable `messages.yml` |
| Placeholders | None | PlaceholderAPI expansion (15+ placeholders) |
| Conversion | N/A | Currency conversion with rates and fees |
| Payment Requests | N/A | Request/accept/deny system with GUI |

### Manual Steps After Migration

1. Review `config.yml` for new options and adjust as needed
2. Set up `ranks.yml` if you want rank multipliers
3. Configure `mob-rewards.yml` for mob kill rewards
4. Customize `messages.yml` for your server

> ⚠️ **Important**: If you were using a custom economy database schema from v1, run `/economy migrate` to migrate your data to the v2 schema.

---

## 🐛 Troubleshooting

### Plugin Won't Enable

**Symptom**: Console shows `Failed to initialize storage! Disabling plugin...`

**Solutions**:
1. Check that `storage.type` in `config.yml` is valid (`SQLITE`, `MYSQL`)
2. For MySQL, verify the connection credentials (host, port, username, password)
3. Check that the MySQL server is accessible from the Minecraft server
4. Ensure the `data.db` file is not read-only (SQLite)

### Commands Not Working

**Symptom**: `/money` returns "Unknown command"

**Solutions**:
1. Verify the plugin is enabled: `/plugins` (should show green `DZEconomy`)
2. Check that `plugin.yml` loaded correctly — no errors in console
3. Another plugin may be overriding the command — check with `/money` aliases like `/bal`

### PlaceholderAPI Placeholders Not Working

**Symptom**: `%dz_money%` shows as-is, not replaced

**Solutions**:
1. Ensure PlaceholderAPI is installed and enabled
2. Check the expansion registered: `/papi ecloud download dz` then `/papi reload`
3. DZEconomy should auto-register on startup — check console for "PlaceholderAPI integration enabled!"

### LuckPerms Ranks Not Applying

**Symptom**: All players show as "Default" rank

**Solutions**:
1. Ensure LuckPerms is installed and running
2. Check console for "LuckPerms integration enabled!"
3. Verify your LuckPerms group names match the keys in `ranks.yml` (case-sensitive)
4. Run `/economy reload` after modifying `ranks.yml`

### MySQL Connection Issues

**Symptom**: `Communications link failure` or `Access denied`

**Solutions**:
1. Verify MySQL is running: `systemctl status mysql`
2. Check credentials in `config.yml` under `storage.mysql`
3. Ensure the database exists: `CREATE DATABASE dzeconomy;`
4. Check firewall rules allow connections on port 3306
5. Add `?useSSL=false` to the connection parameters if SSL is not configured
6. Increase `connection-timeout` if the MySQL server is slow to respond

### Folia-Related Issues

**Symptom**: Tasks not executing, errors about "not on the main thread"

**Solutions**:
1. Ensure you're running DZEconomy v2.0.0+ (Folia support was added in v2)
2. DZEconomy uses `FoliaAdapter` for all scheduling — no manual changes needed
3. Check console for "Folia detected! Using region-based scheduling."
4. See [Folia Support](Folia-Support.md) for known limitations

### Performance Issues

**Symptom**: Server lag when many players are online

**Solutions**:
1. Switch from SQLite to MySQL for large servers (50+ players)
2. Increase `auto-save.interval` (default: 300 seconds)
3. Set `auto-save.save-on-transaction: false` if it's currently true
4. Reduce `baltop.refresh-interval` (default: 300 seconds)
5. Enable `misc.debug: true` temporarily to identify slow operations
6. See [Storage](Storage.md) for detailed performance optimization

---

<p align="center">
  Need more help? Join our <a href="https://discord.com/invite/GYsTt96ypf">Discord</a> for support!
</p>

---
<p align="center">
  <b>DZEconomy Wiki</b> • Developed by <a href="https://github.com/DemonZ-Development">DemonZ Development</a><br>
  <a href="https://github.com/DemonZ-Development/DZEconomy">GitHub Repository</a> | 
  <a href="https://discord.com/invite/GYsTt96ypf">Discord Support</a> | 
  <a href="https://github.com/DemonZ-Development/DZEconomy/wiki/Home">Wiki Home</a>
</p>
