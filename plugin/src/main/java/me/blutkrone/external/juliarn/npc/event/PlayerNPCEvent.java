package me.blutkrone.external.juliarn.npc.event;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import me.blutkrone.external.juliarn.npc.modifier.AbstractNPCModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

/**
 * Represents an event fired when an action between a player and a npc occurs.
 */
public abstract class PlayerNPCEvent extends PlayerEvent {

    // npc we are dealing with
    private final AbstractPlayerNPC npc;

    /**
     * Constructs a new event instance.
     *
     * @param who The player involved in this event
     * @param npc The npc involved in this event
     */
    PlayerNPCEvent(Player who, AbstractPlayerNPC npc) {
        super(who);
        this.npc = npc;
    }

    /**
     * Dispatch the given modifiers to the NPC.
     *
     * @param modifiers modifications to do on this NPC
     */
    public void dispatch(AbstractNPCModifier... modifiers) {
        for (AbstractNPCModifier modifier : modifiers) {
            modifier.flush(super.getPlayer());
        }
    }

    /**
     * Which NPC is involved in the event.
     *
     * @return The npc involved in this event.
     */
    public AbstractPlayerNPC getNPC() {
        return this.npc;
    }
}
