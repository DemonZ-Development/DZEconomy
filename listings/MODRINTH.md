![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# DZEconomy — Multi-Currency Economy Plugin

**Three currencies, rank multipliers, combat tagging, mob rewards, and full Folia support. Runs on Minecraft 1.9 through 1.26.x.**

---

## Features

**Three Currencies** — Money ($), MobCoins (⛃), and Gems (◆). Each fully configurable: symbol, decimal places, starting balance, max balance, and transaction limits. Disable any currency to hide its commands.

**Rank Multipliers** — LuckPerms integration with per-currency earning bonuses. Ranks can grant reduced transfer cooldowns, higher daily limits, combat tag bypass, and interest on held balances.

**Combat Tagging** — Economy actions lock while you are in combat, with a configurable tag duration and an action bar indicator.

**PvP Loot** — Kill players to take a percentage of their balance, with per-currency loss rates, minimum balance protection, and world blacklists.

**Mob Rewards** — Configurable rewards for killing mobs across four categories (neutral, easy, hard, boss), with kill streak bonuses and rank multipliers.

**Currency Conversion** — Convert between any two currencies with configurable exchange rates and a transaction fee.

**Balance Leaderboards** — Per-currency and global baltop with pagination.

**Payment Requests** — Request, accept, and deny payments, with a timeout and a pending request cap.

**PlaceholderAPI Support** — 15+ placeholders with 5-second caching for scoreboards, chat, and tab lists.

**Two Storage Backends** — SQLite by default, or MySQL with connection pooling. Live migration between backends with `/economy migrate`.

**Folia Compatible** — Works on Folia without any configuration. Full region-based scheduling with automatic detection at startup.

**Update Checker** — Automatic Modrinth update notifications for admins.

**Auto-Save and Backups** — Configurable save intervals and manual backup creation.

**Daily Limits and Cooldowns** — Per-currency transfer limits and cooldowns to keep the economy stable.

**Fully Configurable** — Every message, format, and behavior is editable in `messages.yml`.

---

## Quick Start

1. Download and place `DZEconomy-2.1.2.jar` in your `plugins/` folder
2. Restart your server
3. Players can immediately use `/money`, `/mobcoin`, `/gem`
4. Customize `config.yml`, `ranks.yml`, and `mob-rewards.yml`
5. Run `/economy reload` to apply changes

New players start with **$500.00** by default. That is configurable.

---

## Commands Quick Reference

### Currency Commands (`/money`, `/mobcoin`, `/gem`)

| Command | Description |
|---------|-------------|
| `/<currency>` | Check your balance |
| `/<currency> balance [player]` | Check balance (self or others) |
| `/<currency> send <player> <amount>` | Send currency to a player |
| `/<currency> request <player> <amount>` | Request currency from a player |
| `/<currency> accept <player>` | Accept a payment request |
| `/<currency> deny <player>` | Deny a payment request |
| `/<currency> top [page]` | View balance leaderboard |
| `/<currency> add <player> <amount>` | Add currency (admin) |
| `/<currency> remove <player> <amount>` | Remove currency (admin) |
| `/<currency> set <player> <amount>` | Set balance (admin) |

### Admin Commands (`/economy`)

| Command | Description |
|---------|-------------|
| `/economy info` | View plugin information |
| `/economy credits` | View credits |
| `/economy reload` | Reload configuration |
| `/economy version` | Version information |
| `/economy status` | Plugin status and stats |
| `/economy convert <player> <from> <to> <amount>` | Convert currency |
| `/economy give <player> <amount> [currency]` | Give currency from console or chat |
| `/economy migrate <from> <to>` | Migrate storage backend |
| `/economy baltop [currency] [page]` | Global leaderboard |
| `/economy payall <currency> <amount>` | Pay all online players |
| `/economy backup` | Create data backup |

---

## Requirements

| Requirement | Minimum |
|-------------|---------|
| Java | 8 or newer (21 recommended) |
| Minecraft | 1.9 or newer (1.20+ recommended) |
| Server Software | Bukkit, Spigot, Paper, Folia, or Purpur |

The same jar runs on servers from Java 8 all the way up to Java 21+, and from Minecraft 1.9 through 1.26.x. A compatibility layer handles the version differences for you.

## Optional Dependencies

| Plugin | Purpose | Required? |
|--------|---------|-----------|
| [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi) | Placeholders for scoreboards, chat, etc. | No |
| [LuckPerms](https://modrinth.com/plugin/luckperms) | Rank detection for the multiplier system | No |

---

## Links

| Resource | URL |
|----------|-----|
| Wiki | [GitHub Wiki](https://github.com/DemonZ-Development/DZEconomy/wiki) |
| Source Code | [GitHub](https://github.com/DemonZ-Development/DZEconomy) |
| Issue Tracker | [GitHub Issues](https://github.com/DemonZ-Development/DZEconomy/issues) |
| Website | [demonzdevelopment.online](https://demonzdevelopment.online) |

---

## License

GNU General Public License v3.0

---

*DZEconomy v2.1.2 by DemonZ Development*
