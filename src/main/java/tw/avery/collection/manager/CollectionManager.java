package tw.avery.collection.manager;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import tw.avery.collection.CollectionBookPlugin;
import tw.avery.collection.config.ConfigManager;
import tw.avery.collection.data.PlayerData;
import tw.avery.collection.integration.DiscordWebhook;

import java.util.*;

public class CollectionManager {

    private final CollectionBookPlugin plugin;
    private final List<String> allMobNames = new ArrayList<>();
    private final List<String> allItemNames = new ArrayList<>();

    public CollectionManager(CollectionBookPlugin plugin) {
        this.plugin = plugin;
        initData();
    }

    public void initData() {
        allMobNames.clear();
        allItemNames.clear();

        // 掃描生物 (LivingEntities)
        for (EntityType type : EntityType.values()) {
            if (type.isAlive() && type.isSpawnable() && type != EntityType.PLAYER) {
                String name = type.name().toLowerCase();
                if (!isBlacklisted(name)) {
                    allMobNames.add(name);
                }
            }
        }
        Collections.sort(allMobNames);

        // 掃描物品 (Materials)
        ConfigManager config = plugin.getConfigManager();
        for (Material mat : Material.values()) {
            if (mat.isItem() && !mat.isAir()) {
                String name = mat.name().toLowerCase();
                if (!isBlacklisted(name)) {
                    allItemNames.add(name);
                }
            }
        }
        Collections.sort(allItemNames);

        plugin.getLogger().info("圖鑑系統初始化完成！成功載入 " + allMobNames.size() + " 種生物與 " + allItemNames.size() + " 種生存物品。");
    }

    public boolean isBlacklisted(String name) {
        String clean = name.toLowerCase();
        for (String kw : plugin.getConfigManager().getBlacklistKeywords()) {
            if (clean.contains(kw.toLowerCase())) {
                return true;
            }
        }
        // 硬性防呆排除不可在生存模式獲得的技術方塊/特殊方塊
        if (clean.contains("vault") || clean.contains("trial_spawner") || clean.contains("infested") || clean.contains("frogspawn")) {
            return true;
        }
        return false;
    }

    public void unlockMob(Player player, String mobName) {
        String clean = mobName.toLowerCase();
        if (isBlacklisted(clean) || !allMobNames.contains(clean)) return;

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (!data.isMobUnlocked(clean)) {
            int order = plugin.getDataManager().incrementGlobalMobCount(clean);
            data.unlockMob(clean, order);
            plugin.getDataManager().savePlayerDataAsync(data);

            String displayName = tw.avery.collection.util.MinecraftTranslation.getMobChineseName(clean);

            // 訊息與聲音通知
            Component msg = plugin.getLanguageManager().formatComponent("messages.unlock-mob",
                    "name", displayName,
                    "order", order);
            player.sendMessage(msg);

            if (plugin.getConfigManager().isSoundsEnabled()) {
                player.playSound(player.getLocation(), plugin.getConfigManager().getSoundUnlockMob(), 1.0f, 1.8f);
            }

            // 廣播與 Discord 通知
            checkBroadcastAndDiscord(player, clean, displayName, order, "生物");
        }
    }

    public void unlockItem(Player player, String itemName) {
        String clean = itemName.toLowerCase();
        if (isBlacklisted(clean) || !allItemNames.contains(clean)) return;

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (!data.isItemUnlocked(clean)) {
            int order = plugin.getDataManager().incrementGlobalItemCount(clean);
            data.unlockItem(clean, order);
            plugin.getDataManager().savePlayerDataAsync(data);

            String displayName = tw.avery.collection.util.MinecraftTranslation.getItemChineseName(clean);

            // 訊息與聲音通知
            Component msg = plugin.getLanguageManager().formatComponent("messages.unlock-item",
                    "name", displayName,
                    "order", order);
            player.sendMessage(msg);

            if (plugin.getConfigManager().isSoundsEnabled()) {
                player.playSound(player.getLocation(), plugin.getConfigManager().getSoundUnlockItem(), 1.0f, 1.5f);
            }

            // 廣播與 Discord 通知
            checkBroadcastAndDiscord(player, clean, displayName, order, "物品");
        }
    }

