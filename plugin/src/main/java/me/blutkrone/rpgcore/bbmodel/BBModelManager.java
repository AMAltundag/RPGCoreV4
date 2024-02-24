package me.blutkrone.rpgcore.bbmodel;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.bbmodel.owner.IModelOwner;
import me.blutkrone.rpgcore.bbmodel.owner.OwnerMob;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BBModelManager implements Listener {

    private static final int SYNC_TICK_RATE = 3;
    private static final int ASYNC_TICK_RATE = 1;

    // load-distributed registry of managed entities.
    private List<Map<Object, IModelOwner[]>> active_registry = new ArrayList<>();
    // priority registry has a sync tick rate of 1
    private Map<Object, IModelOwner[]> priority_registry = new ConcurrentHashMap<>();
    // hitbox entity mapped to real entity
    private Map<UUID, UUID> hitbox_to_real = new ConcurrentHashMap<>();

    public BBModelManager() {
        // distributed registries to maximize load distribution
        for (int i = 0; i < SYNC_TICK_RATE; i++) {
            this.active_registry.add(new ConcurrentHashMap<>());
        }

        // dispatch updates on normal registry
        for (int i = 0; i < this.active_registry.size(); i++) {
            Map<Object, IModelOwner[]> registry = this.active_registry.get(i);
            // query the async task
            Bukkit.getScheduler().runTaskTimerAsynchronously(RPGCore.inst(), () -> {
                registry.entrySet().removeIf(entry -> {
                    IModelOwner update = entry.getValue()[0].async(ASYNC_TICK_RATE);
                    if (update == null) {
                        return true;
                    }

                    entry.getValue()[0] = update;
                    return false;
                });
            }, 1+i, ASYNC_TICK_RATE);
            // query the sync task
            Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
                for (IModelOwner[] owner : registry.values()) {
                    owner[0].sync(SYNC_TICK_RATE);
                }
            }, 1+i, SYNC_TICK_RATE);
        }

        // dispatch updates on priority registry
        Bukkit.getScheduler().runTaskTimerAsynchronously(RPGCore.inst(), () -> {
            priority_registry.entrySet().removeIf(entry -> {
                IModelOwner update = entry.getValue()[0].async(ASYNC_TICK_RATE);
                if (update == null) {
                    return true;
                }

                entry.getValue()[0] = update;
                return false;
            });
        }, 1, ASYNC_TICK_RATE);
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            for (IModelOwner[] owner : priority_registry.values()) {
                owner[0].sync(SYNC_TICK_RATE);
            }
        }, 1, SYNC_TICK_RATE);
    }

    /**
     * Register as a priority model owner, do note that this method
     * shouldn't be used unless we REALLY need the priority.
     *
     * @param uuid Unique ID for the owner
     * @param owner The owner we want to register
     */
    public void priority(UUID uuid, IModelOwner owner) {
        this.priority_registry.put(uuid, new IModelOwner[]{ owner });
    }

    /**
     * Register as a priority model owner, do note that this method
     * shouldn't be used unless we REALLY need the priority.
     *
     * @param uuid Unique ID for the owner
     * @param owner The owner we want to register
     */
    public void register(UUID uuid, IModelOwner owner) {
        int registry = ThreadLocalRandom.current().nextInt(this.active_registry.size());
        this.active_registry.get(registry).put(uuid, new IModelOwner[]{ owner });
    }

    /**
     * Register a core entity as a managed model owner, upon death make
     * sure that the owner is flagged for recycling.<br>
     * <br>
     * Should we already be registered, we will retrieve that.
     *
     * @param entity Who to register.
     */
    public IModelOwner register(CoreMob entity) {
        // check if we have a previously registered owner
        IModelOwner owner = get(entity);
        // register a new owner if we do not have one
        if (owner == null) {
            owner = new OwnerMob(this, entity);
            int registry = ThreadLocalRandom.current().nextInt(this.active_registry.size());
            this.active_registry.get(registry).put(entity.getUniqueId(), new IModelOwner[]{ owner });
        }
        // offer the owner for the given player
        return owner;
    }

    /**
     * Find the given model owner for an entity, do note that there
     * is no guarantee that the entity is a model owner.
     *
     * @param entity Who to check
     * @return Model tied to entity.
     */
    public IModelOwner get(CoreEntity entity) {
        for (Map<Object, IModelOwner[]> registry : this.active_registry) {
            IModelOwner[] managed = registry.get(entity.getUniqueId());
            if (managed != null) {
                return managed[0];
            }
        }

        return null;
    }

    /**
     * Should the given UUID belong to a hitbox, this will return the
     * UUID of the owner of that hitbox.
     *
     * @param uuid
     * @return
     */
    public UUID getOwnerOfHitbox(UUID uuid) {
        return this.hitbox_to_real.get(uuid);
    }

    /**
     * Assign a hitbox to the owner.
     *
     * @param owner Who owns the hitbox
     * @param hitbox The hitbox to assign
     */
    public void bindHitbox(UUID owner, UUID hitbox) {
        this.hitbox_to_real.put(hitbox, owner);
    }

    /**
     * Delete a hitbox binding.
     *
     * @param hitbox Hitbox to abandon
     */
    public void unbindHitbox(UUID hitbox) {
        this.hitbox_to_real.remove(hitbox);
    }

    /*
     * Should the given UUID belong to a hitbox, this will return the
     * UUID of the owner of that hitbox.
     *
     * @param entity
     * @return
     */
    CoreEntity getOwnerOfHitbox(Entity entity) {
        UUID uuid = this.hitbox_to_real.get(entity.getUniqueId());
        return uuid != null ? RPGCore.inst().getEntityManager().getEntity(uuid) : null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTransferHitbox(PlayerInteractEntityEvent event) {
        CoreEntity owner = getOwnerOfHitbox(event.getRightClicked());
        if (owner != null) {
            Bukkit.getPluginManager().callEvent(new PlayerInteractEntityEvent(
                    event.getPlayer(), owner.getEntity(), event.getHand()));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTransferHitbox(EntityDamageEvent event) {
        CoreEntity owner = getOwnerOfHitbox(event.getEntity());
        if (owner != null) {
            if (event instanceof EntityDamageByEntityEvent) {
                Bukkit.getPluginManager().callEvent(new EntityDamageByEntityEvent(
                        ((EntityDamageByEntityEvent) event).getDamager(),
                        owner.getEntity(), event.getCause(), event.getDamage()));
            } else if (event instanceof EntityDamageByBlockEvent){
                Bukkit.getPluginManager().callEvent(new EntityDamageByBlockEvent(
                        ((EntityDamageByBlockEvent) event).getDamager(),
                        owner.getEntity(), event.getCause(), event.getDamage()));
            } else {
                Bukkit.getPluginManager().callEvent(new EntityDamageEvent(
                        owner.getEntity(), event.getCause(), event.getDamage()));
            }

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTransferHitbox(ProjectileHitEvent event) {
        CoreEntity owner = getOwnerOfHitbox(event.getEntity());
        if (owner != null) {
            Bukkit.getPluginManager().callEvent(new ProjectileHitEvent(
                    event.getEntity(), owner.getEntity(), event.getHitBlock(), event.getHitBlockFace()));
            event.setCancelled(true);
        }
    }
}
