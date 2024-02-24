package me.blutkrone.rpgcore.bbmodel.active;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.bbmodel.active.component.AnimationResult;
import me.blutkrone.rpgcore.bbmodel.active.tints.ActiveTint;
import me.blutkrone.rpgcore.bbmodel.interpolation.AngleInterpolator;
import me.blutkrone.rpgcore.bbmodel.interpolation.ArrayInterpolator;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.Bone;
import me.blutkrone.rpgcore.bbmodel.util.NestedIterator;
import me.blutkrone.rpgcore.nms.api.packet.handle.IItemDisplay;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A wrapper around a bone, that is owned by an
 * entity we have modeled.
 */
public class ActiveBone implements Iterable<ActiveBone> {

    public final Bone handle;
    public final ActiveBone parent;

    public AnimationResult animation;
    public Matrix4f transformation;
    public float[] last_known_position;
    private ArrayInterpolator interpolator_position = null;
    private AngleInterpolator interpolator_rotation = null;

    private Map<String, ActiveBone> children;
    private Map<String, ActiveBone> search_results;
    private Map<String, ActiveTint> tints;

    private ItemStack item;
    private IItemDisplay display;
    private int last_tint;

    /**
     * A wrapper around a bone, that is owned by an
     * entity we have modeled.
     *
     * @param bone The bone to wrap
     */
    public ActiveBone(Bone bone) {
        this.handle = bone;
        this.parent = null;
        this.children = new HashMap<>();
        this.search_results = new ConcurrentHashMap<>();
        this.tints = new ConcurrentHashMap<>();
        this.item = bone.itemize();
        bone.children.forEach((id, child) -> {
            this.children.put(id, new ActiveBone(this, child));
        });
    }

    /*
     * A wrapper around a bone, that is owned by an
     * entity we have modeled.
     *
     * @param parent
     * @param bone
     */
    ActiveBone(ActiveBone parent, Bone bone) {
        this.handle = bone;
        this.parent = parent;
        this.children = new HashMap<>();
        this.search_results = new ConcurrentHashMap<>();
        this.tints = new ConcurrentHashMap<>();
        this.item = bone.itemize();
        bone.children.forEach((id, child) -> {
            this.children.put(id, new ActiveBone(parent, child));
        });
    }

    /**
     * Retrieve the last known itemization.
     *
     * @return The last item known.
     */
    public ItemStack getItemLast() {
        return this.item;
    }

    /**
     * Retrieve an update to the item.
     *
     * @param delta Time since last ticking
     * @return The item, if updated.
     */
    public ItemStack getItemUpdate(int delta) {
        // ignore if item is nulled
        if (this.item == null) {
            return null;
        }

        // scan for the highest priority tint available
        ActiveTint current = null;
        ActiveBone bone = this;
        while (bone != null) {
            for (ActiveTint candidate : this.tints.values()) {
                if (candidate.duration > 0 && (current == null || current.priority < candidate.priority)) {
                    current = candidate;
                }
                candidate.duration -= delta;
            }
            bone = bone.parent;
        }

        // check if we have an update
        int updated = current == null ? 0xFFFFFF : current.color;
        if (updated == this.last_tint) {
            return null;
        }

        // update the tint, offer it up to the model
        ItemMeta meta = this.item.getItemMeta();
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(updated));
        }
        this.item.setItemMeta(meta);
        this.last_tint = updated;

        return this.item;
    }

    /**
     * Apply a tint to this bone, and all its children.
     *
     * @param tintID
     * @param tint
     */
    public void tint(String tintID, ActiveTint tint) {
        this.tints.compute(tintID, ((key, value) -> {
            if (value == null) {
                return tint;
            } else {
                value.duration = tint.duration;
                return value;
            }
        }));
    }

    /**
     * Display entity representing this bone..
     *
     * @return Display entity.
     */
    public IItemDisplay display() {
        if (this.display == null) {
            this.display = RPGCore.inst().getVolatileManager().getPackets().item();
        }

        return this.display;
    }

    /**
     * Children under this bone.
     *
     * @return Direct children.
     */
    public Map<String, ActiveBone> getChildren() {
        return children;
    }

    /**
     * Retrieve a bone that matches the given name that is either
     * this bone, or one of the child bones.
     *
     * @param name The name of the bone.
     * @return This bone, one of the children or null.
     */
    public ActiveBone getBone(String name) {
        return this.search_results.computeIfAbsent(name, (wanted -> {
            for (ActiveBone bone : ActiveBone.this) {
                if (bone.handle.name.equals(wanted)) {
                    return bone;
                }
            }

            return null;
        }));
    }

    /**
     * The rotation of the bone, which is relative to the rotation we
     * inherited from our parent. Interpolation applies.
     *
     * @return Quaternion rotation.
     */
    public float[] getRotation(int delta) {
        // apply local rotation
        float x = this.handle.rotation[0];
        float y = this.handle.rotation[1];
        float z = this.handle.rotation[2];
        // apply animator
        if (this.animation != null) {
            float[] animation = this.animation.rotation();
            x -= animation[0];
            y += animation[1];
            z -= animation[2];
        }
        // assign into interpolator
        float[] want = new float[] { -x, -y, z };
        if (this.interpolator_rotation == null) {
            this.interpolator_rotation = new AngleInterpolator(want);
            return want;
        } else {
            this.interpolator_rotation.update(want);
            return this.interpolator_rotation.interpolate(delta, 4.5f, 0.05f);
        }
    }

    /**
     * The position of the bone, which also serves as our pivot, do
     * note that this is a relative position. Interpolation applies.
     *
     * @return Position of bone, relative to parent.
     */
    public float[] getPosition(int delta) {
        // apply local offset
        float x = this.handle.pivot[0];
        float y = this.handle.pivot[1];
        float z = this.handle.pivot[2];
        // apply animator
        if (this.animation != null) {
            float[] animation = this.animation.position();
            x -= animation[0];
            y += animation[1];
            z -= animation[2];
        }
        // offer up as offset
        float[] want = new float[] { x/-16f, y/16f, z/-16f };
        if (this.interpolator_position == null) {
            this.interpolator_position = new ArrayInterpolator(want);
            return want;
        } else {
            this.interpolator_position.update(want);
            return this.interpolator_position.interpolate(delta, 0.1f, 0.05f);
        }
    }

    @NotNull
    @Override
    public Iterator<ActiveBone> iterator() {
        return new NestedIterator<>(this) {
            @Override
            public Collection<ActiveBone> getNestedFrom(ActiveBone current) {
                return current.children.values();
            }
        };
    }
}
