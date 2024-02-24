package me.blutkrone.rpgcore.bbmodel.io.deserialized.animation;

import me.blutkrone.rpgcore.bbmodel.active.component.AnimationResult;
import me.blutkrone.rpgcore.bbmodel.io.serialized.animation.BBAnimator;
import me.blutkrone.rpgcore.bbmodel.io.serialized.animation.BBKeyFrame;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * An animator that will process one bone.
 */
public class Animator {

    // the bone processed by this animator
    private final String bone_to_animate;
    // keyframes applied to their real-time stamp
    private final NavigableMap<Float, KeyFrame> timeline_position = new TreeMap<>();
    private final NavigableMap<Float, KeyFrame> timeline_rotation = new TreeMap<>();
    private final NavigableMap<Float, KeyFrame> timeline_scale = new TreeMap<>();

    /**
     * An animator that will process one bone.
     *
     * @param bb_animator
     */
    public Animator(BBAnimator bb_animator) {
        // the bone that we are animating
        this.bone_to_animate = bb_animator.geometry_to_animate;
        // load position keyframes
        for (BBKeyFrame bb_keyframe : bb_animator.kf_position) {
            KeyFrame frame = new KeyFrame(bb_keyframe);
            timeline_position.put(frame.timestamp, frame);
        }
        // load rotation keyframes
        for (BBKeyFrame bb_keyframe : bb_animator.kf_rotation) {
            KeyFrame frame = new KeyFrame(bb_keyframe);
            timeline_rotation.put(frame.timestamp, frame);
        }
        // load scale keyframes
        for (BBKeyFrame bb_keyframe : bb_animator.kf_scale) {
            KeyFrame frame = new KeyFrame(bb_keyframe);
            timeline_scale.put(frame.timestamp, frame);
        }
    }

    /**
     * An animator that will process one bone.
     *
     * @param bois
     */
    public Animator(BukkitObjectInputStream bois) throws IOException {
        // the bone that we are animating
        this.bone_to_animate = bois.readUTF();
        // load position keyframes
        int size = bois.readInt();
        for (int i = 0; i < size; i++) {
            KeyFrame frame = new KeyFrame(bois);
            timeline_position.put(frame.timestamp, frame);
        }
        // load rotation keyframes
        size = bois.readInt();
        for (int i = 0; i < size; i++) {
            KeyFrame frame = new KeyFrame(bois);
            timeline_rotation.put(frame.timestamp, frame);
        }
        // load scale keyframes
        size = bois.readInt();
        for (int i = 0; i < size; i++) {
            KeyFrame frame = new KeyFrame(bois);
            timeline_scale.put(frame.timestamp, frame);
        }
    }

    /**
     * Serialize into byte array structure for later serialization.
     *
     * @param boos
     * @throws IOException
     */
    public void dump(BukkitObjectOutputStream boos) throws IOException {
        boos.writeUTF(this.bone_to_animate);
        boos.writeInt(this.timeline_position.size());
        for (KeyFrame keyframe : this.timeline_position.values()) {
            keyframe.dump(boos);
        }
        boos.writeInt(this.timeline_rotation.size());
        for (KeyFrame keyframe : this.timeline_rotation.values()) {
            keyframe.dump(boos);
        }
        boos.writeInt(this.timeline_scale.size());
        for (KeyFrame keyframe : this.timeline_scale.values()) {
            keyframe.dump(boos);
        }
    }

    public String getBoneToAnimate() {
        return bone_to_animate;
    }

    /**
     * Apply this animation to the given data container.
     *
     * @param data     Data container
     * @param progress Progress in animation (0.0 to 1.0)
     * @param weight   Multiplier to transformation magnitude
     */
    public void applyTo(AnimationResult data, float progress, float weight) {
        // perform interpolation
        float[] position = applyTo(this.timeline_position, progress);
        float[] rotation = applyTo(this.timeline_rotation, progress);
        float[] scale = applyTo(this.timeline_scale, progress);
        // // apply magnitude
        // if (weight != 1f) {
        //     BBUtil.multiply(position, weight);
        //     BBUtil.multiply(rotation, weight);
        //     BBUtil.multiply(scale, weight);
        // }
        // sum up with data
        data.results.add(new AnimationResult.SubResult(position, rotation, scale, weight));
    }

    /*
     * Find the interpolated value for the given progress.
     *
     * @param keyframes
     * @param progress
     * @return
     */
    private float[] applyTo(NavigableMap<Float, ? extends KeyFrame> keyframes, float progress) {
        // no entries, default to 0/0/0
        if (keyframes.size() == 0) {
            return new float[3];
        }
        // check what we interpolate between
        Map.Entry<Float, ? extends KeyFrame> lower = keyframes.floorEntry(progress);
        Map.Entry<Float, ? extends KeyFrame> upper = keyframes.ceilingEntry(progress);
        // if missing one, offer the other
        if (lower == null) {
            return upper.getValue().data_point.clone();
        } else if (upper == null) {
            return lower.getValue().data_point.clone();
        } else if (upper.getValue() == lower.getValue()) {
            return upper.getValue().data_point.clone();
        }
        // compute local rate of progress
        float interpolated = (progress - lower.getKey()) / (upper.getKey() - lower.getKey());
        // offer up the interpolation
        float[] first = lower.getValue().data_point.clone();
        float[] second = upper.getValue().data_point.clone();
        float[] output = new float[3];
        for (int i = 0; i < 3; i++) {
            output[i] = lower.getValue().interpolation.with(first[i], second[i], interpolated);
        }
        return output;
    }
}
