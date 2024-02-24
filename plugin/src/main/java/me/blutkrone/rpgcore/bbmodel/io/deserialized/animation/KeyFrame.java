package me.blutkrone.rpgcore.bbmodel.io.deserialized.animation;

import me.blutkrone.rpgcore.bbmodel.io.serialized.animation.BBKeyFrame;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.IOException;

/**
 * A keyframe is intended to modify a model in some shape or form.
 */
public class KeyFrame {
    public float timestamp;
    public Interpolation interpolation;
    public float[] data_point;

    KeyFrame(BBKeyFrame bb_keyframe) {
        this.timestamp = bb_keyframe.timestamp;
        this.interpolation = Interpolation.valueOf(bb_keyframe.interpolation.toUpperCase());
        this.data_point = bb_keyframe.data_point.clone();
    }

    KeyFrame(BukkitObjectInputStream bois) throws IOException {
        this.timestamp = bois.readFloat();
        this.interpolation = Interpolation.valueOf(bois.readUTF());
        this.data_point = new float[]{bois.readFloat(), bois.readFloat(), bois.readFloat()};
    }

    /**
     * Serialize into byte array structure for later serialization.
     *
     * @param boos
     * @throws IOException
     */
    public void dump(BukkitObjectOutputStream boos) throws IOException {
        boos.writeFloat(this.timestamp);
        boos.writeUTF(this.interpolation.name());
        boos.writeFloat(this.data_point[0]);
        boos.writeFloat(this.data_point[1]);
        boos.writeFloat(this.data_point[2]);
    }
}
