![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# DZEconomy

**Multi-currency economy with ranks, combat tagging, mob rewards, and full Folia support.**

---

## Why Choose DZEconomy?

DZEconomy is a modern economy plugin built for Paper and Folia servers. Instead of a single currency, you get three fully configurable ones out of the box: Money, MobCoins, and Gems. Each has its own settings, commands, and leaderboard.

### Key Highlights

- **3 Currencies** — Money ($), MobCoins (⛃), Gems (◆), each independently configurable
- **Rank Multipliers** — LuckPerms integration with per-currency bonuses, cooldown reduction, and interest
- **Folia Native** — Full region-based scheduling with automatic detection
- **High Performance** — MySQL connection pooling, async database operations, 5-second placeholder cache
- **Combat Tagging** — Blocks economy actions during PvP, with an action bar indicator
- **PvP Loot** — Kill players to take a configurable percentage of their balance
- **Mob Rewards** — Per-mob drops with kill streak bonuses
- **Fully Customizable** — Every message, format, and behavior editable in messages.yml

---

## Folia Support

DZEconomy works on Folia without any configuration:

- Drops into the `plugins/` folder like any other plugin
- Detects Folia automatically at startup
- Economy actions stay fast and reliable on Folia's multithreaded model
- Works on Folia 1.19.4+

No configuration changes needed. It just works.

---

## Quick Setup

1. Place `DZEconomy-2.1.2.jar` in `plugins/`
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
| `/economy give <player> <amount> [currency]` | Give currency | `dzeconomy.admin` |
| `/economy migrate <from> <to>` | Migrate storage | `dzeconomy.admin.migrate` |
| `/economy baltop [currency] [page]` | Global leaderboard | `dzeconomy.admin.baltop` |
| `/economy payall <currency> <amount>` | Pay all players | `dzeconomy.admin.payall` |
| `/economy backup` | Create backup | `dzeconomy.admin.backup` |

---

## Performance

All database operations run asynchronously and never block the tick loop. On Folia, nothing runs on a central main thread, and balance changes for different players never slow each other down.

---

## Compatibility

| Software | Versions | Java |
|----------|----------|------|
| Paper | 1.9 – 1.26.x | 8+ |
| Folia | 1.19.4 – 1.26.x | 8+ |
| Spigot | 1.9 – 1.26.x | 8+ |
| Purpur | 1.9 – 1.26.x | 8+ |
| Bukkit | 1.9 – 1.26.x | 8+ |

---

## Links

- **Wiki:** [GitHub Wiki](https://github.com/DemonZ-Development/DZEconomy/wiki)
- **Discord:** [discord.com/invite/GYsTt96ypf](https://discord.com/invite/GYsTt96ypf)
- **GitHub:** [github.com/DemonZ-Development/DZEconomy](https://github.com/DemonZ-Development/DZEconomy)
- **Website:** [demonzdevelopment.online](https://demonzdevelopment.online)

---

*DZEconomy v2.1.2 — Made by DemonZ Development*