# 🛠️ Developer API

Complete guide to integrating with the DZEconomy API v2.1.0.

---

## 📖 Overview

DZEconomy provides a **public API** that allows other plugins to:
- Query and modify player balances
- Transfer and convert currencies
- Get player rank information
- Format currency values

All methods that modify balances are **thread-safe** and use per-player locks.

---

## 🚀 Getting Started

### 1. Add the Dependency

#### Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.DemonZ-Development</groupId>
        <artifactId>DZEconomy</artifactId>
        <version>2.1.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.DemonZ-Development:DZEconomy:2.1.0'
}
```

### 2. Declare the Dependency

In your `plugin.yml`:

```yaml
# Required dependency (plugin won't load without DZEconomy)
depend: [DZEconomy]

# OR soft dependency (plugin loads without DZEconomy, but features may be disabled)
softdepend: [DZEconomy]
```

### 3. Get the API Instance

```java
import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.api.DZEconomyAPI;
import org.bukkit.Bukkit;

public class MyPlugin extends JavaPlugin {

    private DZEconomyAPI economyAPI;

    @Override
    public void onEnable() {
        // Get the DZEconomy plugin instance
        DZEconomy dzeconomy = (DZEconomy) Bukkit.getPluginManager().getPlugin("DZEconomy");

        if (dzeconomy == null) {
            getLogger().severe("DZEconomy not found! Disabling economy features...");
            // Don't disable your plugin, just disable economy features
            return;
        }

        // Get the API instance
        economyAPI = dzeconomy.getAPI();

        getLogger().info("DZEconomy API v" + economyAPI.getAPIVersion() + " hooked successfully!");
    }
}
```

---

## 📚 API Methods

### Balance Operations

```java
/**
 * Get a player's balance for a specific currency.
 * @param uuid The player's UUID
 * @param type The currency type
 * @return The player's balance (0.0 if player not found)
 */
double getBalance(@NotNull UUID uuid, @NotNull CurrencyType type);

/**
 * Check if a player has at least the specified amount.
 * @param uuid The player's UUID
 * @param type The currency type
 * @param amount The amount to check
 * @return true if the player has enough currency
 */
boolean hasBalance(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);

/**
 * Add currency to a player's balance.
 * @param uuid The player's UUID
 * @param type The currency type
 * @param amount The amount to add (must be >= 0)
 * @return true if the operation succeeded
 */
boolean addCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);

/**
 * Remove currency from a player's balance.
 * @param uuid The player's UUID
 * @param type The currency type
 * @param amount The amount to remove (must be >= 0)
 * @return true if the operation succeeded (player had enough)
 */
boolean removeCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);

/**
 * Set a player's balance to a specific amount.
 * @param uuid The player's UUID
 * @param type The currency type
 * @param amount The amount to set (must be >= 0)
 * @return true if the operation succeeded
 */
boolean setCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);
```

### Transfer & Conversion

```java
/**
 * Transfer currency from one player to another (atomic, thread-safe).
 * @param from The sender's UUID
 * @param to The receiver's UUID
 * @param type The currency type
 * @param amount The amount to transfer (must be > 0)
 * @return true if the transfer succeeded
 */
boolean transferCurrency(@NotNull UUID from, @NotNull UUID to, @NotNull CurrencyType type, double amount);

/**
 * Convert currency from one type to another for a player.
 * @param uuid The player's UUID
 * @param from The source currency type
 * @param to The target currency type
 * @param amount The amount to convert (must be > 0)
 * @return true if the conversion succeeded
 */
boolean convertCurrency(@NotNull UUID uuid, @NotNull CurrencyType from, @NotNull CurrencyType to, double amount);

/**
 * Get the conversion rate between two currencies.
 * @param from The source currency type
 * @param to The target currency type
 * @return The conversion rate (1.0 if same currency)
 */
double getConversionRate(@NotNull CurrencyType from, @NotNull CurrencyType to);
```

### Rank Operations

```java
/**
 * Get a player's current rank.
 * @param uuid The player's UUID
 * @return The player's Rank, or null if no rank is found
 */
@Nullable Rank getPlayerRank(@NotNull UUID uuid);

