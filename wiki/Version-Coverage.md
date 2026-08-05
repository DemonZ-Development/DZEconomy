![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# Version Coverage

Supported Minecraft versions and server software for DZEconomy v2.1.2.

---

## Server Software Support

| Server Software | Supported Versions | API Version | Status |
|----------------|-------------------|-------------|--------|
| **Bukkit** | 1.9 – 1.26.x | `1.13` | Supported |
| **Spigot** | 1.9 – 1.26.x | `1.13` | Supported |
| **Paper** | 1.9 – 1.26.x | `1.13` | Recommended |
| **Folia** | 1.19.4 – 1.26.x | `1.13` | Supported |
| **Purpur** | 1.9 – 1.26.x | `1.13` | Supported |

Paper gives the best performance and compatibility, but every listed platform works.

---

## Java Requirements

| Java Version | Status | Notes |
|--------------|--------|-------|
| **Java 8** | Supported | Minimum. The jar compiles to Java 8 bytecode |
| **Java 11** | Supported | |
| **Java 17** | Supported | |
| **Java 21** | Recommended | Best performance |

The jar targets Java 8 bytecode (major version 52), so it loads on any server running Java 8 or newer. At runtime the server adapter picks the right driver loading strategy: legacy Java uses explicit `Class.forName()`, Java 17+ relies on SPI with fallback.

### Check Your Java Version

```
java -version
```

---

## Feature Adapters

DZEconomy handles version differences through a FeatureAdapter layer, selected at startup from `Bukkit.getBukkitVersion()`.

| Feature | Legacy (1.9–1.15) | Modern (1.16+) |
|---------|-------------------|----------------|
| `getOnlinePlayers()` | Reflection. Handles `Player[]` (1.9–1.11) and `Collection` (1.12+) | Direct `Collection` call |
| `isBungeeCord()` | Reflection on `org.spigotmc.SpigotConfig#bungee` | Same. Reflection-safe |
| Hex colors `&#RRGGBB` | Nearest legacy color fallback | Full `§x§R§R§G§G§B§B` support |

---

## Version Support by Platform

### Bukkit

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| 1.9.x – 1.15.x | Full | Legacy feature adapter, Java 8 bytecode |
| 1.16.x | Full | Hex color support begins |
| 1.17.x – 1.19.x | Full | |
| 1.20.x | Full | |
| 1.21.x – 1.26.x | Full | Latest supported |

### Spigot

Same coverage as Bukkit, from 1.9.x through 1.26.x.

### Paper

Same coverage as Bukkit, from 1.9.x through 1.26.x. Best performance of any platform.

### Folia

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| Below 1.19.4 | Not supported | Folia's scheduler API does not exist there |
| 1.19.4 – 1.26.x | Full | Folia adapter handles region-based scheduling |

Folia support starts at 1.19.4 because that is where Folia introduced its region-based scheduling API. The FoliaAdapter depends on it.

### Purpur

Same coverage as Bukkit, from 1.9.x through 1.26.x.

---

## API Version Requirements

DZEconomy declares `api-version: '1.13'` in `plugin.yml`:

- Loads on Minecraft 1.13+ servers
- Pre-1.13 servers (1.9–1.12) ignore `api-version` and load anyway
- Newer server versions keep backward compatibility with this API version

| Feature | Details |
|---------|---------|
| Legacy color codes | Supported |
| Hex color codes | Supported (1.16+ via FeatureAdapter) |
| Action bar messages | Supported (1.9+ via Spigot API) |
| Persistent data container | Optional. Guarded, 1.14+ only |

---

## Tested Configurations

| Software | Version | Java | Storage | Status |
|----------|---------|------|---------|--------|
| Paper | 1.20.4 | 21 | SQLite | Pass |
| Paper | 1.20.4 | 21 | MySQL | Pass |
| Paper | 1.21.4 | 21 | SQLite | Pass |
| Paper | 1.21.4 | 21 | MySQL | Pass |
| Folia | 1.20.4 | 21 | SQLite | Pass |
| Folia | 1.21.4 | 21 | MySQL | Pass |
| Spigot | 1.20.4 | 21 | SQLite | Pass |
| Purpur | 1.20.4 | 21 | SQLite | Pass |
| Purpur | 1.21.1 | 21 | SQLite | Pass (E2E) |

The Java 8 bytecode target is verified in CI. Legacy servers (1.9–1.15) run through the FeatureAdapter layer.

---

## Version Support Policy

| Category | Support Level |
|----------|--------------|
| **Latest release** (1.21.x – 1.26.x) | Full support. All features, bug fixes, testing |
| **Previous release** (1.20.x) | Full support. All features and critical bug fixes |
| **Legacy releases** (1.9 – 1.19) | Full support. FeatureAdapter handles version differences |
| **Future releases** | Supported as soon as Paper/Spigot publishes the API |

DZEconomy may drop support for a Minecraft version when:

- The version is no longer supported by Paper or Spigot
- A critical security vulnerability affects only that version
- Maintaining compatibility requires unreasonable code complexity

Minimum version bumps are announced in advance.

---

## Bundled Dependencies

| Dependency | Minimum Version | Tested Version | Where |
|------------|----------------|----------------|-------|
| PlaceholderAPI | 2.11.0 | 2.11.6 | Optional. Not bundled |
| LuckPerms | 5.0 | 5.4 | Optional. Not bundled |
| HikariCP | 4.0 | 4.0.3 | Shaded into the jar |
| SQLite JDBC | 3.40 | 3.45.0 | Shaded into the jar |
| Gson | 2.10 | 2.10.1 | Shaded into the jar |
| Caffeine | 2.9 | 2.9.3 | Shaded into the jar |

HikariCP, SQLite JDBC, Gson, and Caffeine ship inside the jar. No extra downloads.

---

## Frequently Asked Questions

**Will DZEconomy work on Minecraft 1.27+?**

Yes, as long as the Bukkit API keeps backward compatibility. The `api-version: 1.13` declaration is accepted by every newer server.

**Can I use DZEconomy on a 1.9 server?**

Yes. Java 8 bytecode plus the FeatureAdapter layer. Legacy servers use the legacy adapter for `getOnlinePlayers()` and color codes.

**Does DZEconomy work with Fabric or Forge?**

No. DZEconomy is a Bukkit plugin and needs Bukkit-compatible server software.

**What about CatServer, Mohist, or ArcLight?**

Those hybrid servers might run it, but they are not officially supported. Use at your own risk.

---

<p align="center">
  See <a href="Folia-Support.md">Folia Support</a> for Folia details and <a href="Installation.md">Installation</a> for setup.
</p>

---
### Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***
