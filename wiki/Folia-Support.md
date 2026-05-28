# 🌿 Folia Support

Complete guide to DZEconomy's compatibility with Folia — Paper's region-based multithreading fork.

---

## 🤔 What is Folia?

[Folia](https://github.com/PaperMC/Folia) is a fork of Paper that implements **region-based multithreading**. Instead of processing all game logic on a single main thread, Folia divides the world into independent regions that can tick in parallel on multiple threads.

### Key Differences from Paper

| Aspect | Paper | Folia |
|--------|-------|-------|
| Threading | Single main thread | Multiple region threads |
| Scheduling | `Bukkit.getScheduler()` | Global, Region, and Entity schedulers |
| Thread safety | Not required | Essential |
| Compatibility | All plugins | Only Folia-compatible plugins |

---

## ✅ How DZEconomy Adapts

DZEconomy v2.1.1 was built from the ground up with Folia support. The plugin uses a custom **`FoliaAdapter`** utility class that automatically detects the server type and delegates to the appropriate scheduler API.

### Automatic Detection

On startup, DZEconomy checks for Folia by looking for the class `io.papermc.paper.threadedregions.RegionizedServer`:

```java
// FoliaAdapter.java (simplified)
static {
    boolean folia;
    try {
        Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
        folia = true;
    } catch (ClassNotFoundException e) {
        folia = false;
    }
    FOLIA = folia;
}
```

If Folia is detected, the console shows:
```
[INFO] Folia detected! Using region-based scheduling.
```

If not:
```
[INFO] Running on Paper 1.21.4-...
```

### Region-Based Scheduling

DZEconomy uses three Folia scheduler types depending on the context:

| Scheduler | Use Case | DZEconomy Usage |
|-----------|----------|-----------------|
| **GlobalRegionScheduler** | Tasks not tied to a specific location or entity | Auto-save, daily reset, update checks, combat tag cleanup |
| **RegionScheduler** | Tasks at a specific location | Location-dependent operations |
| **EntityScheduler** | Tasks for a specific entity | Player-bound operations (balance display, notifications) |

### FoliaAdapter API

The `FoliaAdapter` class provides a unified API that works on both Folia and standard Bukkit:

```java
// Run a task on the next tick
FoliaAdapter.runTask(plugin, () -> { ... });

// Run a task asynchronously
FoliaAdapter.runTaskAsynchronously(plugin, () -> { ... });

// Run a task after a delay
FoliaAdapter.runTaskLater(plugin, () -> { ... }, delayTicks);

// Run a repeating task
FoliaAdapter.runTaskTimer(plugin, () -> { ... }, delayTicks, periodTicks);

// Run at a specific entity's region
FoliaAdapter.runAtEntity(plugin, entity, () -> { ... });

// Run at a specific location's region
FoliaAdapter.runAtLocation(plugin, location, () -> { ... });

// Cancel all tasks
FoliaAdapter.cancelTasks(plugin);
```

### Thread-Safe Operations

All balance-modifying operations in DZEconomy are **thread-safe**:

- **Per-player locks**: Each player has their own lock, preventing race conditions
- **Atomic transfers**: The `transfer()` method uses a two-phase commit to ensure consistency
- **Concurrent collections**: Player data caches use `ConcurrentHashMap`
- **Async storage**: All database operations run asynchronously

---

## 📊 Feature Compatibility on Folia

| Feature | Folia Status | Notes |
|---------|-------------|-------|
| Multi-currency balances | ✅ Full support | Thread-safe operations |
| Player-to-player transfers | ✅ Full support | Atomic with per-player locks |
| Payment requests | ✅ Full support | Entity scheduler for notifications |
| Baltop | ✅ Full support | Global scheduler for refresh |
| Mob rewards | ✅ Full support | Entity scheduler for reward delivery |
| PVP loot | ✅ Full support | Entity scheduler for death handling |
| Combat tag | ✅ Full support | Entity scheduler for tag updates |
| Auto-save | ✅ Full support | Global scheduler |
| MySQL storage | ✅ Full support | Async operations |
| SQLite storage | ✅ Full support | Async operations |
| PlaceholderAPI | ✅ Full support | 3-second cache prevents excessive lookups |
| LuckPerms integration | ✅ Full support | Cached group lookups |
| Rank multipliers | ✅ Full support | Cached rank data |
| Currency conversion | ✅ Full support | Thread-safe |
| Payall | ✅ Full support | Entity scheduler per player |
| Update checker | ✅ Full support | Global scheduler |
| GUI (Request Manager) | ✅ Full support | Entity scheduler for GUI opening |
| Config reload | ✅ Full support | Global scheduler |

---

## ⚠️ Known Limitations

### 1. Minimum Tick Delay

On Folia, all scheduled tasks require a minimum delay of **1 tick**. DZEconomy handles this automatically by ensuring all delays are `>= 1`.

### 2. Entity Retirement

When an entity is retired (e.g., player disconnects), tasks scheduled on that entity's scheduler are retired. DZEconomy handles this gracefully:
- Combat tag tasks are cleaned up on player quit
- Pending requests are preserved in cache
- Player data is saved asynchronously on disconnect

### 3. No Global Region Main Thread

Unlike standard Bukkit, Folia does not have a single "main thread." Some operations that would normally require the main thread can run on any region thread. DZEconomy's `FoliaAdapter` handles this transparently.

### 4. Offline Player Lookups

Offline player name lookups (`Bukkit.getOfflinePlayer(uuid).getName()`) may be slower on Folia due to the lack of a main thread guarantee. DZEconomy caches offline player names when possible.

### 5. Third-Party Plugin Compatibility

Some third-party plugins may not be Folia-compatible. DZEconomy's integrations (PlaceholderAPI, LuckPerms) are verified to work on Folia, but other plugins that interact with DZEconomy may have issues.

---

## ⚡ Performance on Folia

### Expected Performance

Folia's multithreaded model provides significant performance benefits for servers with many players spread across different areas:

| Metric | Paper (100 players) | Folia (100 players) |
|--------|---------------------|---------------------|
| TPS | May dip below 20 | Stable at 20 |
| Economy operations | Fast (single thread) | Fast (parallel regions) |
| Auto-save impact | May cause lag spike | Distributed across regions |
| Mob reward processing | Sequential | Parallel by region |

### DZEconomy-Specific Performance

- **Balance lookups**: O(1) from cache — no difference between Folia and Paper
- **Balance modifications**: Per-player locked — no contention between different players
- **Mob reward processing**: Parallel on Folia — faster with many players in different regions
- **Auto-save**: Uses global scheduler — same performance as Paper
- **Baltop refresh**: Uses global scheduler — same performance as Paper

### Optimization Tips for Folia

1. **Use MySQL** for shared network setups — SQLite is fine for single-server Folia
2. **Increase auto-save interval** — Folia distributes the load, but longer intervals are still better
3. **Reduce combat tag duration** — Shorter combat tags mean fewer active entity scheduler tasks
4. **Disable unnecessary features** — If you don't use PVP loot or combat tags, disable them

---

## 🔧 Troubleshooting Folia Issues

### "Not on the main thread" Errors

If you see errors about operations not being on the main thread:
1. Ensure you're running DZEconomy v2.1.1+ (earlier versions don't support Folia)
2. Check that no other plugin is calling DZEconomy methods from an unexpected thread
3. The `FoliaAdapter` should handle all scheduling automatically — report issues on GitHub

### Tasks Not Executing

If scheduled tasks don't seem to execute:
1. Check console for "Folia detected!" message at startup
2. Ensure the region/entity the task is scheduled on is actually loaded
3. For entity tasks, the entity must still be valid (not retired)

### Performance Regression on Folia

If DZEconomy performs worse on Folia than Paper:
1. Check that you're using the correct Folia build (not an outdated one)
2. Verify your Folia version is compatible (1.19.4+)
3. Ensure no other plugin is causing region contention
4. Try reducing `auto-save.interval` to spread the load more evenly

---

<p align="center">
  See <a href="Version-Coverage.md">Version Coverage</a> for supported Folia versions.
</p>

---
### 📖 Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***
