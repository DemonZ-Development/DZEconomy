![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# Installation Guide

Setup instructions for DZEconomy v2.1.2 on all supported server platforms.

---

## Requirements

| Requirement | Details |
|-------------|---------|
| **Java** | 8 or newer (21 recommended) |
| **Minecraft** | 1.9+ (varies by server software) |
| **Server Software** | Bukkit, Spigot, Paper, Folia, or Purpur |

The jar compiles to Java 8 bytecode, so it loads on any server running Java 8 and up. A FeatureAdapter layer handles the version differences in the Bukkit API.

---

## Installation Steps

### 1. Download

Download the latest `DZEconomy-2.1.2.jar` from [Modrinth](https://modrinth.com/plugin/dzeconomy).

### 2. Place the Jar

Put it in your server's `plugins/` directory:

```
server/
├── plugins/
│   └── DZEconomy-2.1.2.jar   ← Place here
├── server.jar
└── ...
```

### 3. Start the Server

Start (or restart) the server. DZEconomy generates its default config files on first run.

### 4. Verify

Look for this in the console:

```
[INFO] DZEconomy v2.1.2 has been successfully enabled!
[INFO] Running on Paper 1.21.4-...
```

### 5. Configure

Edit `plugins/DZEconomy/config.yml` to fit your server. See the [Configuration](Configuration.md) page for every option.

---

## Platform-Specific Instructions

### Bukkit

| Item | Details |
|------|---------|
| Supported Versions | 1.9 – 1.26.x |
| API Version | `1.13` |
| Notes | Basic support; no async chunk loading |

Place the jar in `plugins/`, restart, done.

### Spigot

| Item | Details |
|------|---------|
| Supported Versions | 1.9 – 1.26.x |
| API Version | `1.13` |
| Notes | Full support |

Place the jar in `plugins/`, restart, done.

### Paper

| Item | Details |
|------|---------|
| Supported Versions | 1.9 – 1.26.x |
| API Version | `1.13` |
| Notes | **Recommended** — best performance |

Place the jar in `plugins/`, restart. Paper's async chunk loading helps performance.

### Folia

| Item | Details |
|------|---------|
| Supported Versions | 1.19.4 – 1.26.x |
| API Version | `1.13` |
| Notes | Full region-based scheduling support |

Place the jar in `plugins/`, restart. DZEconomy detects Folia automatically and uses region-based schedulers. See [Folia Support](Folia-Support.md).

### Purpur

| Item | Details |
|------|---------|
| Supported Versions | 1.9 – 1.26.x |
| API Version | `1.13` |
| Notes | Full support (inherits Paper features) |

Place the jar in `plugins/`, restart, done.

---

## Optional Dependencies

### PlaceholderAPI

| | |
|---|---|
| **Required?** | No |
| **Purpose** | Placeholders for scoreboards, chat, tab lists |
| **Download** | [SpigotMC](https://www.spigotmc.org/resources/placeholderapi.6245/) |

With PlaceholderAPI installed, DZEconomy registers the `dz` expansion:

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
| **Required?** | No |
| **Purpose** | Rank detection for the multiplier system |
| **Download** | [LuckPerms](https://luckperms.net/) |

With LuckPerms installed:
- DZEconomy detects the player's LuckPerms group
- Applies rank multipliers from `ranks.yml`
- Grants rank perks (reduced cooldowns, higher limits, etc.)

Without LuckPerms:
- Everyone uses the `default` rank
- Default rank multipliers still apply

---

## First-Time Setup

After installation, DZEconomy creates this structure:

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

1. **Set your storage backend** — `storage.type` in `config.yml` (default: `SQLITE`)
2. **Configure currencies** — Starting balances, symbols, and limits
3. **Set up ranks** — Edit `ranks.yml` to match your LuckPerms groups
4. **Configure mob rewards** — Edit `mob-rewards.yml` for your economy
5. **Customize messages** — Edit `messages.yml` for your language and theme
6. **Reload** — `/economy reload` applies changes without a restart

---

## Troubleshooting

### Plugin Won't Enable

**Symptom**: Console shows `Failed to initialize storage! Disabling plugin...`

**Solutions**:
1. Check `storage.type` in `config.yml` is valid (`SQLITE`, `MYSQL`, `FLATFILE`)
2. For MySQL, verify the connection credentials
3. Check the MySQL server is reachable from the Minecraft server
4. Make sure `data.db` is not read-only (SQLite)

### Commands Not Working

**Symptom**: `/money` returns "Unknown command"

**Solutions**:
1. Verify the plugin is enabled: `/plugins` should show green `DZEconomy`
2. Check the console for errors on startup
3. Another plugin may override the command. Try the `/bal` alias.

### PlaceholderAPI Placeholders Not Working

**Symptom**: `%dz_money%` shows as-is

**Solutions**:
1. Confirm PlaceholderAPI is installed and enabled
2. Check the expansion registered: `/papi ecloud download dz` then `/papi reload`
3. DZEconomy auto-registers on startup — look for "PlaceholderAPI integration enabled!"

### LuckPerms Ranks Not Applying

**Symptom**: Everyone shows as "Default"

**Solutions**:
1. Confirm LuckPerms is installed and running
2. Check the console for "LuckPerms integration enabled!"
3. Verify your LuckPerms group names match the keys in `ranks.yml` (case-sensitive)
4. Run `/economy reload` after editing `ranks.yml`

### MySQL Connection Issues

**Symptom**: `Communications link failure` or `Access denied`

**Solutions**:
1. Verify MySQL is running: `systemctl status mysql`
2. Check credentials under `storage.mysql` in `config.yml`
3. Ensure the database exists: `CREATE DATABASE dzeconomy;`
4. Check firewall rules allow port 3306
5. Add `?useSSL=false` to the connection parameters if SSL is not configured
6. Increase `connection-timeout` if the MySQL server is slow to respond

### Folia-Related Issues

**Symptom**: Tasks not executing, "not on the main thread" errors

**Solutions**:
1. Run DZEconomy v2.1.2+ (Folia support exists since v2)
2. DZEconomy uses `FoliaAdapter` for all scheduling — no manual changes needed
3. Look for "Folia detected! Using region-based scheduling." in the console
4. See [Folia Support](Folia-Support.md) for known limitations

### Performance Issues

**Symptom**: Server lag with many players online

**Solutions**:
1. Switch from SQLite to MySQL for 50+ players
2. Increase `auto-save.interval` (default: 300 seconds)
3. Enable `misc.debug: true` temporarily to find slow operations
4. See [Storage](Storage.md) for detailed optimization

---

<p align="center">
  Need help? Join our <a href="https://discord.com/invite/GYsTt96ypf">Discord</a>.
</p>

---
### Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***
