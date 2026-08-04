# 📊 Version Coverage

Supported Minecraft versions and server software for DZEconomy v26.2.0.

---

## 🖥️ Server Software Support

### Compatibility Matrix

| Server Software | Supported Versions | API Version | Status |
|----------------|-------------------|-------------|--------|
| **Bukkit** | 1.9 – 1.26.x | `1.13` | ✅ Supported |
| **Spigot** | 1.9 – 1.26.x | `1.13` | ✅ Supported |
| **Paper** | 1.9 – 1.26.x | `1.13` | ✅ **Recommended** |
| **Folia** | 1.19.4 – 1.26.x | `1.13` | ✅ Supported |
| **Purpur** | 1.9 – 1.26.x | `1.13` | ✅ Supported |

> 💡 **Paper** is the recommended server software for best performance and compatibility.

---

## ☕ Java Requirements

| Java Version | Status | Notes |
|--------------|--------|-------|
| **Java 8** | ✅ Supported | Minimum — compiled with Java 8 bytecode |
| **Java 11** | ✅ Supported | |
| **Java 17** | ✅ Supported | |
| **Java 21** | ✅ **Recommended** | Best performance and features |

DZEconomy v26.2.0 compiles to Java 8 bytecode (major version 52). The JAR loads on any server running Java 8+. The **Server Adapter** system detects the Java version at runtime and provides appropriate driver loading behavior: legacy (pre-Java 17) uses explicit `Class.forName()`, modern (Java 17+) relies on SPI with fallback.

### Checking Your Java Version

```bash
java -version
# Expected output: openjdk version "21.0.x" or higher
```

---

## 🔌 Feature Adapters

DZEconomy uses a **FeatureAdapter** layer to handle version-specific APIs:

| Feature | Legacy (1.9–1.15) | Modern (1.16+) |
|---------|-------------------|----------------|
| `getOnlinePlayers()` | Reflection — handles both `Player[]` (1.9–1.11) and `Collection` (1.12+) | Direct `Collection` call |
| `isBungeeCord()` | Pure reflection on `org.spigotmc.SpigotConfig#bungee` | Same — reflection-safe |
| Hex colors `&#RRGGBB` | Nearest legacy color fallback | Full `§x§R§R§G§G§B§B` support |

The adapter is selected at startup based on `Bukkit.getBukkitVersion()`.

---

## 📋 Detailed Version Support

### Bukkit

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| 1.9.x – 1.15.x | ✅ Full | Legacy feature adapter, Java 8 bytecode |
| 1.16.x | ✅ Full | Hex color support begins |
| 1.17.x – 1.19.x | ✅ Full | |
| 1.20.x | ✅ Full | API version `1.13` |
| 1.21.x – 1.26.x | ✅ Full | Latest supported |

### Spigot

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| 1.9.x – 1.15.x | ✅ Full | Legacy feature adapter, Java 8 bytecode |
| 1.16.x | ✅ Full | Hex color support begins |
| 1.17.x – 1.19.x | ✅ Full | |
| 1.20.x | ✅ Full | |
| 1.21.x – 1.26.x | ✅ Full | Latest supported |

### Paper

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| 1.9.x – 1.15.x | ✅ Full | Legacy feature adapter, Java 8 bytecode |
| 1.16.x | ✅ Full | Hex color support begins, best performance |
| 1.17.x – 1.19.x | ✅ Full | Best performance |
| 1.20.x | ✅ Full | Best performance |
| 1.21.x – 1.26.x | ✅ Full | Best performance, latest supported |

### Folia

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| < 1.19.4 | ❌ Not supported | Folia's scheduler API not available |
| 1.19.4 – 1.26.x | ✅ Full | Folia adapter handles region-based scheduling |

> **Why does Folia support start at 1.19.4?**
> Folia's region-based scheduling API was introduced in 1.19.4. DZEconomy requires these APIs for its `FoliaAdapter` to function.

### Purpur

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| 1.9.x – 1.15.x | ✅ Full | Legacy feature adapter, Java 8 bytecode |
| 1.16.x | ✅ Full | Hex color support begins |
| 1.17.x – 1.19.x | ✅ Full | |
| 1.20.x | ✅ Full | |
| 1.21.x – 1.26.x | ✅ Full | Latest supported |

