package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.attribute.TagModifier;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class EntityEffectTask extends BukkitRunnable {

    private final CoreEntity entity;
    private final Map<String, TagModifier> tags = new HashMap<>();

    public EntityEffectTask(CoreEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        // process ailments
        this.entity.getAilmentTracker().values().removeIf(tracker -> {
            if (tracker.tick(10)) {
                tracker.abandon();
                return true;
            } else {
                tracker.decorate(10);
                return false;
            }
        });
        // process generic status effects
        this.entity.getStatusEffects().values().removeIf(effect -> effect.tickEffect(10));
        // validate presence of skills
        this.entity.updateSkills();
    }
}
