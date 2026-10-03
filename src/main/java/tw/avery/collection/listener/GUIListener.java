package tw.avery.collection.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import tw.avery.collection.CollectionBookPlugin;
import tw.avery.collection.data.PlayerData;

public class GUIListener implements Listener {

    private final CollectionBookPlugin plugin;

    public GUIListener(CollectionBookPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 快取載入玩家數據
        plugin.getDataManager().getPlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 卸載並儲存玩家數據
        plugin.getDataManager().unloadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) return;

        if (holder instanceof tw.avery.collection.gui.MainMenuHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;
            int slot = event.getRawSlot();

            playSound(player, plugin.getConfigManager().getSoundClick());

            if (slot == 11) {
                player.openInventory(new tw.avery.collection.gui.CategoryMenuHolder(plugin, player, true, 1, tw.avery.collection.gui.CategoryMenuHolder.FilterMode.ALL).getInventory());
            } else if (slot == 13) {
                player.openInventory(new tw.avery.collection.gui.CategoryMenuHolder(plugin, player, false, 1, tw.avery.collection.gui.CategoryMenuHolder.FilterMode.ALL).getInventory());
            } else if (slot == 15) {
                player.openInventory(new tw.avery.collection.gui.RewardMenuHolder(plugin, player).getInventory());
            } else if (slot == 22) {
                player.openInventory(new tw.avery.collection.gui.LeaderboardMenuHolder(plugin).getInventory());
            }

        } else if (holder instanceof tw.avery.collection.gui.CategoryMenuHolder categoryMenu) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= 54) return;

            boolean isInspect = !categoryMenu.getTargetUuid().equals(player.getUniqueId());

            if (slot == 48) { // 上一頁
                if (categoryMenu.getPage() > 1) {
                    playSound(player, plugin.getConfigManager().getSoundPageChange());
                    player.openInventory(new tw.avery.collection.gui.CategoryMenuHolder(plugin, player,
                            categoryMenu.getTargetUuid(), categoryMenu.getTargetName(),
                            categoryMenu.isMobType(), categoryMenu.getPage() - 1, categoryMenu.getFilterMode()).getInventory());
                }
            } else if (slot == 49) { // 返回主選單
                playSound(player, plugin.getConfigManager().getSoundClick());
                if (isInspect) {
                    player.openInventory(new tw.avery.collection.gui.LeaderboardMenuHolder(plugin).getInventory());
                } else {
                    player.openInventory(new tw.avery.collection.gui.MainMenuHolder(plugin, player).getInventory());
                }
            } else if (slot == 50) { // 下一頁
                if (categoryMenu.getPage() < categoryMenu.getMaxPage()) {
                    playSound(player, plugin.getConfigManager().getSoundPageChange());
                    player.openInventory(new tw.avery.collection.gui.CategoryMenuHolder(plugin, player,
                            categoryMenu.getTargetUuid(), categoryMenu.getTargetName(),
                            categoryMenu.isMobType(), categoryMenu.getPage() + 1, categoryMenu.getFilterMode()).getInventory());
                }
            } else if (slot == 52) { // 類別切換 (生物 <-> 物品)
                playSound(player, plugin.getConfigManager().getSoundClick());
                boolean nextIsMob = !categoryMenu.isMobType();
                player.openInventory(new tw.avery.collection.gui.CategoryMenuHolder(plugin, player,
                        categoryMenu.getTargetUuid(), categoryMenu.getTargetName(),
                        nextIsMob, 1, categoryMenu.getFilterMode()).getInventory());
            } else if (slot == 53) { // 篩選模式切換
                playSound(player, plugin.getConfigManager().getSoundClick());
                var nextMode = categoryMenu.getFilterMode().next();
                player.openInventory(new tw.avery.collection.gui.CategoryMenuHolder(plugin, player,
                        categoryMenu.getTargetUuid(), categoryMenu.getTargetName(),
                        categoryMenu.isMobType(), 1, nextMode).getInventory());
            } else if (slot < 45 && !isInspect) { // 點擊項目卡片（個人分享功能）
                ItemStack clicked = event.getInventory().getItem(slot);
                if (clicked == null || clicked.getType().isAir()) return;

                PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
                String rawName = categoryMenu.getRawName(slot);
                if (rawName != null) {
                    boolean isUnlocked = categoryMenu.isMobType() ? data.isMobUnlocked(rawName) : data.isItemUnlocked(rawName);
                    if (isUnlocked) {
                        int order = categoryMenu.isMobType() ? data.getMobOrder(rawName) : data.getItemOrder(rawName);
                        String typeName = categoryMenu.isMobType() ? "生物" : "物品";
                        plugin.getCollectionManager().shareCollection(player, rawName, order, typeName);
                        player.closeInventory();
                    }
                }
            }

        } else if (holder instanceof tw.avery.collection.gui.LeaderboardMenuHolder leaderboardHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;
            int slot = event.getRawSlot();
            if (slot == 31) {
                playSound(player, plugin.getConfigManager().getSoundClick());
                player.openInventory(new tw.avery.collection.gui.MainMenuHolder(plugin, player).getInventory());
            } else {
                org.bukkit.OfflinePlayer target = leaderboardHolder.getTargetPlayer(slot);
                if (target != null) {
                    playSound(player, plugin.getConfigManager().getSoundClick());
                    String targetName = target.getName() != null ? target.getName() : "Unknown";
                    player.openInventory(new tw.avery.collection.gui.CategoryMenuHolder(plugin, player,
                            target.getUniqueId(), targetName, true, 1, tw.avery.collection.gui.CategoryMenuHolder.FilterMode.ALL).getInventory());
                }
            }

        } else if (holder instanceof tw.avery.collection.gui.RewardMenuHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;
            int slot = event.getRawSlot();
            if (slot == 22) {
                playSound(player, plugin.getConfigManager().getSoundClick());
                player.openInventory(new tw.avery.collection.gui.MainMenuHolder(plugin, player).getInventory());
            } else if (slot == 10 || slot == 12 || slot == 14 || slot == 16) {
                int index = (slot - 10) / 2;
                var milestones = plugin.getRewardManager().getMilestones();
                if (index < milestones.size()) {
                    var ms = milestones.get(index);
                    if (plugin.getRewardManager().claimReward(player, ms)) {
                        player.openInventory(new tw.avery.collection.gui.RewardMenuHolder(plugin, player).getInventory());
                    }
                }
            }
        }
    }

    private String resolveRawName(ItemStack item, boolean isMob) {
        if (isMob) {
            String name = item.getType().name().toLowerCase();
            if (name.endsWith("_spawn_egg")) {
                return name.replace("_spawn_egg", "");
            }
        } else {
            return item.getType().name().toLowerCase();
        }
        return null;
    }

    private void playSound(Player player, org.bukkit.Sound sound) {
        if (plugin.getConfigManager().isSoundsEnabled() && sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }
}
