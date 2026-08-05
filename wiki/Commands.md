![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# Commands Reference

Command reference for DZEconomy v2.1.2. All commands support tab completion.

---

## Currency Commands

Each currency (Money, MobCoin, Gem) has its own base command with identical subcommands.

| Currency | Command | Aliases |
|----------|---------|---------|
| Money | `/money` | `/bal`, `/balance` |
| MobCoin | `/mobcoin` | `/mobcoins`, `/mc` |
| Gem | `/gem` | `/gems` |

> The subcommands below use `/money` as the example. Replace it with `/mobcoin` or `/gem` for the other currencies.

---

### `/money balance [player]`

Check your own or another player's balance.

| | |
|---|---|
| **Permission** | `dzeconomy.money.balance` (self) / `dzeconomy.money.balance.others` (others) |
| **Default** | `true` / `op` |
| **Player Only** | Yes |

**Examples:**
```
/money              → Check your own balance
/money balance      → Check your own balance
/money balance Steve → Check Steve's balance
```

---

### `/money send <player> <amount>`

Send currency to another player.

| | |
|---|---|
| **Permission** | `dzeconomy.money.send` |
| **Default** | `true` |
| **Aliases** | `pay`, `give` |
| **Player Only** | Yes |

**Examples:**
```
/money send Steve 100      → Send $100 to Steve
/money pay Steve 500.50    → Send $500.50 to Steve
/money give Steve 1000     → Send $1,000 to Steve
```

**Checks performed:**
- Cannot send to yourself (unless `allow-self-transfer: true`)
- Must have sufficient balance
- Amount must exceed `min-transaction`
- Amount must not exceed `max-transaction` (if set)
- Must not exceed the daily transfer limit
- Must not be on transfer cooldown
- Must not be combat-tagged (if `block-during-combat: true`)

Transfers apply a configurable tax from the sender's rank. The receiver gets the amount minus the tax.

---

### `/money request <player> <amount>`

Request currency from another player.

| | |
|---|---|
| **Permission** | `dzeconomy.money.request` |
| **Default** | `true` |
| **Aliases** | `req` |
| **Player Only** | Yes |

**Examples:**
```
/money request Steve 100    → Request $100 from Steve
/money req Steve 500        → Request $500 from Steve
```

**Behavior:**
- Steve gets a notification with the request
- The request expires after `request.timeout` seconds (default: 120s)
- Maximum pending requests per player: `request.max-pending` (default: 5)
- Cannot request from yourself

---

### `/money accept <player>`

Accept a payment request from another player.

| | |
|---|---|
| **Permission** | `dzeconomy.money.accept` |
| **Default** | `true` |
| **Player Only** | Yes |

**Examples:**
```
/money accept Steve    → Accept Steve's Money request
```

**Behavior:**
- Currency transfers from your balance to the requester
- Must have sufficient balance
- Applies the same limits as a normal send (max-transaction, cooldown, daily limit)
- Cannot accept while combat-tagged

---

### `/money deny <player>`

Deny a payment request from another player.

| | |
|---|---|
| **Permission** | `dzeconomy.money.deny` |
| **Default** | `true` |
| **Aliases** | `reject` |
| **Player Only** | Yes |

**Examples:**
```
/money deny Steve      → Deny Steve's request
/money reject Steve    → Same as above
```

---

### `/money add <player> <amount>`

**Admin only** — Add currency to a player's balance.

| | |
|---|---|
| **Permission** | `dzeconomy.money.add` |
| **Default** | `op` |
| **Player Only** | Yes |

**Examples:**
```
/money add Steve 1000     → Add $1,000 to Steve's balance
/money add Steve 500.25   → Add $500.25 to Steve's balance
```

---

### `/money remove <player> <amount>`

**Admin only** — Remove currency from a player's balance.

| | |
|---|---|
| **Permission** | `dzeconomy.money.remove` |
| **Default** | `op` |
| **Aliases** | `take` |
| **Player Only** | Yes |

**Examples:**
```
/money remove Steve 500     → Remove $500 from Steve's balance
/money take Steve 100       → Remove $100 from Steve's balance
```

---

### `/money set <player> <amount>`

**Admin only** — Set a player's balance to a specific amount.

| | |
|---|---|
| **Permission** | `dzeconomy.money.set` |
| **Default** | `op` |
| **Player Only** | Yes |

**Examples:**
```
/money set Steve 10000     → Set Steve's balance to $10,000
/money set Steve 0         → Reset Steve's balance to $0
```

---

### `/money top [page]`

View the balance leaderboard for this currency.

| | |
|---|---|
| **Permission** | `dzeconomy.money.top` |
| **Default** | `true` |
| **Aliases** | `baltop` |
| **Player Only** | Yes |

**Examples:**
```
/money top          → Page 1 of the Money leaderboard
/money top 2        → Page 2
/money baltop 3     → Page 3
```

---

## Economy Admin Commands

The `/economy` command (aliases: `/econ`, `/dzeconomy`, `/dze`) provides admin functionality.

---

### `/economy info`

View plugin information.

| | |
|---|---|
| **Permission** | `dzeconomy.economy.info` |
| **Default** | `true` |
| **Console** | Yes |

**Example Output:**
```
─────────────────────────────────
  DZEconomy v2.1.2
  
  Currencies:
    ▸ money - Enabled ($)
    ▸ mobcoin - Enabled (⛃)
    ▸ gem - Enabled (◆)

  Storage: sqlite
  Language: en
─────────────────────────────────
```

---

### `/economy version`

View detailed version information.

| | |
|---|---|
| **Permission** | `dzeconomy.admin` |
| **Default** | `op` |
| **Console** | Yes |

**Example Output:**
```
─────────────────────────────────
  DZEconomy Version Info

  ▸ Installed: v2.1.2
  ▸ Server: Paper 1.21.4-...
  ▸ Bukkit API: 1.20.4-R0.1-SNAPSHOT
  ▸ Java: 21.0.2
─────────────────────────────────
```

---

### `/economy reload`

Reload all configuration files without restarting.

| | |
|---|---|
| **Permission** | `dzeconomy.admin.reload` |
| **Default** | `op` |
| **Console** | Yes |

**Behavior:**
- Reloads `config.yml`, `messages.yml`, `ranks.yml`, and `mob-rewards.yml`
- Does not disconnect storage or lose cached data
- Refresh rank and combat tag configurations

```
/economy reload    → Reloads all configuration
```

---

### `/economy status`

View live plugin status and statistics.

| | |
|---|---|
| **Permission** | `dzeconomy.admin.status` |
| **Default** | `op` |
| **Console** | Yes |

---

### `/economy convert <player> <from> <to> <amount>`

Convert currency from one type to another for a player.

| | |
|---|---|
| **Permission** | `dzeconomy.admin.convert` |
| **Default** | `op` |
| **Console** | Yes |

**Examples:**
```
/economy convert Steve money gem 1000    → Convert $1,000 of Steve's Money to Gems
/economy convert Steve mobcoin money 50  → Convert 50 of Steve's MobCoins to Money
```

**Behavior:**
- Uses conversion rates from `config.yml` → `conversion.rates`
- Applies the conversion fee: `conversion.fee-percent` (default: 5%)
- Cannot convert a currency to itself
- Both currencies must be enabled
- The confirmation message shows the actual amount received after rates and fees

---

### `/economy give <player> <amount> [currency]`

Add currency to a player's balance.

| | |
|---|---|
| **Permission** | `dzeconomy.admin` |
| **Default** | `op` |
| **Console** | Yes |

**Examples:**
```
/economy give Steve 1000             → Give $1,000 to Steve
/economy give Steve 500 mobcoin      → Give 500 MobCoins to Steve
/economy give Steve 200 gem          → Give 200 Gems to Steve
```

**Behavior:**
- Defaults to money when no currency is given
- Accepts `money`, `mobcoin(s)`, and `gem(s)` as currency names
- Notifies the target player
- Works from the console

---

### `/economy migrate <from> <to>`

Migrate all player data between storage backends.

| | |
|---|---|
| **Permission** | `dzeconomy.admin.migrate` |
| **Default** | `op` |
| **Console** | Yes |

**Examples:**
```
/economy migrate sqlite mysql     → Migrate from SQLite to MySQL
/economy migrate mysql sqlite     → Migrate from MySQL to SQLite
/economy migrate flatfile mysql   → Migrate from Flat File to MySQL
```

**Valid backends:** `sqlite`, `mysql`, `yaml` (flat file)

> Migration runs asynchronously. Do not shut down the server during migration.

---

### `/economy baltop [currency] [page]`

View the global balance leaderboard across all currencies.

| | |
|---|---|
| **Permission** | `dzeconomy.admin.baltop` |
| **Default** | `op` |
| **Console** | Yes |

**Examples:**
```
/economy baltop                → Global baltop (all currencies)
/economy baltop money          → Money-only baltop
/economy baltop gem 2          → Gem baltop, page 2
/economy baltop 2              → Global baltop, page 2
```

---

### `/economy payall <currency> <amount>`

Distribute currency to all online players.

| | |
|---|---|
| **Permission** | `dzeconomy.admin.payall` |
| **Default** | `op` |
| **Console** | Yes |

**Examples:**
```
/economy payall money 100      → Give $100 to all online players
/economy payall gem 5          → Give 5 Gems to all online players
/economy payall mobcoin 10     → Give 10 MobCoins to all online players
```

---

### `/economy credits`

View plugin credits and links.

| | |
|---|---|
| **Permission** | None |
| **Default** | `true` |
| **Console** | Yes |

---

### `/economy backup`

Create a manual backup of all economy data.

| | |
|---|---|
| **Permission** | `dzeconomy.admin.backup` |
| **Default** | `op` |
| **Console** | Yes |

**Behavior:**
- Saves all cached player data to storage
- Creates a timestamped backup in `plugins/DZEconomy/backups/`
- Includes all player balances across all currencies

---

## Quick Reference Table

### Currency Commands (applies to `/money`, `/mobcoin`, `/gem`)

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/<currency>` | Check your balance | `dzeconomy.<currency>.balance` | `true` |
| `/<currency> balance [player]` | Check balance | `dzeconomy.<currency>.balance` | `true` |
| `/<currency> balance <player>` | Check other's balance | `dzeconomy.<currency>.balance.others` | `op` |
| `/<currency> send <player> <amount>` | Send currency | `dzeconomy.<currency>.send` | `true` |
| `/<currency> request <player> <amount>` | Request currency | `dzeconomy.<currency>.request` | `true` |
| `/<currency> accept <player>` | Accept request | `dzeconomy.<currency>.accept` | `true` |
| `/<currency> deny <player>` | Deny request | `dzeconomy.<currency>.deny` | `true` |
| `/<currency> top [page]` | View leaderboard | `dzeconomy.<currency>.top` | `true` |
| `/<currency> add <player> <amount>` | Add currency | `dzeconomy.<currency>.add` | `op` |
| `/<currency> remove <player> <amount>` | Remove currency | `dzeconomy.<currency>.remove` | `op` |
| `/<currency> set <player> <amount>` | Set balance | `dzeconomy.<currency>.set` | `op` |

### Admin Commands (`/economy`)

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/economy info` | View plugin info | `dzeconomy.economy.info` | `true` |
| `/economy credits` | View credits | None | `true` |
| `/economy reload` | Reload config | `dzeconomy.admin.reload` | `op` |
| `/economy version` | Version info | `dzeconomy.admin` | `op` |
| `/economy status` | Plugin status | `dzeconomy.admin.status` | `op` |
| `/economy convert <player> <from> <to> <amount>` | Convert currency | `dzeconomy.admin.convert` | `op` |
| `/economy give <player> <amount> [currency]` | Add currency | `dzeconomy.admin` | `op` |
| `/economy migrate <from> <to>` | Migrate storage | `dzeconomy.admin.migrate` | `op` |
| `/economy baltop [currency] [page]` | Global leaderboard | `dzeconomy.admin.baltop` | `op` |
| `/economy payall <currency> <amount>` | Pay all players | `dzeconomy.admin.payall` | `op` |
| `/economy backup` | Create backup | `dzeconomy.admin.backup` | `op` |

---

<p align="center">
  See <a href="Permissions.md">Permissions</a> for the full permission node reference.
</p>

---
### Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***