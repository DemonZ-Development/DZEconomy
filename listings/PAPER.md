![DZEconomy Banner](../assets/banner.png)

# DZEconomy

**Multi-currency economy with ranks, combat tagging, mob rewards, and full Folia support.**

---

## Why Choose DZEconomy?

DZEconomy is a modern, feature-rich economy plugin built for Paper and Folia servers. Unlike single-currency economy plugins, DZEconomy provides **three fully configurable currencies** out of the box — Money, MobCoins, and Gems — each with independent settings, commands, and leaderboards.

### Key Highlights

- **3 Currencies** — Money ($), MobCoins (⛃), Gems (◆) — each independently configurable
- **Rank Multipliers** — LuckPerms integration with per-currency bonuses, cooldown reduction, and interest
- **Folia Native** — Full region-based scheduling with automatic detection and transparent adaptation
- **High Performance** — HikariCP MySQL pooling, async operations, cached leaderboards, 3-second placeholder cache
- **Combat Tagging** — Block economy actions during PvP with action bar indicator
- **PvP Loot** — Kill players to steal a configurable percentage of their balance
- **Mob Rewards** — Per-mob drops, kill streak bonuses, and time-based event multipliers
- **Fully Customizable** — Every message, format, and behavior editable in messages.yml

---

## Folia Support

DZEconomy has **first-class Folia support** with:

- Automatic Folia detection via `io.papermc.paper.threadedregions.RegionizedServer`
- `FoliaAdapter` utility for transparent scheduler adaptation
- Global, Region, and Entity scheduler support
- Thread-safe balance operations with per-player locks
- All features tested and working on Folia 1.19.4+

No configuration changes needed — DZEconomy adapts automatically.

---

## Quick Setup

1. Place `DZEconomy-2.0.0.jar` in `plugins/`
2. Restart the server
3. Players can immediately use `/money`, `/mobcoin`, `/gem`
4. Customize `config.yml`, `ranks.yml`, `mob-rewards.yml`
5. `/economy reload` to apply changes

---

## Commands

### Currency Commands (`/money`, `/mobcoin`, `/gem`)

| Command | Description | Permission |
|---------|-------------|------------|
| `/<currency>` | Check balance | `dzeconomy.<currency>.balance` |
| `/<currency> send <player> <amount>` | Send currency | `dzeconomy.<currency>.send` |
| `/<currency> request <player> <amount>` | Request currency | `dzeconomy.<currency>.request` |
| `/<currency> accept <player>` | Accept request | `dzeconomy.<currency>.accept` |
| `/<currency> deny <player>` | Deny request | `dzeconomy.<currency>.deny` |
| `/<currency> top [page]` | Leaderboard | `dzeconomy.<currency>.top` |
| `/<currency> add <player> <amount>` | Add currency | `dzeconomy.<currency>.add` |
| `/<currency> remove <player> <amount>` | Remove currency | `dzeconomy.<currency>.remove` |
| `/<currency> set <player> <amount>` | Set balance | `dzeconomy.<currency>.set` |

### Admin Commands (`/economy`)

| Command | Description | Permission |
|---------|-------------|------------|
| `/economy info` | Plugin info | `dzeconomy.economy.info` |
| `/economy reload` | Reload config | `dzeconomy.admin.reload` |
| `/economy status` | Plugin status | `dzeconomy.admin.status` |
| `/economy convert <player> <from> <to> <amount>` | Convert currency | `dzeconomy.admin.convert` |
| `/economy migrate <from> <to>` | Migrate storage | `dzeconomy.admin.migrate` |
| `/economy baltop [currency] [page]` | Global leaderboard | `dzeconomy.admin.baltop` |
| `/economy payall <currency> <amount>` | Pay all players | `dzeconomy.admin.payall` |
| `/economy backup` | Create backup | `dzeconomy.admin.backup` |

---

## Performance

| Metric | SQLite (50 players) | MySQL (100 players) |
|--------|---------------------|---------------------|
| Balance lookup | <1ms (cached) | <1ms (cached) |
| Transfer operation | <5ms | <5ms |
| Baltop refresh (cached) | <10ms | <10ms |
| Auto-save (300s interval) | <50ms | <30ms |
| Placeholder resolution | <1ms (3s cache) | <1ms (3s cache) |

All database operations run asynchronously and never block the main thread (or region threads on Folia).

---

## Compatibility

| Software | Versions | Java |
|----------|----------|------|
| Paper | 1.16.5 – 1.21.5 | 17+ (21 rec.) |
| Folia | 1.19.4 – 1.21.5 | 17+ (21 rec.) |
| Spigot | 1.16 – 1.21.5 | 17+ (21 rec.) |
| Purpur | 1.16.5 – 1.21.5 | 17+ (21 rec.) |

---

## bStats

![bStats](https://bstats.org/signatures/bukkit/DZEconomy.svg)

---

## Links

- **Wiki:** [GitHub Wiki](https://github.com/DemonZDevelopment/DZEconomy/wiki)
- **Discord:** [discord.gg/dzeconomy](https://discord.gg/dzeconomy)
- **GitHub:** [github.com/DemonZDevelopment/DZEconomy](https://github.com/DemonZDevelopment/DZEconomy)
- **Website:** [demonzdevelopment.online](https://demonzdevelopment.online)

---

*DZEconomy v2.0.0 — Made with ❤️ by DemonZ Development*
