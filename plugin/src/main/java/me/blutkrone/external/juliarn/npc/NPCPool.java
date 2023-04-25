package me.blutkrone.external.juliarn.npc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import com.google.common.base.Preconditions;
import me.blutkrone.external.juliarn.npc.event.PlayerNPCHideEvent;
import me.blutkrone.external.juliarn.npc.event.PlayerNPCInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the main management point for {@link AbstractPlayerNPC}s.
 */
public class NPCPool implements Listener {

    private final Plugin plugin;

    private final double spawnDistance;
    private final double actionDistance;
    private final int tabListRemoveTicks;

    private final Map<Integer, AbstractPlayerNPC> registered = new ConcurrentHashMap<>();

    /**
     * Creates a new NPC pool which handles events, spawning and destruction of the NPCs for players.
     * Please use {@link #builder(Plugin)} instead, this constructor will be private in a further
     * release.
     *
     * @param plugin             the instance of the plugin which creates this pool
     * @param spawnDistance      the distance in which NPCs are spawned for players
     * @param actionDistance     the distance in which NPC actions are displayed for players
     * @param tabListRemoveTicks the time in ticks after which the NPC will be removed from the
     *                           players tab
     */
    private NPCPool(@NotNull Plugin plugin, int spawnDistance, int actionDistance, int tabListRemoveTicks) {
        this.plugin = plugin;
        this.spawnDistance = Math.min(spawnDistance * spawnDistance, Math.pow(Bukkit.getViewDistance() << 4, 2));
        this.actionDistance = actionDistance * actionDistance;
        this.tabListRemoveTicks = tabListRemoveTicks;
        // handle events relating to NPCs
        Bukkit.getPluginManager().registerEvents(this, plugin);
        // handle ticking behaviour of NPCs
        Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            Collection<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.tick(players));
        }, 20, 2);
        // handle packet interactions with NPCs
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.plugin, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer container = event.getPacket();
                int targetId = container.getIntegers().read(0);
                if (NPCPool.this.registered.containsKey(targetId)) {
                    AbstractPlayerNPC npc = NPCPool.this.registered.get(targetId);
                    WrappedEnumEntityUseAction useAction = container.getEnumEntityUseActions().read(0);

                    PlayerNPCInteractEvent.EntityUseAction[] actions = PlayerNPCInteractEvent.EntityUseAction.values();
                    PlayerNPCInteractEvent.EntityUseAction action = actions[useAction.getAction().ordinal()];
                    EquipmentSlot slot = EquipmentSlot.HAND;
                    if (action != PlayerNPCInteractEvent.EntityUseAction.ATTACK) {
                        if (useAction.getHand() == EnumWrappers.Hand.OFF_HAND) {
                            slot = EquipmentSlot.OFF_HAND;
                        }
                    }

                    PlayerNPCInteractEvent bukkit_event = new PlayerNPCInteractEvent(event.getPlayer(), npc, action, slot);
                    Bukkit.getScheduler().runTask(NPCPool.this.plugin, () -> Bukkit.getPluginManager().callEvent(bukkit_event));
                }
            }
        });
    }

    /**
     * Creates a new builder for a npc pool.
     *
     * @param plugin the instance of the plugin which creates the builder for the pool.
     * @return a new builder for creating a npc pool instance.
     * @since 2.5-SNAPSHOT
     */
    public static Builder builder(@NotNull Plugin plugin) {
        return new Builder(plugin);
    }

    public double getSpawnDistance() {
        return spawnDistance;
    }

    public double getActionDistance() {
        return actionDistance;
    }

    public int getTabListRemoveTicks() {
        return tabListRemoveTicks;
    }

    /**
     * Plugin which created the pool.
     *
     * @return the plugin we are working with.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Fetches the npc associated with that entity ID
     *
     * @param id the entity id of the npc to get.
     * @return The NPC associated with the ID
     */
    public AbstractPlayerNPC get(int id) {
        return this.registered.get(id);
    }

    /**
     * Removes the npc associated with that entity ID
     *
     * @param id the entity id of the npc to get.
     */
    public void remove(int id) {
        AbstractPlayerNPC npc = this.get(id);
        if (npc != null) {
            this.registered.remove(id);
            for (Player player : npc.watching()) {
                npc.hide(player, PlayerNPCHideEvent.Reason.REMOVED);
            }
        }
    }

    /**
     * Register a new NPC to the pool.
     *
     * @param npc the NPC that we want registered.
     */
    public void register(AbstractPlayerNPC npc) {
        this.registered.put(npc.id(), npc);
    }

    /**
     * Get an unmodifiable copy of all NPCs handled by this pool.
     *
     * @return a copy of the NPCs this pool manages.
     */
    public Collection<AbstractPlayerNPC> getAll() {
        return Collections.unmodifiableCollection(this.registered.values());
    }

    /*
     * Process NPC ticking.
     */
    private void tick(Collection<Player> players) {
        for (Player player : players) {
            for (AbstractPlayerNPC npc : this.registered.values()) {
                npc.update(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void handleRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        this.registered.forEach((id, npc) -> {
            if (npc.isWatching(player)) {
                npc.hide(player, PlayerNPCHideEvent.Reason.RESPAWNED);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void handleQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.registered.forEach((id, npc) -> {
            if (npc.isWatching(player)) {
                npc.hide(player, PlayerNPCHideEvent.Reason.QUIT);
            }
        });
    }

    /**
     * Represents the main management point for NPCs
     */
    public static class Builder {

        private final Plugin plugin;
        private int spawnDistance = 50;
        private int actionDistance = 20;
        private int tabListRemoveTicks = 30;

        /**
         * Represents the main management point for NPCs
         *
         * @param plugin plugin we are owned by.
         */
        private Builder(@NotNull Plugin plugin) {
            this.plugin = plugin;
        }

        /**
         * Distance to spawn NPCs in.
         *
         * @param spawnDistance spawn distance.
         * @return The same instance of this class, for chaining.
         */
        public Builder spawnDistance(int spawnDistance) {
            Preconditions.checkArgument(spawnDistance > 0, "Spawn distance must be more than 0");
            this.spawnDistance = spawnDistance;
            return this;
        }

        /**
         * Distance to inform about NPC actions.
         *
         * @param actionDistance action distance
         * @return The same instance of this class, for chaining.
         */
        public Builder actionDistance(int actionDistance) {
            Preconditions.checkArgument(actionDistance > 0, "Action distance must be more than 0");
            this.actionDistance = actionDistance;
            return this;
        }

        /**
         * Ticks before hiding from the tab list.
         *
         * @param tabListRemoveTicks time before hiding
         * @return The same instance of this class, for chaining.
         */
        public Builder tabListRemoveTicks(int tabListRemoveTicks) {
            this.tabListRemoveTicks = tabListRemoveTicks;
            return this;
        }

        /**
         * Construct the pool.
         *
         * @return NPC pool.
         */
        public NPCPool build() {
            return new NPCPool(this.plugin, this.spawnDistance, this.actionDistance, this.tabListRemoveTicks);
        }
    }
}
