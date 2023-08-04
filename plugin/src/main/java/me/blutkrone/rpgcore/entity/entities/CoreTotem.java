package me.blutkrone.rpgcore.entity.entities;

import me.blutkrone.rpgcore.api.entity.EntityProvider;
import org.bukkit.entity.LivingEntity;

/**
 * This is used purely for identification purpose, a totem by
 * itself will do nothing. A totem can invoke logic, and reset
 * that logic upon death.
 * <br>
 * Please ensure that whenever this instance is created, a
 * parent-child relationship is established.
 */
public class CoreTotem extends CoreEntity {

    public CoreTotem(LivingEntity entity, EntityProvider provider) {
        super(entity, provider);
    }

    @Override
    public void remove() {
        super.remove();

        LivingEntity handle = getEntity();
        if (handle != null) {
            handle.remove();
        }
    }
}
