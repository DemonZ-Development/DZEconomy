# 📋 Changelog

All notable changes to DZEconomy will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [26.2.0] — 2026-08-01

### Removed

- **Dead code** — deleted `UpdateCheckTask`, `RequestGUIManager` (and the `gui` package), `TransactionLogEntry`, and dozens of unused public methods, fields, and imports across `DZEconomy`, `CurrencyManager`, `StorageProvider` implementations, `UpdateManager`, `CombatTagManager`, `Rank`, `MoneyUtil`, `ColorUtil`, `ServerPlatform`, and `CurrencyType`; this also removes the now-unused `MessageUtil` integration that surfaced broken message wiring
- **Dead config keys** — removed unused `display-format`, `auto-save.save-on-transaction`, `conversion.player-convert`, combat-tag `blocked-actions`/`include-pve`/`action-bar`, request, baltop, payall, updates-notify, ranks, and misc sections from `config.yml`; unused message keys stripped from `messages.yml`

### Added

- **`storage.sqlite.file` option** — configurable SQLite database file name (defaults to `data.db`)
- **`/economy give <player> <amount> [currency]`** — unified admin command to give any currency (defaults to money; aliases: `money`, `mobcoin(s)`, `gem(s)`) with success/target notifications and tab completion

### Fixed

- **Broken message wiring** — ~20 messages (`reload-success`, `convert-success`, `migrate-*`, `payall-*`, `combat-tagged`, `unknown-subcommand`, `update-available`, ...) referenced nonexistent `messages.yml` keys and rendered "Message not found"; aliases and correct placeholder mappings (`{from}`, `{to}`, `{from_balance}`, `{to_balance}`, `{count}`) are now in place
- **Update checker ignoring config** — the scheduled update check now honors `updates.check-enabled`
- **Lifetime statistics being wiped daily** — `resetDailyReceived()` overwrote the lifetime `money_received` total with 0; the daily reset now only clears daily counters and daily sent amounts
- **Per-player lock leak** — `CurrencyManager.playerLocks` entries were never removed, growing unbounded on servers with many join/quit cycles; locks are now released on player unload when safe
- **Migration silently reporting success** — `MigrationManager` ignored `initialize()` results and counted players as migrated even when `savePlayerData` failed; failed saves are now counted and reported, and migrations abort if a storage backend fails to initialize
- **Corrupt SQLite backups** — backups zipped the live WAL-mode database without checkpointing, producing torn files; a `wal_checkpoint(TRUNCATE)` is now issued before backup
- **Dead config keys for transfer limits** — `/money send` etc. read nonexistent `currencies.<cur>.max-transaction/send-cooldown/daily-limit` keys, so limits and cooldowns were silently never enforced; the code now reads `transfer.max-transaction`, `transfer.cooldowns.*`, and `transfer.daily-limit.*` (with legacy per-currency fallback), and honors `transfer.allow-self-transfer` and `transfer.block-during-combat`
- **Cooldown/daily-limit reset exploit** — send cooldowns and daily sent amounts were in-memory only, so relogging reset them; both are now persisted (SQLite/MySQL columns `sent_amount`/`send_time` added with a safe schema upgrade, FlatFile keys added)
- **Request accept bypassing limits** — `/money accept` transferred without max-transaction, cooldown, or daily-limit checks; it now applies the same limits as `/send`
- **Dead config keys for PvP loss** — `PlayerDeathListener` read nonexistent `pvp.<currency>.enabled/loss-percentage/broadcast-threshold` keys, defaulting to 100% loss with stock config; it now reads `pvp.loss-percent.*` (percent), `pvp.minimum-balance.*`, `pvp.broadcast.*`, and honors `pvp.world-blacklist`
- **New-player detection broken** — `isNewPlayer` was never set, so starting balances and first-join welcome messages never fired; SQLite/MySQL `loadPlayerData` now return null for players without a record (matching FlatFile), and new players get their configured starting balance once
- **`%balance%` placeholder clobbering `{amount}`** — messages using both placeholders (e.g. send confirmations) could show the new balance as the transferred amount
- **Broken message paths** — welcome messages (`welcome-new-player`/`welcome-back`), PvP loss/gain messages, `max-transaction-exceeded`, and update notifications mapped to nonexistent keys and rendered "Message not found"; all now resolve to the correct `messages.yml` entries, and missing placeholder mappings (`{command}`, `{symbol}`, `{money}`, `{mobcoins}`, `{gems}`, `{current}`, `{latest}`) were added
- **Request notification misleading** — told players to `/accept <id>` although the command takes a player name; the message and help now say `<player>`
- **Pre-release version ordering** — `SemanticVersion` compared suffixes as plain strings, so `1.0-rc10` sorted below `1.0-rc9` and `1.0-alpha` above `1.0-beta`; prerelease identifiers are now compared per semver rules
- **FlatFile save error reporting** — `savePlayerData` returned void and hid write failures; all storage providers now return a boolean success flag that callers report on failure
- **`{timeout}` rendered literally in request messages** — `Request sent ... (Expires in {timeout}s)` showed the raw placeholder because the sender notification never passed a timeout value; it now includes the configured `request.timeout`
- **`{symbol}` rendered literally in payall messages** — `/economy payall` success/broadcast messages showed `{symbol}` because the symbol placeholder was never filled; it is now passed through
- **Convert confirmation showed the wrong amount** — `/economy convert` reported the input amount for the target currency even when conversion rates/fees change it (e.g. rate 100x with 5% fee); the message now shows the actual amount received (`{to_amount}`)
- **`/economy baltop` and `/money top` showing stale balances** — leaderboards read the storage backend directly, which lags behind the in-memory cache (writes flush on unload/autosave); cached balances are now overlaid so rankings are current
- **New message keys invisible on existing installs** — `messages.yml` was read with a fallback that bypassed JAR defaults, so missing keys rendered "Message not found"; missing keys now fall back to the JAR default automatically
- **PvP loss/gain messages showing literal `{killer}`/`{victim}`** — the placeholder replacement table never mapped `%killer%`/`%victim%`, so both players saw raw placeholders instead of names
- **Mob reward messages showing literal `{mob}`** — `%mob%` was never mapped, so kill rewards showed `{mob}` instead of the mob name
- **PvP high-value kill broadcast rendering "Message not found"** — the `pvp-broadcast` message key was not mapped to `pvp.broadcast` in `messages.yml`

