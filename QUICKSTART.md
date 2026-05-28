# Quick Start Guide - DZEconomy

Welcome to DZEconomy! This guide will help you get started quickly.

---

## 📦 Step 1: Installation

1. **Download** the latest DZEconomy JAR file
2. **Stop** your server
3. **Install dependencies**:
   - Download [LuckPerms](https://luckperms.net/) (Optional but recommended)
   - Download [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) (Optional but recommended)
4. **Place** all JARs in your `plugins` folder
5. **Start** your server

---

## ⚙️ Step 2: Basic Configuration

After first startup, configure these files in `plugins/DZEconomy/`:

### **config.yml**
Set your preferred storage type:
```yaml
storage:
  type: FLATFILE  # or SQLITE or MYSQL
```

### **ranks.yml**
Create your custom ranks to match your LuckPerms groups:
```yaml
ranks:
  member:
    display-name: "&7Member"
    priority: 0
    money:
      transfer-tax: 5.0
      transfer-cooldown: 300
      daily-transfer-limit: 5
    # ... configure other currencies
  
  vip:
    display-name: "&6VIP"
    priority: 10
    money:
      transfer-tax: 2.0
      transfer-cooldown: 150
      daily-transfer-limit: 10
    # ... configure other currencies
```

**Important:** Make sure rank names in `ranks.yml` match your LuckPerms group names!

---

## 🎮 Step 3: Test Basic Commands

Try these commands in-game:

```
/money balance           - Check your balance
/money send Steve 1000   - Send $1000 to Steve
/mobcoin balance         - Check MobCoins
/gem balance            - Check Gems
/economy convert Steve money gem 10000  - Convert currencies for a player
```

---

## 🔑 Step 4: Set Up Permissions

### **For Regular Players:**
Grant default permission: `dzeconomy.default`

### **For VIP/Donor Ranks:**
Grant all currency permissions:
```
dzeconomy.money.*
dzeconomy.mobcoin.*
dzeconomy.gem.*
dzeconomy.economy.convert
```

### **For Staff:**
Grant admin permissions:
```
dzeconomy.admin.*
```

Example LuckPerms commands:
```
/lp group default permission set dzeconomy.default true
/lp group default permission set dzeconomy.money.* true
/lp group admin permission set dzeconomy.admin.* true
```

---

## 🎯 Step 5: Customize Your Economy

### **Adjust Starting Balances**
In `config.yml`:
```yaml
currencies:
  money:
    starting-balance: 50000.00  # New players get $50,000
  mobcoin:
    starting-balance: 500.00    # New players get 500 MC
  gem:
    starting-balance: 5.00      # New players get 5 Gems
```

### **Configure Exchange Rates**
In `config.yml`:
```yaml
conversion:
  rates:
    gem-to-mobcoin: 100.0      # 1 Gem = 100 MobCoins
    gem-to-money: 10000.0      # 1 Gem = $10,000
    mobcoin-to-money: 100.0    # 1 MobCoin = $100
```

### **Enable/Disable PVP Transfers**
In `config.yml`:
```yaml
pvp-economy:
  enabled: true
  transfer-money: true
  transfer-mobcoins: true
  transfer-gems: true
```

---

## 💡 Step 6: Optional Enhancements

### **PlaceholderAPI Setup**
If you installed PlaceholderAPI, use these placeholders in other plugins:

- `%dz_money%` - Player's money (short form)
- `%dz_money_formatted%` - With $ symbol
- `%dz_mobcoin%` - Player's MobCoins
- `%dz_gem%` - Player's Gems
- `%dz_rank%` - Player's rank name

### **Customize Messages**
Edit `messages.yml` to change all messages, colors, and formats.

### **Adjust Mob Rewards**
Edit `mob-rewards.yml` to change which mobs give rewards and how much.

---

## 🔄 Step 7: Reload Configuration

After making changes, reload the plugin:
```
/economy reload
```

---

## 📊 Common Configurations

### **High-Economy Server (Inflated)**
```yaml
currencies:
  money:
    starting-balance: 1000000.00
conversion:
  rates:
    gem-to-money: 100000.0
    mobcoin-to-money: 1000.0
```

### **Low-Economy Server (Deflated)**
```yaml
currencies:
  money:
    starting-balance: 1000.00
conversion:
  rates:
    gem-to-money: 1000.0
    mobcoin-to-money: 10.0
```

### **Competitive Server (Harsh)**
```yaml
ranks:
  default:
    money:
      transfer-tax: 10.0
      daily-transfer-limit: 3
pvp-economy:
  enabled: true  # Lose everything on death
```

### **Casual Server (Friendly)**
```yaml
ranks:
  default:
    money:
      transfer-tax: 1.0
      daily-transfer-limit: 50
pvp-economy:
  enabled: false  # Keep currency on death
```

---

## 🛠️ Advanced: MySQL Setup

For large servers or networks, use MySQL:

1. **Create Database:**
```sql
CREATE DATABASE dzeconomy;
CREATE USER 'dzeconomy'@'localhost' IDENTIFIED BY 'yourpassword';
GRANT ALL PRIVILEGES ON dzeconomy.* TO 'dzeconomy'@'localhost';
FLUSH PRIVILEGES;
```

2. **Configure in config.yml:**
```yaml
storage:
  type: MYSQL
  mysql:
    host: "localhost"
    port: 3306
    database: "dzeconomy"
    username: "dzeconomy"
    password: "yourpassword"
```

3. **Restart server** - Tables will be created automatically

---

## ❓ Troubleshooting

### **Players can't send currency**
- Check they have the `dzeconomy.money.send` permission
- Check they haven't hit their daily limit
- Check they're not on cooldown
- Check they have enough balance + tax

### **Ranks not working**
- Ensure rank names in `ranks.yml` match LuckPerms groups exactly (case-sensitive)
- Run `/economy reload` after changing ranks.yml
- Check LuckPerms with `/lp user <player> info`

### **Mob rewards not working**
- Check `mob-rewards.yml` has the mob listed
- Ensure player killed the mob (not environment)
- Check if mob category is enabled

### **Request GUI not opening**
- Player might have another inventory open
- Check `gui.request.enabled` is `true` in config.yml
- Check `gui.request.respect-open-inventories` setting

---

## 📚 Need More Help?

- Read the full **README.md** for detailed documentation
- Check **API_EXAMPLE.java** if you're a developer
- Contact: DemonZ Development
- Website: [demonzdevelopment.online](https://demonzdevelopment.online)
- GitHub: [github.com/DemonZDevelopment/DZEconomy](https://github.com/DemonZDevelopment/DZEconomy)

---

**Enjoy DZEconomy! 🚀**