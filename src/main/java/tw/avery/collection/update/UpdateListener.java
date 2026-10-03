package tw.avery.collection.update;

import tw.avery.collection.CollectionBookPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 管理員進服更新提示監聽器
 */
public final class UpdateListener implements Listener {

    private final CollectionBookPlugin plugin;
    private final UpdateService updateService;

    public UpdateListener(CollectionBookPlugin plugin, UpdateService updateService) {
        this.plugin = plugin;
        this.updateService = updateService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (!player.hasPermission("collectionbook.admin")) {
            return;
        }

        if (!plugin.getConfig().getBoolean("updater.notify-admin-on-join", true)) {
            return;
        }

        if (updateService.hasUpdate()) {
            var release = updateService.getCachedLatestRelease();
            if (release != null) {
                // 稍微延遲 2 秒發送，避免玩家剛進服被其他訊息刷掉
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        String typeTag = release.isPrerelease() ? "§b[測試版 🧪]" : "§a[正式版 🌟]";
                        player.sendMessage("§6[aCollectionBook] 圖鑑系統發現新版本 " + typeTag + ": §f" + release.tagName() + " §7(目前: v" + plugin.getDescription().getVersion() + ")");
                        player.sendMessage("§e請執行指令 §b/colladmin update §e查看詳細日誌或選擇下載更新。");
                    }
                }, 40L);
            }
        }
    }
}
