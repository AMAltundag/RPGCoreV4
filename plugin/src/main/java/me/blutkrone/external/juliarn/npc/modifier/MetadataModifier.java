package me.blutkrone.external.juliarn.npc.modifier;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileSkin;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileStatus;
import org.bukkit.entity.Pose;
import org.jetbrains.annotations.NotNull;

/**
 * A modifier for modifying the metadata of a player.
 */
public class MetadataModifier extends AbstractNPCModifier {

    private final AbstractPlayerNPC npc;

    /**
     * A modifier for modifying the metadata of a player.
     *
     * @param npc The npc this modifier is for.
     */
    public MetadataModifier(@NotNull AbstractPlayerNPC npc) {
        this.npc = npc;
    }

    /**
     * Establish a new mask on the status.
     *
     * @param status which status flags to use.
     * @return this instance, for chaining
     */
    public MetadataModifier status(VolatileStatus... status) {
        return (MetadataModifier) super.queue(this.npc.packet().status(status));
    }

    /**
     * Change the pose utilized.
     *
     * @param pose which pose to use.
     * @return this instance, for chaining
     */
    public MetadataModifier pose(Pose pose) {
        return (MetadataModifier) super.queue(this.npc.packet().pose(pose));
    }

    /**
     * Change the parts of the skin.
     *
     * @param skin which parts of the skin to use.
     * @return this instance, for chaining
     */
    public MetadataModifier skin(VolatileSkin... skin) {
        return (MetadataModifier) super.queue(this.npc.packet().skin(skin));
    }
}
