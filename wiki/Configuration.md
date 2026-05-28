# ⚙️ Configuration Reference

Complete configuration reference for DZEconomy v2.1.0. All configuration files are located in `plugins/DZEconomy/`.

---

## 📁 Configuration Files

| File | Purpose |
|------|---------|
| `config.yml` | Main plugin configuration |
| `messages.yml` | All translatable messages |
| `ranks.yml` | Rank definitions and multipliers |
| `mob-rewards.yml` | Mob kill reward configuration |

> ⚠️ **Do not manually change `config-version`!** It is used internally for automatic configuration migration.

---

## 📄 config.yml

### General

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `config-version` | Integer | `2` | Internal version number for config migration. **Do not change manually!** |

---

### Storage

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `storage.type` | String | `SQLITE` | Storage backend. Options: `SQLITE`, `MYSQL`, `FLATFILE` |

#### MySQL Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `storage.mysql.host` | String | `localhost` | MySQL server hostname |
| `storage.mysql.port` | Integer | `3306` | MySQL server port |
| `storage.mysql.database` | String | `dzeconomy` | Database name |
| `storage.mysql.username` | String | `root` | Database username |
| `storage.mysql.password` | String | `changeme` | Database password |
| `storage.mysql.parameters` | String | `?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8` | JDBC URL parameters |
| `storage.mysql.pool-size` | Integer | `10` | HikariCP connection pool size |
| `storage.mysql.connection-timeout` | Integer | `30` | Maximum wait time (seconds) for a pool connection |

#### SQLite Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `storage.sqlite.file` | String | `data.db` | Database file name (inside plugin data folder) |

---

### Auto-Save

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `auto-save.interval` | Integer | `300` | Save interval in seconds. Minimum recommended: `60`. Set to `-1` to disable. |
| `auto-save.save-on-transaction` | Boolean | `false` | Save after every transaction. **Warning**: Can significantly reduce performance! |

---

### Currencies

Each currency has identical configuration options.

#### Money (Primary Currency)

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `currencies.money.enabled` | Boolean | `true` | Whether Money is active. Disabled currencies hide commands & GUI. |
| `currencies.money.singular` | String | `Dollar` | Singular name |
| `currencies.money.plural` | String | `Dollars` | Plural name |
| `currencies.money.symbol` | String | `$` | Display symbol |
| `currencies.money.decimal-places` | Integer | `2` | Number of decimal places. `0` = whole numbers only. |
| `currencies.money.starting-balance` | Double | `500.00` | Balance given to new players |
| `currencies.money.max-balance` | Double | `-1` | Maximum balance. `-1` = unlimited. |
| `currencies.money.min-transaction` | Double | `0.01` | Minimum amount for any transaction |
| `currencies.money.display-format` | String | `{symbol}{amount}` | Format string. Placeholders: `{symbol}`, `{amount}`, `{currency_singular}`, `{currency_plural}` |

#### MobCoin (Secondary Currency)

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `currencies.mobcoin.enabled` | Boolean | `true` | Whether MobCoins are active |
| `currencies.mobcoin.singular` | String | `MobCoin` | Singular name |
| `currencies.mobcoin.plural` | String | `MobCoins` | Plural name |
| `currencies.mobcoin.symbol` | String | `⛃` | Display symbol |
| `currencies.mobcoin.decimal-places` | Integer | `0` | Number of decimal places |
| `currencies.mobcoin.starting-balance` | Double | `0` | Balance given to new players |
| `currencies.mobcoin.max-balance` | Double | `-1` | Maximum balance. `-1` = unlimited. |
| `currencies.mobcoin.min-transaction` | Double | `1` | Minimum amount for any transaction |
| `currencies.mobcoin.display-format` | String | `{symbol}{amount}` | Format string |

#### Gem (Premium Currency)

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `currencies.gem.enabled` | Boolean | `true` | Whether Gems are active |
| `currencies.gem.singular` | String | `Gem` | Singular name |
| `currencies.gem.plural` | String | `Gems` | Plural name |
| `currencies.gem.symbol` | String | `◆` | Display symbol |
| `currencies.gem.decimal-places` | Integer | `0` | Number of decimal places |
| `currencies.gem.starting-balance` | Double | `0` | Balance given to new players |
| `currencies.gem.max-balance` | Double | `-1` | Maximum balance. `-1` = unlimited. |
| `currencies.gem.min-transaction` | Double | `1` | Minimum amount for any transaction |
| `currencies.gem.display-format` | String | `{symbol}{amount}` | Format string |

---

### Transfer Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `transfer.max-transaction` | Double | `-1` | Maximum amount per single transaction. `-1` = no limit. |