---

## 🔧 API Version Requirements

DZEconomy declares `api-version: '1.13'` in `plugin.yml`. This means:

- The plugin loads on Minecraft 1.13+ servers
- Pre-1.13 servers (1.9–1.12) ignore `api-version` and load the plugin anyway
- Newer server versions maintain backward compatibility with this API version

### What `api-version: 1.13` Means

| Feature | Details |
|---------|---------|
| Legacy color codes | Supported |
| Hex color codes | Supported (1.16+ via FeatureAdapter) |
| Action bar messages | Supported (1.9+ via Spigot API) |
| Persistent data container | Optional (guarded — 1.14+ only) |

---

## 🧪 Tested Configurations

DZEconomy is tested on the following configurations:

| Software | Version | Java | Storage | Status |
|----------|---------|------|---------|--------|
| Paper | 1.20.4 | 21 | SQLite | ✅ Pass |
| Paper | 1.20.4 | 21 | MySQL | ✅ Pass |
| Paper | 1.21.4 | 21 | SQLite | ✅ Pass |
| Paper | 1.21.4 | 21 | MySQL | ✅ Pass |
| Folia | 1.20.4 | 21 | SQLite | ✅ Pass |
| Folia | 1.21.4 | 21 | MySQL | ✅ Pass |
| Spigot | 1.20.4 | 21 | SQLite | ✅ Pass |
| Purpur | 1.20.4 | 21 | SQLite | ✅ Pass |
| Purpur | 1.21.1 | 21 | SQLite | ✅ Pass (E2E) |

> **Note**: Java 8 bytecode (major version 52) is verified. Legacy servers (1.9–1.15) use the FeatureAdapter layer for version-specific APIs.

---

## 🔄 Version Support Policy

| Category | Support Level |
|----------|--------------|
| **Latest release** (1.21.x–1.26.x) | Full support — all features, bug fixes, and testing |
| **Previous release** (1.20.x) | Full support — all features and critical bug fixes |
| **Legacy releases** (1.9–1.19) | Full support — FeatureAdapter handles version differences |
| **Future releases** | Supported as soon as Paper/Spigot publishes the API |

### Dropping Version Support

DZEconomy may drop support for Minecraft versions when:
- The version is no longer supported by Paper/Spigot
- A critical security vulnerability affects only that version
- Maintaining compatibility requires significant code complexity

**Minimum version bumps will always be announced in advance.**

---

## 📦 Dependency Compatibility

| Dependency | Minimum Version | Tested Version | Required |
|------------|----------------|----------------|----------|
| PlaceholderAPI | 2.11.0 | 2.11.6 | No (optional) |
| LuckPerms | 5.0 | 5.4 | No (optional) |
| HikariCP | 4.0 | 4.0.3 | Bundled (shaded) |
| SQLite JDBC | 3.40 | 3.45.0 | Bundled (shaded) |
| Gson | 2.10 | 2.10.1 | Bundled (shaded) |
| Caffeine | 2.9 | 2.9.3 | Bundled (shaded) |

> **Note**: HikariCP, SQLite JDBC, Gson, and Caffeine are bundled (shaded) into the DZEconomy jar. No additional downloads are needed.

---

## ❓ Frequently Asked Questions

### Will DZEconomy work on Minecraft 1.22+?

Yes, as long as the Bukkit API maintains backward compatibility. DZEconomy uses `api-version: 1.13`, which is supported by all newer server versions.

### Can I use DZEconomy on a 1.9 server?

Yes. DZEconomy compiles to Java 8 bytecode and uses a FeatureAdapter layer to handle version-specific APIs. Legacy servers (1.9–1.15) use the legacy adapter for `getOnlinePlayers()` and color codes.

### Does DZEconomy work with Fabric/Forge?

No. DZEconomy is a Bukkit-based plugin and only works on Bukkit API-compatible server software (Bukkit, Spigot, Paper, Folia, Purpur).

### What about CatServer, Mohist, or ArcLight?

These hybrid servers (Bukkit + Forge/Mod loader) may work but are **not officially supported**. Use at your own risk.

---

<p align="center">
  See <a href="Folia-Support.md">Folia Support</a> for Folia-specific details and <a href="Installation.md">Installation</a> for setup instructions.
</p>

---
### 📖 Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***
