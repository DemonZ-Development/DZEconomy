# 🏆 Rank System

Complete guide to the DZEconomy rank system, LuckPerms integration, and rank configuration.

---

## 📖 Overview

DZEconomy's rank system provides **multipliers and perks** based on a player's LuckPerms group. Ranks affect how much currency players earn from mob kills, PVP loot, payall, and admin additions.

### Key Features

- 📈 **Per-currency multipliers** — Each rank can have different multipliers for Money, MobCoins, and Gems
- 🎁 **Rank perks** — Reduced cooldowns, increased daily limits, combat tag bypass, and interest
- 🔄 **Multiplier stacking** — Choose how multiple multipliers combine (multiply, add, or highest)
- 🏷️ **LuckPerms integration** — Automatic group detection with cached lookups
- ⚡ **Priority system** — Highest-priority rank wins when a player has multiple groups

---

## 🔗 LuckPerms Integration

### Requirements

| | |
|---|---|
| **Plugin** | [LuckPerms](https://luckperms.net/) |
| **Required?** | No (optional, but recommended for ranks) |
| **Soft-depend** | DZEconomy automatically detects LuckPerms |

### How It Works

1. When a player performs an economy action, DZEconomy checks their **LuckPerms groups**
2. If a group matches a rank defined in `ranks.yml`, the rank's **multipliers** are applied
3. If a player has multiple groups, the rank with the **highest priority** is used
4. The way multipliers stack is controlled by `config.yml` → `ranks.multiplier-stacking`

### Without LuckPerms

If LuckPerms is not installed:
- All players use the **`default`** rank from `ranks.yml`
- Multipliers and perks from the default rank still apply
- No group-based rank detection

### Setup Steps

1. **Install LuckPerms** on your server
2. **Create groups** in LuckPerms that match your desired ranks
3. **Configure `ranks.yml`** — the rank key must match the LuckPerms group name **exactly** (case-sensitive)
4. **Reload** DZEconomy: `/economy reload`

---

## 📊 Multipliers

### How Multipliers Work

Multipliers are applied to currency earnings from:
- Mob rewards
- PVP loot
- Payall distributions
- Admin additions (`/money add`)

**Examples:**

| Multiplier | Effect |
|------------|--------|
| `1.0` | Normal (100%) — no bonus |
| `1.25` | 25% bonus (VIP) |
| `1.5` | 50% bonus (Premium) |
| `2.0` | Double (2x) rewards |

### Multiplier Stacking

Controlled by `config.yml` → `ranks.multiplier-stacking`:

| Mode | Formula | Example |
|------|---------|---------|
| `MULTIPLY` | All multipliers multiply together | 1.5 × 1.2 = **1.8x** |
| `ADD` | All multipliers add together | 0.5 + 0.2 + 1.0 = **1.7x** |
| `HIGHEST` | Only the highest multiplier is used | max(1.5, 1.2) = **1.5x** |

### Stacking Order

When a player earns currency, multipliers are applied in this order:

```
Base Reward × Rank Multiplier × Global Multiplier
```

For mob rewards specifically:
```
Base Reward × Rank Multiplier × Global Multiplier × Kill Streak Bonus × Event Bonus
```

---

## ⚙️ Configuring ranks.yml

### File Structure

```yaml
# The rank key MUST match the LuckPerms group name EXACTLY (case-sensitive)
rank_name:
  display-name: "&aDisplay Name"
  priority: 1
  multipliers:
    money: 1.0
    mobcoin: 1.0
    gem: 1.0
  perks:
    reduced-cooldown: false
    cooldown-reduction: 1.0
    increased-daily-limit: false
    limit-multiplier: 1.0
    bypass-combat-tag: false
    interest:
      enabled: false
      rate: 0.0
      interval: 86400
      max-balance: -1
  permissions: []
```

### Rank Properties

| Property | Type | Description |
|----------|------|-------------|
| `display-name` | String | Friendly name with color codes, shown in messages & GUI |
| `priority` | Integer | Higher = more important. Used when a player has multiple groups. |
| `multipliers` | Section | Per-currency reward multipliers |
| `perks` | Section | Special perks for this rank |
| `permissions` | List | Additional permissions granted while this rank is active |

### Perk Details

| Perk | Type | Default | Description |
|------|------|---------|-------------|
| `reduced-cooldown` | Boolean | `false` | Whether this rank has reduced transfer cooldowns |
| `cooldown-reduction` | Double | `1.0` | Cooldown multiplier (0.5 = half the normal cooldown) |
| `increased-daily-limit` | Boolean | `false` | Whether this rank has increased daily limits |
| `limit-multiplier` | Double | `1.0` | Daily limit multiplier (2.0 = double the normal limit) |
| `bypass-combat-tag` | Boolean | `false` | Bypass combat tag economy restrictions |
| `interest.enabled` | Boolean | `false` | Whether this rank earns interest on their balance |
| `interest.rate` | Double | `0.0` | Interest rate per interval (percentage) |
| `interest.interval` | Integer | `86400` | How often interest is paid (in seconds) |
| `interest.max-balance` | Double | `-1` | Maximum balance that earns interest (-1 = unlimited) |

### Default Rank (Built-In)

The `default` rank is **always present** and cannot be removed. It applies to all players who don't match any other rank.

```yaml
default:
  display-name: "&7Default"
  priority: 0
  multipliers:
    money: 1.0
    mobcoin: 1.0
    gem: 1.0
  perks:
    reduced-cooldown: false
    cooldown-reduction: 1.0
    increased-daily-limit: false
    limit-multiplier: 1.0
    bypass-combat-tag: false
    interest:
      enabled: false
      rate: 0.0
      interval: 86400
      max-balance: -1
  permissions: []
```

### Example: VIP Rank

```yaml
vip:
  display-name: "&aVIP"
  priority: 1
  multipliers:
    money: 1.25
    mobcoin: 1.5
    gem: 1.0
  perks:
    reduced-cooldown: true
    cooldown-reduction: 0.75     # 25% shorter cooldowns
    increased-daily-limit: true
    limit-multiplier: 1.5         # 50% higher daily limit
    bypass-combat-tag: false
    interest:
      enabled: false
      rate: 0.0
      interval: 86400
      max-balance: -1
  permissions:
    - "dzeconomy.vip.chat"
```

### Example: Premium Rank

```yaml
premium:
  display-name: "&6Premium"
  priority: 2
  multipliers:
    money: 1.5
    mobcoin: 2.0
    gem: 1.5
  perks:
    reduced-cooldown: true
    cooldown-reduction: 0.5      # 50% shorter cooldowns
    increased-daily-limit: true
    limit-multiplier: 2.0         # Double daily limit
    bypass-combat-tag: true       # Can trade while in combat
    interest:
      enabled: true
      rate: 0.1                   # 0.1% interest per day
      interval: 86400             # Every 24 hours
      max-balance: 100000         # Interest on first 100,000 only
  permissions:
    - "dzeconomy.premium.chat"
    - "dzeconomy.premium.join-message"
```

### Adding Custom Ranks

To add a new rank, simply add a new section with the **LuckPerms group name** as the key:

```yaml
mythic:
  display-name: "&d&lMythic"
  priority: 3
  multipliers:
    money: 2.0
    mobcoin: 2.5
    gem: 2.0
  perks:
    reduced-cooldown: true
    cooldown-reduction: 0.25     # 75% shorter cooldowns
    increased-daily-limit: true
    limit-multiplier: 3.0         # Triple daily limit
    bypass-combat-tag: true
    interest:
      enabled: true
      rate: 0.25                  # 0.25% interest
      interval: 43200             # Every 12 hours
      max-balance: 500000
  permissions:
    - "dzeconomy.mythic.chat"
    - "dzeconomy.mythic.join-message"
    - "dzeconomy.mythic.particle"
```

> ⚠️ **Important**: The rank key **MUST** match the LuckPerms group name **EXACTLY** (case-sensitive). For example, if your LuckPerms group is `MythicRank`, the key must be `MythicRank`, not `mythicrank` or `MYTHICRANK`.

---

## 🔄 Rank Resolution

When a player has **multiple LuckPerms groups**, DZEconomy resolves their rank as follows:

1. Get all of the player's LuckPerms groups
2. Find matching ranks in `ranks.yml`
3. Select the rank with the **highest priority**
4. Apply that rank's multipliers and perks

### Example

If a player is in both `vip` (priority 1) and `premium` (priority 2):
- The `premium` rank is used (priority 2 > priority 1)
- Premium multipliers and perks apply
- VIP multipliers are **not** applied (unless using `MULTIPLY` stacking with global multipliers)

---

## 💰 Interest System

The interest system rewards players for holding balances. It's configured per-rank.

### How Interest Works

1. Every `interest.interval` seconds, DZEconomy checks all online players
2. For each player with an interest-enabled rank:
   - Calculate interest: `balance × (rate / 100)`
   - Cap at `max-balance` if set
   - Add the interest to the player's balance
   - Send a notification

### Example Calculation

| Balance | Rate | Interval | Interest Earned |
|---------|------|----------|-----------------|
| $10,000 | 0.1% | 24h | $10.00 |
| $100,000 | 0.1% | 24h | $100.00 |
| $200,000 | 0.1% (max: 100K) | 24h | $100.00 (capped) |

### Interest Tips

- Use low rates (0.05% – 0.5%) to prevent inflation
- Set `max-balance` to prevent wealthy players from earning excessive interest
- Shorter intervals = more frequent but smaller payments
- Interest is only earned while the player is **online**

---

## 🏷️ PlaceholderAPI Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%dz_rank%` | Player's rank display name (e.g., "&aVIP") |
| `%dz_rank_name%` | Player's rank internal name (e.g., "vip") |

---

<p align="center">
  See <a href="Configuration.md">Configuration</a> for rank-related config options and <a href="Permissions.md">Permissions</a> for permission nodes.
</p>