    private void checkBroadcastAndDiscord(Player player, String rawName, String displayName, int order, String type) {
        boolean isLegendary = isLegendary(rawName);

        // 1. 遊戲內聊天室傳奇解鎖廣播
        if (isLegendary && plugin.getConfigManager().isBroadcastInGameLegendaryUnlock()) {
            Component bcast = plugin.getLanguageManager().formatComponent("messages.legend-broadcast",
                    "player", player.getName(),
                    "type", type,
                    "name", displayName,
                    "order", order);
            Bukkit.broadcast(bcast);
        }

        // 2. Discord Webhook 傳奇/稀有/全部解鎖廣播
        if (plugin.getConfigManager().isDiscordEnabled()) {
            boolean broadcastAll = plugin.getConfigManager().isDiscordBroadcastAllUnlocks();
            boolean broadcastLegendary = plugin.getConfigManager().isBroadcastDiscordLegendaryUnlock();
            boolean isRare = isRare(rawName);
            if (broadcastAll || ((isLegendary || isRare) && broadcastLegendary)) {
                String webhookUrl = plugin.getConfigManager().getDiscordUnlockWebhookUrl();
                if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
                    plugin.getLogger().warning("[aCollectionBook] Discord 功能已啟用，但解鎖通知 Webhook URL 未填寫 (請檢查 config.yml 中的 webhook-url 或 unlock-webhook-url)！");
                    return;
                }
                var lang = plugin.getLanguageManager();
                String username = lang.getRaw("discord.username");
                String title = lang.formatRaw("discord.unlock-embed.title", "player", player.getName(), "name", displayName, "type", type, "order", order);
                String desc = lang.formatRaw("discord.unlock-embed.description", "player", player.getName(), "name", displayName, "type", type, "order", order);
                int color = lang.getInt("discord.unlock-embed.color", 16755200);
                String fieldName = lang.formatRaw("discord.unlock-embed.field-name", "player", player.getName(), "name", displayName, "type", type, "order", order);
                String fieldValue = lang.formatRaw("discord.unlock-embed.field-value", "player", player.getName(), "name", displayName, "type", type, "order", order);
                String footer = lang.formatRaw("discord.unlock-embed.footer", "player", player.getName(), "name", displayName, "type", type, "order", order);

                DiscordWebhook.sendEmbed(webhookUrl, username, title, desc, color, fieldName, fieldValue, footer);
            }
        }
    }

    public void shareCollection(Player player, String entryName, int order, String type) {
        boolean isMob = type.equals("生物");
        String displayName = isMob ?
                tw.avery.collection.util.MinecraftTranslation.getMobChineseName(entryName) :
                tw.avery.collection.util.MinecraftTranslation.getItemChineseName(entryName);

        Component bcast = plugin.getLanguageManager().formatComponent("messages.share-broadcast",
                "player", player.getName(),
                "type", type,
                "name", displayName,
                "order", order);

        // 1. 遊戲內聊天室玩家主動展示廣播
        if (plugin.getConfigManager().isBroadcastInGamePlayerShare()) {
            Bukkit.broadcast(bcast);
        } else {
            player.sendMessage(bcast);
        }

        if (plugin.getConfigManager().isSoundsEnabled()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), plugin.getConfigManager().getSoundShare(), 1.0f, 1.2f);
            }
        }

        // 2. Discord Webhook 玩家主動展示廣播
        boolean discordEnabled = plugin.getConfigManager().isDiscordEnabled();
        boolean shareEnabled = plugin.getConfigManager().isBroadcastDiscordPlayerShare();
        String webhookUrl = plugin.getConfigManager().getDiscordShareWebhookUrl();

        plugin.getLogger().info("[aCollectionBook] 玩家 " + player.getName() + " 在 GUI 點擊展示：" + type + "【" + displayName + "】");

        if (!discordEnabled) {
            plugin.getLogger().info("[aCollectionBook] -> 跳過 Discord 展示 Webhook (原因: config.yml 中 discord.enabled 為 false)");
        } else if (!shareEnabled) {
            plugin.getLogger().info("[aCollectionBook] -> 跳過 Discord 展示 Webhook (原因: config.yml 中 discord.player-share 為 false)");
        } else if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            plugin.getLogger().warning("[aCollectionBook] -> ⚠️ Discord Webhook 未傳送！原因: config.yml 中未填寫 webhook-url 或 share-webhook-url 網址！");
        } else {
            plugin.getLogger().info("[aCollectionBook] -> 正在發送展示 Webhook 至 Discord 頻道 (" + webhookUrl + ")...");
            var lang = plugin.getLanguageManager();
            String username = lang.getRaw("discord.username");
            String title = lang.formatRaw("discord.share-embed.title", "player", player.getName(), "name", displayName, "type", type, "order", order);
            String desc = lang.formatRaw("discord.share-embed.description", "player", player.getName(), "name", displayName, "type", type, "order", order);
            int color = lang.getInt("discord.share-embed.color", 3447003);
            String fieldName = lang.formatRaw("discord.share-embed.field-name", "player", player.getName(), "name", displayName, "type", type, "order", order);
            String fieldValue = lang.formatRaw("discord.share-embed.field-value", "player", player.getName(), "name", displayName, "type", type, "order", order);
            String footer = lang.formatRaw("discord.share-embed.footer", "player", player.getName(), "name", displayName, "type", type, "order", order);

            DiscordWebhook.sendEmbed(webhookUrl, username, title, desc, color, fieldName, fieldValue, footer);
        }
    }

    public boolean isLegendary(String name) {
        String clean = name.toLowerCase();
        for (String kw : plugin.getConfigManager().getLegendaryItems()) {
            if (clean.contains(kw.toLowerCase())) return true;
        }
        return clean.equals("ender_dragon") || clean.equals("wither") || clean.equals("warden") || clean.equals("elder_guardian");
    }

    public boolean isRare(String name) {
        String clean = name.toLowerCase();
        for (String kw : plugin.getConfigManager().getRareItems()) {
            if (clean.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    public List<String> getAllMobNames() { return allMobNames; }
    public List<String> getAllItemNames() { return allItemNames; }

    public int getTotalCategoryCount() {
        return allMobNames.size() + allItemNames.size();
    }

    public double calculateCompletionPercentage(PlayerData data) {
        int total = getTotalCategoryCount();
        if (total == 0) return 0.0;
        int unlocked = data.getTotalUnlockedCount();
        return Math.min(100.0, Math.round((double) unlocked / total * 1000.0) / 10.0);
    }

    public double calculateGlobalCompletionPercentage() {
        int total = getTotalCategoryCount();
        if (total == 0) return 0.0;
        int globalUnlocked = getGlobalUniqueUnlockedTotal();
        return Math.min(100.0, Math.round((double) globalUnlocked / total * 1000.0) / 10.0);
    }

    public int getGlobalUniqueUnlockedTotal() {
        return plugin.getDataManager().getGlobalUniqueMobsUnlockedCount() + plugin.getDataManager().getGlobalUniqueItemsUnlockedCount();
    }
}
