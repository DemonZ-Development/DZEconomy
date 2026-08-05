![DZEconomy Banner](https://raw.githubusercontent.com/DemonZ-Development/DZEconomy/main/assets/bannerv2-release.png)

# Permissions Reference

Complete permission node reference for DZEconomy v2.1.2.

---

## Permission Defaults

| Default | Meaning |
|---------|---------|
| `true` | Everyone has this permission |
| `op` | Only operators |
| `false` | Nobody by default |

---

## Money Permissions

| Node | Description | Default |
|------|-------------|---------|
| `dzeconomy.money.balance` | Check your own Money balance | `true` |
| `dzeconomy.money.balance.others` | Check other players' Money balance | `op` |
| `dzeconomy.money.send` | Send Money to other players | `true` |
| `dzeconomy.money.request` | Request Money from other players | `true` |
| `dzeconomy.money.accept` | Accept Money requests | `true` |
| `dzeconomy.money.deny` | Deny Money requests | `true` |
| `dzeconomy.money.top` | View the Money leaderboard | `true` |
| `dzeconomy.money.add` | Add Money to a player's balance | `op` |
| `dzeconomy.money.remove` | Remove Money from a player's balance | `op` |
| `dzeconomy.money.set` | Set a player's Money balance | `op` |

---

## MobCoin Permissions

| Node | Description | Default |
|------|-------------|---------|
| `dzeconomy.mobcoin.balance` | Check your own MobCoin balance | `true` |
| `dzeconomy.mobcoin.balance.others` | Check other players' MobCoin balance | `op` |
| `dzeconomy.mobcoin.send` | Send MobCoins to other players | `true` |
| `dzeconomy.mobcoin.request` | Request MobCoins from other players | `true` |
| `dzeconomy.mobcoin.accept` | Accept MobCoin requests | `true` |
| `dzeconomy.mobcoin.deny` | Deny MobCoin requests | `true` |
| `dzeconomy.mobcoin.top` | View the MobCoin leaderboard | `true` |
| `dzeconomy.mobcoin.add` | Add MobCoins to a player's balance | `op` |
| `dzeconomy.mobcoin.remove` | Remove MobCoins from a player's balance | `op` |
| `dzeconomy.mobcoin.set` | Set a player's MobCoin balance | `op` |

---

## Gem Permissions

| Node | Description | Default |
|------|-------------|---------|
| `dzeconomy.gem.balance` | Check your own Gem balance | `true` |
| `dzeconomy.gem.balance.others` | Check other players' Gem balance | `op` |
| `dzeconomy.gem.send` | Send Gems to other players | `true` |
| `dzeconomy.gem.request` | Request Gems from other players | `true` |
| `dzeconomy.gem.accept` | Accept Gem requests | `true` |
| `dzeconomy.gem.deny` | Deny Gem requests | `true` |
| `dzeconomy.gem.top` | View the Gem leaderboard | `true` |
| `dzeconomy.gem.add` | Add Gems to a player's balance | `op` |
| `dzeconomy.gem.remove` | Remove Gems from a player's balance | `op` |
| `dzeconomy.gem.set` | Set a player's Gem balance | `op` |

---

## Economy Admin Permissions

| Node | Description | Default |
|------|-------------|---------|
| `dzeconomy.economy.info` | View plugin info (`/economy info`) | `true` |
| `dzeconomy.admin` | Access all admin commands. Parent node | `op` |
| `dzeconomy.admin.reload` | Reload plugin configuration | `op` |
| `dzeconomy.admin.convert` | Convert currencies between players | `op` |
| `dzeconomy.admin.migrate` | Migrate storage backends | `op` |
| `dzeconomy.admin.status` | View plugin status | `op` |
| `dzeconomy.admin.baltop` | View the global balance leaderboard | `op` |
| `dzeconomy.admin.payall` | Pay all online players | `op` |
| `dzeconomy.admin.update` | Receive update notifications | `op` |
| `dzeconomy.admin.backup` | Create a data backup | `op` |

---

## Rank and Mob Reward Permissions

| Node | Description | Default |
|------|-------------|---------|
| `dzeconomy.mobreward.multiplier` | Receive the mob reward multiplier | `true` |
| `dzeconomy.request.autoaccept` | Auto-accept incoming payment requests | `false` |

---

## The `dzeconomy.admin` Parent Node

Granting `dzeconomy.admin` unlocks every admin sub-permission:

| Child | Granted |
|-------|---------|
| `dzeconomy.admin.reload` | Yes |
| `dzeconomy.admin.convert` | Yes |
| `dzeconomy.admin.migrate` | Yes |
| `dzeconomy.admin.status` | Yes |
| `dzeconomy.admin.baltop` | Yes |
| `dzeconomy.admin.payall` | Yes |
| `dzeconomy.admin.update` | Yes |
| `dzeconomy.admin.backup` | Yes |

---

## LuckPerms Setup Examples

**Give a player all admin permissions**

```
/lp user Steve permission set dzeconomy.admin true
```

**Let a group check other players' balances**

```
/lp group moderator permission set dzeconomy.money.balance.others true
/lp group moderator permission set dzeconomy.mobcoin.balance.others true
/lp group moderator permission set dzeconomy.gem.balance.others true
```

**Let a group add, remove, and set currency**

```
/lp group admin permission set dzeconomy.money.add true
/lp group admin permission set dzeconomy.money.remove true
/lp group admin permission set dzeconomy.money.set true
/lp group admin permission set dzeconomy.mobcoin.add true
/lp group admin permission set dzeconomy.mobcoin.remove true
/lp group admin permission set dzeconomy.mobcoin.set true
/lp group admin permission set dzeconomy.gem.add true
/lp group admin permission set dzeconomy.gem.remove true
/lp group admin permission set dzeconomy.gem.set true
```

**Let a group receive the mob reward multiplier**

```
/lp group vip permission set dzeconomy.mobreward.multiplier true
```

Bypassing combat tag restrictions is handled through the `bypass-combat-tag` perk in `ranks.yml`, not a permission node.

---

## Full Permission Summary

| Node | Description | Default |
|------|-------------|---------|
| `dzeconomy.money.balance` | Check own Money balance | `true` |
| `dzeconomy.money.balance.others` | Check others' Money balance | `op` |
| `dzeconomy.money.send` | Send Money | `true` |
| `dzeconomy.money.request` | Request Money | `true` |
| `dzeconomy.money.accept` | Accept Money requests | `true` |
| `dzeconomy.money.deny` | Deny Money requests | `true` |
| `dzeconomy.money.top` | View Money leaderboard | `true` |
| `dzeconomy.money.add` | Add Money (admin) | `op` |
| `dzeconomy.money.remove` | Remove Money (admin) | `op` |
| `dzeconomy.money.set` | Set Money balance (admin) | `op` |
| `dzeconomy.mobcoin.balance` | Check own MobCoin balance | `true` |
| `dzeconomy.mobcoin.balance.others` | Check others' MobCoin balance | `op` |
| `dzeconomy.mobcoin.send` | Send MobCoins | `true` |
| `dzeconomy.mobcoin.request` | Request MobCoins | `true` |
| `dzeconomy.mobcoin.accept` | Accept MobCoin requests | `true` |
| `dzeconomy.mobcoin.deny` | Deny MobCoin requests | `true` |
| `dzeconomy.mobcoin.top` | View MobCoin leaderboard | `true` |
| `dzeconomy.mobcoin.add` | Add MobCoins (admin) | `op` |
| `dzeconomy.mobcoin.remove` | Remove MobCoins (admin) | `op` |
| `dzeconomy.mobcoin.set` | Set MobCoin balance (admin) | `op` |
| `dzeconomy.gem.balance` | Check own Gem balance | `true` |
| `dzeconomy.gem.balance.others` | Check others' Gem balance | `op` |
| `dzeconomy.gem.send` | Send Gems | `true` |
| `dzeconomy.gem.request` | Request Gems | `true` |
| `dzeconomy.gem.accept` | Accept Gem requests | `true` |
| `dzeconomy.gem.deny` | Deny Gem requests | `true` |
| `dzeconomy.gem.top` | View Gem leaderboard | `true` |
| `dzeconomy.gem.add` | Add Gems (admin) | `op` |
| `dzeconomy.gem.remove` | Remove Gems (admin) | `op` |
| `dzeconomy.gem.set` | Set Gem balance (admin) | `op` |
| `dzeconomy.economy.info` | View plugin info | `true` |
| `dzeconomy.admin` | All admin permissions (parent) | `op` |
| `dzeconomy.admin.reload` | Reload configuration | `op` |
| `dzeconomy.admin.convert` | Convert currencies | `op` |
| `dzeconomy.admin.migrate` | Migrate storage | `op` |
| `dzeconomy.admin.status` | View plugin status | `op` |
| `dzeconomy.admin.baltop` | Global leaderboard | `op` |
| `dzeconomy.admin.payall` | Pay all online players | `op` |
| `dzeconomy.admin.update` | Update notifications | `op` |
| `dzeconomy.admin.backup` | Create backup | `op` |
| `dzeconomy.mobreward.multiplier` | Mob reward multiplier | `true` |
| `dzeconomy.request.autoaccept` | Auto-accept requests | `false` |

---

<p align="center">
  See <a href="Commands.md">Commands</a> for command usage and <a href="Ranks.md">Ranks</a> for rank perks.
</p>

---
### Quick Links
[**DZEconomy GitHub**](https://github.com/DemonZ-Development/DZEconomy) • [**Discord Support**](https://discord.com/invite/GYsTt96ypf) • [**Wiki Home**](https://github.com/DemonZ-Development/DZEconomy/wiki/Home)

*Developed by **[DemonZ Development](https://github.com/DemonZ-Development)***