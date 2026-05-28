![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# DZEconomy

**Advanced Multi-Currency Economy Plugin** — v2.1.0 by DemonZ Development

---

## About

DZEconomy is a comprehensive economy plugin introducing **three distinct currencies** — Money, MobCoins, and Gems — with rank multipliers, combat tagging, PvP loot, mob rewards, and full Folia support. Every feature is configurable through YAML files, and all messages can be customized.

---

## Key Features

- **3 Currencies** — Money ($), MobCoins (⛃), Gems (◆) — each with independent symbols, decimals, starting balance, max balance, and transaction limits
- **Rank Multipliers** — LuckPerms integration for per-currency earning bonuses, cooldown reduction, daily limit increases, combat tag bypass, and passive interest
- **Combat Tagging** — Block economy actions during PvP with configurable duration and action bar indicator
- **PvP Loot** — Kill players to steal a configurable % of their balance with minimum balance protection and world blacklists
- **Mob Rewards** — Configurable per-mob drops across 4 categories with kill streak bonuses and event multipliers
- **Currency Conversion** — Convert between currencies with configurable rates and fees
- **Payment Requests** — Request/accept/deny with timeout, max pending, sounds, and GUI
- **Balance Leaderboards** — Per-currency and global baltop with pagination, offline support, and cached refresh
- **API & Placeholders** — Public developer API + 15+ PlaceholderAPI placeholders with 3s cache
- **3 Storage Backends** — SQLite (default), MySQL (HikariCP), Flat File — live migration between them
- **Folia Native** — Full region-based scheduling with automatic detection
- **Legacy Support** — Server Adapter system for broad version compatibility
- **Auto-Save & Backups** — Configurable intervals and manual backup creation

---

## Installation

1. Download `DZEconomy-2.1.0.jar`
2. Place in `plugins/`
3. Restart server
4. Run `/economy reload` after configuring

**Requirements:** Java 17+ (21 recommended), Minecraft 1.16+, Bukkit/Spigot/Paper/Folia/Purpur

---

## Quick Start

```
/money           → Check balance
/money send Steve 100  → Send $100
/money top       → Leaderboard
/mobcoin         → MobCoin balance
/gem             → Gem balance
/economy reload  → Apply config changes
```

---

## Configuration

**config.yml** — Storage, currencies, transfer limits, conversion rates, PvP, combat tag, mob rewards, baltop, auto-save, updates
**messages.yml** — Every message customizable with color codes, hex colors, MiniMessage gradients
**ranks.yml** — Rank definitions with per-currency multipliers, perks, and interest (requires LuckPerms)
**mob-rewards.yml** — Per-mob reward config with categories, kill streaks, and event multipliers

---

## Links

- **Wiki:** https://github.com/DemonZ-Development/DZEconomy/wiki
- **GitHub:** https://github.com/DemonZ-Development/DZEconomy
- **Issues:** https://github.com/DemonZ-Development/DZEconomy/issues
- **Website:** https://demonzdevelopment.online

---

*DZEconomy v2.1.0 — Licensed under GNU GPLv3 — Made by DemonZ Development*
