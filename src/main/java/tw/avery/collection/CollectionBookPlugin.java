package tw.avery.collection;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import tw.avery.collection.command.CollectionAdminCommand;
import tw.avery.collection.command.CollectionCommand;
import tw.avery.collection.config.ConfigManager;
import tw.avery.collection.config.LanguageManager;
import tw.avery.collection.data.DataManager;
import tw.avery.collection.listener.GUIListener;
import tw.avery.collection.listener.ItemUnlockListener;
import tw.avery.collection.listener.MobUnlockListener;
import tw.avery.collection.manager.CollectionManager;
import tw.avery.collection.manager.RewardManager;

public class CollectionBookPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private LanguageManager languageManager;
    private DataManager dataManager;
    private CollectionManager collectionManager;
    private RewardManager rewardManager;

    @Override
    public void onEnable() {
        getLogger().info("==========================================");
        getLogger().info(" 全能圖鑑系統 aCollectionBook v" + getDescription().getVersion());
        getLogger().info(" 正在加載系統資源、多語言設定與資料庫...");

        // 初始化 Config 與 語言
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        tw.avery.collection.util.MinecraftTranslation.load(this);

        // 初始化數據庫與圖鑑管理器
        this.dataManager = new DataManager(this);
        this.collectionManager = new CollectionManager(this);
        this.rewardManager = new RewardManager(this);

        // 註冊事件監聽器
        Bukkit.getPluginManager().registerEvents(new MobUnlockListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemUnlockListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);

        // 註冊指令
        CollectionCommand collCmd = new CollectionCommand(this);
        if (getCommand("collection") != null) {
            getCommand("collection").setExecutor(collCmd);
            getCommand("collection").setTabCompleter(collCmd);
        }
        if (getCommand("colltop") != null) {
            getCommand("colltop").setExecutor(collCmd);
            getCommand("colltop").setTabCompleter(collCmd);
        }

        CollectionAdminCommand adminCmd = new CollectionAdminCommand(this);
        if (getCommand("colladmin") != null) {
            getCommand("colladmin").setExecutor(adminCmd);
            getCommand("colladmin").setTabCompleter(adminCmd);
        }

        getLogger().info(" 全能圖鑑系統 CollectionBook 已成功啟動！");
        getLogger().info("==========================================");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getLogger().info("全能圖鑑系統 CollectionBook 已安全卸載並儲存數據。");
    }

    public void reloadAll() {
        configManager.reload();
        languageManager.loadLanguage();
        tw.avery.collection.util.MinecraftTranslation.load(this);
        dataManager.reloadAllData();
        collectionManager.initData();

        for (var player : Bukkit.getOnlinePlayers()) {
            for (var item : player.getInventory().getContents()) {
                if (item != null && !item.getType().isAir()) {
                    collectionManager.unlockItem(player, item.getType().name().toLowerCase());
                }
            }
        }
    }

    public ConfigManager getConfigManager() { return configManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public DataManager getDataManager() { return dataManager; }
    public CollectionManager getCollectionManager() { return collectionManager; }
    public RewardManager getRewardManager() { return rewardManager; }
}
