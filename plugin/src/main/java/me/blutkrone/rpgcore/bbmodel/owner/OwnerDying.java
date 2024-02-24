package me.blutkrone.rpgcore.bbmodel.owner;

import me.blutkrone.rpgcore.bbmodel.active.ActiveBone;
import me.blutkrone.rpgcore.bbmodel.active.ActiveModel;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.Model;
import me.blutkrone.rpgcore.bbmodel.util.exception.BBExceptionRecycled;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class OwnerDying implements IModelOwner {

    private final ActiveModel model;
    private final Location last_location;
    private final float size;

    public OwnerDying(ActiveModel model, Location last_location, float size) {
        this.model = model;
        this.last_location = last_location;
        this.size = size;
    }

    @Override
    public void use(Model model) throws BBExceptionRecycled {
        // dead owner cannot change models
    }

    @Override
    public Location getLocation(String bone, boolean normalize) {
        if (this.last_location == null) {
            return null;
        }

        ActiveBone found = this.model.getBone().getBone(bone);
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
        // dead owner cannot be recycled
    }

    @Override
    public void sync(int delta) {
        // dead owner has no sync ticking
    }

    @Override
    public IModelOwner async(int delta) {
        if (this.model.async(delta, this.size, null)) {
            this.model.recycle();
            return null; // model finished, we can delete
        } else {
            return this; // model is still animating
        }
    }
}
