package tw.avery.collection.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tw.avery.collection.CollectionBookPlugin;
import tw.avery.collection.data.PlayerData;

import java.util.List;

public class MainMenuHolder implements InventoryHolder {

    private final CollectionBookPlugin plugin;
    private final Inventory inventory;

    public MainMenuHolder(CollectionBookPlugin plugin, Player player) {
        this.plugin = plugin;
        Component title = plugin.getLanguageManager().getComponent("gui.main.title");
        this.inventory = Bukkit.createInventory(this, 27, title);
        buildMenu(player);
    }

    private void buildMenu(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        double percent = plugin.getCollectionManager().calculateCompletionPercentage(data);
        int unlocked = data.getTotalUnlockedCount();
        int total = plugin.getCollectionManager().getTotalCategoryCount();

        // 邊框與裝飾
        ItemStack glass = createItem(Material.DARK_OAK_HANGING_SIGN, "<gray>", null);
        ItemStack fill = createItem(Material.BLACK_STAINED_GLASS_PANE, "<gray> ", null);
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, fill);
        }

        // 個人動態進度條 Item (Slot 3)
        String progressBar = renderProgressBar(percent);
        Component progSub = plugin.getLanguageManager().formatComponent("gui.main.progress-bar-format",
                "bar", progressBar,
                "percent", String.format("%.1f", percent),
                "unlocked", unlocked,
                "total", total);

        ItemStack infoItem = createItem(Material.BOOK, plugin.getLanguageManager().getComponent("gui.main.progress-title"),
                List.of(progSub));
        inventory.setItem(3, infoItem);

        // 全服探索進度 Item (Slot 5)
        double gPercent = plugin.getCollectionManager().calculateGlobalCompletionPercentage();
        int gUnlocked = plugin.getCollectionManager().getGlobalUniqueUnlockedTotal();
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
        inventory.setItem(5, createItem(Material.NETHER_STAR, gTitle, gLore));

        // 生物圖鑑按鈕 (Slot 11)
        Component mobsName = plugin.getLanguageManager().getComponent("gui.main.mobs-button");
        List<Component> mobsLore = plugin.getLanguageManager().getComponentList("gui.main.mobs-lore");
        inventory.setItem(11, createItem(Material.ZOMBIE_HEAD, mobsName, mobsLore));

        // 物品圖鑑按鈕 (Slot 13)
        Component itemsName = plugin.getLanguageManager().getComponent("gui.main.items-button");
        List<Component> itemsLore = plugin.getLanguageManager().getComponentList("gui.main.items-lore");
        inventory.setItem(13, createItem(Material.CHEST, itemsName, itemsLore));

        // 里程碑獎勵 (Slot 15)
        Component rewardName = plugin.getLanguageManager().getComponent("gui.main.rewards-button");
        List<Component> rewardLore = plugin.getLanguageManager().getComponentList("gui.main.rewards-lore");
        inventory.setItem(15, createItem(Material.NETHER_STAR, rewardName, rewardLore));

        // 排行榜按鈕 (Slot 22)
        Component topName = plugin.getLanguageManager().getComponent("gui.main.leaderboard-button");
        List<Component> topLore = plugin.getLanguageManager().getComponentList("gui.main.leaderboard-lore");
        inventory.setItem(22, createItem(Material.GOLDEN_HELMET, topName, topLore));
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

    private ItemStack createItem(Material type, String name, List<Component> lore) {
        ItemStack is = new ItemStack(type);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getLanguageManager().getComponent(name));
            if (lore != null) meta.lore(lore);
            is.setItemMeta(meta);
        }
        return is;
    }

    private ItemStack createItem(Material type, Component name, List<Component> lore) {
        ItemStack is = new ItemStack(type);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            if (lore != null) meta.lore(lore);
            is.setItemMeta(meta);
        }
        return is;
    }

    private ItemStack createItem(Material fallback, Component name, List<Component> lore, Material fallback2) {
        Material mat = fallback;
        try {
            mat = Material.valueOf("GOLDEN_TROPHY");
        } catch (Exception e) {
            mat = fallback2;
        }
        return createItem(mat, name, lore);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
