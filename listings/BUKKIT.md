![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# DZEconomy

**Multi-currency economy plugin** — v2.1.2 by DemonZ Development

---

## About

DZEconomy gives you three currencies out of the box: Money, MobCoins, and Gems. Rank multipliers, combat tagging, PvP loot, and mob rewards come standard. Every feature is configurable through YAML files, and all messages can be customized.

---

## Key Features

- **3 Currencies** — Money ($), MobCoins (⛃), Gems (◆), each with independent symbols, decimals, starting balance, max balance, and transaction limits
- **Rank Multipliers** — LuckPerms integration for per-currency earning bonuses, plus cooldown reduction, daily limit increases, combat tag bypass, and interest
- **Combat Tagging** — Blocks economy actions during PvP, with a configurable duration and action bar indicator
- **PvP Loot** — Kill players to take a configurable percentage of their balance, with minimum balance protection and world blacklists
- **Mob Rewards** — Per-mob drops across 4 categories with kill streak bonuses and rank multipliers
- **Currency Conversion** — Convert between currencies with configurable rates and fees
- **Payment Requests** — Request, accept, and deny with a timeout and pending request cap
- **Balance Leaderboards** — Per-currency and global baltop with pagination
- **Placeholders** — 15+ PlaceholderAPI placeholders with 5-second caching
- **2 Storage Backends** — SQLite by default, MySQL with connection pooling. Live migration between them
- **Folia Native** — Full region-based scheduling with automatic detection
- **Legacy Support** — Same jar runs from Java 8 and Minecraft 1.9 upward
- **Auto-Save and Backups** — Configurable intervals and manual backup creation

---

## Installation

1. Download `DZEconomy-2.1.2.jar`
2. Place it in `plugins/`
3. Restart the server
4. Run `/economy reload` after configuring

**Requirements:** Java 8+ (21 recommended), Minecraft 1.9+, Bukkit/Spigot/Paper/Folia/Purpur

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

**config.yml** — Storage, currencies, transfer limits, conversion rates, PvP, combat tag, mob rewards, requests, updates
**messages.yml** — Every message customizable with color codes, hex colors, MiniMessage gradients
**ranks.yml** — Rank definitions with per-currency multipliers, perks, and interest (requires LuckPerms)
**mob-rewards.yml** — Per-mob reward config with categories and kill streaks

---

## Links

- **Wiki:** https://github.com/DemonZ-Development/DZEconomy/wiki
- **GitHub:** https://github.com/DemonZ-Development/DZEconomy
- **Issues:** https://github.com/DemonZ-Development/DZEconomy/issues
- **Website:** https://demonzdevelopment.online

---

*DZEconomy v2.1.2 — Licensed under GNU GPLv3 — Made by DemonZ Development*