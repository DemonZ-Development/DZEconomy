![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# Developer API

Integrate other plugins with the DZEconomy API v2.1.2.

---

## Overview

DZEconomy exposes a public API for other plugins:

- Query and modify player balances
- Transfer and convert currencies
- Get player rank information
- Format currency values

Every method that modifies a balance is thread-safe and uses per-player locks.

---

## Getting Started

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
        <version>2.1.2</version>
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
    compileOnly 'com.github.DemonZ-Development:DZEconomy:2.1.2'
}
```

### 2. Declare the Dependency

```yaml
# Hard dependency. Your plugin won't load without DZEconomy.
depend: [DZEconomy]

# Soft dependency. Your plugin loads without it, features degrade.
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
        DZEconomy dzeconomy = (DZEconomy) Bukkit.getPluginManager().getPlugin("DZEconomy");

        if (dzeconomy == null) {
            getLogger().severe("DZEconomy not found. Economy features disabled.");
            return;
        }

        economyAPI = dzeconomy.getAPI();
        getLogger().info("Hooked DZEconomy API v" + economyAPI.getAPIVersion());
    }
}
```

---

## API Methods

### Balance Operations

```java
double getBalance(@NotNull UUID uuid, @NotNull CurrencyType type);
boolean hasBalance(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);
boolean addCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);
boolean removeCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);
boolean setCurrency(@NotNull UUID uuid, @NotNull CurrencyType type, double amount);
```

- `getBalance` returns `0.0` for unknown players.
- `addCurrency` and `removeCurrency` reject negative amounts.
- `removeCurrency` returns `false` when the player does not have enough.

### Transfer and Conversion

```java
boolean transferCurrency(@NotNull UUID from, @NotNull UUID to, @NotNull CurrencyType type, double amount);
boolean convertCurrency(@NotNull UUID uuid, @NotNull CurrencyType from, @NotNull CurrencyType to, double amount);
double getConversionRate(@NotNull CurrencyType from, @NotNull CurrencyType to);
```

- `transferCurrency` is atomic. The sender loses the full amount, the receiver gains the amount minus the transfer tax.
- `convertCurrency` applies the configured conversion rates and fee.
- `getConversionRate` returns `1.0` when both currencies are the same.

### Rank Operations

```java
@Nullable Rank getPlayerRank(@NotNull UUID uuid);
@NotNull List<Rank> getAllRanks();
```

### Formatting

```java
@NotNull String formatCurrency(double amount, @NotNull CurrencyType type);
@NotNull String formatCurrencyShort(double amount);
int getAPIVersion();
```

- `formatCurrency` returns something like `$1,234.56`.
- `formatCurrencyShort` returns compact forms like `1.2K` or `3.4M`.

---

## CurrencyType Enum

```java
package online.demonzdevelopment.dzeconomy.currency;

public enum CurrencyType {
    MONEY("money", "Money", "$", 0.0),
    MOBCOIN("mobcoin", "MobCoin", "★", 0.0),
    GEM("gem", "Gem", "◆", 0.0);

    String getId();
    String getDisplayName();
    String getDefaultSymbol();
    double getDefaultBalance();

    static CurrencyType fromString(String name);
}
```

`fromString` accepts plurals and aliases, and ignores spaces, dashes, and underscores:

- `MONEY`: `money`, `cash`, `dollars`, `balance`
- `MOBCOIN`: `mobcoin`, `mobcoins`
- `GEM`: `gem`, `gems`

It returns `null` for unknown input.

---

## Rank Class

```java
package online.demonzdevelopment.dzeconomy.rank;

public class Rank {
    String getName();
    String getDisplayName();
    int getPriority();
    double getMultiplier(String currencyKey);
    RankCurrencySettings getCurrencySettings(String currencyKey);
    Map<String, RankCurrencySettings> getAllCurrencySettings();
}
```

---

## Code Examples

### Check if a Player Can Afford Something

```java
public boolean canAfford(Player player, double price) {
    return economyAPI.hasBalance(player.getUniqueId(), CurrencyType.MONEY, price);
}
```

### Deduct Money for a Purchase

```java
public boolean processPurchase(Player player, double price) {
    UUID uuid = player.getUniqueId();

    if (!economyAPI.hasBalance(uuid, CurrencyType.MONEY, price)) {
        player.sendMessage(ChatColor.RED + "You can't afford this!");
        return false;
    }

    boolean success = economyAPI.removeCurrency(uuid, CurrencyType.MONEY, price);
    if (success) {
        player.sendMessage(ChatColor.GREEN + "Purchase complete.");
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
    } else {
        sender.sendMessage(ChatColor.RED + "Transfer failed. Check the balance.");
    }
    return success;
}
```

### Get a Player's Rank

```java
public void showRankInfo(Player player) {
    Rank rank = economyAPI.getPlayerRank(player.getUniqueId());
    if (rank != null) {
        player.sendMessage("Your rank: " + rank.getDisplayName());
        player.sendMessage("Money multiplier: " + rank.getMultiplier("money"));
    }
}
```

### Convert Currencies

```java
public void convertMoneyToGems(Player player, double moneyAmount) {
    double rate = economyAPI.getConversionRate(CurrencyType.MONEY, CurrencyType.GEM);
    boolean success = economyAPI.convertCurrency(
        player.getUniqueId(),
        CurrencyType.MONEY,
        CurrencyType.GEM,
        moneyAmount
    );

    if (success) {
        player.sendMessage("Converted $" + moneyAmount + " to " + (moneyAmount * rate) + " Gems!");
    } else {
        player.sendMessage("Conversion failed. Check your balance.");
    }
}
```

### Format Currency

```java
public void showBalance(Player player) {
    double money = economyAPI.getBalance(player.getUniqueId(), CurrencyType.MONEY);
    player.sendMessage("Balance: " + economyAPI.formatCurrency(money, CurrencyType.MONEY));
    player.sendMessage("Short: " + economyAPI.formatCurrencyShort(money));
}
```

---

## Best Practices

### Always Check for Null

```java
DZEconomy dzeconomy = (DZEconomy) Bukkit.getPluginManager().getPlugin("DZEconomy");
if (dzeconomy == null) {
    return;
}
DZEconomyAPI api = dzeconomy.getAPI();
```

### Prefer Soft Dependencies

```yaml
softdepend: [DZEconomy]
```

Your plugin keeps working when DZEconomy is absent, just without economy features.

### Validate User Input with fromString

```java
CurrencyType type = CurrencyType.fromString(userInput);
if (type == null) {
    sender.sendMessage("Unknown currency type!");
    return;
}
```

### Mind the Thread

Balance methods are thread-safe. Bukkit calls like `player.sendMessage()` are not, so schedule them on the right thread. On Folia, use the entity or region schedulers; on regular Bukkit, use the main thread.

---

## API Versioning

| API Version | Plugin Version | Notes |
|-------------|----------------|-------|
| 2 | 2.0.0 | Initial public API |

Check it at runtime if you rely on newer features:

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
### Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***