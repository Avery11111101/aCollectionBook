package tw.avery.collection.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import tw.avery.collection.CollectionBookPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final CollectionBookPlugin plugin;
    private final File dataFolder;
    private final File globalFile;
    private YamlConfiguration globalConfig;

    private final Map<UUID, PlayerData> loadedPlayers = new ConcurrentHashMap<>();
    private final Map<String, Integer> globalMobCountMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> globalItemCountMap = new ConcurrentHashMap<>();

    public DataManager(CollectionBookPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data/players");
        this.globalFile = new File(plugin.getDataFolder(), "data/global.yml");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        loadGlobalData();
    }

    public synchronized void loadGlobalData() {
        if (!globalFile.exists()) {
            try {
                globalFile.getParentFile().mkdirs();
                globalFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("無法建立全域數據檔案 global.yml");
            }
        }
        this.globalConfig = YamlConfiguration.loadConfiguration(globalFile);

        globalMobCountMap.clear();
        if (globalConfig.isConfigurationSection("global_mob_counts")) {
            for (String mob : globalConfig.getConfigurationSection("global_mob_counts").getKeys(false)) {
                globalMobCountMap.put(mob.toLowerCase(), globalConfig.getInt("global_mob_counts." + mob));
            }
        }

        globalItemCountMap.clear();
        if (globalConfig.isConfigurationSection("global_item_counts")) {
            for (String item : globalConfig.getConfigurationSection("global_item_counts").getKeys(false)) {
                globalItemCountMap.put(item.toLowerCase(), globalConfig.getInt("global_item_counts." + item));
            }
        }
    }

    public synchronized void saveGlobalData() {
        if (globalConfig == null) return;

        for (Map.Entry<String, Integer> entry : globalMobCountMap.entrySet()) {
            globalConfig.set("global_mob_counts." + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : globalItemCountMap.entrySet()) {
            globalConfig.set("global_item_counts." + entry.getKey(), entry.getValue());
        }
        try {
            globalConfig.save(globalFile);
        } catch (IOException e) {
            plugin.getLogger().severe("無法儲存全域數據檔案 global.yml: " + e.getMessage());
        }
    }

    public synchronized void reloadAllData() {
        saveAll();
        loadGlobalData();
        for (var p : Bukkit.getOnlinePlayers()) {
            loadedPlayers.put(p.getUniqueId(), loadPlayerData(p.getUniqueId()));
        }
    }

    public int getGlobalUniqueMobsUnlockedCount() {
        int count = 0;
        for (int v : globalMobCountMap.values()) {
            if (v > 0) count++;
        }
        return count;
    }

    public int getGlobalUniqueItemsUnlockedCount() {
        int count = 0;
        for (int v : globalItemCountMap.values()) {
            if (v > 0) count++;
        }
        return count;
    }

    public PlayerData getPlayerData(UUID uuid) {
        return loadedPlayers.computeIfAbsent(uuid, this::loadPlayerData);
    }

    public PlayerData loadPlayerData(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        File playerFile = new File(dataFolder, uuid + ".yml");
        if (!playerFile.exists()) {
            return data;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        if (config.isConfigurationSection("unlocked_mobs")) {
            for (String mob : config.getConfigurationSection("unlocked_mobs").getKeys(false)) {
                int order = config.getInt("unlocked_mobs." + mob);
                data.unlockMob(mob, order);
            }
        }

        if (config.isConfigurationSection("unlocked_items")) {
            for (String item : config.getConfigurationSection("unlocked_items").getKeys(false)) {
                int order = config.getInt("unlocked_items." + item);
                data.unlockItem(item, order);
            }
        }

        List<Integer> rewards = config.getIntegerList("claimed_rewards");
        for (int r : rewards) {
            data.setRewardClaimed(r);
        }

        return data;
    }

    public void savePlayerDataAsync(PlayerData data) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> savePlayerData(data));
    }

    public synchronized void savePlayerData(PlayerData data) {
        File playerFile = new File(dataFolder, data.getUuid() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, Integer> entry : data.getMobOrderMap().entrySet()) {
            config.set("unlocked_mobs." + entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : data.getItemOrderMap().entrySet()) {
            config.set("unlocked_items." + entry.getKey(), entry.getValue());
        }

        config.set("claimed_rewards", new ArrayList<>(data.getClaimedRewards()));

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("無法儲存玩家數據 " + data.getUuid() + ": " + e.getMessage());
        }
    }

    public int getGlobalMobUnlockCount(String mob) {
        return globalMobCountMap.getOrDefault(mob.toLowerCase(), 0);
    }

    public int getGlobalItemUnlockCount(String item) {
        return globalItemCountMap.getOrDefault(item.toLowerCase(), 0);
    }

    public synchronized int incrementGlobalMobCount(String mob) {
        int next = getGlobalMobUnlockCount(mob) + 1;
        globalMobCountMap.put(mob.toLowerCase(), next);
        saveGlobalData();
        return next;
    }

    public synchronized int incrementGlobalItemCount(String item) {
        int next = getGlobalItemUnlockCount(item) + 1;
        globalItemCountMap.put(item.toLowerCase(), next);
        saveGlobalData();
        return next;
    }

    public void unloadPlayer(UUID uuid) {
        PlayerData data = loadedPlayers.remove(uuid);
        if (data != null) {
            savePlayerDataAsync(data);
        }
    }

    public void saveAll() {
        saveGlobalData();
        for (PlayerData data : loadedPlayers.values()) {
            savePlayerData(data);
        }
    }

    public List<PlayerData> getAllPlayerDatas() {
        List<PlayerData> list = new ArrayList<>();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files) {
                try {
                    String uuidStr = f.getName().replace(".yml", "");
                    UUID uuid = UUID.fromString(uuidStr);
                    list.add(getPlayerData(uuid));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }
}
