package me.blutkrone.rpgcore.bbmodel.hitbox;

import me.blutkrone.rpgcore.bbmodel.active.ActiveModel;
import me.blutkrone.rpgcore.bbmodel.owner.OwnerMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Interaction;
import org.bukkit.util.BoundingBox;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Generate a hitbox based off the cubes within the
 * hitbox bone.
 */
public class SimpleHitbox implements IHitbox {

    private final OwnerMob owner;
    private final ActiveModel model;

    private List<ActiveHitbox> hitboxes = new ArrayList<>();

    public SimpleHitbox(OwnerMob owner, ActiveModel model) {
        this.owner = owner;
        this.model = model;
        Location location = owner.getLastLocation();
        World world = location.getWorld();
        if (world == null) {
            throw new NullPointerException("Bad location!");
        }

        for (BoundingBox hitbox : model.getTemplate().hitboxes) {
            Interaction interaction = world.spawn(location, Interaction.class, (entity -> {
                entity.setInvulnerable(true);
                entity.setSilent(true);
                entity.setPersistent(false);
            }));
            this.hitboxes.add(new ActiveHitbox(interaction, hitbox));
        }
    }

    @Override
    public Collection<UUID> getIds() {
        Queue<UUID> output = new LinkedList<>();
        for (ActiveHitbox hitbox : this.hitboxes) {
            output.add(hitbox.uuid);
        }
        return output;
    }

    @Override
    public void update() {
        float size = this.owner.getSize();
        Location location = this.owner.getLastLocation();

        for (ActiveHitbox hitbox : this.hitboxes) {
            // identify the bounds of the hitbox
            double width = Math.max(hitbox.hitbox.getWidthX(), hitbox.hitbox.getWidthZ());
            double height = hitbox.hitbox.getHeight();
            double x = hitbox.hitbox.getCenterX();
            double y = hitbox.hitbox.getCenterY() - height/2d;
            double z = hitbox.hitbox.getCenterZ();
            // update the hitbox
            Interaction entity = hitbox.getEntity();
            entity.teleport(location.clone().add(x*size, y*size, z*size));
            entity.setInteractionHeight((float) (height*size));
            entity.setInteractionWidth((float) (width*size));
        }
    }

    @Override
    public void recycle() {
        for (ActiveHitbox hitbox : this.hitboxes) {
            Interaction entity = hitbox.getEntity();
            if (entity != null) {
                entity.remove();
            }
        }
        this.hitboxes.clear();
    }

    /**
     * Wrapper for a single hitbox.
     */
    private class ActiveHitbox {
        private UUID uuid;
        private Reference<Interaction> reference;
        private BoundingBox hitbox;

        public ActiveHitbox(Interaction entity, BoundingBox hitbox) {
            this.uuid = entity.getUniqueId();
            this.reference = new WeakReference<>(entity);
            this.hitbox = hitbox;
        }

        public Interaction getEntity() {
            Interaction entity = this.reference.get();
            if (entity == null) {
                entity = (Interaction) Bukkit.getEntity(this.uuid);
                this.reference = new WeakReference<>(entity);
            }
            return entity;
        }
    }
}
