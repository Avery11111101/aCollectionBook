package tw.avery.collection.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.spigotmc.event.entity.EntityMountEvent;
import tw.avery.collection.CollectionBookPlugin;

public class MobUnlockListener implements Listener {

    private final CollectionBookPlugin plugin;

    public MobUnlockListener(CollectionBookPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!plugin.getConfigManager().isTriggerMobSlay()) return;
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) return;

        Player killer = victim.getKiller();
        if (killer == null && victim.getLastDamageCause() != null) {
            if (victim.getLastDamageCause() instanceof org.bukkit.event.entity.EntityDamageByEntityEvent edbe) {
                if (edbe.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
                    killer = p;
                }
            }
        }

        if (killer != null) {
            String mobName = victim.getType().name().toLowerCase();
            plugin.getCollectionManager().unlockMob(killer, mobName);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (!plugin.getConfigManager().isTriggerMobBreed()) return;
        if (event.getBreeder() instanceof Player player) {
            String mobName = event.getEntity().getType().name().toLowerCase();
            plugin.getCollectionManager().unlockMob(player, mobName);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityMount(EntityMountEvent event) {
        if (!plugin.getConfigManager().isTriggerMobRide()) return;
        if (event.getEntity() instanceof Player player) {
            String mobName = event.getMount().getType().name().toLowerCase();
            plugin.getCollectionManager().unlockMob(player, mobName);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!plugin.getConfigManager().isTriggerMobInteract()) return;
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType().isAir()) return;

        String mobName = event.getRightClicked().getType().name().toLowerCase();
        plugin.getCollectionManager().unlockMob(player, mobName);
    }
}
