package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.proxy.AbstractSkillProxy;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityProxyTask extends BukkitRunnable {

    private final CoreEntity entity;

    public EntityProxyTask(CoreEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        this.entity.getProxies().removeIf(AbstractSkillProxy::update);
    }
}
