# 📋 Changelog

All notable changes to DZEconomy will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

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
- Minimum Java version: 17 (21 recommended)
- Minimum Minecraft version: 1.16
- API version set to `1.20`

---

## [1.0.0] — Initial Release

- Basic single-currency economy
- SQLite storage
- Simple balance commands

---

[2.0.0]: https://github.com/DemonZDevelopment/DZEconomy/releases/tag/v2.0.0
[1.0.0]: https://github.com/DemonZDevelopment/DZEconomy/releases/tag/v1.0.0
