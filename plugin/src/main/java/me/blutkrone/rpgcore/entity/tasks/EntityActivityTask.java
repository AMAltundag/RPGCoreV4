package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityActivityTask extends BukkitRunnable {

    private final CoreEntity entity;

    public EntityActivityTask(CoreEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        IActivity activity = this.entity.getActivity();
        if (activity == null)
            return;
        if (activity.update()) {
            this.entity.setActivity(null);
        }
    }
}
