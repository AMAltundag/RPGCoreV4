package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityRecoveryTask extends BukkitRunnable {

    private final CoreEntity entity;

    public EntityRecoveryTask(CoreEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        this.entity.getHealth().tickRecovery(10);
        this.entity.getStamina().tickRecovery(10);
        this.entity.getMana().tickRecovery(10);
    }
}
