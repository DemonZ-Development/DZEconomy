![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# DZEconomy

**Advanced Multi-Currency Economy Plugin** — v2.0.0 by DemonZ Development

---

## About

DZEconomy is a comprehensive economy plugin that introduces three distinct currencies — Money, MobCoins, and Gems — with rank multipliers, combat tagging, PvP loot, mob rewards, and full Folia support. Every feature is configurable through YAML files, and all messages can be customized.

---

## Features

- **Three Currencies** — Money ($), MobCoins (⛃), and Gems (◆), each with independent configuration for symbols, decimal places, starting balances, maximum balances, and minimum transaction amounts
- **Rank Multipliers** — LuckPerms integration provides per-currency earning bonuses, reduced cooldowns, increased daily limits, combat tag bypass, and passive interest earnings
- **Combat Tagging** — Blocks economy actions (send, request, accept) during PvP combat with configurable duration and action bar indicator
- **PvP Loot** — Kill players to steal a configurable percentage of their balance with minimum balance protection and world blacklists
- **Mob Rewards** — Configurable rewards for killing mobs across 4 categories (neutral, easy, hard, boss) with kill streak bonuses and time-based event multipliers
- **Currency Conversion** — Convert between currencies with configurable exchange rates and transaction fees
- **Payment Requests** — Request, accept, and deny payments with timeout, max pending limits, and sound notifications
- **Balance Leaderboards** — Per-currency and global baltop with pagination, offline player support, and cached refresh
- **PlaceholderAPI Support** — 15+ placeholders with 3-second caching for scoreboards, chat, and tab lists
- **Multiple Storage Backends** — SQLite (default), MySQL with HikariCP connection pooling, and Flat File with live migration between backends
- **Folia Support** — Full region-based scheduling compatibility with automatic Folia detection
- **Auto-Save & Backups** — Configurable auto-save intervals and manual backup creation
- **Transaction Logging** — Optional audit log with file rotation for compliance and debugging
- **Fully Customizable** — Every message, format, and behavior can be customized in messages.yml

---

## Commands

### Currency Commands

Applies to `/money` (aliases: `/bal`, `/balance`), `/mobcoin` (aliases: `/mobcoins`, `/mc`), and `/gem` (aliases: `/gems`).

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/<currency>` | Check your balance | `dzeconomy.<currency>.balance` | true |
| `/<currency> balance [player]` | Check balance (self or others) | `dzeconomy.<currency>.balance` | true |
| `/<currency> send <player> <amount>` | Send currency to a player | `dzeconomy.<currency>.send` | true |
| `/<currency> request <player> <amount>` | Request currency from a player | `dzeconomy.<currency>.request` | true |
| `/<currency> accept <player>` | Accept a payment request | `dzeconomy.<currency>.accept` | true |
| `/<currency> deny <player>` | Deny a payment request | `dzeconomy.<currency>.deny` | true |
| `/<currency> top [page]` | View balance leaderboard | `dzeconomy.<currency>.top` | true |
| `/<currency> add <player> <amount>` | Add currency to a player | `dzeconomy.<currency>.add` | op |
| `/<currency> remove <player> <amount>` | Remove currency from a player | `dzeconomy.<currency>.remove` | op |
| `/<currency> set <player> <amount>` | Set a player's balance | `dzeconomy.<currency>.set` | op |

### Admin Commands

`/economy` (aliases: `/econ`, `/dzeconomy`, `/dze`)

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/economy info` | View plugin information | `dzeconomy.economy.info` | true |
| `/economy credits` | View credits | None | true |
| `/economy reload` | Reload configuration | `dzeconomy.admin.reload` | op |
| `/economy version` | Version information | `dzeconomy.admin` | op |
| `/economy status` | Plugin status & statistics | `dzeconomy.admin.status` | op |
| `/economy convert <player> <from> <to> <amount>` | Convert currency for a player | `dzeconomy.admin.convert` | op |
| `/economy migrate <from> <to>` | Migrate storage backends | `dzeconomy.admin.migrate` | op |
| `/economy baltop [currency] [page]` | Global balance leaderboard | `dzeconomy.admin.baltop` | op |
| `/economy payall <currency> <amount>` | Pay all online players | `dzeconomy.admin.payall` | op |
| `/economy backup` | Create data backup | `dzeconomy.admin.backup` | op |

---

## Permissions

### Money Permissions

| Node | Description | Default |
|------|-------------|---------|
| `dzeconomy.money.balance` | Check own Money balance | true |
| `dzeconomy.money.balance.others` | Check others' Money balance | op |
| `dzeconomy.money.send` | Send Money | true |
| `dzeconomy.money.request` | Request Money | true |
| `dzeconomy.money.accept` | Accept Money requests | true |
| `dzeconomy.money.deny` | Deny Money requests | true |
| `dzeconomy.money.top` | View Money leaderboard | true |
| `dzeconomy.money.add` | Add Money to a player | op |
| `dzeconomy.money.remove` | Remove Money from a player | op |
| `dzeconomy.money.set` | Set a player's Money balance | op |

### MobCoin Permissions

| Node | Description | Default |
|------|-------------|---------|
| `dzeconomy.mobcoin.balance` | Check own MobCoin balance | true |
| `dzeconomy.mobcoin.balance.others` | Check others' MobCoin balance | op |
| `dzeconomy.mobcoin.send` | Send MobCoins | true |
| `dzeconomy.mobcoin.request` | Request MobCoins | true |
| `dzeconomy.mobcoin.accept` | Accept MobCoin requests | true |
| `dzeconomy.mobcoin.deny` | Deny MobCoin requests | true |
| `dzeconomy.mobcoin.top` | View MobCoin leaderboard | true |
| `dzeconomy.mobcoin.add` | Add MobCoins to a player | op |
| `dzeconomy.mobcoin.remove` | Remove MobCoins from a player | op |
| `dzeconomy.mobcoin.set` | Set a player's MobCoin balance | op |

### Gem Permissions

| Node | Description | Default |
|------|-------------|---------|
| `dzeconomy.gem.balance` | Check own Gem balance | true |
| `dzeconomy.gem.balance.others` | Check others' Gem balance | op |
| `dzeconomy.gem.send` | Send Gems | true |
| `dzeconomy.gem.request` | Request Gems | true |
| `dzeconomy.gem.accept` | Accept Gem requests | true |
| `dzeconomy.gem.deny` | Deny Gem requests | true |
| `dzeconomy.gem.top` | View Gem leaderboard | true |
| `dzeconomy.gem.add` | Add Gems to a player | op |
| `dzeconomy.gem.remove` | Remove Gems from a player | op |
| `dzeconomy.gem.set` | Set a player's Gem balance | op |

### Admin Permissions

| Node | Description | Default |
|------|-------------|---------|
| `dzeconomy.economy.info` | View plugin info | true |
| `dzeconomy.admin` | All admin permissions (parent) | op |
| `dzeconomy.admin.reload` | Reload configuration | op |
| `dzeconomy.admin.convert` | Convert currencies | op |
| `dzeconomy.admin.migrate` | Migrate storage | op |
| `dzeconomy.admin.status` | View plugin status | op |
| `dzeconomy.admin.baltop` | Global leaderboard | op |
| `dzeconomy.admin.payall` | Pay all online players | op |
| `dzeconomy.admin.update` | Update notifications | op |
| `dzeconomy.admin.backup` | Create backup | op |

---

## Configuration

### config.yml

Main plugin configuration covering:

- **Storage** — SQLite, MySQL, or Flat File with connection settings and pooling
- **Auto-Save** — Interval, save-on-transaction toggle
- **Currencies** — Enable/disable, symbol, decimal places, starting balance, max balance, min transaction, display format
- **Transfer Settings** — Max transaction, daily limits, cooldowns, combat tag blocking, self-transfer
- **Conversion Rates** — Per-currency-pair exchange rates and fee percentage
- **PvP** — Loss percentages, minimum balance, broadcast, world blacklist
- **Combat Tag** — Duration, blocked actions, PVE support, action bar format
- **Mob Rewards** — Global toggle, world filters, spawner/spawn egg filtering, default multiplier
- **Request Settings** — Timeout, max pending, auto-accept, notification type and sound
- **Baltop** — Entries per page, refresh interval, offline player inclusion, formatting
- **Payall** — Cooldown, broadcast, console access
- **Updates** — Check interval, admin notifications, Modrinth integration
- **Ranks** — Enable/disable, multiplier stacking mode
- **Misc** — Number format, debug mode, transaction logging

### messages.yml

Fully customizable messages with color code support, hex colors, and MiniMessage gradients. Sections include: prefix, balance, send, request, admin, error, economy, combat-tag, pvp, update, welcome, gui, baltop, help, mob-rewards, rank, and misc.

### ranks.yml

Rank definitions with per-currency multipliers, perks (reduced cooldowns, increased limits, combat tag bypass, interest), and additional permissions. Requires LuckPerms for group detection.

### mob-rewards.yml

Per-mob reward configuration with 4 categories (neutral, easy, hard, boss), custom mob support, global multipliers, kill streak bonuses, and time-based event multipliers.

---

## Installation

1. Download `DZEconomy-2.0.0.jar`
2. Place it in your server's `plugins/` folder
3. Restart the server
4. Configure the plugin files in `plugins/DZEconomy/`
5. Run `/economy reload` to apply changes

**Requirements:** Java 17+ (21 recommended), Minecraft 1.16+, Bukkit/Spigot/Paper/Folia/Purpur

**Optional Dependencies:**
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) — 15+ placeholders
- [LuckPerms](https://luckperms.net/) — Rank detection and multipliers

---

## Change Log

### v2.0.0 (Current)
- Complete rewrite with three-currency system (Money, MobCoins, Gems)
- Added rank multiplier system with LuckPerms integration
- Added combat tagging system with action bar indicator
- Added PvP loot system with configurable loss percentages
- Added mob rewards with kill streak bonuses and event multipliers
- Added currency conversion with rates and fees
- Added payment request system with GUI
- Added PlaceholderAPI expansion (15+ placeholders)
- Added MySQL storage with HikariCP connection pooling
- Added Flat File storage option
- Added live storage migration (`/economy migrate`)
- Added Folia support with region-based scheduling
- Added auto-save and backup system
- Added transaction logging with file rotation
- Added daily transfer limits and cooldowns
- Added fully customizable messages.yml
- Added Modrinth update checker
- Added baltop with pagination and caching

---

## bStats

![bStats](https://bstats.org/signatures/bukkit/DZEconomy.svg)

---

## Links

- **Wiki:** [GitHub Wiki](https://github.com/DemonZDevelopment/DZEconomy/wiki)
- **Discord:** [discord.com/invite/GYsTt96ypf](https://discord.com/invite/GYsTt96ypf)
- **GitHub:** [github.com/DemonZDevelopment/DZEconomy](https://github.com/DemonZDevelopment/DZEconomy)
- **Issues:** [github.com/DemonZDevelopment/DZEconomy/issues](https://github.com/DemonZDevelopment/DZEconomy/issues)
- **Website:** [demonzdevelopment.online](https://demonzdevelopment.online)

---

*DZEconomy v2.0.0 — Licensed under GNU GPLv3 — Made by DemonZ Development*
