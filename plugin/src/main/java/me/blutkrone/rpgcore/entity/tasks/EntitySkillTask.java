package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import org.bukkit.scheduler.BukkitRunnable;

public class EntitySkillTask extends BukkitRunnable {

    private final CoreEntity entity;

    public EntitySkillTask(CoreEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        this.entity.getActions().removeIf(CoreAction.ActionPipeline::update);
    }
}