#### Daily Limits

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `transfer.daily-limit.enabled` | Boolean | `false` | Whether daily transfer limits are enabled |
| `transfer.daily-limit.money` | Double | `-1` | Daily Money transfer limit. `-1` = no limit. |
| `transfer.daily-limit.mobcoin` | Double | `-1` | Daily MobCoin transfer limit. `-1` = no limit. |
| `transfer.daily-limit.gem` | Double | `-1` | Daily Gem transfer limit. `-1` = no limit. |

#### Cooldowns

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `transfer.cooldowns.enabled` | Boolean | `false` | Whether transfer cooldowns are enabled |
| `transfer.cooldowns.money` | Integer | `5` | Money transfer cooldown in seconds |
| `transfer.cooldowns.mobcoin` | Integer | `5` | MobCoin transfer cooldown in seconds |
| `transfer.cooldowns.gem` | Integer | `10` | Gem transfer cooldown in seconds |

#### Combat Tag & Self-Transfer

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `transfer.block-during-combat` | Boolean | `true` | Block transfers while combat-tagged |
| `transfer.allow-self-transfer` | Boolean | `false` | Allow sending currency to yourself |

---

### Conversion Rates

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `conversion.player-convert` | Boolean | `false` | Whether players can convert currencies. If `false`, only admins can. |
| `conversion.fee-percent` | Double | `5.0` | Conversion fee percentage (0.0 – 100.0). `0` = no fee. |
| `conversion.rates.money-to-mobcoin` | Double | `10.0` | 1 Money = 10 MobCoins |
| `conversion.rates.money-to-gem` | Double | `100.0` | 1 Money = 100 Gems |
| `conversion.rates.mobcoin-to-money` | Double | `0.1` | 1 MobCoin = 0.1 Money |
| `conversion.rates.mobcoin-to-gem` | Double | `10.0` | 1 MobCoin = 10 Gems |
| `conversion.rates.gem-to-money` | Double | `0.01` | 1 Gem = 0.01 Money |
| `conversion.rates.gem-to-mobcoin` | Double | `0.1` | 1 Gem = 0.1 MobCoins |

> **Rate format**: `1 unit of source = rate units of target`

---

### PVP Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `pvp.enabled` | Boolean | `true` | Whether PVP currency loss is enabled |
| `pvp.loss-percent.money` | Double | `5.0` | Percentage of Money lost on PVP death |
| `pvp.loss-percent.mobcoin` | Double | `2.0` | Percentage of MobCoins lost on PVP death |
| `pvp.loss-percent.gem` | Double | `0.0` | Percentage of Gems lost on PVP death (0 = disabled) |
| `pvp.minimum-balance.money` | Double | `100.0` | Minimum Money retained after PVP death |
| `pvp.minimum-balance.mobcoin` | Double | `0` | Minimum MobCoins retained after PVP death |
| `pvp.minimum-balance.gem` | Double | `0` | Minimum Gems retained after PVP death |
| `pvp.broadcast.enabled` | Boolean | `true` | Broadcast PVP kills |
| `pvp.broadcast.threshold` | Double | `1000.0` | Only broadcast if total value dropped exceeds this (in Money) |
| `pvp.world-blacklist` | List | `["spawn", "creative"]` | Worlds where PVP loss is disabled |

---

### Combat Tag

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `combat-tag.enabled` | Boolean | `true` | Whether combat tagging is enabled |
| `combat-tag.duration` | Integer | `15` | Duration in seconds |
| `combat-tag.blocked-actions` | List | `["send", "request", "accept"]` | Economy actions blocked while tagged |
| `combat-tag.include-pve` | Boolean | `false` | Whether mob attacks also trigger combat tag |
| `combat-tag.action-bar.enabled` | Boolean | `true` | Show combat tag on action bar |
| `combat-tag.action-bar.format` | String | `&c&l⚔ &eCombat Tag &7- &c{time}s remaining` | Action bar format. Placeholder: `{time}` |

---

### Mob Rewards

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `mob-rewards.enabled` | Boolean | `true` | Whether mob rewards are enabled globally |
| `mob-rewards.world-whitelist` | List | `[]` | Only give rewards in these worlds. Empty = all worlds. |
| `mob-rewards.world-blacklist` | List | `["spawn", "creative"]` | Never give rewards in these worlds |
| `mob-rewards.allow-spawner-mobs` | Boolean | `false` | Whether spawner mobs can give rewards |
| `mob-rewards.allow-spawn-egg-mobs` | Boolean | `false` | Whether spawn egg mobs can give rewards |
| `mob-rewards.reward-message` | String | `&a+{amount} {currency} &7(from killing {mob})` | Reward message. Placeholders: `{amount}`, `{currency}`, `{mob}` |
| `mob-rewards.default-multiplier` | Double | `1.0` | Default multiplier for all mob rewards. Permission: `dzeconomy.mobreward.multiplier` |

