package tw.avery.collection.config;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import tw.avery.collection.CollectionBookPlugin;

import java.util.List;

public class ConfigManager {

    private final CollectionBookPlugin plugin;
    private String language;
    private boolean broadcastInGameLegendaryUnlock;
    private boolean broadcastInGamePlayerShare;
    private boolean broadcastDiscordLegendaryUnlock;
    private boolean broadcastDiscordPlayerShare;
    private boolean discordBroadcastAllUnlocks;

    private boolean discordEnabled;
    private String discordWebhookUrl;
    private String discordUnlockWebhookUrl;
    private String discordShareWebhookUrl;
    private int discordEmbedColor;

    private boolean soundsEnabled;
    private Sound soundUnlockMob;
    private Sound soundUnlockItem;
    private Sound soundPageChange;
    private Sound soundClick;
    private Sound soundShare;
    private Sound soundRewardClaim;

    private boolean triggerMobSlay;
    private boolean triggerMobBreed;
    private boolean triggerMobRide;
    private boolean triggerMobInteract;

    private boolean triggerItemPickup;
    private boolean triggerItemCraft;
    private boolean triggerItemSmelt;

    private List<String> blacklistKeywords;
    private List<String> legendaryItems;
    private List<String> rareItems;

    private boolean showLockedNames;

    public ConfigManager(CollectionBookPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.language = config.getString("language", "zh_TW");
        this.showLockedNames = config.getBoolean("show-locked-names", true);

        this.broadcastInGameLegendaryUnlock = config.getBoolean("broadcast.in-game.legendary-unlock", true);
        this.broadcastInGamePlayerShare = config.getBoolean("broadcast.in-game.player-share", true);
        this.broadcastDiscordLegendaryUnlock = config.getBoolean("discord.legendary-unlock", config.getBoolean("broadcast.discord.legendary-unlock", true));
        this.broadcastDiscordPlayerShare = config.getBoolean("discord.player-share", config.getBoolean("broadcast.discord.player-share", true));
        this.discordBroadcastAllUnlocks = config.getBoolean("discord.all-unlocks", config.getBoolean("broadcast.discord.all-unlocks", config.getBoolean("discord.broadcast-all-unlocks", false)));

        this.discordEnabled = config.getBoolean("discord.enabled", true);
        this.discordWebhookUrl = config.getString("discord.webhook-url", "");
        this.discordUnlockWebhookUrl = config.getString("discord.unlock-webhook-url", "");
        this.discordShareWebhookUrl = config.getString("discord.share-webhook-url", "");
        this.discordEmbedColor = config.getInt("discord.embed-color", 0xFFAA00);

        this.soundsEnabled = config.getBoolean("sounds.enabled", true);
        this.soundUnlockMob = parseSound(config.getString("sounds.unlock-mob", "ENTITY_PLAYER_LEVELUP"), Sound.ENTITY_PLAYER_LEVELUP);
        this.soundUnlockItem = parseSound(config.getString("sounds.unlock-item", "ENTITY_EXPERIENCE_ORB_PICKUP"), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        this.soundPageChange = parseSound(config.getString("sounds.page-change", "ITEM_BOOK_PAGE_TURN"), Sound.ITEM_BOOK_PAGE_TURN);
        this.soundClick = parseSound(config.getString("sounds.click", "UI_BUTTON_CLICK"), Sound.UI_BUTTON_CLICK);
        this.soundShare = parseSound(config.getString("sounds.share", "BLOCK_NOTE_BLOCK_CHIME"), Sound.BLOCK_NOTE_BLOCK_CHIME);
        this.soundRewardClaim = parseSound(config.getString("sounds.reward-claim", "UI_TOAST_CHALLENGE_COMPLETE"), Sound.UI_TOAST_CHALLENGE_COMPLETE);

        this.triggerMobSlay = config.getBoolean("triggers.mobs.slay", true);
        this.triggerMobBreed = config.getBoolean("triggers.mobs.breed", true);
        this.triggerMobRide = config.getBoolean("triggers.mobs.ride", true);
        this.triggerMobInteract = config.getBoolean("triggers.mobs.interact", true);

        this.triggerItemPickup = config.getBoolean("triggers.items.pickup", true);
        this.triggerItemCraft = config.getBoolean("triggers.items.craft", true);
        this.triggerItemSmelt = config.getBoolean("triggers.items.smelt", true);

        this.blacklistKeywords = config.getStringList("blacklist-keywords");
        this.legendaryItems = config.getStringList("rarity.legendary-items");
        this.rareItems = config.getStringList("rarity.rare-items");
    }

    private Sound parseSound(String soundName, Sound fallback) {
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (Exception e) {
            return fallback;
        }
    }

    public String getLanguage() { return language; }
    public boolean isShowLockedNames() { return showLockedNames; }
    public boolean isBroadcastInGameLegendaryUnlock() { return broadcastInGameLegendaryUnlock; }
    public boolean isBroadcastInGamePlayerShare() { return broadcastInGamePlayerShare; }
    public boolean isBroadcastDiscordLegendaryUnlock() { return broadcastDiscordLegendaryUnlock; }
    public boolean isBroadcastDiscordPlayerShare() { return broadcastDiscordPlayerShare; }
    public boolean isDiscordBroadcastAllUnlocks() { return discordBroadcastAllUnlocks; }

    public boolean isDiscordEnabled() { return discordEnabled; }
    public String getDiscordWebhookUrl() { return discordWebhookUrl; }
    public String getDiscordUnlockWebhookUrl() {
        if (discordUnlockWebhookUrl != null && !discordUnlockWebhookUrl.trim().isEmpty()) {
            return discordUnlockWebhookUrl;
        }
        return discordWebhookUrl;
    }
    public String getDiscordShareWebhookUrl() {
        if (discordShareWebhookUrl != null && !discordShareWebhookUrl.trim().isEmpty()) {
            return discordShareWebhookUrl;
        }
        return discordWebhookUrl;
    }
    public int getDiscordEmbedColor() { return discordEmbedColor; }

    public boolean isSoundsEnabled() { return soundsEnabled; }
    public Sound getSoundUnlockMob() { return soundUnlockMob; }
    public Sound getSoundUnlockItem() { return soundUnlockItem; }
    public Sound getSoundPageChange() { return soundPageChange; }
    public Sound getSoundClick() { return soundClick; }
    public Sound getSoundShare() { return soundShare; }
    public Sound getSoundRewardClaim() { return soundRewardClaim; }

    public boolean isTriggerMobSlay() { return triggerMobSlay; }
    public boolean isTriggerMobBreed() { return triggerMobBreed; }
    public boolean isTriggerMobRide() { return triggerMobRide; }
    public boolean isTriggerMobInteract() { return triggerMobInteract; }

    public boolean isTriggerItemPickup() { return triggerItemPickup; }
    public boolean isTriggerItemCraft() { return triggerItemCraft; }
    public boolean isTriggerItemSmelt() { return triggerItemSmelt; }

    public List<String> getBlacklistKeywords() { return blacklistKeywords; }
    public List<String> getLegendaryItems() { return legendaryItems; }
    public List<String> getRareItems() { return rareItems; }
}
