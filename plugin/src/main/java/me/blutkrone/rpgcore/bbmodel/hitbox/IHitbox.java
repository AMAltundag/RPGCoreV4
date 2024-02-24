package me.blutkrone.rpgcore.bbmodel.hitbox;

import java.util.Collection;
import java.util.UUID;

public interface IHitbox {

    /**
     * Retrieve the UUID of all registered hitboxes.
     *
     * @return Registered hitboxes
     */
    Collection<UUID> getIds();

    /**
     * Update the hitbox to align with the model.
     */
    void update();

    /**
     * Destroy the hitboxes registered to us.
     */
    void recycle();
}
