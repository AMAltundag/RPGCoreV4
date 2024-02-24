package me.blutkrone.rpgcore.bbmodel.active;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.bbmodel.active.component.AnimationActive;
import me.blutkrone.rpgcore.bbmodel.active.component.AnimationResult;
import me.blutkrone.rpgcore.bbmodel.active.component.LocationSnapshot;
import me.blutkrone.rpgcore.bbmodel.active.component.Observation;
import me.blutkrone.rpgcore.bbmodel.active.tints.ActiveTint;
import me.blutkrone.rpgcore.bbmodel.interpolation.AngleInterpolator;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.Animation;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.Model;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.animation.Animator;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.animation.Loop;
import me.blutkrone.rpgcore.bbmodel.io.serialized.purpose.BedrockEntityModel;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;
import me.blutkrone.rpgcore.nms.api.packet.grouping.IBundledPacket;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileBillboard;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileDisplay;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveModel {

    // the model which is being rendered
    private final Model model_template;
    // personal copy of a bone
    private final ActiveBone active_bone;
    // players who are observing the model
    private final Map<Player, Observation> observators;
    // active animations of the model
    private final Map<String, AnimationActive> active_animations;

    // mutable parameters of the model
    private AngleInterpolator yaw_interpolator;
    private LocationSnapshot last_known_location;
    private boolean waiting_for_death_animation;

    public ActiveModel(Model model) {
        this.model_template = model;
        this.active_bone = new ActiveBone(this.model_template.root_bone);
        this.observators = Collections.synchronizedMap(new WeakHashMap<>());
        this.active_animations = new ConcurrentHashMap<>();
    }

    /**
     * Asynchronous update handling heavy lifting.
     *
     * @param delta Ticks since last update.
     * @param size Size multiplier for model
     * @param current_location Updated location
     * @return Model can be deleted
     */
    public boolean async(int delta, float size, LocationSnapshot current_location) {
        size = size * (1f / BedrockEntityModel.EFFECTIVE_SCALE);
        if (current_location == null) {
            current_location = this.last_known_location;
        }

        // disregard no longer relevant observers
        this.observators.entrySet().removeIf(entry -> entry.getValue() == null || !entry.getKey().isValid());

        if (!this.observators.isEmpty()) {
            // interpolate rotation angle
            if (this.yaw_interpolator == null) {
                this.yaw_interpolator = new AngleInterpolator(new float[] { current_location.yaw });
            }
            this.yaw_interpolator.update(new float[] { current_location.yaw });
            float yaw = this.yaw_interpolator.interpolate(delta, 4.5f, 0.05f)[0];

            // prepare base transformation matrix
            final Matrix4f matrix = new Matrix4f();
            matrix.rotateY((float) Math.toRadians(yaw * -1f));
            matrix.scale(size);

            Bukkit.getLogger().severe("ROTATION: " + yaw);

            // update animations, recompute matrices
            this.animate(delta);
            this.updateMatrix(this.active_bone, matrix, delta);

            // compute entity level motion
            double[] teleport = null;
            float[] movement = null;
            if (this.last_known_location != null && this.last_known_location.distSq(current_location) > BBUtil.EPSILON) {
                float dX = (float) (current_location.x - this.last_known_location.x);
                float dY = (float) (current_location.y - this.last_known_location.y);
                float dZ = (float) (current_location.z - this.last_known_location.z);
                if (Math.abs(dX) < 8f && Math.abs(dY) <= 8f && Math.abs(dZ) <= 8f) {
                    movement = new float[] { dX, dY, dZ };
                } else {
                    teleport = new double[] { current_location.x, current_location.y, current_location.z };
                }
            }

            // dispatch the packets and clean up
            for (ActiveBone bone : this.active_bone) {
                ItemStack item_for_update = bone.getItemUpdate(delta);
                ItemStack item_for_show = bone.getItemLast();

                // ignore items that aren't visible
                if (item_for_show == null) {
                    continue;
                }

                // compute packets used for 'show'
                IBundledPacket bundled_show = RPGCore.inst().getVolatileManager().getPackets().bundle();
                bundled_show.takeFromOther(bone.display().spawn(current_location.x, current_location.y, current_location.z));
                bundled_show.takeFromOther(bone.display().transform(delta, BBUtil.Matrix.transformFromMatrix(bone.transformation)));
                bundled_show.takeFromOther(bone.display().item(item_for_show, VolatileBillboard.FIXED, VolatileDisplay.FIXED));
                // compute packets used for 'hide'
                IBundledPacket bundled_hide = RPGCore.inst().getVolatileManager().getPackets().bundle();
                bundled_hide.takeFromOther(bone.display().destroy());
                // compute packets used for 'update'
                IBundledPacket bundled_update = RPGCore.inst().getVolatileManager().getPackets().bundle();
                bundled_update.takeFromOther(bone.display().transform(delta, BBUtil.Matrix.transformFromMatrix(bone.transformation)));
                if (movement != null) {
                    bundled_update.takeFromOther(bone.display().move(movement[0], movement[1], movement[2]));
                } else if (teleport != null) {
                    bundled_update.takeFromOther(bone.display().teleport(teleport[0], teleport[1], teleport[2]));
                }
                if (item_for_update != null) {
                    bundled_update.takeFromOther(bone.display().item(item_for_update, VolatileBillboard.FIXED, VolatileDisplay.FIXED));
                }

                // dispatch the packets to relevant players
                for (Map.Entry<Player, Observation> observator : this.observators.entrySet()) {
                    if (observator.getValue() == Observation.SHOW && bone.handle.visible) {
                        bundled_show.dispatch(observator.getKey());
                    } else if (observator.getValue() == Observation.HIDE) {
                        bundled_hide.dispatch(observator.getKey());
                    } else if (observator.getValue() == Observation.UPDATE) {
                        bundled_update.dispatch(observator.getKey());
                    }
                }
            }

            // shift observers to their next state
            for (Map.Entry<Player, Observation> entry : this.observators.entrySet()) {
                if (entry.getValue() == Observation.SHOW) {
                    entry.setValue(Observation.UPDATE);
                } else if (entry.getValue() == Observation.HIDE) {
                    entry.setValue(null);
                }
            }
        }

        // update location snapshot (for relative move)
        this.last_known_location = current_location;

        // if death animation finished, terminate
        if (this.waiting_for_death_animation) {
            AnimationActive death_animation = this.active_animations.get("death");
            Animation death_template = this.model_template.animations.get("death");
            if (death_animation == null || death_template == null) {
                return true;
            } else {
                return death_animation.progress >= death_template.length;
            }
        }

        // retain the model we've created
        return false;
    }

    /**
     * The root bone representing this entity, all other bones are
     * normalized to be relative to their hierarchy.
     *
     * @return Bone.
     */
    public ActiveBone getBone() {
        return this.active_bone;
    }

    /**
     * The template this active model instance is based off.
     *
     * @return The template we are based off.
     */
    public Model getTemplate() {
        return model_template;
    }

    /**
     * Force an animation to loop, even if it isn't meant to
     * be looped.
     *
     * @param animation Animation to be played
     * @param speed Playback speed of animation
     */
    public void forcePlay(String animation, float speed) {
        AnimationActive active = this.active_animations.computeIfAbsent(animation, (k -> new AnimationActive()));
        active.active = true;
        active.speed = 0.05f * speed;
        active.weight = 1f;
        active.force_loop = true;
    }

    /**
     * Start playing an animation.
     *
     * @param animation Animation to be played
     * @param speed Playback speed of animation
     */
    public void play(String animation, float speed) {
        AnimationActive active = this.active_animations.computeIfAbsent(animation, (k -> new AnimationActive()));
        active.active = true;
        active.speed = 0.05f * speed;
        active.weight = 1f;
        active.force_loop = false;
    }

    /**
     * Tint a bone in a certain color, do note that the
     * tint is inherited across the hierarchy.
     *
     * @param bone Bone to tint
     * @param id Tint ID
     * @param tint Tint to apply
     */
    public void tint(String bone, String id, ActiveTint tint) {
        ActiveBone active_bone = getBone().getBone(bone);
        if (active_bone != null) {
            active_bone.tint(id, tint);
        }
    }

    /**
     * Check if we are playing a certain animation still.
     *
     * @param animation The animation to check
     * @return Animation is playing
     */
    public boolean playing(String animation) {
        AnimationActive active = this.active_animations.computeIfAbsent(animation, (k -> new AnimationActive()));
        return active.active;
    }

    /**
     * Start playing an animation, do note that the 'weight' is internally
     * interpolated
     *
     * @param animation Animation to be played
     * @param speed Playback speed of animation
     * @param weight Multiplier to animation intensity
     */
    public void play(String animation, float speed, float weight) {
        AnimationActive active = this.active_animations.computeIfAbsent(animation, (k -> new AnimationActive()));
        active.active = true;
        active.speed = 0.05f * speed;
        active.weight = weight;
        active.fade = false;
    }

    /**
     * Stop playing an animation.
     *
     * @param animation Animation to be played
     */
    public void stop(String animation) {
        AnimationActive active = this.active_animations.computeIfAbsent(animation, (k -> new AnimationActive()));
        active.active = false;
    }

    /**
     * Stop playing an animation.
     *
     * @param animation Animation to be played
     */
    public void fade(String animation) {
        AnimationActive active = this.active_animations.computeIfAbsent(animation, (k -> new AnimationActive()));
        active.fade = true;
    }

    /**
     * Check if the model is being observed by the given player.
     *
     * @param player Who to check.
     * @return Model is observed?
     */
    public boolean isObserved(Player player) {
        Observation status = this.observators.get(player);
        return status == Observation.SHOW || status == Observation.UPDATE;
    }

    /**
     * A player wishes to observe this entity.
     *
     * @param player Player to process.
     */
    public void observe(Player player) {
        this.observators.put(player, Observation.SHOW);
    }

    /**
     * A player wishes to observe this entity.
     *
     * @param player Player to process.
     */
    public void unobserve(Player player) {
        this.observators.put(player, Observation.HIDE);
    }

    /**
     * Clean up the model
     */
    public void recycle() {
        for (ActiveBone bone : this.active_bone) {
            bone.display().destroy().dispatch(this.observators.keySet());
        }
        this.observators.clear();
    }

    /**
     * Perform a natural death, iE allow no other animations but
     * the death once and remove once done.
     */
    public void dieNaturally() {
        this.waiting_for_death_animation = true;
        this.play("death", 1f);
    }

    /*
     * Update all active animations, offer up the cumulative modification
     * that respects hierarchy.
     *
     * @param delta How many ticks since last update
     */
    private void animate(int delta) {
        // flush outdated animation data
        for (ActiveBone bone : this.active_bone) {
            bone.animation = new AnimationResult();
        }

        // if dying, allow no new animations
        if (this.waiting_for_death_animation) {
            this.active_animations.keySet().removeIf(a -> !a.equals("death"));
        }

        // update with new animation data
        this.active_animations.forEach((id, active_animation) -> {
            Animation animation = this.model_template.animations.get(id);
            if (animation == null) {
                // invalid animation, mark as inactive
                active_animation.active = false;
            } else if (active_animation.active) {
                // handle cycling of animations
                if (animation.loop == Loop.LOOP || active_animation.force_loop) {
                    if (active_animation.progress > animation.length) {
                        active_animation.progress = 0f;
                        if (active_animation.fade) {
                            active_animation.active = false;
                        }
                    }
                } else if (animation.loop == Loop.HOLD) {
                    if (active_animation.progress > animation.length) {
                        active_animation.progress = animation.length;
                        if (active_animation.fade) {
                            active_animation.active = false;
                        }
                    }
                } else if (animation.loop == Loop.ONCE) {
                    if (active_animation.progress > animation.length) {
                        active_animation.progress = 0f;
                        active_animation.active = false;
                    }
                }

                // while fading, reduce animation weight
                if (active_animation.fade) {
                    active_animation.weight -= 0.2f;
                }

                // apply keyframe state of each animator
                if (active_animation.weight > 0f) {
                    for (Animator animator : animation.animators) {
                        ActiveBone bone = this.active_bone.getBone(animator.getBoneToAnimate());
                        animator.applyTo(bone.animation, active_animation.progress, active_animation.weight);
                    }
                } else {
                    active_animation.active = false;
                }

                // increment progress of animation
                active_animation.progress += active_animation.speed * delta;
            }
        });
    }

    /*
     * Recompute the transformation matrix in a recursive fashion, a
     * secondary draw-call will be performed by the calling method.
     *
     * @param current The bone we are currently at
     * @param parent Matrix inherited from parent
     * @param delta Ticks since last update
     */
    private void updateMatrix(ActiveBone current, Matrix4f parent, int delta) {
        final float[] position = current.getPosition(delta);
        final float[] rotation = current.getRotation(delta);

        // compute transformation matrix
        Matrix4f transform = BBUtil.Matrix.transform(position, rotation, 1f);
        current.transformation = new Matrix4f(parent).mul(transform);
        // compute snapshot of bone location
        Vector4f vector = new Vector4f(0f, 0f, 0f, 1f).mul(current.transformation);
        current.last_known_position = new float[] { vector.x, vector.y, vector.z };
        // process linked children of element
        for (ActiveBone child : current.getChildren().values()) {
            updateMatrix(child, new Matrix4f(current.transformation), delta);
        }
    }
}