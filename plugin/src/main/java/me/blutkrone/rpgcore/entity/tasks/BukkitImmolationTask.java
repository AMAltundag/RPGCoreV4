package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitImmolationTask extends BukkitRunnable {

    private final CoreEntity entity;

    public BukkitImmolationTask(CoreEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        if (this.entity.checkForTag("BUKKIT_BURNING")) {
            int current = this.entity.getEntity().getFireTicks();
            this.entity.getEntity().setFireTicks(Math.max(20, current));
        }
    }
}
