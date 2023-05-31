package me.blutkrone.rpgcore.entity;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.entity.entities.CoreTotem;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import me.blutkrone.rpgcore.util.world.ChunkIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager implements Listener {

    // snapshots of observable players in a range
    private Map<ChunkIdentifier, List<Player>> observation = new HashMap<>();
    // entities which are registered in the core
    private Map<UUID, CoreEntity> entity = new ConcurrentHashMap<>();
    // death related handling
    private int grave_timer;
    private ItemStack default_grave;
    private boolean free_resurrect;

    public EntityManager() {
        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("grave.yml"));
            this.grave_timer = config.getInt("grave-duration", 100);
            this.default_grave = ItemBuilder.of(config.getString("default-grave", "NETHERRACK")).build();
            this.free_resurrect = config.getBoolean("allow-free-resurrect", false);
        } catch (IOException e) {
            this.grave_timer = 100;
            this.default_grave = new ItemStack(Material.NETHERRACK);
            this.free_resurrect = true;
        }

        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            if (!RPGCore.inst().isEnabled()) {
                return;
            }

            // snapshot player locations
            Map<Player, ChunkIdentifier> snapshot = new HashMap<>();
            Bukkit.getOnlinePlayers().forEach(player -> {
                snapshot.put(player, new ChunkIdentifier(player.getLocation()));
            });
            // run async to reduce performance impact
            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                if (!RPGCore.inst().isEnabled()) {
                    return;
                }

                // compute which player is observing what chunk
                Map<ChunkIdentifier, List<Player>> computed = new HashMap<>();
                snapshot.forEach((player, where) -> {
                    for (int i = -3; i <= +3; i++) {
                        for (int j = -3; j <= +3; j++) {
                            computed.computeIfAbsent(where.withOffset(i, j), (k -> new ArrayList<>())).add(player);
                        }
                    }
                });
                // sync storage to avoid data conflicts
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    if (!RPGCore.inst().isEnabled()) {
                        return;
                    }

                    observation = Collections.unmodifiableMap(computed);
                });
            });
        }, 1, 1);

        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    /**
     * How many ticks the allies of a dead player have to resurrect them
     * from their grave.
     *
     * @return Grave duration
     */
    public int getGraveTimer() {
        return grave_timer;
    }

    /**
     * The icon to use for the grave of a player.
     *
     * @return The default grave.
     */
    public ItemStack getGraveDefault() {
        return default_grave;
    }

    /**
     * Whether allies can resurrect with just a right click, otherwise
     * a resurrection skill is required.
     *
     * @return Resurrect at no cost.
     */
    public boolean isGraveFreeResurrect() {
        return free_resurrect;
    }

    /**
     * Handle backing the entity manager.
     *
     * @return active entities
     */
    public Map<UUID, CoreEntity> getHandleUnsafe() {
        return entity;
    }

    /**
     * Register the entity to the entity mapping, a finished entity is
     * expected to be provided
     */
    public void register(UUID uuid, CoreEntity entity) {
        this.entity.put(uuid, entity);
    }

    /**
     * Retrieve the entity which has a certain UUID, this method
     * will not create the core entity.
     *
     * @param uuid which entity to retrieve
     * @return entity to retrieve
     */
    public CoreEntity getEntity(UUID uuid) {
        return this.entity.get(uuid);
    }

    /**
     * Retrieve the player, if the associated entity is a player.
     *
     * @param uuid which entity to retrieve
     * @return entity to retrieve
     */
    public CorePlayer getPlayer(UUID uuid) {
        CoreEntity found = getEntity(uuid);
        return found instanceof CorePlayer ? ((CorePlayer) found) : null;
    }

    /**
     * Retrieve the mob, if the associated entity is a mob.
     *
     * @param uuid which entity to retrieve
     * @return entity to retrieve
     */
    public CoreMob getMob(UUID uuid) {
        CoreEntity found = getEntity(uuid);
        return found instanceof CoreMob ? ((CoreMob) found) : null;
    }


    /**
     * Retrieve the mob, if the associated entity is a mob.
     *
     * @param uuid which entity to retrieve
     * @return entity to retrieve
     */
    public CoreTotem getTotem(UUID uuid) {
        CoreEntity found = getEntity(uuid);
        return found instanceof CoreTotem ? ((CoreTotem) found) : null;
    }

    /**
     * Retrieve the entity which has a certain UUID, this method
     * will not create the core entity.
     *
     * @param entity which entity to retrieve
     * @return entity to retrieve
     */
    public CoreEntity getEntity(LivingEntity entity) {
        return this.entity.get(entity.getUniqueId());
    }

    /**
     * Retrieve the player, if the associated entity is a player.
     *
     * @param entity which entity to retrieve
     * @return entity to retrieve, if available
     */
    public CorePlayer getPlayer(LivingEntity entity) {
        CoreEntity found = getEntity(entity);
        return found instanceof CorePlayer ? ((CorePlayer) found) : null;
    }

    /**
     * Retrieve the mob, if the associated entity is a mob.
     *
     * @param entity which entity to retrieve
     * @return entity to retrieve, if available
     */
    public CoreMob getMob(LivingEntity entity) {
        CoreEntity found = getEntity(entity);
        return found instanceof CoreMob ? ((CoreMob) found) : null;
    }

    /**
     * Request to drop a given entity from the core.
     *
     * @param entity which entity to unregister
     */
    public void unregister(UUID entity) {
        CoreEntity removed = this.entity.remove(entity);
        if (removed != null) {
            removed.remove();
        }
    }

    /**
     * Request to drop all entities from the core.
     */
    public void unregisterAll() {
        for (CoreEntity entity : this.entity.values())
            entity.remove();
        this.entity.clear();
    }

    /**
     * Provides a snapshot on entities which are within observation distance
     * of the given location.
     *
     * @param where the location which we want to be observed.
     * @return the players which are observed.
     */
    public List<Player> getObserving(Location where) {
        return observation.getOrDefault(new ChunkIdentifier(where), Collections.emptyList());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onDeathEvent(EntityDeathEvent e) {
        // prevent drops from any core entity
        CoreEntity entity = getEntity(e.getEntity());
        if (entity != null) {
            e.getDrops().clear();
            e.setDroppedExp(0);
        }
    }
}
