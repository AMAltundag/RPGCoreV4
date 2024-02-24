package me.blutkrone.rpgcore.bbmodel.owner;

import me.blutkrone.rpgcore.bbmodel.active.tints.ActiveTint;
import me.blutkrone.rpgcore.bbmodel.util.exception.BBExceptionRecycled;

public interface IActiveOwner extends IModelOwner {

    /**
     * Start an animation
     *
     * @param animation The animation to play
     * @param speed     Playback rate of animation
     */
    void playAnimation(String animation, float speed) throws BBExceptionRecycled;

    /**
     * Stop an active animation.
     *
     * @param animation The animation to stop
     */
    void stopAnimation(String animation) throws BBExceptionRecycled;

    /**
     * Fade out an active animation.
     *
     * @param animation The animation to fade
     */
    void fadeAnimation(String animation) throws BBExceptionRecycled;

    /**
     * Tint a bone in a certain color, do note that the
     * tint is inherited across the hierarchy.
     *
     * @param bone Bone to tint
     * @param id   Tint ID
     * @param tint Tint to apply
     */
    void tint(String bone, String id, ActiveTint tint) throws BBExceptionRecycled;

    /**
     * Multiplier to final size of the model, with 1.0 being the
     * default size for it.
     *
     * @param size Size multiplier.
     */
    void size(float size);
}
