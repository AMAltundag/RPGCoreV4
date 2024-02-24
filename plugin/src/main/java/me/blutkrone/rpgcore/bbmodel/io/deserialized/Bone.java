package me.blutkrone.rpgcore.bbmodel.io.deserialized;

import me.blutkrone.rpgcore.bbmodel.io.serialized.geometry.BBGeometry;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;
import me.blutkrone.rpgcore.bbmodel.util.NestedIterator;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class Bone implements Iterable<Bone> {

    // who is the parent of this bone
    public Bone parent;
    // name of this bone
    public String name;
    // origin of this bone
    public float[] pivot;
    // rotation of this bone
    public float[] rotation;
    // whether bone is visible or not
    public boolean visible;
    // geometry size
    public float geometry_width;
    public float geometry_height;
    // all bones attached to this bone
    public Map<String, Bone> children = new HashMap<>();
    // linked item
    public ItemStack item = null;
    // bone has been normalized?
    public boolean normalized;

    public Bone(Bone parent, BBGeometry bb_geometry) {
        this.parent = parent;
        this.name = bb_geometry.name;
        this.pivot = bb_geometry.pivot.clone();
        this.rotation = bb_geometry.rotation.clone();
        this.visible = bb_geometry.visible;
        float[] geometry_size = BBUtil.sizeOf(bb_geometry.elements);
        this.geometry_width = geometry_size[0] / 16f;
        this.geometry_height = geometry_size[1] / 16f;

        bb_geometry.children.forEach((id, child) -> {
            this.children.put(id, new Bone(this, child));
        });

        if (bb_geometry.render_data != -1) {
            this.item = ItemBuilder.of(Material.LEATHER_HORSE_ARMOR)
                    .model(bb_geometry.render_data).color(0xFFFFFF).build();
        }
    }

    public Bone(Bone parent, BukkitObjectInputStream bois) throws IOException {
        this.parent = parent;
        this.name = bois.readUTF();
        this.pivot = new float[] { bois.readFloat(), bois.readFloat(), bois.readFloat() };
        this.rotation = new float[] { bois.readFloat(), bois.readFloat(), bois.readFloat() };
        this.visible = bois.readBoolean();
        this.geometry_width = bois.readFloat();
        this.geometry_height = bois.readFloat();

        int render_data = bois.readInt();
        if (render_data != -1) {
            this.item = ItemBuilder.of(Material.LEATHER_HORSE_ARMOR)
                    .model(render_data).color(0xFFFFFF).build();
        }


        int size = bois.readInt();
        for (int i = 0; i < size; i++) {
            this.children.put(bois.readUTF(), new Bone(this, bois));
        }
    }

    /**
     * Perform a normalization that changes the absolute transformations
     * provided by blockbench into local transformations that can be used
     * by the scene-graph hierarchy we render the model by.
     */
    public void normalize() {
        boolean normalize_again = true;
        while (normalize_again) {
            normalize_again = false;
            // normalize every bone depth upwards
            for (Bone bone : this) {
                // check conditions for being normalized
                boolean allow_normalize = bone.parent != null && !bone.normalized;
                if (allow_normalize) {
                    for (Bone child : bone.children.values()) {
                        if (!child.normalized) {
                            allow_normalize = false;
                        }
                    }
                }
                // perform normalization
                if (allow_normalize) {
                    BBUtil.subtract(bone.pivot, bone.parent.pivot);
                    BBUtil.subtract(bone.rotation, bone.parent.rotation);
                    bone.normalized = true;
                    normalize_again = true;
                }
            }
        }
    }

    /**
     * Retrieve a copy of the itemization, should the item be null the
     * output value is also null.
     *
     * @return Bone as an item.
     */
    public ItemStack itemize() {
        return this.item == null ? null : this.item.clone();
    }

    /**
     * Serialize into byte array structure for later serialization.
     *
     * @param boos
     * @throws IOException
     */
    public void dump(BukkitObjectOutputStream boos) throws IOException {
        boos.writeUTF(this.name);
        boos.writeFloat(this.pivot[0]);
        boos.writeFloat(this.pivot[1]);
        boos.writeFloat(this.pivot[2]);
        boos.writeFloat(this.rotation[0]);
        boos.writeFloat(this.rotation[1]);
        boos.writeFloat(this.rotation[2]);
        boos.writeBoolean(this.visible);
        boos.writeFloat(this.geometry_width);
        boos.writeFloat(this.geometry_height);

        if (this.item == null) {
            boos.writeInt(-1);
        } else {
            boos.writeInt(this.item.getItemMeta().getCustomModelData());
        }


        boos.writeInt(this.children.size());
        for (Map.Entry<String, Bone> entry : this.children.entrySet()) {
            boos.writeUTF(entry.getKey());
            entry.getValue().dump(boos);
        }
    }

    /**
     * Retrieve a bone that matches the given name that is either
     * this bone, or one of the child bones.
     *
     * @param name The name of the bone.
     * @return This bone, one of the children or null.
     */
    public Bone getBone(String name) {
        Queue<Bone> working = new LinkedList<>();
        working.add(this);
        while (!working.isEmpty()) {
            Bone current = working.poll();
            if (current.name.equals(name)) {
                return current;
            } else {
                working.addAll(current.children.values());
            }
        }

        return null;
    }

    @NotNull
    @Override
    public Iterator<Bone> iterator() {
        return new NestedIterator<>(this) {
            @Override
            public Collection<Bone> getNestedFrom(Bone current) {
                return current.children.values();
            }
        };
    }

}