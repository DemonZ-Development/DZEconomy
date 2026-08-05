![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# DZEconomy Wiki

A multi-currency economy plugin for Minecraft. By [DemonZ Development](https://demonzdevelopment.online).

---

## Overview

DZEconomy gives you three currencies, a rank multiplier system, combat tagging, PVP loot, and mob rewards in one plugin. It runs on Bukkit, Spigot, Paper, Folia, and Purpur, from Minecraft 1.9 through 1.26.x.

### The Three Currencies

| Currency | Symbol | What it does |
|----------|--------|--------------|
| **Money** | `$` | Primary currency. Starting balance, PVP loot, transfers. |
| **MobCoins** | `⛃` | Earned by killing mobs. Secondary economy. |
| **Gems** | `◆` | Premium currency. Rare boss drops, conversion. |

### Key Features

- **Multi-currency system** — Money, MobCoins, and Gems, each configured independently
- **Rank multipliers** — LuckPerms integration with per-rank earning bonuses
- **Combat tagging** — Blocks economy actions during combat, with action bar indicator
- **PVP loot** — Kill players to take a percentage of their balance
- **Mob rewards** — Configurable rewards per mob, with kill streak bonuses
- **Currency conversion** — Convert between currencies with rates and fees
- **Balance top** — Per-currency and global leaderboards with pagination
- **Payment requests** — Request, accept, and deny payments
- **PlaceholderAPI support** — 15+ placeholders with 3-second caching
- **Three storage backends** — SQLite, MySQL (HikariCP), and Flat File, with live migration
- **Folia compatible** — Region-based scheduling out of the box
- **Update checker** — Automatic notifications from Modrinth
- **Auto-save and backups** — Configurable intervals and manual backups
- **Daily limits and cooldowns** — Per-currency transfer limits
- **Fully configurable** — Every message, format, and behavior is changeable

---

## Quick Start

### 1. Install

1. Download the latest **DZEconomy v2.1.2** jar from [Modrinth](https://modrinth.com/plugin/dzeconomy)
2. Drop it in your server's `plugins/` folder
3. Restart the server

### 2. Verify

You should see this in the console:

```
[INFO] DZEconomy v2.1.2 has been successfully enabled!
[INFO] Running on Paper 1.21.4-...
```

### 3. Play

Players can immediately use:

```
/money          → Check their Money balance
/mobcoin        → Check their MobCoin balance
/gem            → Check their Gem balance
/money send Steve 100  → Send $100 to Steve
```

> New players start with **$500.00** by default. Change it under `currencies.money.starting-balance` in `config.yml`.

---

## Wiki Pages

| Page | Description |
|------|-------------|
| [Installation](Installation.md) | Setup guide for all server software |
| [Commands](Commands.md) | Full command reference |
| [Permissions](Permissions.md) | All permission nodes |
| [Configuration](Configuration.md) | Every config option explained |
| [Storage](Storage.md) | Database setup, migration, and optimization |
| [Ranks](Ranks.md) | Rank system and LuckPerms integration |
| [API](API.md) | Developer API and integration guide |
| [Folia Support](Folia-Support.md) | Folia compatibility details |
| [Version Coverage](Version-Coverage.md) | Supported Minecraft versions |

---

## Links

| Resource | Link |
|----------|------|
| Download | [Modrinth](https://modrinth.com/plugin/dzeconomy) |
| Wiki | [GitHub Wiki](https://github.com/DemonZ-Development/DZEconomy/wiki) |
| Discord | [discord.com/invite/GYsTt96ypf](https://discord.com/invite/GYsTt96ypf) |
| Issue Tracker | [GitHub Issues](https://github.com/DemonZ-Development/DZEconomy/issues) |
| Source Code | [GitHub](https://github.com/DemonZ-Development/DZEconomy) |
| Website | [demonzdevelopment.online](https://demonzdevelopment.online) |

---

## License

DZEconomy is licensed under the GNU General Public License v3.0. See [LICENSE](https://www.gnu.org/licenses/gpl-3.0.txt).

---

<p align="center">
  DZEconomy v2.1.2 — by <a href="https://demonzdevelopment.online">DemonZ Development</a>
</p>

---
### Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***
