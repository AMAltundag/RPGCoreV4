package me.blutkrone.external.juliarn.npc;

import me.blutkrone.external.juliarn.npc.event.PlayerNPCHideEvent;
import me.blutkrone.external.juliarn.npc.event.PlayerNPCShowEvent;
import me.blutkrone.external.juliarn.npc.modifier.*;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.nms.api.packet.handle.IHologram;
import me.blutkrone.rpgcore.nms.api.packet.handle.IPlayerNPC;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileGameProfile;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileInfoAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A fake player serving as a NPC.
 */
public abstract class AbstractPlayerNPC {

    // pool that the NPC is registered to
    private NPCPool pool;
    // players who are seeing an NPC
    private Collection<Player> seeing = new CopyOnWriteArraySet<>();
    // packet pipeline backing the NPC
    private IPlayerNPC packet;
    // spawn location of the npc
    private Location location;
    // hologram for custom names
    private IHologram hologram;
    // game profile we want to utilize
    private VolatileGameProfile profile;

    /**
     * A fake player serving as a NPC.
     *
     * @param pool     The pool we are initialized by.
     * @param location The location of the npc.
     */
    public AbstractPlayerNPC(NPCPool pool, Location location, VolatileGameProfile profile) {
        this.pool = pool;
        this.location = location;
        this.profile = profile;
        this.hologram = RPGCore.inst().getVolatileManager().getPackets().hologram();
        this.packet = RPGCore.inst().getVolatileManager().getPackets().npc(profile.getId());
    }

    /**
     * Check if a certain player is allowed to see this NPC.
     *
     * @param player who are we updating for.
     * @return true if NPC can be seen
     */
    protected abstract boolean canSee(Player player);

    /**
     * Packet pipeline invoked upon NPC turning visible, this is
     * invoked AFTER the base packets were fired.
     *
     * @param player who are we updating for.
     */
    public abstract void prepare(Player player);

    /**
     * Ticking process of the NPC, we are only ticked if the player
     * can actually see the NPC.
     *
     * @param player who are we updating for.
     */
    public abstract void tick(Player player);

    /**
     * A right click interaction was performed against an NPC.
     *
     * @param player who are we updating for.
     */
    public abstract void interact(Player player);

    //
    // ===
    //

    /**
     * Grab the profile backing the NPC
     *
     * @return profile for this NPC
     */
    public VolatileGameProfile profile() {
        return profile;
    }

    /**
     * The pool the NPC is registered to.
     *
     * @return pool we are registered to.
     */
    public NPCPool pool() {
        return pool;
    }

    /**
     * Packet pipeline backing up the NPC.
     *
     * @return packet pipeline we are backed by.
     */
    public IPlayerNPC packet() {
        return packet;
    }

    /**
     * Entity ID of the backing NPC
     *
     * @return the entity id of this npc.
     */
    public int id() {
        return this.packet().getEntityId();
    }

    /**
     * Spawn position of NPC, this is immutable and refers the
     * primary location.
     *
     * @return spawn position of the NPC.
     */
    public Location location() {
        return this.location.clone();
    }

    /**
     * All players rendered the NPC
     *
     * @return players that rendered the NPC
     */
    public Collection<Player> watching() {
        return Collections.unmodifiableCollection(this.seeing);
    }

    /**
     * The hologram intended for the name of the NPC.
     *
     * @return NPC hologram.
     */
    public IHologram hologram() {
        return hologram;
    }

    //
    // ===
    //

    /**
     * Ticking method handling general logic.
     *
     * @param player who are we updating for.
     */
    public void update(Player player) {
        Location npc_position = this.location();

        if (npc_position.getWorld() != player.getWorld()) {
            // hide because out of world
            if (this.isWatching(player)) {
                this.hide(player, PlayerNPCHideEvent.Reason.SPAWN_DISTANCE);
            }
        } else if (!npc_position.getWorld().isChunkLoaded(npc_position.getBlockX() >> 4, npc_position.getBlockZ() >> 4)) {
            // hide because chunk unloaded
            if (this.isWatching(player)) {
                this.hide(player, PlayerNPCHideEvent.Reason.UNLOADED_CHUNK);
            }
        } else {
            double distance = npc_position.distanceSquared(player.getLocation());
            boolean inRange = distance <= this.pool.getSpawnDistance();

            if ((this.isHiddenFrom(player) || !inRange) && this.isWatching(player)) {
                // hide because out of range
                this.hide(player, PlayerNPCHideEvent.Reason.SPAWN_DISTANCE);
            } else if ((!this.isHiddenFrom(player) && inRange) && !this.isWatching(player)) {
                // show because entered range
                this.show(player);
            }

            // stare at target while within range
            if (this.isWatching(player) && distance <= this.pool.getActionDistance()) {
                this.tick(player);
                // this.rotation().target(player_position).flush(player);
            }
        }
    }

