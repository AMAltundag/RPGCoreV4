package me.blutkrone.rpgcore.util;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility to assist with animations in a chest-gui
 */
public class MenuAnimator {

    private List<Animation> animations = new ArrayList<>();
    private boolean empty_before = false;

    /**
     * Queue an animation, the animation will NOT loop.
     *
     * @param animation which animation to query
     * @param offset horizontal offset
     */
    public void queue(String animation, int offset) {
        this.animations.add(new Animation(animation, offset));
    }

    /**
     * Merge our animation into the message.
     *
     * @param msb what message to merge into
     * @return true if we merged anything
     */
    public boolean merge(MagicStringBuilder msb) {
        // nothing can be animated like this
        if (this.animations.isEmpty()) {
            return false;
        }
        // merge the next frame of the animation
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        animations.removeIf(anim -> {
            try {
                IndexedTexture texture = rpm.texture("animation_slot_" + anim.id + "_" + anim.frame);
                anim.frame += 1;
                msb.shiftToExact(anim.offset).append(texture);
                return false;
            } catch (NullPointerException e) {
                // if we cannot find an animation, we are done.
                return true;
            }
        });
        // notify about an update
        return true;
    }

    private class Animation {
        private String id;
        private int offset;
        private int frame;

        public Animation(String id, int offset) {
            this.id = id;
            this.offset = offset;
        }
    }
}
