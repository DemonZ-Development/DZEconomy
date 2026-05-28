package online.demonzdevelopment.dzeconomy.listener;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyManager;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dzeconomy.config.ConfigManager;
import online.demonzdevelopment.dzeconomy.util.MessagesUtil;
import online.demonzdevelopment.dzeconomy.util.ColorUtil;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityDeathListener implements Listener {

    private final DZEconomy plugin;
    private final Map<String, MobRewardData> mobRewardsCache = new ConcurrentHashMap<>();
    
    // Cached config values to avoid reading config files on every single entity death event
    private boolean rewardsEnabled = true;
    private final List<String> worldWhitelist = new ArrayList<>();
    private final List<String> worldBlacklist = new ArrayList<>();
    private boolean allowSpawnerMobs = false;
    private boolean allowSpawnEggMobs = false;
    private boolean requirePlayerKill = true;
    private double defaultMultiplier = 1.0;
    private double bossBonus = 1.0;
    private double notifyThreshold = 1.0;

    public EntityDeathListener(DZEconomy plugin) {
        this.plugin = plugin;
        loadRewards();
    }

    public void reload() {
        loadRewards();
    }

    /**
     * Parse and load all mob rewards from mob-rewards.yml.
     */
    public void loadRewards() {
        mobRewardsCache.clear();
        ConfigManager config = plugin.getConfigManager();
        FileConfiguration mobRewardsConfig = config.getMobRewards();
        
        FileConfiguration mainConfig = config.getConfig();
        rewardsEnabled = mainConfig.getBoolean("mob-rewards.enabled", true);
        
        worldWhitelist.clear();
        List<String> whitelist = mainConfig.getStringList("mob-rewards.world-whitelist");
        if (whitelist != null) {
            for (String w : whitelist) {
                worldWhitelist.add(w.toLowerCase());
            }
        }
        
        worldBlacklist.clear();
        List<String> blacklist = mainConfig.getStringList("mob-rewards.world-blacklist");
        if (blacklist != null) {
            for (String w : blacklist) {
                worldBlacklist.add(w.toLowerCase());
            }
        }
        
        allowSpawnerMobs = mainConfig.getBoolean("mob-rewards.allow-spawner-mobs", false);
        allowSpawnEggMobs = mainConfig.getBoolean("mob-rewards.allow-spawn-egg-mobs", false);
        requirePlayerKill = mainConfig.getBoolean("mobcoin.require-player-kill", true);
        defaultMultiplier = mainConfig.getDouble("mob-rewards.default-multiplier", 1.0);
        bossBonus = mainConfig.getDouble("mob-rewards.boss-bonus", 1.0);
        notifyThreshold = mainConfig.getDouble("mob-rewards.notify-threshold", 1.0);

        if (mobRewardsConfig == null) {
            return;
        }

        for (String category : new String[]{"neutral", "easy", "hard", "boss", "custom"}) {
            ConfigurationSection categorySection = mobRewardsConfig.getConfigurationSection(category);
            if (categorySection == null) {
                continue;
            }
            for (String mobName : categorySection.getKeys(false)) {
                String cacheKey = mobName.toUpperCase();
                if (categorySection.isConfigurationSection(mobName)) {
                    ConfigurationSection mobSection = categorySection.getConfigurationSection(mobName);
                    mobRewardsCache.put(cacheKey, parseMobReward(cacheKey, mobSection));
                } else {
                    Object obj = categorySection.get(mobName);
                    mobRewardsCache.put(cacheKey, parseMobRewardLegacy(cacheKey, obj));
                }
            }
        }
    }

    private MobRewardData parseMobReward(String mobName, ConfigurationSection section) {
        RewardValue mobcoin = parseRewardValue(section, "mobcoin");
        RewardValue money = parseRewardValue(section, "money");
        RewardValue gem = parseRewardValue(section, "gem");
        double chance = section.getDouble("chance", 1.0);
        String message = section.getString("message", "");
        return new MobRewardData(mobName, mobcoin, money, gem, chance, message);
    }

    private MobRewardData parseMobRewardLegacy(String mobName, Object obj) {
        RewardValue mobcoin;
        if (obj instanceof Number) {
            mobcoin = new RewardValue(((Number) obj).doubleValue());
        } else if (obj instanceof String) {
            try {
                mobcoin = new RewardValue(Double.parseDouble((String) obj));
            } catch (NumberFormatException e) {
                mobcoin = new RewardValue(0.0);
            }
        } else if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Object minObj = map.get("min");
            Object maxObj = map.get("max");
            if (minObj instanceof Number && maxObj instanceof Number) {
                mobcoin = new RewardValue(((Number) minObj).doubleValue(), ((Number) maxObj).doubleValue());
            } else if (map.get("amount") instanceof Number) {
                mobcoin = new RewardValue(((Number) map.get("amount")).doubleValue());
            } else {
                mobcoin = new RewardValue(0.0);
            }
        } else {
            mobcoin = new RewardValue(0.0);
        }
        return new MobRewardData(mobName, mobcoin, new RewardValue(0.0), new RewardValue(0.0), 1.0, "");
    }

    private RewardValue parseRewardValue(ConfigurationSection parent, String path) {
        if (!parent.contains(path)) {
            return new RewardValue(0.0);
        }
        if (parent.isConfigurationSection(path)) {
            ConfigurationSection sec = parent.getConfigurationSection(path);
            double min = sec.getDouble("min-amount", 0.0);
            if (min == 0.0) {
                min = sec.getDouble("min", 0.0);
            }
            double max = sec.getDouble("max-amount", 0.0);
            if (max == 0.0) {
                max = sec.getDouble("max", 0.0);
            }
            return new RewardValue(min, max);
        }
        double val = parent.getDouble(path, 0.0);
        return new RewardValue(val);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            event.getEntity().setMetadata("dzeconomy-spawner", new FixedMetadataValue(plugin, true));
        } else if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            event.getEntity().setMetadata("dzeconomy-spawn-egg", new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Check if mob rewards are enabled globally
        if (!rewardsEnabled) {
            return;
        }

        // World whitelist check
        String worldName = entity.getWorld().getName().toLowerCase();
        if (!worldWhitelist.isEmpty() && !worldWhitelist.contains(worldName)) {
            return;
        }

        // World blacklist check
        if (!worldBlacklist.isEmpty() && worldBlacklist.contains(worldName)) {
            return;
        }

        // Spawner mob check
        if (!allowSpawnerMobs && entity.hasMetadata("dzeconomy-spawner")) {
            return;
        }

        // Spawn egg mob check
        if (!allowSpawnEggMobs && entity.hasMetadata("dzeconomy-spawn-egg")) {
            return;
        }

        Player killer = entity.getKiller();
        if (requirePlayerKill && killer == null) {
            return;
        }

        if (!requirePlayerKill && killer == null) {
            return;
        }

        String mobType = entity.getType().name();
        MobRewardData rewardData = mobRewardsCache.get(mobType);
        if (rewardData == null) {
            rewardData = mobRewardsCache.get("DEFAULT");
        }
        if (rewardData == null) {
            return;
        }

        if (Math.random() > rewardData.getChance()) {
            return;
        }

        double mobcoinAmt = rewardData.getMobcoin().getAmount();
        double moneyAmt = rewardData.getMoney().getAmount();
        double gemAmt = rewardData.getGem().getAmount();

        if (mobcoinAmt <= 0 && moneyAmt <= 0 && gemAmt <= 0) {
            return;
        }

        double bonus = getRankBonus(killer, entity);
        
        // Player's rank multiplier
        double rankMultiplierMoney = plugin.getRankManager().getMultiplier(killer.getUniqueId(), CurrencyType.MONEY);
        double rankMultiplierMobcoin = plugin.getRankManager().getMultiplier(killer.getUniqueId(), CurrencyType.MOBCOIN);
        double rankMultiplierGem = plugin.getRankManager().getMultiplier(killer.getUniqueId(), CurrencyType.GEM);

        final double finalMobcoin = Math.round(mobcoinAmt * (1.0 + bonus) * defaultMultiplier * rankMultiplierMobcoin * 100.0) / 100.0;
        final double finalMoney = Math.round(moneyAmt * (1.0 + bonus) * defaultMultiplier * rankMultiplierMoney * 100.0) / 100.0;
        final double finalGem = Math.round(gemAmt * (1.0 + bonus) * defaultMultiplier * rankMultiplierGem * 100.0) / 100.0;
        
        final String customMessage = rewardData.getMessage();

        online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runTaskAsynchronously(plugin, () -> {
            CurrencyManager cm = plugin.getCurrencyManager();
            UUID killerUuid = killer.getUniqueId();

            if (finalMobcoin > 0) {
                if (cm.addBalance(killerUuid, CurrencyType.MOBCOIN, finalMobcoin)) {
                    notifyReward(killer, finalMobcoin, CurrencyType.MOBCOIN, entity, customMessage);
                }
            }

            if (finalMoney > 0) {
                if (cm.addBalance(killerUuid, CurrencyType.MONEY, finalMoney)) {
                    notifyReward(killer, finalMoney, CurrencyType.MONEY, entity, customMessage);
                }
            }

            if (finalGem > 0) {
                if (cm.addBalance(killerUuid, CurrencyType.GEM, finalGem)) {
                    notifyReward(killer, finalGem, CurrencyType.GEM, entity, customMessage);
                }
            }
        });
    }

    private void notifyReward(Player player, double amount, CurrencyType type, LivingEntity entity, String customMessage) {
        if (customMessage != null && !customMessage.isEmpty()) {
            String symbol = plugin.getConfigManager().getConfig().getString("currencies." + type.getId() + ".symbol", type.getDefaultSymbol());
            String msg = ColorUtil.translate(customMessage
                .replace("{amount}", String.format("%,.2f", amount))
                .replace("{currency}", type.getDisplayName())
                .replace("{symbol}", symbol)
                .replace("{mob}", formatMobName(entity.getType().name())));
            online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, player, () -> player.sendMessage(msg));
        } else {
            String messageKey = type.getId() + "-earned";
            if (amount >= notifyThreshold) {
                double newBalance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), type);
                online.demonzdevelopment.dzeconomy.util.FoliaAdapter.runAtEntity(plugin, player, () -> {
                    MessagesUtil.sendMessage(player, messageKey,
                            "%amount%", String.format("%,.2f", amount),
                            "%mob%", formatMobName(entity.getType().name()),
                            "%balance%", String.format("%,.2f", newBalance));
                });
            }
        }
    }

    private double getRankBonus(Player killer, LivingEntity entity) {
        boolean isBoss = false;

        if (entity.getCustomName() != null) {
            isBoss = true;
        }

        String typeName = entity.getType().name();
        switch (typeName) {
            case "ENDER_DRAGON":
            case "WITHER":
            case "ELDER_GUARDIAN":
                isBoss = true;
                break;
            default:
                break;
        }

        if (!isBoss) {
            return 0.0;
        }

        ConfigurationSection ranksSection = plugin.getConfigManager().getRanks().getConfigurationSection("ranks");
        if (ranksSection == null) {
            ranksSection = plugin.getConfigManager().getRanks();
        }
        if (ranksSection != null) {
            for (String rank : ranksSection.getKeys(false)) {
                if (rank.equals("default-rank") || rank.equals("config-version")) continue;
                if (killer.hasPermission("dzeconomy.rank." + rank)) {
                    double rankBonus = plugin.getConfigManager().getConfig().getDouble(
                            "mob-rewards.rank-bonuses." + rank + ".boss-multiplier", bossBonus);
                    return rankBonus;
                }
            }
        }

        return bossBonus;
    }

    private String formatMobName(String name) {
        String[] parts = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(parts[i].substring(0, 1).toUpperCase())
              .append(parts[i].substring(1).toLowerCase());
        }
        return sb.toString();
    }

    public static class RewardValue {
        private final double flatAmount;
        private final double minAmount;
        private final double maxAmount;
        private final boolean isRange;

        public RewardValue(double flatAmount) {
            this.flatAmount = flatAmount;
            this.minAmount = 0;
            this.maxAmount = 0;
            this.isRange = false;
        }

        public RewardValue(double minAmount, double maxAmount) {
            this.flatAmount = 0;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.isRange = true;
        }

        public double getAmount() {
            if (isRange) {
                return minAmount + (Math.random() * (maxAmount - minAmount));
            }
            return flatAmount;
        }

        public boolean hasReward() {
            return isRange ? (maxAmount > 0) : (flatAmount > 0);
        }
    }

    public static class MobRewardData {
        private final String mobType;
        private final RewardValue mobcoin;
        private final RewardValue money;
        private final RewardValue gem;
        private final double chance;
        private final String message;

        public MobRewardData(String mobType, RewardValue mobcoin, RewardValue money, RewardValue gem, double chance, String message) {
            this.mobType = mobType;
            this.mobcoin = mobcoin;
            this.money = money;
            this.gem = gem;
            this.chance = chance;
            this.message = message;
        }

        public String getMobType() { return mobType; }
        public RewardValue getMobcoin() { return mobcoin; }
        public RewardValue getMoney() { return money; }
        public RewardValue getGem() { return gem; }
        public double getChance() { return chance; }
        public String getMessage() { return message; }
    }
}