> See `mob-rewards.yml` for per-mob reward configuration.

---

### Request Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `request.enabled` | Boolean | `true` | Whether the request system is enabled |
| `request.timeout` | Integer | `120` | Request expiry time in seconds |
| `request.max-pending` | Integer | `5` | Maximum pending requests per player |
| `request.auto-accept-permission` | Boolean | `false` | Auto-accept from players with `dzeconomy.request.autoaccept` |
| `request.notification.type` | String | `MESSAGE` | Notification type. Options: `MESSAGE`, `ACTION_BAR`, `TITLE`, `BOSS_BAR` |
| `request.notification.sound` | String | `ENTITY_EXPERIENCE_ORB_PICKUP:1.0:1.0` | Sound format: `SOUND_NAME:VOLUME:PITCH` |

---

### Baltop Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `baltop.enabled` | Boolean | `true` | Whether baltop is enabled |
| `baltop.entries-per-page` | Integer | `10` | Players shown per page |
| `baltop.refresh-interval` | Integer | `300` | Cache refresh interval in seconds |
| `baltop.include-offline` | Boolean | `true` | Include offline players in leaderboard |
| `baltop.entry-format` | String | `&e#{rank} &7- &f{name} &8» &a{amount} {currency}` | Entry format |
| `baltop.header` | String | *(multi-line)* | Header format |
| `baltop.footer` | String | *(multi-line)* | Footer format |

---

### Payall Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `payall.enabled` | Boolean | `true` | Whether payall is enabled |
| `payall.cooldown` | Integer | `60` | Minimum seconds between payall commands |
| `payall.broadcast` | Boolean | `true` | Broadcast to all players when payall is used |
| `payall.allow-console` | Boolean | `true` | Whether the console can execute payall |

---

### Update Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `updates.check-enabled` | Boolean | `true` | Whether to check for updates |
| `updates.check-interval` | Integer | `21600` | Check interval in seconds (minimum: 3600) |
| `updates.notify.on-join` | Boolean | `true` | Notify admins on join when update available |
| `updates.notify.permission` | String | `dzeconomy.admin.update` | Permission to receive notifications |
| `updates.notify.console-log` | Boolean | `true` | Log to console when update found |
| `updates.modrinth-project-id` | String | `dzeconomy` | Modrinth project ID (change for forks) |

---

### Rank Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `ranks.enabled` | Boolean | `true` | Whether the rank system is enabled. Requires LuckPerms. |
| `ranks.multiplier-stacking` | String | `MULTIPLY` | How multipliers stack. Options: `MULTIPLY`, `ADD`, `HIGHEST` |

**Stacking Modes:**
- `MULTIPLY` — All multipliers multiply together (1.5 × 1.2 = 1.8)
- `ADD` — All multipliers add together (0.5 + 0.2 = 1.7)
- `HIGHEST` — Only the highest multiplier is used

> See `ranks.yml` for full rank configuration.

---

### Miscellaneous Settings

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `misc.number-format` | String | `FULL` | Number display format. Options: `FULL`, `COMPACT`, `LOCALE` |
| `misc.debug` | Boolean | `false` | Enable verbose console logging. **Only enable when troubleshooting!** |

**Number Format Options:**
- `FULL` — Always show full number (1,000,000.00)
- `COMPACT` — Abbreviate large numbers (1.0M, 1.0K)
- `LOCALE` — Use the server's locale formatting

#### Transaction Logging

| Path | Type | Default | Description |
|------|------|---------|-------------|
| `misc.transaction-log.enabled` | Boolean | `false` | Log all transactions for auditing |
| `misc.transaction-log.file` | String | `transactions.log` | Log file name (inside plugin data folder) |
| `misc.transaction-log.max-size` | Integer | `10` | Maximum log file size in MB before rotation |
| `misc.transaction-log.max-files` | Integer | `5` | Number of rotated log files to keep |

---

## 📄 mob-rewards.yml

See the `mob-rewards.yml` file for per-mob configuration. Key sections:

| Section | Description |
|---------|-------------|
| `neutral` | Passive mob rewards (cows, sheep, etc.) |
| `easy` | Easy hostile mob rewards (zombies, skeletons, etc.) |
| `hard` | Hard hostile mob rewards (creepers, blazes, etc.) |
| `boss` | Boss mob rewards (ender dragon, wither, etc.) |
| `custom` | Custom mob entries (MythicMobs, etc.) |
| `global-multipliers` | Global reward multipliers |
| `kill-streaks` | Kill streak bonus configuration |
| `events` | Time-based event multipliers |

---

## 📄 ranks.yml

See [Ranks](Ranks.md) for full rank configuration details.

---

## 📄 messages.yml

Every message in DZEconomy can be customized. Key sections:

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
| `gui` | GUI-related messages |
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
### 📖 Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***
