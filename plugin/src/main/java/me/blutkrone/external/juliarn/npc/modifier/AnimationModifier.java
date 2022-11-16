package me.blutkrone.external.juliarn.npc.modifier;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileAnimation;
import org.jetbrains.annotations.NotNull;

/**
 * A modifier for various animations a npc can play.
 */
public class AnimationModifier extends AbstractNPCModifier {

    private final AbstractPlayerNPC npc;

    /**
     * A modifier for various animations a npc can play.
     *
     * @param npc The npc this modifier is for.
     */
    public AnimationModifier(@NotNull AbstractPlayerNPC npc) {
        this.npc = npc;
    }

    /**
     * Play the given animation.
     *
     * @param animation the animation to be queried.
     * @return this instance, for chaining
     */
    public AnimationModifier queue(VolatileAnimation animation) {
        return (AnimationModifier) super.queue(this.npc.packet().animation(animation));
    }
}
