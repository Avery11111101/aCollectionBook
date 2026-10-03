package tw.avery.collection.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import tw.avery.collection.CollectionBookPlugin;
import tw.avery.collection.data.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardMenuHolder implements InventoryHolder {

    private final CollectionBookPlugin plugin;
    private final Inventory inventory;

    private final java.util.Map<Integer, OfflinePlayer> slotPlayerMap = new java.util.HashMap<>();

    public LeaderboardMenuHolder(CollectionBookPlugin plugin) {
        this.plugin = plugin;
        Component title = plugin.getLanguageManager().getComponent("gui.leaderboard.title");
        this.inventory = Bukkit.createInventory(this, 36, title);
        buildMenu();
    }

    private void buildMenu() {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 36; i++) inventory.setItem(i, glass);

        // 全服探索進度 Item (Slot 4)
        double gPercent = plugin.getCollectionManager().calculateGlobalCompletionPercentage();
        int gUnlocked = plugin.getCollectionManager().getGlobalUniqueUnlockedTotal();
        int total = plugin.getCollectionManager().getTotalCategoryCount();
        int gMobs = plugin.getDataManager().getGlobalUniqueMobsUnlockedCount();
        int gItems = plugin.getDataManager().getGlobalUniqueItemsUnlockedCount();
        String gBar = renderProgressBar(gPercent);

        Component gTitle = plugin.getLanguageManager().getComponent("gui.main.global-progress-title");
        List<Component> gLore = plugin.getLanguageManager().getComponentList("gui.main.global-progress-lore",
                "bar", gBar,
                "percent", String.format("%.1f", gPercent),
                "unlocked", gUnlocked,
                "total", total,
                "mobs", gMobs,
                "items", gItems);
        inventory.setItem(4, createItem(Material.BEACON, gTitle, gLore));

        List<PlayerData> allData = plugin.getDataManager().getAllPlayerDatas();
        allData.sort((a, b) -> Integer.compare(b.getTotalUnlockedCount(), a.getTotalUnlockedCount()));

        int slotStart = 10;
        int rank = 1;
        for (PlayerData data : allData) {
            if (rank > 10) break;
            if (data.getTotalUnlockedCount() == 0) continue;

            OfflinePlayer op = Bukkit.getOfflinePlayer(data.getUuid());
            String pName = op.getName() != null ? op.getName() : "Unknown";
            double percent = plugin.getCollectionManager().calculateCompletionPercentage(data);

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(op);
                meta.displayName(plugin.getLanguageManager().formatComponent("gui.leaderboard.entry-name",
                        "rank", rank,
                        "player", pName));

                List<Component> lore = plugin.getLanguageManager().getComponentList("gui.leaderboard.entry-lore",
                        "unlocked", data.getTotalUnlockedCount(),
                        "percent", String.format("%.1f", percent));
                meta.lore(lore);
                skull.setItemMeta(meta);
            }

            int currentSlot = slotStart + (rank - 1);
            if (rank > 7) currentSlot += 2; // 第二列
            inventory.setItem(currentSlot, skull);
            slotPlayerMap.put(currentSlot, op);
            rank++;
        }

        if (rank == 1) {
            ItemStack empty = new ItemStack(Material.PAPER);
            var meta = empty.getItemMeta();
            if (meta != null) {
                meta.displayName(plugin.getLanguageManager().getComponent("gui.leaderboard.empty"));
                empty.setItemMeta(meta);
            }
            inventory.setItem(13, empty);
        }

        // 返回按鈕
        ItemStack back = new ItemStack(Material.BARRIER);
        var backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(plugin.getLanguageManager().getComponent("gui.category.back-main"));
            back.setItemMeta(backMeta);
        }
        inventory.setItem(31, back);
    }

    public OfflinePlayer getTargetPlayer(int slot) {
        return slotPlayerMap.get(slot);
    }

    private String renderProgressBar(double percent) {
        int totalBlocks = 10;
        int filled = (int) Math.round((percent / 100.0) * totalBlocks);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < totalBlocks; i++) {
            if (i < filled) sb.append("■");
            else sb.append("□");
        }
        sb.append("]");
        return sb.toString();
    }

    private ItemStack createItem(Material type, Component name, List<Component> lore) {
        ItemStack is = new ItemStack(type);
        org.bukkit.inventory.meta.ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            if (lore != null) meta.lore(lore);
            is.setItemMeta(meta);
        }
        return is;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
