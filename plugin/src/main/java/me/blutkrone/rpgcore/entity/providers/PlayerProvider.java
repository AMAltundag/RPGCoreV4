package me.blutkrone.rpgcore.entity.providers;

import me.blutkrone.rpgcore.api.entity.EntityProvider;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class PlayerProvider implements EntityProvider {
    @Override
    public LivingEntity create(Location where, Object... args) {
        throw new UnsupportedOperationException("Cannot provide player instances!");
    }

    @Override
    public BoundingBox getBounds(LivingEntity entity) {
        return entity.getBoundingBox().clone();
    }

    @Override
    public Location getHeadLocation(LivingEntity entity) {
        return entity.getEyeLocation();
    }

    @Override
    public List<Location> getSpecialLocations(LivingEntity entity, String location) {
        return new ArrayList<>();
    }
}