/**
 * Get all configured ranks.
 * @return An unmodifiable list of all ranks
 */
@NotNull List<Rank> getAllRanks();
```

### Formatting

```java
/**
 * Format a currency amount with the currency's symbol.
 * @param amount The amount to format
 * @param type The currency type
 * @return Formatted string (e.g., "$1,234.56")
 */
@NotNull String formatCurrency(double amount, @NotNull CurrencyType type);

/**
 * Format a currency amount in compact/short form.
 * @param amount The amount to format
 * @return Short-formatted string (e.g., "1.2K", "3.4M")
 */
@NotNull String formatCurrencyShort(double amount);

/**
 * Get the API version.
 * @return The API version number (currently 3)
 */
int getAPIVersion();
```

---

## 📋 CurrencyType Enum

```java
public enum CurrencyType {
    MONEY("money", "Money", "$", 0.0, 'a'),
    MOBCOIN("mobcoin", "MobCoin", "★", 0.0, 'e'),
    GEM("gem", "Gem", "◆", 0.0, 'b');

    // Methods
    String getId();
    String getDisplayName();
    String getDefaultSymbol();
    double getDefaultBalance();
    char getColorCode();
    String getColor();
    String getColoredDisplayName();

    // Parse from string
    static CurrencyType fromString(String name); // Supports aliases
}
```

**Supported aliases for `fromString()`:**
- `MONEY`: `money`, `cash`, `dollars`, `balance`
- `MOBCOIN`: `mobcoin`, `mobcoins`, `mob_coin`, `mob_coins`
- `GEM`: `gem`, `gems`

---

## 📋 Rank Class

```java
public class Rank {
    String getName();                        // Internal rank name
    String getDisplayName();                 // Display name with color codes
    int getPriority();                       // Priority level
    RankCurrencySettings getCurrencySettings(String currencyKey);
    Map<String, RankCurrencySettings> getAllCurrencySettings();
}