## [2.1.0] — 2026-05-28

### Added

- **Server Adapter System** — Version detection and platform-specific behavior via `ServerAdapter` interface
- **LegacyServerAdapter** — Explicit `Class.forName()` SQLite driver loading for Java 8-16 (fixes "No suitable driver found" on older servers)
- **ModernServerAdapter** — SPI-based driver discovery with `Class.forName()` fallback for Java 17+
- **ServerAdapterProvider** — Automatic Java version detection and adapter selection
- **SLF4J Shading** — Relocated to prevent runtime linkage conflicts with other plugins
- **FoliaAdapter migration** — `MigrationManager` now uses `FoliaAdapter` instead of direct `Bukkit.getScheduler()`

### Fixed

- **SQLite JDBC driver not loading** on legacy Paper/Spigot servers (classloader SPI issue)
- **EntityDeathListener dead code** — `requirePlayerKill=false` setting had no effect due to redundant null check
- **Java 14 switch expression** replaced with traditional switch for broader compatibility
- **Shading gaps** — SLF4J, CheckerFramework, and ErrorProne now properly relocated
- **Stale module-info.class** excluded from JAR
- **Stale native-image.properties** excluded from JAR
- **Empty META-INF/versions/9/org/ directories** cleaned from JAR
- **ServerAdapterProvider null safety** — robust handling of edge cases in version detection

## [2.0.0] — 2026-05-28

### 🚀 Complete Rewrite

DZEconomy v2.0.0 is a ground-up rewrite of the plugin with a modern architecture and dramatically expanded feature set.

### Added

- **Three-Currency System** — Money ($), MobCoins (★), and Gems (◆), each independently configurable
- **Rank Multiplier System** — LuckPerms integration with per-currency earning bonuses
- **Combat Tagging** — Blocks economy actions during PvP combat with action bar indicator
- **PvP Loot System** — Configurable percentage-based balance loss on PvP death
- **Mob Rewards** — Per-mob currency drops across 4 categories with kill streak bonuses
- **Currency Conversion** — Convert between currencies with configurable rates and fees
- **Payment Request System** — Request, accept, deny payments with timeout and GUI
- **PlaceholderAPI Expansion** — 15+ placeholders with 3-second caching
- **MySQL Storage** — HikariCP connection pooling for large servers
- **Flat File Storage** — YAML-based option for testing and small servers
- **Live Storage Migration** — `/economy migrate` command to switch backends without data loss
- **Folia Support** — Full region-based scheduling with automatic detection via `FoliaAdapter`
- **Auto-Save & Backups** — Configurable intervals and manual backup creation
- **Transaction Logging** — Audit log with file rotation for compliance
- **Daily Transfer Limits & Cooldowns** — Per-currency limits and cooldowns to prevent abuse
- **Fully Customizable Messages** — `messages.yml` with color codes, hex colors, and MiniMessage
- **Modrinth Update Checker** — Automatic update notifications for admins
- **Baltop Leaderboards** — Per-currency and global with pagination and caching
- **Developer API** — Public API for third-party plugin integration via JitPack
- **bStats Metrics** — Anonymous usage statistics
- **Purpur Support** — Full compatibility with Purpur server software

### Changed

- Rewritten from scratch with modular architecture
- Minimum Java version: 21
- Minimum Minecraft version: 1.16
- API version set to `1.20`

---

## [1.0.0] — Initial Release

- Basic single-currency economy
- SQLite storage
- Simple balance commands

---

[26.2.0]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v26.2.0
[2.1.0]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v2.1.0
[2.0.0]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v2.0.0
[1.0.0]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v1.0.0
