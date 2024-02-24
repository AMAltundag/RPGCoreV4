package me.blutkrone.rpgcore.bbmodel.io.deserialized;

import me.blutkrone.rpgcore.bbmodel.io.serialized.BBModel;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.IOException;
import java.util.*;

public class Model {

    // unique identifier for model
    public String id;
    // bones are used for animation handling
    public Bone root_bone;
    // animations establish how to move bones
    public Map<String, Animation> animations = new LinkedHashMap<>();
    // basic hitboxes for the model
    public List<BoundingBox> hitboxes = new ArrayList<>();

    public Model(String model_id, BBModel bbmodel) {
        this.id = model_id;
        this.root_bone = new Bone(null, bbmodel.geometry);
        this.root_bone.normalize();
        for (BoundingBox hitbox : bbmodel.hitboxes) {
            this.hitboxes.add(new BoundingBox(
                    hitbox.getMinX()/16f,
                    hitbox.getMinY()/16f,
                    hitbox.getMinZ()/16f,
                    hitbox.getMaxX()/16f,
                    hitbox.getMaxY()/16f,
                    hitbox.getMaxZ()/16f
            ));
        }

        bbmodel.animations.forEach((id, animation) -> {
            this.animations.put(id, new Animation(animation));
        });
    }

    public Model(String model_id, BukkitObjectInputStream bois) throws IOException {
        this.id = model_id;
        this.root_bone = new Bone(null, bois);
        this.animations = new HashMap<>();
        int size = bois.readInt();
        for (int i = 0; i < size; i++) {
            this.animations.put(bois.readUTF(), new Animation(bois));
        }
        size = bois.readInt();
        for (int i = 0; i < size; i++) {
            double x1 = bois.readDouble();
            double y1 = bois.readDouble();
            double z1 = bois.readDouble();
            double x2 = bois.readDouble();
            double y2 = bois.readDouble();
            double z2 = bois.readDouble();
            this.hitboxes.add(new BoundingBox(x1, y1, z1, x2, y2, z2));
        }
    }

    /**
     * Serialize into byte array structure for later serialization.
     *
     * @param boos
     * @throws IOException
     */
    public void dump(BukkitObjectOutputStream boos) throws IOException {
        root_bone.dump(boos);
        boos.writeInt(animations.size());
        for (Map.Entry<String, Animation> entry : animations.entrySet()) {
            boos.writeUTF(entry.getKey());
            entry.getValue().dump(boos);
        }
        boos.writeInt(hitboxes.size());
        for (BoundingBox hitbox : hitboxes) {
            boos.writeDouble(hitbox.getMinX());
            boos.writeDouble(hitbox.getMinY());
            boos.writeDouble(hitbox.getMinZ());
            boos.writeDouble(hitbox.getMaxX());
            boos.writeDouble(hitbox.getMaxY());
            boos.writeDouble(hitbox.getMaxZ());
        }
    }
}