    /**
     * Shows this npc to a player, a negative tabList duration will
     * never remove the NPC from the tab-list.
     *
     * @param player The player to show this npc to.
     */
    public void show(Player player) {
        this.seeing.add(player);

        this.visibility().action(VolatileInfoAction.ADD_PLAYER).flush(player);

        Bukkit.getScheduler().runTaskLater(this.pool.getPlugin(), () -> {
            this.visibility().spawn().flush(player);
            this.prepare(player);

            if (this.pool.getTabListRemoveTicks() >= 0) {
                Bukkit.getScheduler().runTaskLater(this.pool.getPlugin(), () -> {
                    this.visibility().action(VolatileInfoAction.REMOVE_PLAYER).flush(player);
                }, this.pool.getTabListRemoveTicks());
            }

            Bukkit.getPluginManager().callEvent(new PlayerNPCShowEvent(player, this));
        }, 10L);
    }

    /**
     * Hides this npc from a player.
     *
     * @param player The player to hide the npc for.
     * @param reason The reason why the npc was hidden for the player.
     */
    public void hide(Player player, PlayerNPCHideEvent.Reason reason) {
        if (reason != PlayerNPCHideEvent.Reason.QUIT) {
            this.visibility().action(VolatileInfoAction.REMOVE_PLAYER).destroy().flush(player);
            this.hologram.destroy(player);
            this.seeing.remove(player);
            Bukkit.getScheduler().runTask(this.pool.getPlugin(), () -> {
                Bukkit.getPluginManager().callEvent(new PlayerNPCHideEvent(player, this, reason));
            });
        } else {
            this.seeing.remove(player);
        }
    }

    /**
     * Check if the given player rendered the NPC.
     *
     * @param player The player to check.
     * @return player has rendered NPC
     */
    public boolean isWatching(Player player) {
        return this.seeing.contains(player);
    }

    /**
     * Check if a player is allowed to render an NPC.
     *
     * @param player The player to check.
     * @return player is allowed to render NPC.
     */
    public boolean isHiddenFrom(Player player) {
        // admins always should see NPCs
        if (player.hasPermission("rpg.admin")) {
            return false;
        }
        // check if we are allowed to see
        return !canSee(player);
    }

    // ==================================
    // MODIFIERS SERVING AS PACKET BRIDGE
    // ==================================

    /**
     * Modifier that can manipulate animation
     *
     * @return modifier that can manipulate the NPC
     */
    public AnimationModifier animation() {
        return new AnimationModifier(this);
    }

    /**
     * Modifier to manipulate rotation
     *
     * @return modifier that can manipulate the NPC
     */
    public RotationModifier rotation() {
        return new RotationModifier(this);
    }

    /**
     * Modifier to manipulate equipment
     *
     * @return modifier that can manipulate the NPC
     */
    public EquipmentModifier equipment() {
        return new EquipmentModifier(this);
    }

    /**
     * Modifier to manipulate NPC metadata
     *
     * @return modifier that can manipulate the NPC
     */
    public MetadataModifier metadata() {
        return new MetadataModifier(this);
    }

    /**
     * Modifier to handle spawning/destroying
     *
     * @return modifier that can manipulate the NPC
     */
    public VisibilityModifier visibility() {
        return new VisibilityModifier(this);
    }

    /**
     * Modifier to handle teleportation
     *
     * @return modifier that can manipulate the NPC
     */
    public TeleportModifier teleport() {
        return new TeleportModifier(this);
    }

}
