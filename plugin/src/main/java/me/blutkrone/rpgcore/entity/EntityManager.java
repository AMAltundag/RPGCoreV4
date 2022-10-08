package me.blutkrone.rpgcore.entity;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.entity.entities.CoreTotem;
import me.blutkrone.rpgcore.util.world.ChunkIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class EntityManager {

    // snapshots of observable players in a range
    private Map<ChunkIdentifier, List<Player>> observation = new HashMap<>();
    // entities which are registered in the core
    private Map<UUID, CoreEntity> entity = new HashMap<>();

    public EntityManager() {
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            // snapshot player locations
            Map<Player, ChunkIdentifier> snapshot = new HashMap<>();
            Bukkit.getOnlinePlayers().forEach(player -> {
                snapshot.put(player, new ChunkIdentifier(player.getLocation()));
            });
            // run async to reduce performance impact
            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
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
                    observation = computed;
                });
            });
        }, 1, 1);
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
}
