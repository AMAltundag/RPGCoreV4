package me.blutkrone.external.juliarn.npc.event;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * An event fired when a npc is hidden for a certain player.
 */
public class PlayerNPCHideEvent extends PlayerNPCEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    // why the NPC was hidden
    private final Reason reason;

    /**
     * Constructs a new event instance.
     *
     * @param who    The player who is no longer seeing the npc
     * @param npc    The npc the player is no longer seeing
     * @param reason The reason why the npc was hidden
     */
    public PlayerNPCHideEvent(Player who, AbstractPlayerNPC npc, Reason reason) {
        super(who, npc);
        this.reason = reason;
    }

    /**
     * Get the handlers for this event.
     *
     * @return the handlers for this event.
     */
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * @return The reason why the npc was hidden
     */
    public Reason getReason() {
        return this.reason;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Represents a reason why a npc was hidden for a player.
     */
    public enum Reason {
        /**
         * The player has manually been excluded from seeing the npc.
         */
        EXCLUDED,
        /**
         * The distance from npc and player is now higher than the configured spawn distance.
         */
        SPAWN_DISTANCE,
        /**
         * NPC was in an unloaded chunk.
         */
        UNLOADED_CHUNK,
        /**
         * The npc was removed from the pool.
         */
        REMOVED,
        /**
         * The player seeing the npc respawned.
         */
        RESPAWNED,
        /**
         * The player left the server.
         */
        QUIT
    }
}
