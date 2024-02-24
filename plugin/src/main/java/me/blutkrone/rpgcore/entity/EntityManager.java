package me.blutkrone.rpgcore.entity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
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
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager implements Listener {

    // snapshots of observable players in a range
    private Map<ChunkIdentifier, List<Player>> players_observing_chunk = new HashMap<>();

    // entities which are registered in the core
    private final Map<UUID, CoreEntity> registered_entities = new ConcurrentHashMap<>();

    // packet driven entity visibility
    private final Object ENTITY_TRACKER_SYNC = new Object();
    private final Map<UUID, Set<Integer>> entities_tracked_by_player = new ConcurrentHashMap<>();

    // snapshot of player mapped to location
    private Map<UUID, Location> location_snapshot = new ConcurrentHashMap<>();

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

        // tracker for what entities a player has rendered
        PacketAdapter.AdapterParameteters pa_param = new PacketAdapter.AdapterParameteters();
        pa_param.plugin(RPGCore.inst());
        pa_param.options(ListenerOptions.SYNC);
        pa_param.types(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        pa_param.types(PacketType.Play.Server.ENTITY_DESTROY);
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(pa_param) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
                    // inform about entities being added
                    int id = event.getPacket().getIntegers().read(0);
                    synchronized (ENTITY_TRACKER_SYNC) {
                        entities_tracked_by_player.computeIfAbsent(event.getPlayer().getUniqueId(), (k -> new HashSet<>())).add(id);
                    }
                } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_DESTROY) {
                    // inform about entities being removed
                    List<Integer> ids = event.getPacket().getIntLists().read(0);
                    synchronized (ENTITY_TRACKER_SYNC) {
                        for (Integer id : ids) {
                            entities_tracked_by_player.computeIfAbsent(event.getPlayer().getUniqueId(), (k -> new HashSet<>())).remove(id);
                        }
                    }
                }
            }
        });

        // a best-effort at finding players who may observe a chunk
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            if (!RPGCore.inst().isEnabled()) {
                return;
            }

            // snapshot location of entities for thread-safe access
            Map<UUID, Location> location_snapshot = new ConcurrentHashMap<>();
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    location_snapshot.put(entity.getUniqueId(), entity.getLocation());
                }
            }
            this.location_snapshot = location_snapshot;

            // snapshot player locations
            Map<Player, ChunkIdentifier> chunk_snapshot = new HashMap<>();
            Bukkit.getOnlinePlayers().forEach(player -> {
                chunk_snapshot.put(player, new ChunkIdentifier(player.getLocation()));
            });


            // run async to reduce performance impact
            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                if (!RPGCore.inst().isEnabled()) {
                    return;
                }

                // compute which player is observing what chunk
                Map<ChunkIdentifier, List<Player>> computed = new HashMap<>();
                chunk_snapshot.forEach((player, where) -> {
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

                    players_observing_chunk = Collections.unmodifiableMap(computed);
                });
            });
        }, 1, 1);

        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    /**
     * Retrieve last known location of a player.<br>
     * <br>
     * Never modify this return value.
     *
     * @param owner Whose last location to check
     * @return Last location known by, may be null
     */
    public Location getLastLocation(UUID owner) {
        return this.location_snapshot.get(owner);
    }

    /**
     * Retrieve last known location of a player.<br>
     * <br>
     * Never modify this return value.
     *
     * @return Snapshot of all entity locations
     */
    public Map<UUID, Location> getLastLocation() {
        return this.location_snapshot;
    }

    /**
     * Retrieve entityIds observed by the given player.
     *
     * @param player Whose observation to check
     * @return Ids of the entity
     */
    public Set<Integer> getObservedBy(Player player) {
        return this.entities_tracked_by_player.getOrDefault(player.getUniqueId(), Collections.emptySet());
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
        return registered_entities;
    }

    /**
     * Register the entity to the entity mapping, a finished entity is
     * expected to be provided
     */
    public void register(UUID uuid, CoreEntity entity) {
        this.registered_entities.put(uuid, entity);
    }

    /**
     * Retrieve the entity which has a certain UUID, this method
     * will not create the core entity.
     *
     * @param uuid which entity to retrieve
     * @return entity to retrieve
     */
    public CoreEntity getEntity(UUID uuid) {
        CoreEntity found = null;

        if (uuid != null) {
            found = this.registered_entities.get(uuid);
            if (found == null) {
                uuid = RPGCore.inst().getBBModelManager().getOwnerOfHitbox(uuid);
                if (uuid != null) {
                    found = this.registered_entities.get(uuid);
                }
            }
        }

        return found;
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
        return this.registered_entities.get(entity.getUniqueId());
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
        CoreEntity removed = this.registered_entities.remove(entity);
        if (removed != null) {
            removed.remove();
        }
    }

    /**
     * Request to drop all entities from the core.
     */
    public void unregisterAll() {
        for (CoreEntity entity : this.registered_entities.values())
            entity.remove();
        this.registered_entities.clear();
    }

    /**
     * Provides a snapshot on entities which are within observation distance
     * of the given location.
     *
     * @param where the location which we want to be observed.
     * @return the players which are observed.
     */
    public List<Player> getObserving(Location where) {
        return players_observing_chunk.getOrDefault(new ChunkIdentifier(where), Collections.emptyList());
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

    @EventHandler(priority = EventPriority.MONITOR)
    void on(PlayerRespawnEvent event) {
        synchronized (ENTITY_TRACKER_SYNC) {
            this.entities_tracked_by_player.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void on(PlayerChangedWorldEvent event) {
        synchronized (ENTITY_TRACKER_SYNC) {
            this.entities_tracked_by_player.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void on(PlayerQuitEvent event) {
        synchronized (ENTITY_TRACKER_SYNC) {
            this.entities_tracked_by_player.remove(event.getPlayer().getUniqueId());
        }
    }
}
