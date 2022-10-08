package me.blutkrone.rpgcore.entity.providers;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;

/**
 * A provider capable of providing us with a simple
 * vanilla entity, without any mutations.
 */
public class LivingProvider implements EntityProvider {

    private EntityType type;

    public LivingProvider(EntityType type) {
        this.type = type;
    }

    @Override
    public LivingEntity create(Location where, Object... args) {
        try {
            return RPGCore.inst().getVolatileManager().spawnEntity(type, where).getBukkitHandle();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public BoundingBox getBounds(LivingEntity entity) {
        return entity.getBoundingBox().clone();
    }
}
