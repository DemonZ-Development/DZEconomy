# 📊 Version Coverage

Supported Minecraft versions and server software for DZEconomy v2.1.1.

---

## 🖥️ Server Software Support

### Compatibility Matrix

| Server Software | Supported Versions | API Version | Status |
|----------------|-------------------|-------------|--------|
| **Bukkit** | 1.16 – 1.21.5 | `1.20` | ✅ Supported |
| **Spigot** | 1.16 – 1.21.5 | `1.20` | ✅ Supported |
| **Paper** | 1.16.5 – 1.21.5 | `1.20` | ✅ **Recommended** |
| **Folia** | 1.19.4 – 1.21.5 | `1.20` | ✅ Supported |
| **Purpur** | 1.16.5 – 1.21.5 | `1.20` | ✅ Supported |

> 💡 **Paper** is the recommended server software for best performance and compatibility.

---

## ☕ Java Requirements

| Java Version | Status | Notes |
|--------------|--------|-------|
| Java 8 | ⚠️ Legacy (adapter) | Legacy server adapter provides Class.forName fallback |
| Java 11 | ⚠️ Legacy (adapter) | Legacy adapter handles SPI classloader issues |
| **Java 17** | ✅ Minimum for modern | Full modern adapter (SPI driver loading) |
| **Java 21** | ✅ **Recommended** | Best performance and features |

DZEconomy v2.1.1 is compiled with Java 21. The built JAR requires Java 21+ to run. The **Server Adapter** system detects the Java version at runtime and provides appropriate driver loading behavior: legacy (pre-Java 17) uses explicit `Class.forName()`, modern (Java 17+) relies on SPI with fallback.

### Checking Your Java Version

```bash
java -version
# Expected output: openjdk version "21.0.x" or higher
```

---

## 📋 Detailed Version Support

### Bukkit

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| 1.16.x | ✅ Full | Minimum supported version |
| 1.17.x | ✅ Full | |
| 1.18.x | ✅ Full | |
| 1.19.x | ✅ Full | |
| 1.20.x | ✅ Full | API version `1.20` |
| 1.21.x | ✅ Full | Latest supported |
| 1.21.5 | ✅ Full | Latest tested |

### Spigot

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| 1.16.x | ✅ Full | Minimum supported version |
| 1.17.x | ✅ Full | |
| 1.18.x | ✅ Full | |
| 1.19.x | ✅ Full | |
| 1.20.x | ✅ Full | |
| 1.21.x | ✅ Full | |
| 1.21.5 | ✅ Full | Latest tested |

### Paper

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| 1.16.5 | ✅ Full | Minimum supported version |
| 1.17.x | ✅ Full | |
| 1.18.x | ✅ Full | |
| 1.19.x | ✅ Full | |
| 1.20.x | ✅ Full | Best performance |
| 1.21.x | ✅ Full | Best performance |
| 1.21.5 | ✅ Full | Latest tested |

> **Why does Paper support start at 1.16.5 instead of 1.16?**
> Paper made significant API changes in 1.16.5 that DZEconomy depends on. Earlier 1.16 builds are not tested.

### Folia

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| < 1.19.4 | ❌ Not supported | Folia's scheduler API not available |
| 1.19.4 | ✅ Full | Minimum supported Folia version |
| 1.20.x | ✅ Full | |
| 1.21.x | ✅ Full | |
| 1.21.5 | ✅ Full | Latest tested |

> **Why does Folia support start at 1.19.4?**
> Folia's region-based scheduling API was introduced in 1.19.4. DZEconomy requires these APIs for its `FoliaAdapter` to function.

### Purpur

| Version Range | Support Level | Notes |
|---------------|--------------|-------|
| 1.16.5 | ✅ Full | Minimum supported version |
| 1.17.x | ✅ Full | |
| 1.18.x | ✅ Full | |
| 1.19.x | ✅ Full | |
| 1.20.x | ✅ Full | |
| 1.21.x | ✅ Full | |
| 1.21.5 | ✅ Full | Latest tested |

---

## 🔧 API Version Requirements

DZEconomy declares `api-version: '1.20'` in `plugin.yml`. This means:

- The plugin uses the 1.20+ Bukkit API
- Older server versions may not have all required API methods
- Newer server versions maintain backward compatibility with this API version

### What `api-version: 1.20` Means

| Feature | Details |
|---------|---------|
| Text component API | Available |
| Adventure API | Available (Paper) |
| New event types | Available |
| Config sections | Modern behavior |
| Tab completion | Modern API |

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

---

## 🔄 Version Support Policy

| Category | Support Level |
|----------|--------------|
| **Latest release** (1.21.x) | Full support — all features, bug fixes, and testing |
| **Previous release** (1.20.x) | Full support — all features and critical bug fixes |
| **Older releases** (1.16–1.19) | Best-effort support — features work, but less testing |
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
| HikariCP | 5.0 | 5.1.0 | Bundled (shaded) |
| SQLite JDBC | 3.40 | 3.45.0 | Bundled (shaded) |
| Gson | 2.10 | 2.10.1 | Bundled (shaded) |

> **Note**: HikariCP, SQLite JDBC, and Gson are bundled (shaded) into the DZEconomy jar. No additional downloads are needed.

---

## ❓ Frequently Asked Questions

### Will DZEconomy work on Minecraft 1.22+?

Yes, as long as the Bukkit API maintains backward compatibility. DZEconomy uses `api-version: 1.20`, which is supported by all newer server versions.

### Can I use DZEconomy on a 1.15 server?

No. DZEconomy requires Minecraft 1.16+ due to API requirements. The hex color code support (`&#RRGGBB`) in messages also requires 1.16+.

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
