package me.blutkrone.rpgcore.entity.providers;

import me.blutkrone.rpgcore.api.entity.EntityProvider;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;

public class PlayerProvider implements EntityProvider {
    @Override
    public LivingEntity create(Location where, Object... args) {
        throw new UnsupportedOperationException("Cannot provide player instances!");
    }

    @Override
    public BoundingBox getBounds(LivingEntity entity) {
        return entity.getBoundingBox().clone();
    }
}
