# 📋 Changelog

All notable changes to DZEconomy will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

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

[2.1.0]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v2.1.0
[2.0.0]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v2.0.0
[1.0.0]: https://github.com/DemonZ-Development/DZEconomy/releases/tag/v1.0.0
