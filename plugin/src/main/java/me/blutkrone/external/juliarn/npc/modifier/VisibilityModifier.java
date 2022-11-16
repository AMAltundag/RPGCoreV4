package me.blutkrone.external.juliarn.npc.modifier;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileInfoAction;
import org.jetbrains.annotations.NotNull;

/**
 * A modifier for modifying the visibility of a player.
 */
public class VisibilityModifier extends AbstractNPCModifier {

    private final AbstractPlayerNPC npc;

    /**
     * A modifier for modifying the visibility of a player.
     *
     * @param npc The npc this modifier is for.
     */
    public VisibilityModifier(@NotNull AbstractPlayerNPC npc) {
        this.npc = npc;
    }

    /**
     * Update the info state of an NPC
     *
     * @param action the action we want to perform
     * @return this instance, for chaining.
     */
    public VisibilityModifier action(VolatileInfoAction action) {
        return (VisibilityModifier) super.queue(this.npc.packet().info(action, npc.profile()));
    }

    /**
     * Enqueues the spawn of the wrapped npc.
     *
     * @return The same instance of this class, for chaining.
     */
    public VisibilityModifier spawn() {
        return (VisibilityModifier) super.queue(this.npc.packet().spawn(this.npc.location()));
    }

    /**
     * Enqueues the de-spawn of the wrapped npc.
     *
     * @return The same instance of this class, for chaining.
     */
    public VisibilityModifier destroy() {
        return (VisibilityModifier) super.queue(this.npc.packet().destroy());
    }
}