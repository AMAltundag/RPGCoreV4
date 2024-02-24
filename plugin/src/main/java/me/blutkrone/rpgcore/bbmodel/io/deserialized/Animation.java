package me.blutkrone.rpgcore.bbmodel.io.deserialized;

import me.blutkrone.rpgcore.bbmodel.io.deserialized.animation.Loop;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.animation.Animator;
import me.blutkrone.rpgcore.bbmodel.io.serialized.animation.BBAnimation;
import me.blutkrone.rpgcore.bbmodel.io.serialized.animation.BBAnimator;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.IOException;
import java.util.*;

public class Animation {

    // unique name of the animation
    public String name;
    // will override non-overriding animations
    public boolean override;
    // loop (repeat), hold (hold at end), once (play once and reset)
    public Loop loop;
    // complete animation over seconds
    public float length;
    // each animator handles one bone
    public List<Animator> animators = new ArrayList<>();

    public Animation(BBAnimation bb_animation) {
        this.name = bb_animation.name;
        this.override = bb_animation.override;
        this.loop = Loop.valueOf(bb_animation.loop.toUpperCase());
        this.length = bb_animation.length;
        for (BBAnimator bb_animator : bb_animation.animators) {
            this.animators.add(new Animator(bb_animator));
        }
    }

    public Animation(BukkitObjectInputStream bois) throws IOException {
        this.name = bois.readUTF();
        this.override = bois.readBoolean();
        this.loop = Loop.valueOf(bois.readUTF().toUpperCase());
        this.length = bois.readFloat();
        int size = bois.readInt();
        for (int i = 0; i < size; i++) {
            this.animators.add(new Animator(bois));
        }
    }

    /**
     * Serialize into byte array structure for later serialization.
     *
     * @param boos
     * @throws IOException
     */
    public void dump(BukkitObjectOutputStream boos) throws IOException {
        boos.writeUTF(this.name);
        boos.writeBoolean(this.override);
        boos.writeUTF(this.loop.name());
        boos.writeFloat(this.length);
        boos.writeInt(this.animators.size());
        for (Animator animator : this.animators) {
            animator.dump(boos);
        }
    }
}
