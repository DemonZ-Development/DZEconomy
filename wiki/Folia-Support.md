![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# Folia Support

How DZEconomy runs on Folia, Paper's region-based multithreading fork.

---

## What Folia Changes

[Folia](https://github.com/PaperMC/Folia) divides the world into independent regions that tick in parallel on multiple threads, instead of one main thread.

| Aspect | Paper | Folia |
|--------|-------|-------|
| Threading | One main thread | Multiple region threads |
| Scheduling | `Bukkit.getScheduler()` | Global, region, and entity schedulers |
| Thread safety | Not required | Required |
| Compatibility | Any plugin | Only Folia-aware plugins |

---

## How DZEconomy Adapts

DZEconomy routes all scheduling through `FoliaAdapter`, a utility class that detects Folia at startup and picks the right scheduler.

### Detection

On class load, DZEconomy checks for `io.papermc.paper.threadedregions.RegionizedServer`. When found, it initializes the Folia scheduler methods through reflection.

### The Schedulers

| Scheduler | Use Case | DZEconomy Usage |
|-----------|----------|-----------------|
| GlobalRegionScheduler | Tasks not tied to a location or entity | Auto-save, daily reset, update checks, combat tag cleanup |
| RegionScheduler | Tasks for a specific location | Location-bound work |
| EntityScheduler | Tasks for a specific entity | Player notifications, reward delivery |
| AsyncScheduler | Off-thread work | Database operations |

### FoliaAdapter API

```java
FoliaAdapter.isFolia()                              // true when running Folia
FoliaAdapter.runTask(plugin, () -> { ... })         // next tick, global/main thread
FoliaAdapter.runTaskAsynchronously(plugin, () -> { ... })
FoliaAdapter.runTaskLater(plugin, () -> { ... }, delayTicks)
FoliaAdapter.runTaskTimer(plugin, () -> { ... }, delayTicks, periodTicks)
FoliaAdapter.runTaskTimerAsynchronously(plugin, () -> { ... }, delayTicks, periodTicks)
FoliaAdapter.runAtEntity(plugin, entity, () -> { ... })
FoliaAdapter.runAtLocation(plugin, location, () -> { ... })
FoliaAdapter.cancelTasks(plugin)
```

Every call returns a `FoliaTask` handle with a single `cancel()` method, no matter which scheduler backed it.

### Thread Safety

All balance-modifying operations are safe from any thread:

- **Per-player locks** — each player has their own lock
- **Atomic transfers** — a two-phase commit keeps transfers consistent
- **Concurrent collections** — data caches use `ConcurrentHashMap`
- **Async storage** — database writes run off the tick loop

---

## Feature Compatibility on Folia

| Feature | Status |
|---------|--------|
| Multi-currency balances | Full support |
| Player-to-player transfers | Full support |
| Payment requests | Full support |
| Baltop | Full support |
| Mob rewards | Full support |
| PVP loot | Full support |
| Combat tag | Full support |
| Auto-save | Full support |
| MySQL storage | Full support |
| SQLite storage | Full support |
| PlaceholderAPI | Full support |
| LuckPerms integration | Full support |
| Rank multipliers | Full support |
| Currency conversion | Full support |
| Payall | Full support |
| Update checker | Full support |
| Config reload | Full support |

---

## Known Limitations

### Minimum Tick Delay

Folia schedulers require a minimum delay of 1 tick for delayed and repeating tasks. `FoliaAdapter` clamps all delays to at least 1.

### Entity Retirement

When an entity retires, its scheduled tasks are cancelled. DZEconomy handles this without errors: combat tag tasks clean up on quit, player data saves asynchronously on disconnect, and the retirement callback is a no-op.

### No Main Thread

Folia has no single main thread. Code that shipped expecting a main thread must run on the owning region's thread. The adapter routes `runAtEntity`, `runAtLocation`, and `runTask` accordingly.

### Bytecode Detection

Because the Folia scheduler classes only exist on Folia, the adapter loads them through reflection at startup. Standard Bukkit servers never touch that code path, so they get no class-loading errors.

---

## Performance on Folia

Folia helps servers where many players spread across different areas:

| Metric | Paper (100 players) | Folia (100 players) |
|--------|---------------------|---------------------|
| TPS | Can dip below 20 | Stable at 20 |
| Economy operations | Fast, one thread | Fast, parallel regions |
| Auto-save impact | Possible lag spike | Distributed |
| Mob reward processing | Sequential | Parallel by region |

Balance lookups hit the in-memory cache in constant time on both platforms. Balance changes only contend on the same player's lock, so different players never block each other.

### Tuning for Folia

1. Use MySQL for multi-server networks. SQLite is fine for a single Folia server.
2. Keep the auto-save interval reasonable. Folia spreads the load, but fewer saves still helps.
3. Disable features you do not use. PVP loot and combat tag off means fewer scheduled tasks.

---

## Troubleshooting

**"Not on the main thread" errors**

No such thing exists on Folia. If you see this from another plugin, that plugin calls `Bukkit.getScheduler()` directly and will not work on Folia. DZEconomy never does.

**Tasks not executing**

Check whether the entity or location is still valid. A retired entity never runs its scheduled tasks. Confirm console shows DZEconomy enabled normally.

**Performance regression**

1. Confirm your Folia build is current (1.19.4+).
2. Check whether another plugin is the bottleneck, not DZEconomy.
3. Raise `auto-save.interval` if the save tasks contend with gameplay regions.

---

<p align="center">
  See <a href="Version-Coverage.md">Version Coverage</a> for supported Folia versions.
</p>

---
### Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***