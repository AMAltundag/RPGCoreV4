package me.blutkrone.rpgcore.bbmodel.owner;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.bbmodel.BBModelManager;
import me.blutkrone.rpgcore.bbmodel.active.ActiveBone;
import me.blutkrone.rpgcore.bbmodel.active.ActiveModel;
import me.blutkrone.rpgcore.bbmodel.active.component.LocationSnapshot;
import me.blutkrone.rpgcore.bbmodel.active.tints.ActiveTint;
import me.blutkrone.rpgcore.bbmodel.hitbox.IHitbox;
import me.blutkrone.rpgcore.bbmodel.hitbox.SimpleHitbox;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.Model;
import me.blutkrone.rpgcore.bbmodel.util.exception.BBExceptionRecycled;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.lang.ref.WeakReference;
import java.util.UUID;

/*
 * Model handling for an entity.
 */
public class OwnerMob implements IActiveOwner {

    private final BBModelManager manager;

    CoreMob owner;
    WeakReference<IEntityBase> reference_for_entity_base;
    WeakReference<LivingEntity> reference_for_entity_handle;
    ActiveModel active;
    Location last_location;
    boolean recycled;
    boolean has_updated_once;
    float size;
    IHitbox hitbox;

    public OwnerMob(BBModelManager manager, CoreMob owner) {
        this.manager = manager;
        this.owner = owner;
        this.reference_for_entity_base = new WeakReference<>(null);
        this.reference_for_entity_handle = new WeakReference<>(null);
        this.active = null;
        this.last_location = null;
        this.recycled = false;
        this.has_updated_once = false;
        this.size = 1.0f;

        LivingEntity entity = getEntity();
        if (entity != null) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1, false, false, false));
            EntityEquipment equipment = entity.getEquipment();
            if (equipment != null) {
                equipment.clear();
            }
        }
    }

    @Override
    public void use(Model model) {
        if (this.recycled) {
            return;
        }

        this.last_location = this.getEntity().getLocation();

        // initialize the new model we are using
        if (this.active != null) {
            // clear previous model
            ActiveModel previous = this.active;
            this.active = new ActiveModel(model);
            previous.recycle();
            // clean previous hitbox
            this.hitbox.getIds().forEach(manager::unbindHitbox);
            this.hitbox.recycle();
        } else {
            this.active = new ActiveModel(model);
        }

        // prepare the custom hitbox for the mobs
        this.hitbox = new SimpleHitbox(this, this.active);

        // bind the hitboxes
        for (UUID hitbox : this.hitbox.getIds()) {
            manager.bindHitbox(this.owner.getUniqueId(), hitbox);
        }
    }

    @Override
    public Location getLocation(String bone, boolean normalized) {
        if (this.last_location == null) {
            return null;
        }

        ActiveBone found = this.active.getBone().getBone(bone);
        if (found != null) {
            float[] pos = found.last_known_position;
            Location self_location = this.last_location.clone().add(new Vector(pos[0], pos[1], pos[2]));
            ActiveBone parent_bone = found.parent;
            if (parent_bone != null) {
                pos = parent_bone.last_known_position;
                Location parent_location = this.last_location.clone().add(new Vector(pos[0], pos[1], pos[2]));
                self_location.setDirection(parent_location.toVector().subtract(self_location.toVector()).normalize());
            }
            return self_location;
        } else {
            return this.last_location.clone();
        }
    }

    @Override
    public void recycle() {
        if (this.recycled) {
            return;
        }

        if (this.active != null) {
            // prevent further interactions
            this.recycled = true;
            // recycle the hitboxes
            this.hitbox.getIds().forEach(manager::unbindHitbox);
            this.hitbox.recycle();
            this.hitbox = null;
            // request the 'natural' death animation
            this.active.dieNaturally();
        }
    }

    @Override
    public void sync(int delta) {
        if (this.recycled) {
            return;
        }

        IEntityBase base = getBase();
        if (this.active != null && base != null && this.has_updated_once) {
            // play or stop the walking animation
            float speed = (float) base.getWalkingSpeed();
            if (base.isWalking()) {
                this.active.fade("idle");
                this.active.play("walk", speed, 1f);
            } else {
                this.active.fade("walk");
                this.active.play("idle", 1f, 1f);
            }

            // sync hitbox back to the entity
            this.hitbox.update();
        }

        // recompute model size
        float updated_size = (float) Math.max(0.1f, 1f + this.owner.evaluateAttribute("MOB_SIZE"));
        this.size = this.owner.getTemplate().model_size * updated_size;
    }

    @Override
    public IModelOwner async(int delta) {
        if (this.recycled) {
            return new OwnerDying(this.active, this.last_location, this.size);
        }

        this.last_location = RPGCore.inst().getEntityManager().getLastLocation(this.owner.getUniqueId());
        LocationSnapshot snapshot = new LocationSnapshot(this.last_location);

        // update observation state where necessary
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location player_location = RPGCore.inst().getEntityManager().getLastLocation(player.getUniqueId());
            if (player_location != null) {
                if (active.isObserved(player) && snapshot.distSq(player_location) > 64 * 64) {
                    active.unobserve(player);
                } else if (!active.isObserved(player) && snapshot.distSq(player_location) < 64 * 64) {
                    active.observe(player);
                }
            }
        }

        // perform actual ticking
        this.active.async(delta, this.size, snapshot);
        this.has_updated_once = true;
        return this;
    }

    @Override
    public void playAnimation(String animation, float speed) {
        if (this.recycled) {
            return;
        }

        this.active.play(animation, speed);
    }

    @Override
    public void stopAnimation(String animation) {
        if (this.recycled) {
            return;
        }

        this.active.stop(animation);
    }

    @Override
    public void fadeAnimation(String animation) throws BBExceptionRecycled {
        if (this.recycled) {
            return;
        }

        this.active.fade(animation);
    }

    @Override
    public void tint(String bone, String id, ActiveTint tint) {
        if (this.recycled) {
            return;
        }

        ActiveBone active_bone = this.active.getBone().getBone(bone);
        if (active_bone != null) {
            active_bone.tint(id, tint);
        }
    }

    @Override
    public void size(float size) {
        this.size = size;
    }

    /**
     *
     * @return
     */
    public Location getLastLocation() {
        return last_location;
    }

    /**
     * Retrieve NMS logic wrapper
     *
     * @return
     */
    public IEntityBase getBase() {
        IEntityBase output = reference_for_entity_base.get();
        if (output == null) {
            reference_for_entity_base = new WeakReference<>(output = owner.getBase());
        }
        return output;
    }

    /**
     * Retrieve owning bukkit entity.
     *
     * @return
     */
    public LivingEntity getEntity() {
        LivingEntity output = reference_for_entity_handle.get();
        if (output == null) {
            reference_for_entity_handle = new WeakReference<>(output = owner.getEntity());
        }
        return output;
    }

    /**
     * Size to apply on the model.
     *
     * @return Size of model.
     */
    public float getSize() {
        return size;
    }
}
