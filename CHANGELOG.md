# 📋 Changelog

All notable changes to DZEconomy will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.1.2] — 2026-08-05

DZEconomy now runs on almost any server. The jar targets Java 8 bytecode and ships with a feature adapter layer, so it loads on Minecraft 1.9 through 1.26.x. This update also quiets the console and fixes a pile of broken messages and config wiring.

### Added

- **Minecraft 1.9–1.26.x support.** A `FeatureAdapter` picks the right API for the server version: `getOnlinePlayers()` handles both the old `Player[]` and the newer `Collection`, bungee detection uses pure reflection, hex colors map to `§x` on 1.16+ and fall back to the nearest legacy color below that.
- **Java 8 bytecode.** Compiled with `sourceCompatibility`/`targetCompatibility` 1.8 (major version 52). No more Java 21 requirement.
- **`/economy give <player> <amount> [currency]`.** One admin command to credit any currency. Money is the default; `mobcoin(s)` and `gem(s)` work too. Includes notification messages and tab completion.
- **`storage.sqlite.file` option.** Pick the SQLite database file name. Defaults to `data.db`.

### Removed

- **Dead code.** Deleted `UpdateCheckTask`, `RequestGUIManager` (and the `gui` package), `TransactionLogEntry`, and unused methods across the codebase.
- **Dead config keys.** Unused sections stripped from `config.yml` and `messages.yml` so existing installs don't chase phantom settings.

### Fixed

- **Messages showed "Message not found".** Around twenty messages pointed at `messages.yml` keys that didn't exist. Aliases and placeholder mappings are now correct, so welcome, PvP, payall, convert, and reload messages render properly.
- **New players never got a starting balance.** `isNewPlayer` was never set, so the welcome balance and messages never fired. New players now get their configured starting balance once.
- **Relogging reset cooldowns and daily limits.** Both were kept in memory only. They now persist across restarts.
- **Transfer limits and cooldowns did nothing.** `send`/`request`/`accept` read config keys that didn't exist. They now read `transfer.max-transaction`, `transfer.cooldowns.*`, and `transfer.daily-limit.*`, and honor `allow-self-transfer` and `block-during-combat`.
- **`/money accept` skipped the limits.** Accepting a request now applies the same max-transaction, cooldown, and daily-limit checks as a normal send.
- **PVP loot config was dead.** The death listener read keys that didn't exist and defaulted to wiping 100% of a victim's balance on a kill. It now reads `pvp.loss-percent.*`, `pvp.minimum-balance.*`, `pvp.broadcast.*`, and the world blacklist.
- **Leaderboards showed stale balances.** Baltop read storage directly and lagged behind in-memory balances. Rankings now reflect live cached balances.
- **Killer was told the wrong loot.** The PvP gain message reported the gross amount even though transfer tax reduced it. It now shows the net credited.
- **`{killer}`, `{victim}`, `{mob}`, `{symbol}`, `{timeout}` printed literally.** These placeholders were missing from the replacement table. They now resolve to real values.
- **Convert confirmation showed the wrong amount.** It displayed the input amount instead of what the player actually got after rates and fees. It now reports the received amount.
- **Daily reset wiped lifetime stats.** `resetDailyReceived` zeroed the lifetime `money_received` total. Daily resets now only clear daily counters.
- **Per-player locks leaked.** `playerLocks` entries grew with every join/quit. They now release on player unload.
- **SQLite backups could be torn.** Backups zipped the live WAL file without a checkpoint. A `wal_checkpoint(TRUNCATE)` now runs first.
- **Migration could lie about success.** Failed saves were counted as migrated. Failures are now counted, reported, and abort if a backend can't initialize.
- **`getSpigotConfig()` threw `NoSuchMethodError`.** The bungee detection fallback now uses reflection and catches `Throwable`.
- **Version comparison was wrong.** `1.0-rc10` sorted below `1.0-rc9`. Prerelease identifiers now compare per semver rules.
- **FlatFile hid write failures.** Save methods now return success flags callers report on failure.

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

[2.1.2]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v2.1.2
[2.1.0]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v2.1.0
[2.0.0]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v2.0.0
[1.0.0]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v1.0.0