public static class RankCurrencySettings {
    String getCurrencyKey();
    double getTransferTax();                 // Tax rate (0.0 - 1.0)
    int getCooldown();                       // Cooldown in seconds
    double getDailyLimit();                  // Max daily amount (-1 = unlimited)
    int getRequestCooldown();                // Request cooldown in seconds
    double getBossKillBonus();               // Boss kill bonus multiplier
}
```

---

## 💡 Code Examples

### Check if a Player Can Afford Something

```java
public boolean canAfford(Player player, double price) {
    return economyAPI.hasBalance(player.getUniqueId(), CurrencyType.MONEY, price);
}
```

### Deduct Money for a Shop Purchase

```java
public boolean processPurchase(Player player, double price) {
    UUID uuid = player.getUniqueId();

    if (!economyAPI.hasBalance(uuid, CurrencyType.MONEY, price)) {
        player.sendMessage(ChatColor.RED + "You can't afford this!");
        return false;
    }

    boolean success = economyAPI.removeCurrency(uuid, CurrencyType.MONEY, price);
    if (success) {
        player.sendMessage(ChatColor.GREEN + "Purchase successful! -$" + 
            economyAPI.formatCurrency(price, CurrencyType.MONEY));
    }
    return success;
}
```

### Give MobCoins as a Reward

```java
public void rewardMobCoins(Player player, int amount) {
    economyAPI.addCurrency(player.getUniqueId(), CurrencyType.MOBCOIN, amount);
    player.sendMessage(ChatColor.GREEN + "+" + amount + " MobCoins!");
}
```

### Transfer Between Players

```java
public boolean payPlayer(Player sender, Player receiver, double amount) {
    boolean success = economyAPI.transferCurrency(
        sender.getUniqueId(),
        receiver.getUniqueId(),
        CurrencyType.MONEY,
        amount
    );

    if (success) {
        sender.sendMessage(ChatColor.GREEN + "Sent $" + amount + " to " + receiver.getName());
        receiver.sendMessage(ChatColor.GREEN + "Received $" + amount + " from " + sender.getName());
    } else {
        sender.sendMessage(ChatColor.RED + "Transfer failed! Insufficient funds.");
    }
    return success;
}
```

### Get Player's Rank Info

```java
public void showRankInfo(Player player) {
    Rank rank = economyAPI.getPlayerRank(player.getUniqueId());

    if (rank != null) {
        player.sendMessage("Your rank: " + rank.getDisplayName());
        player.sendMessage("Priority: " + rank.getPriority());

        // Show multipliers
        for (Map.Entry<String, RankCurrencySettings> entry : rank.getAllCurrencySettings().entrySet()) {
            String currency = entry.getKey();
            RankCurrencySettings settings = entry.getValue();
            player.sendMessage(currency + " bonus: " + settings.getBossKillBonus() + "x boss kill bonus");
        }
    } else {
        player.sendMessage("No rank found.");
    }
}
```

### Convert Currencies

```java
public void convertMoneyToGems(Player player, double moneyAmount) {
    double rate = economyAPI.getConversionRate(CurrencyType.MONEY, CurrencyType.GEM);
    double expectedGems = moneyAmount * rate;

    boolean success = economyAPI.convertCurrency(
        player.getUniqueId(),
        CurrencyType.MONEY,
        CurrencyType.GEM,
        moneyAmount
    );

    if (success) {
        player.sendMessage("Converted $" + moneyAmount + " to " + expectedGems + " Gems!");
    } else {
        player.sendMessage("Conversion failed! Check your balance.");
    }
}
```

### Format Currency Display

```java
public void showBalance(Player player) {
    double money = economyAPI.getBalance(player.getUniqueId(), CurrencyType.MONEY);
    String formatted = economyAPI.formatCurrency(money, CurrencyType.MONEY);
    String shortForm = economyAPI.formatCurrencyShort(money);

    player.sendMessage("Balance: " + formatted);     // e.g., "$1,234,567.89"
    player.sendMessage("Short: " + shortForm);       // e.g., "1.2M"
}
```

---

## 🔄 Event System

DZEconomy uses Bukkit's event system. You can listen for economy events to trigger custom actions.

### Available Events

> **Note**: Custom event classes are planned for a future update. Currently, use the API methods directly to observe balance changes.

---

## ⚠️ Best Practices

### 1. Always Check for Null

```java
DZEconomy dzeconomy = (DZEconomy) Bukkit.getPluginManager().getPlugin("DZEconomy");
if (dzeconomy == null) {
    // DZEconomy not installed — handle gracefully
    return;
}
DZEconomyAPI api = dzeconomy.getAPI();
```

### 2. Use Soft Dependencies When Possible

```yaml
# plugin.yml
softdepend: [DZEconomy]
```

This allows your plugin to function without DZEconomy, just with reduced features.

### 3. Don't Make Assumptions About Balances

```java
// BAD: Assumes player exists and has a balance
double balance = economyAPI.getBalance(uuid, CurrencyType.MONEY);
if (balance > 0) { ... }

// GOOD: Check for null/zero explicitly
double balance = economyAPI.getBalance(uuid, CurrencyType.MONEY);
// getBalance returns 0.0 for unknown players, which is fine
```

### 4. Use CurrencyType.fromString() for User Input

```java
CurrencyType type = CurrencyType.fromString(userInput);
if (type == null) {
    sender.sendMessage("Unknown currency type!");
    return;
}
```

### 5. Thread Safety

All API methods that modify balances are **thread-safe** and use per-player locks. However:
- Bukkit API calls (e.g., `player.sendMessage()`) must be made on the main thread
- Use `FoliaAdapter.runTask()` or `Bukkit.getScheduler().runTask()` to schedule callbacks

---

## 📌 API Versioning

| API Version | Plugin Version | Changes |
|-------------|----------------|---------|
| 2 | 2.0.0 | Initial public API |
| 3 | 2.1.0 | Server Adapter system, legacy Java support, FoliaAdapter migration |

Check the API version at runtime:

```java
int version = economyAPI.getAPIVersion();
if (version < 2) {
    getLogger().warning("Unsupported DZEconomy API version: " + version);
}
```

---

<p align="center">
  See the <a href="https://github.com/DemonZ-Development/DZEconomy">GitHub repository</a> for the latest API source code.
</p>

---
### 📖 Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***
