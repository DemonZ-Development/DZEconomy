![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# Configuration Reference

Configuration reference for DZEconomy v2.1.2. All config files live in `plugins/DZEconomy/`.

---

## Configuration Files

| File | Purpose |
|------|---------|
| `config.yml` | Main plugin configuration |
| `messages.yml` | All translatable messages |
| `ranks.yml` | Rank definitions and multipliers |
| `mob-rewards.yml` | Mob kill reward configuration |

Do not change `config-version` by hand. The plugin uses it to run config migrations.

---

## config.yml

### Storage

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `storage.type` | String | `SQLITE` | Storage backend. Options: `SQLITE`, `MYSQL` |

Changing the backend requires a migration. Use `/economy migrate` to move data between backends.

#### MySQL Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `storage.mysql.host` | String | `localhost` | MySQL server hostname |
| `storage.mysql.port` | Integer | `3306` | MySQL server port |
| `storage.mysql.database` | String | `dzeconomy` | Database name |
| `storage.mysql.username` | String | `root` | Database username |
| `storage.mysql.password` | String | `changeme` | Database password |
| `storage.mysql.use-ssl` | Boolean | `false` | Whether to connect with SSL |
| `storage.mysql.pool-size` | Integer | `10` | HikariCP connection pool size |

#### SQLite Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `storage.sqlite.file` | String | `data.db` | Database file name (inside plugin data folder) |

---

### Auto-Save

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `auto-save.interval` | Integer | `300` | Save interval in seconds. Minimum recommended: `60`. Set to `-1` to disable. |

---

### Currencies

Each currency has identical settings.

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `currencies.<cur>.enabled` | Boolean | `true` | Whether the currency is active. Disabled currencies hide their commands. |
| `currencies.<cur>.singular` | String | varies | Singular name |
| `currencies.<cur>.plural` | String | varies | Plural name |
| `currencies.<cur>.symbol` | String | `$` / `⛃` / `◆` | Display symbol |
| `currencies.<cur>.decimal-places` | Integer | `2` / `0` / `0` | Decimal places shown. `0` = whole numbers only. |
| `currencies.<cur>.starting-balance` | Double | `500.00` / `0` / `0` | Balance given to new players |
| `currencies.<cur>.max-balance` | Double | `-1` | Maximum balance. `-1` = unlimited. |
| `currencies.<cur>.min-transaction` | Double | `0.01` / `1` / `1` | Minimum amount for any transaction |

Where `<cur>` is `money`, `mobcoin`, or `gem`.

---

### Transfer Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `transfer.max-transaction` | Double | `-1` | Maximum amount per transaction. `-1` = no limit. |
| `transfer.block-during-combat` | Boolean | `true` | Block transfers while combat-tagged |
| `transfer.allow-self-transfer` | Boolean | `false` | Allow sending currency to yourself |

#### Daily Limits

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `transfer.daily-limit.enabled` | Boolean | `false` | Whether daily transfer limits are enabled |
| `transfer.daily-limit.money` | Double | `-1` | Daily Money transfer limit. `-1` = no limit. |
| `transfer.daily-limit.mobcoin` | Double | `-1` | Daily MobCoin transfer limit. `-1` = no limit. |
| `transfer.daily-limit.gem` | Double | `-1` | Daily Gem transfer limit. `-1` = no limit. |

Limits reset at midnight (server time). Cooldowns and daily sent amounts persist across restarts.

#### Cooldowns

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `transfer.cooldowns.enabled` | Boolean | `false` | Whether transfer cooldowns are enabled |
| `transfer.cooldowns.money` | Integer | `5` | Money transfer cooldown in seconds |
| `transfer.cooldowns.mobcoin` | Integer | `5` | MobCoin transfer cooldown in seconds |
| `transfer.cooldowns.gem` | Integer | `10` | Gem transfer cooldown in seconds |

---

### Conversion Rates

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `conversion.fee-percent` | Double | `5.0` | Conversion fee percentage (0.0 – 100.0). `0` = no fee. |
| `conversion.rates.money-to-mobcoin` | Double | `10.0` | 1 Money = 10 MobCoins |
| `conversion.rates.money-to-gem` | Double | `100.0` | 1 Money = 100 Gems |
| `conversion.rates.mobcoin-to-money` | Double | `0.1` | 1 MobCoin = 0.1 Money |
| `conversion.rates.mobcoin-to-gem` | Double | `10.0` | 1 MobCoin = 10 Gems |
| `conversion.rates.gem-to-money` | Double | `0.01` | 1 Gem = 0.01 Money |
| `conversion.rates.gem-to-mobcoin` | Double | `0.1` | 1 Gem = 0.1 MobCoins |

**Rate format**: `1 unit of source = <rate> units of target`

---

### PVP Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `pvp.enabled` | Boolean | `true` | Whether PVP currency loss is enabled |
| `pvp.loss-percent.money` | Double | `5.0` | % of Money dropped on PVP death |
| `pvp.loss-percent.mobcoin` | Double | `2.0` | % of MobCoins dropped on PVP death |
| `pvp.loss-percent.gem` | Double | `0.0` | % of Gems dropped on PVP death (0 = disabled) |
| `pvp.minimum-balance.money` | Double | `100.0` | Minimum Money kept after PVP death |
| `pvp.minimum-balance.mobcoin` | Double | `0` | Minimum MobCoins kept after PVP death |
| `pvp.minimum-balance.gem` | Double | `0` | Minimum Gems kept after PVP death |
| `pvp.broadcast.enabled` | Boolean | `true` | Broadcast big PVP kills |
| `pvp.broadcast.threshold` | Double | `1000.0` | Only broadcast when dropped value reaches this |
| `pvp.world-blacklist` | List | `["spawn", "creative"]` | Worlds where PVP loss is disabled |

The transfer tax applies to PVP loot the same way it applies to regular sends. The killer's gain message reports the net amount credited.

---

### Combat Tag

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `combat-tag.enabled` | Boolean | `true` | Whether combat tagging is enabled |
| `combat-tag.duration` | Integer | `15` | Duration in seconds |

Combat tag blocks economy actions while you are tagged. The tag expires when the fight ends or the timer runs out.

---

### Mob Rewards

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `mob-rewards.enabled` | Boolean | `true` | Whether mob rewards are enabled globally |
| `mob-rewards.world-whitelist` | List | `[]` | Only give rewards in these worlds. Empty = all worlds. |
| `mob-rewards.world-blacklist` | List | `["spawn", "creative"]` | Never give rewards in these worlds |
| `mob-rewards.allow-spawner-mobs` | Boolean | `false` | Whether spawner mobs give rewards |
| `mob-rewards.allow-spawn-egg-mobs` | Boolean | `false` | Whether spawn egg mobs give rewards |
| `mob-rewards.reward-message` | String | `&a+{amount} {currency} &7(from killing {mob})` | Reward message |
| `mob-rewards.default-multiplier` | Double | `1.0` | Default reward multiplier |

Per-mob rewards live in `mob-rewards.yml`.

---

### Request Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `request.timeout` | Integer | `120` | Request expiry in seconds |
| `request.max-pending` | Integer | `5` | Maximum pending requests per player |

---

### Update Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `updates.check-enabled` | Boolean | `true` | Whether to check for updates |
| `updates.check-interval` | Integer | `21600` | Check interval in seconds (minimum: 3600) |
| `updates.modrinth-project-id` | String | `dzeconomy` | Modrinth project ID (change for forks) |

---

## mob-rewards.yml

Per-mob reward configuration. Key sections:

| Section | Description |
|---------|-------------|
| `neutral` | Passive mob rewards (cows, sheep, etc.) |
| `easy` | Easy hostile mob rewards (zombies, skeletons, etc.) |
| `hard` | Hard hostile mob rewards (creepers, blazes, etc.) |
| `boss` | Boss mob rewards (ender dragon, wither, etc.) |
| `custom` | Custom mob entries (MythicMobs, etc.) |
| `kill-streaks` | Kill streak bonus configuration |

---

## ranks.yml

See [Ranks](Ranks.md) for rank configuration details.

---

## messages.yml

Every message in DZEconomy is customizable. Key sections:

| Section | Description |
|---------|-------------|
| `prefix` | Global message prefix |
| `balance` | Balance display messages |
| `send` | Send/receive messages |
| `request` | Payment request messages |
| `admin` | Admin operation messages |
| `error` | Error messages |
| `economy` | Economy command messages |
| `combat-tag` | Combat tag messages |
| `pvp` | PVP loot messages |
| `update` | Update notification messages |
| `welcome` | First-join/returning messages |
| `baltop` | Baltop formatting |
| `help` | Help command messages |
| `mob-rewards` | Mob reward messages |
| `rank` | Rank messages |
| `misc` | Miscellaneous messages |

**Color Codes:** `&0-9`, `&a-f` for colors, `&k-o` for formatting, `&r` for reset.
**Hex Colors (1.16+):** `&#RRGGBB`
**MiniMessage Gradients:** `<gradient:#ff0000:#0000ff>text</>`

---

<p align="center">
  See <a href="Commands.md">Commands</a> and <a href="Permissions.md">Permissions</a> for related references.
</p>

---
### Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***