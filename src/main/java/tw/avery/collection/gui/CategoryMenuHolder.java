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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategoryMenuHolder implements InventoryHolder {

    public enum FilterMode {
        ALL, UNLOCKED_ONLY, LOCKED_ONLY;

        public FilterMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private final CollectionBookPlugin plugin;
    private final Inventory inventory;
    private final boolean isMobType;
    private final UUID targetUuid;
    private final String targetName;
    private int page;
    private int maxPage;
    private FilterMode filterMode = FilterMode.ALL;
    private final String[] slotRawNames = new String[45];

    public CategoryMenuHolder(CollectionBookPlugin plugin, Player player, boolean isMobType, int page, FilterMode mode) {
        this(plugin, player, player.getUniqueId(), player.getName(), isMobType, page, mode);
    }

    public CategoryMenuHolder(CollectionBookPlugin plugin, Player viewer, UUID targetUuid, String targetName, boolean isMobType, int page, FilterMode mode) {
        this.plugin = plugin;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.isMobType = isMobType;
        this.page = Math.max(1, page);
        this.filterMode = mode;

        this.inventory = Bukkit.createInventory(this, 54, Component.text(""));
        buildMenu(viewer);
    }

    public void buildMenu(Player viewer) {
        PlayerData data = plugin.getDataManager().getPlayerData(targetUuid);
        List<String> rawList = isMobType ? plugin.getCollectionManager().getAllMobNames() : plugin.getCollectionManager().getAllItemNames();
        boolean isInspect = !targetUuid.equals(viewer.getUniqueId());

        List<String> unlockedList = new ArrayList<>();
        List<String> lockedList = new ArrayList<>();
        for (String entry : rawList) {
            boolean isUnlocked = isMobType ? data.isMobUnlocked(entry) : data.isItemUnlocked(entry);
            if (isUnlocked) {
                unlockedList.add(entry);
            } else {
                lockedList.add(entry);
            }
        }

        List<String> filtered = new ArrayList<>();
        if (filterMode == FilterMode.ALL) {
            filtered.addAll(unlockedList);
            filtered.addAll(lockedList);
        } else if (filterMode == FilterMode.UNLOCKED_ONLY) {
            filtered.addAll(unlockedList);
        } else if (filterMode == FilterMode.LOCKED_ONLY) {
            filtered.addAll(lockedList);
        }

        this.maxPage = Math.max(1, (int) Math.ceil((double) filtered.size() / 45));
        if (page > maxPage) page = maxPage;

        Component title;
        if (isInspect) {
            title = isMobType ?
                    plugin.getLanguageManager().formatComponent("gui.category.inspect-mobs-title", "target", targetName, "page", page, "max_page", maxPage) :
                    plugin.getLanguageManager().formatComponent("gui.category.inspect-items-title", "target", targetName, "page", page, "max_page", maxPage);
        } else {
            title = isMobType ?
                    plugin.getLanguageManager().formatComponent("gui.category.mobs-title", "page", page, "max_page", maxPage) :
                    plugin.getLanguageManager().formatComponent("gui.category.items-title", "page", page, "max_page", maxPage);
        }

        inventory.clear();

        boolean showLockedNames = plugin.getConfigManager().isShowLockedNames();

        int start = (page - 1) * 45;
        for (int i = 0; i < 45; i++) {
            int idx = start + i;
            if (idx >= filtered.size()) break;

            String name = filtered.get(idx);
            slotRawNames[i] = name;
            boolean isUnlocked = isMobType ? data.isMobUnlocked(name) : data.isItemUnlocked(name);
            int globalCount = isMobType ? plugin.getDataManager().getGlobalMobUnlockCount(name) : plugin.getDataManager().getGlobalItemUnlockCount(name);
            int playerOrder = isMobType ? data.getMobOrder(name) : data.getItemOrder(name);

            ItemStack item;
            String cnName = formatName(name);
            if (isUnlocked) {
                Material mat = resolveIconMaterial(name, isMobType);
                String lorePath = isInspect ? "gui.category.inspect-unlocked-lore" : "gui.category.unlocked-lore";
                List<Component> lore = plugin.getLanguageManager().getComponentList(lorePath,
                        "global_count", globalCount,
                        "player_order", playerOrder);
                Component itemNameComp = plugin.getLanguageManager().formatComponent("gui.category.unlocked-item-name", "name", cnName);
                item = createItem(mat, itemNameComp, lore);
            } else {
                Material mat = isMobType ? Material.COAL : Material.GRAY_DYE;
                Component mobItemName;
                if (showLockedNames) {
                    String langPath = isMobType ? "gui.category.locked-mob-name" : "gui.category.locked-item-name";
                    mobItemName = plugin.getLanguageManager().formatComponent(langPath, "name", cnName);
                } else {
                    mobItemName = plugin.getLanguageManager().getComponent("gui.category.hidden-locked-name");
                }

                String lorePath = isMobType ? "gui.category.locked-mob-lore" : "gui.category.locked-item-lore";
                List<Component> lore = plugin.getLanguageManager().getComponentList(lorePath, "global_count", globalCount);
                item = createItem(mat, mobItemName, lore);
            }
            inventory.setItem(i, item);
        }

        // 底部導覽列
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, glass);
        }

        if (page > 1) {
            inventory.setItem(48, createItem(Material.ARROW, plugin.getLanguageManager().getComponent("gui.category.prev-page"), null));
        }

        inventory.setItem(49, createItem(Material.BARRIER, plugin.getLanguageManager().getComponent("gui.category.back-main"), null));

        if (page < maxPage) {
            inventory.setItem(50, createItem(Material.ARROW, plugin.getLanguageManager().getComponent("gui.category.next-page"), null));
        }

        // 類別切換按鈕 (Slot 52: 生物 <-> 物品圖鑑切換)
        Material toggleMat = isMobType ? Material.CHEST : Material.ZOMBIE_HEAD;
        Component toggleTitle = isMobType ?
                plugin.getLanguageManager().getComponent("gui.category.switch-to-items") :
                plugin.getLanguageManager().getComponent("gui.category.switch-to-mobs");
        inventory.setItem(52, createItem(toggleMat, toggleTitle, null));

        // 篩選模式按鈕 (Slot 53)
        String modeTextPath = switch (filterMode) {
            case ALL -> "gui.category.filter-all";
            case UNLOCKED_ONLY -> "gui.category.filter-unlocked";
            case LOCKED_ONLY -> "gui.category.filter-locked";
        };
        Component filterComp = plugin.getLanguageManager().formatComponent("gui.category.filter-button",
                "mode", plugin.getLanguageManager().getRaw(modeTextPath));
        inventory.setItem(53, createItem(Material.HOPPER, filterComp, null));
    }

    private Material resolveIconMaterial(String name, boolean isMob) {
        if (isMob) {
            try {
                return Material.valueOf(name.toUpperCase() + "_SPAWN_EGG");
            } catch (Exception e) {
                return Material.PAPER;
            }
        } else {
            try {
                return Material.valueOf(name.toUpperCase());
            } catch (Exception e) {
                return Material.PAPER;
            }
        }
    }

    private String formatName(String raw) {
        if (isMobType) {
            return tw.avery.collection.util.MinecraftTranslation.getMobChineseName(raw);
        } else {
            return tw.avery.collection.util.MinecraftTranslation.getItemChineseName(raw);
        }
    }

    private ItemStack createItem(Material type, String name, List<Component> lore) {
        return createItem(type, plugin.getLanguageManager().getComponent(name), lore);
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

    public boolean isMobType() { return isMobType; }
    public int getPage() { return page; }
    public int getMaxPage() { return maxPage; }
    public FilterMode getFilterMode() { return filterMode; }
    public UUID getTargetUuid() { return targetUuid; }
    public String getTargetName() { return targetName; }

    public String getRawName(int slot) {
        if (slot >= 0 && slot < 45) {
            return slotRawNames[slot];
        }
        return null;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
