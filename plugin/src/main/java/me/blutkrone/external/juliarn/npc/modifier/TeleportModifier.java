package me.blutkrone.external.juliarn.npc.modifier;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * A teleport modifier for a player
 */
public class TeleportModifier extends AbstractNPCModifier {

    private final AbstractPlayerNPC npc;

    /**
     * A teleport modifier for a player
     *
     * @param npc The npc this modifier is for.
     */
    public TeleportModifier(@NotNull AbstractPlayerNPC npc) {
        this.npc = npc;
    }

    /**
     * Teleport to the given location.
     *
     * @param where    where to teleport
     * @param grounded target is grounded
     * @return this instance, for chaining.
     */
    public TeleportModifier teleport(Location where, boolean grounded) {
        return (TeleportModifier) super.queue(this.npc.packet().teleport(where, grounded));
    }
}
