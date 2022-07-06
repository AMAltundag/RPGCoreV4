package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityEffectTask extends BukkitRunnable {

    private final CoreEntity entity;

    public EntityEffectTask(CoreEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        this.entity.getAilmentTracker().values().removeIf(tracker -> {
            if (tracker.tick(10)) {
                tracker.abandon();
                return true;
            } else {
                tracker.decorate(10);
                return false;
            }
        });
        this.entity.getStatusEffects().forEach((clazz, effect) -> {
            effect.entrySet().removeIf(entry -> entry.getValue().tickEffect(10));
        });
    }
}
