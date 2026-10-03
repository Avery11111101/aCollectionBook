package tw.avery.collection.listener;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import tw.avery.collection.CollectionBookPlugin;

public class ItemUnlockListener implements Listener {

    private final CollectionBookPlugin plugin;

    public ItemUnlockListener(CollectionBookPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!plugin.getConfigManager().isTriggerItemPickup()) return;
        if (event.getEntity() instanceof Player player) {
            ItemStack item = event.getItem().getItemStack();
            checkAndUnlockItem(player, item);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!plugin.getConfigManager().isTriggerItemCraft()) return;
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack result = event.getRecipe().getResult();
            checkAndUnlockItem(player, result);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        if (!plugin.getConfigManager().isTriggerItemSmelt()) return;
        Player player = event.getPlayer();
        String matName = event.getItemType().name().toLowerCase();
        plugin.getCollectionManager().unlockItem(player, matName);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getCaught() instanceof Item caughtItem) {
            checkAndUnlockItem(event.getPlayer(), caughtItem.getItemStack());
        }
    }

    // --- 1. 直接出現在背包中解鎖 (Inventory Click / Drag / Open / Join 實時全背包掃描) ---

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            scanPlayerInventory(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            checkAndUnlockItem(player, event.getCurrentItem());
            checkAndUnlockItem(player, event.getCursor());
            scanPlayerInventory(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            scanPlayerInventory(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        scanPlayerInventory(event.getPlayer());
    }

    // --- 2. 被潑到藥水 / 藥水效果解鎖 ("被潑到藥水 也要解鎖") ---

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 解鎖普通藥水、潑濺藥水與滯留藥水
            plugin.getCollectionManager().unlockItem(player, "potion");
            plugin.getCollectionManager().unlockItem(player, "splash_potion");
            plugin.getCollectionManager().unlockItem(player, "lingering_potion");

            // 如果是被特定生物 (女巫, 凋零, 巫師) 造成的效果，亦觸發生物圖鑑解鎖
            if (event.getCause() == EntityPotionEffectEvent.Cause.ATTACK || event.getCause() == EntityPotionEffectEvent.Cause.POTION_SPLASH || event.getCause() == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD) {
                if (event.getNewEffect() != null) {
                    String typeStr = event.getNewEffect().getType().getName().toLowerCase();
                    if (typeStr.contains("wither")) {
                        plugin.getCollectionManager().unlockMob(player, "wither_skeleton");
                    } else if (typeStr.contains("poison")) {
                        plugin.getCollectionManager().unlockMob(player, "cave_spider");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        for (var entity : event.getAffectedEntities()) {
            if (entity instanceof Player player) {
                plugin.getCollectionManager().unlockItem(player, "splash_potion");
                plugin.getCollectionManager().unlockItem(player, "potion");
                ItemStack item = event.getPotion().getItem();
                checkAndUnlockItem(player, item);
            }
        }
    }

    private void scanPlayerInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            checkAndUnlockItem(player, item);
        }
    }

    private void checkAndUnlockItem(Player player, ItemStack item) {
        if (item != null && !item.getType().isAir()) {
            String matName = item.getType().name().toLowerCase();
            plugin.getCollectionManager().unlockItem(player, matName);
        }
    }
}
