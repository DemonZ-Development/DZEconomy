![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# Rank System

The DZEconomy rank system, LuckPerms integration, and rank configuration.

---

## Overview

Ranks give players multipliers and perks based on their LuckPerms group. A rank affects how much currency a player earns from mob kills and how their transfers behave.

### Key Features

- **Per-currency multipliers** — Each rank can set different multipliers for Money, MobCoins, and Gems
- **Rank perks** — Reduced cooldowns, increased daily limits, combat tag bypass, and interest
- **LuckPerms integration** — Primary group detection with a 30-second cached lookup
- **Default rank fallback** — Everyone without a matching group uses the `default` rank

---

## LuckPerms Integration

| | |
|---|---|
| **Plugin** | [LuckPerms](https://luckperms.net/) |
| **Required?** | No. Without it, everyone uses the `default` rank |
| **Detection** | Automatic via the LuckPerms service provider |

### How It Works

1. When a player earns currency, DZEconomy asks LuckPerms for their primary group.
2. If that group matches a rank in `ranks.yml`, the rank's multipliers apply.
3. If there is no match, the player falls back to `default`.
4. Group lookups are cached for 30 seconds, then refreshed.

### Setup

1. Install LuckPerms.
2. Create groups that match your ranks.
3. Edit `ranks.yml`. The rank key must match the LuckPerms group name exactly. Case matters.
4. Run `/economy reload`.

---

## Multipliers

Multipliers apply to currency earnings from mob rewards.

| Multiplier | Effect |
|------------|--------|
| `1.0` | Normal. No bonus |
| `1.25` | 25% bonus |
| `1.5` | 50% bonus |
| `2.0` | Double rewards |

Mob reward calculation:

```
base reward × (1 + boss bonus) × mob-rewards.default-multiplier × rank multiplier
```

The rank multiplier is looked up per currency, so a rank can boost Money without touching Gems.

---

## Configuring ranks.yml

### File Structure

```yaml
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
| `display-name` | String | Friendly name with color codes |
| `priority` | Integer | Higher = more important |
| `multipliers` | Section | Per-currency reward multipliers |
| `perks` | Section | Special perks for this rank |
| `permissions` | List | Extra permissions granted while this rank is active |

### Perk Details

| Perk | Type | Default | Description |
|------|------|---------|-------------|
| `reduced-cooldown` | Boolean | `false` | Whether this rank gets shorter transfer cooldowns |
| `cooldown-reduction` | Double | `1.0` | Cooldown multiplier. `0.5` = half the normal cooldown |
| `increased-daily-limit` | Boolean | `false` | Whether this rank gets a higher daily transfer limit |
| `limit-multiplier` | Double | `1.0` | Daily limit multiplier. `2.0` = double the normal limit |
| `bypass-combat-tag` | Boolean | `false` | Bypass combat tag restrictions on economy actions |
| `interest.enabled` | Boolean | `false` | Whether this rank earns interest on their balance |
| `interest.rate` | Double | `0.0` | Interest rate per interval, as a percentage |
| `interest.interval` | Integer | `86400` | How often interest is paid, in seconds |
| `interest.max-balance` | Double | `-1` | Highest balance that earns interest. `-1` = unlimited |

### The Default Rank

The `default` rank is always present and cannot be removed. It applies to every player who does not match another rank.

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
    cooldown-reduction: 0.75
    increased-daily-limit: true
    limit-multiplier: 1.5
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
    cooldown-reduction: 0.5
    increased-daily-limit: true
    limit-multiplier: 2.0
    bypass-combat-tag: true
    interest:
      enabled: true
      rate: 0.1
      interval: 86400
      max-balance: 100000
  permissions:
    - "dzeconomy.premium.chat"
    - "dzeconomy.premium.join-message"
```

### Adding Custom Ranks

Add a new section with the LuckPerms group name as the key:

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
    cooldown-reduction: 0.25
    increased-daily-limit: true
    limit-multiplier: 3.0
    bypass-combat-tag: true
    interest:
      enabled: true
      rate: 0.25
      interval: 43200
      max-balance: 500000
  permissions:
    - "dzeconomy.mythic.chat"
    - "dzeconomy.mythic.join-message"
    - "dzeconomy.mythic.particle"
```

The rank key must match the LuckPerms group name exactly. If your LuckPerms group is `MythicRank`, the key is `MythicRank`, not `mythicrank`.

---

## Rank Resolution

When a player earns currency, DZEconomy resolves their rank:

1. Ask LuckPerms for the player's primary group.
2. Find the matching rank in `ranks.yml`.
3. Apply that rank's multipliers and perks.
4. No match found? Use `default`.

Group changes are picked up within the 30-second cache window. A LuckPerms data recalculate event clears the cache immediately.

---

## Interest System

Ranks can pay interest on held balances.

### How It Works

1. Every `interest.interval` seconds, DZEconomy checks online players.
2. Each player with an interest-enabled rank earns `balance × (rate / 100)`.
3. The interest is capped at `max-balance` when set.
4. The player gets a notification.

### Example Calculation

| Balance | Rate | Interval | Interest Earned |
|---------|------|----------|-----------------|
| $10,000 | 0.1% | 24h | $10.00 |
| $100,000 | 0.1% | 24h | $100.00 |
| $200,000 | 0.1% (max 100K) | 24h | $100.00, capped |

### Tips

- Keep rates between 0.05% and 0.5% to avoid inflation.
- Set `max-balance` so wealthy players cannot farm interest.
- Interest only accrues while the player is online.

---

## PlaceholderAPI Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%dz_rank%` | Player's rank display name |
| `%dz_rank_name%` | Player's rank internal name |

---

<p align="center">
  See <a href="Configuration.md">Configuration</a> for config options and <a href="Permissions.md">Permissions</a> for permission nodes.
</p>

---
### Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***