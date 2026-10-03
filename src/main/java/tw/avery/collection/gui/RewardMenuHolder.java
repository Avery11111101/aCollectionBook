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
import tw.avery.collection.manager.RewardManager;

import java.util.ArrayList;
import java.util.List;

public class RewardMenuHolder implements InventoryHolder {

    private final CollectionBookPlugin plugin;
    private final Inventory inventory;

    public RewardMenuHolder(CollectionBookPlugin plugin, Player player) {
        this.plugin = plugin;
        Component title = plugin.getLanguageManager().getComponent("gui.rewards.title");
        this.inventory = Bukkit.createInventory(this, 27, title);
        buildMenu(player);
    }

    private void buildMenu(Player player) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inventory.setItem(i, glass);

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        double currentPercent = plugin.getCollectionManager().calculateCompletionPercentage(data);
        List<RewardManager.MilestoneReward> milestones = plugin.getRewardManager().getMilestones();

        int[] slots = {10, 12, 14, 16};
        for (int i = 0; i < milestones.size() && i < slots.length; i++) {
            RewardManager.MilestoneReward ms = milestones.get(i);
            boolean claimed = data.isRewardClaimed(ms.percentage);
            boolean eligible = currentPercent >= ms.percentage;

            Material mat;
            Component nameComp;
            List<Component> lore = new ArrayList<>();

            lore.add(Component.text(""));
            lore.add(plugin.getLanguageManager().formatComponent("gui.main.progress-bar-format",
                    "bar", renderBar(currentPercent, ms.percentage),
                    "percent", String.format("%.1f", currentPercent),
                    "unlocked", data.getTotalUnlockedCount(),
                    "total", plugin.getCollectionManager().getTotalCategoryCount()));

            if (claimed) {
                mat = Material.MINECART;
                nameComp = plugin.getLanguageManager().formatComponent("gui.rewards.claimed", "target", ms.percentage);
                lore.add(plugin.getLanguageManager().getComponent("gui.rewards.claimed-lore"));
            } else if (eligible) {
                mat = Material.CHEST;
                nameComp = plugin.getLanguageManager().formatComponent("gui.rewards.claimable", "target", ms.percentage);
                lore.add(plugin.getLanguageManager().getComponent("gui.rewards.claimable-lore"));
            } else {
                mat = Material.IRON_BARS;
                nameComp = plugin.getLanguageManager().formatComponent("gui.rewards.locked",
                        "percent", String.format("%.1f", currentPercent),
                        "target", ms.percentage);
            }

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(nameComp);
                meta.lore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slots[i], item);
        }

        // 返回按鈕
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(plugin.getLanguageManager().getComponent("gui.category.back-main"));
            back.setItemMeta(backMeta);
        }
        inventory.setItem(22, back);
    }

    private String renderBar(double current, int target) {
        int total = 10;
        double ratio = Math.min(1.0, current / target);
        int filled = (int) Math.round(ratio * total);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < total; i++) {
            if (i < filled) sb.append("■");
            else sb.append("□");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
