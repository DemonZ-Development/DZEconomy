# 🏦 DZEconomy Wiki

> **A powerful multi-currency economy plugin for Minecraft** — by [DemonZ Development](https://demonzdevelopment.online)

---

## ✨ Overview

DZEconomy is a feature-rich, high-performance economy plugin that introduces **three distinct currencies**, a **rank multiplier system**, **combat tagging**, **PVP loot**, and **mob rewards** — all in one plugin. Built from the ground up for modern servers, it supports **Bukkit, Spigot, Paper, Folia, and Purpur** with full Folia region-based scheduling compatibility.

### 💰 Three Currencies

| Currency | Symbol | Description |
|----------|--------|-------------|
| **Money** | `$` | Primary currency — starting balance, PVP loot, transfers |
| **MobCoins** | `⛃` | Earned by killing mobs — secondary economy |
| **Gems** | `◆` | Premium currency — rare boss drops, conversion |

### 🌟 Key Features

- 💵 **Multi-Currency System** — Money, MobCoins, and Gems, each independently configurable
- 🏆 **Rank Multipliers** — LuckPerms integration with per-rank earning bonuses and perks
- ⚔️ **Combat Tagging** — Block economy actions during combat with action bar indicator
- 💀 **PVP Loot** — Kill players to steal a percentage of their balance
- 🐷 **Mob Rewards** — Configurable rewards for killing mobs with kill streak bonuses
- 🔄 **Currency Conversion** — Players or admins can convert between currencies with configurable rates and fees
- 📊 **Balance Top** — Per-currency and global leaderboards with pagination
- 💸 **Payment Requests** — Request, accept, and deny payments with timeout and GUI
- 🌐 **PlaceholderAPI Support** — 15+ placeholders with 3-second caching
- 🗄️ **Multiple Storage Backends** — SQLite, MySQL (HikariCP), and Flat File with live migration
- 🚀 **Folia Compatible** — Full region-based scheduling support out of the box
- 🔔 **Update Checker** — Automatic Modrinth API update notifications
- 💾 **Auto-Save & Backups** — Configurable auto-save intervals and manual backups
- 🎯 **Daily Limits & Cooldowns** — Per-currency transfer limits and cooldowns
- 📜 **Transaction Logging** — Optional audit log with file rotation
- 🔧 **Fully Configurable** — Every message, format, and behavior can be customized

---

## 🚀 Quick Start

### 1. Install the Plugin

1. Download the latest **DZEconomy v2.0.0** jar from [Modrinth](https://modrinth.com/plugin/dzeconomy)
2. Place it in your server's `plugins/` folder
3. Restart your server (or use a plugin manager)

### 2. Verify Installation

```
[INFO] DZEconomy v2.0.0 has been successfully enabled!
[INFO] Running on Paper 1.21.4-...
```

### 3. Start Playing!

Players can immediately use:
```
/money          → Check their Money balance
/mobcoin        → Check their MobCoin balance
/gem            → Check their Gem balance
/money send Steve 100  → Send $100 to Steve
```

> 💡 **New players** start with **$500.00** by default (configurable in `config.yml`)

---

## 📖 Wiki Pages

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

## 📸 Screenshots

| | |
|---|---|
| ![Balance Display](../assets/screenshots/balance_display.png) | ![Baltop Leaderboard](../assets/screenshots/baltop_leaderboard.png) |
| *Balance Display* | *Baltop Leaderboard* |
| ![Combat Tag Indicator](../assets/screenshots/combat_tag.png) | ![Mob Reward Notification](../assets/screenshots/mob_reward.png) |
| *Combat Tag Indicator* | *Mob Reward Notification* |
| ![Payment Request](../assets/screenshots/payment_request.png) | ![Admin Status Panel](../assets/screenshots/admin_status.png) |
| *Payment Request GUI* | *Admin Status Panel* |

---

## 🔗 Links

| Resource | Link |
|----------|------|
| 📦 Download | [Modrinth](https://modrinth.com/plugin/dzeconomy) |
| 📖 Wiki | [GitHub Wiki](https://github.com/DemonZDevelopment/DZEconomy/wiki) |
| 💬 Discord | [discord.gg/dzeconomy](https://discord.gg/dzeconomy) |
| 🐛 Issue Tracker | [GitHub Issues](https://github.com/DemonZDevelopment/DZEconomy/issues) |
| 📝 Source Code | [GitHub](https://github.com/DemonZDevelopment/DZEconomy) |
| 🌐 Website | [demonzdevelopment.online](https://demonzdevelopment.online) |

---

## 📜 License

DZEconomy is licensed under the **GNU General Public License v3.0**. See [LICENSE](https://www.gnu.org/licenses/gpl-3.0.txt) for details.

---

<p align="center">
  <strong>DZEconomy v2.0.0</strong> — Made with ❤️ by <a href="https://demonzdevelopment.online">DemonZ Development</a>
</p>
