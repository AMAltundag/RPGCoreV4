package me.blutkrone.rpgcore.mob.ai;

import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.other.EditorMobLogic;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.nms.api.mob.AbstractEntityRoutine;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.mechanic.BarrierMechanic;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoreMobLogic extends CoreAction {

    // the group to share among other logic
    private final String group;
    // cooldown after being invoked
    private final int cooldown;
    // cooldown after failing the start condition
    private final int wait;
    // priority used for sorting
    private final double priority;
    // logic only starts after meeting conditions
    private final List<AbstractCoreSelector> start_when_found;

    public CoreMobLogic(EditorMobLogic editor) {
        super(editor);
        this.group = editor.group;
        this.cooldown = (int) editor.cooldown;
        this.wait = (int) editor.wait;
        this.priority = editor.priority;
        this.start_when_found = AbstractEditorSelector.unwrap(editor.start_when_found);
    }

    /**
     * Priority of this logic compared to others.
     *
     * @return priority
     */
    public double getPriority() {
        return priority;
    }

    /**
     * Share the namespace with other logic, only one logic can
     * run from the same group. Do note that
     *
     * @return a group shared by other logic
     */
    public String getGroup() {
        return group;
    }

    /**
     * Construct an entity routine which is created specifically
     * for the entity.
     *
     * @param base_entity a wrapper around the bukkit entity
     * @param core_entity a wrapper around the core entity
     * @return the entity routine we created.
     */
    public AbstractEntityRoutine construct(IEntityBase base_entity, CoreMob core_entity) {
        return new SmartEntityRoutine(base_entity, core_entity);
    }

    class SmartEntityRoutine extends AbstractEntityRoutine {

        // core mob the routine is backed by
        private CoreMob core_entity;
        // iterator on the AI routine of the mob
        private ActionPipeline pipeline;

        public SmartEntityRoutine(IEntityBase entity, CoreMob core_entity) {
            super(entity);
            this.core_entity = core_entity;
        }

        @Override
        public boolean doStart() {
            // cannot start if no steps defined
            if (mechanics.isEmpty()) {
                return false;
            }
            // we can start if the selector discovers anything
            List<IOrigin> current = new ArrayList<>();
            current.add(core_entity);
            for (AbstractCoreSelector selector : start_when_found) {
                current = selector.doSelect(core_entity, current);
            }
            // initialize the task to start
            if (current.isEmpty()) {
                return false;
            }
            // initialize an iterator
            pipeline = pipeline(core_entity, Collections.singletonList(core_entity));
            // allow the task to start
            return true;
        }

        @Override
        public boolean doUpdate() {
            return this.pipeline.update();
        }

        @Override
        public boolean doBarrierDamageSoak(int damage) {
            BarrierMechanic.ActiveBarrier barrier = this.pipeline.getBarrier();
            if (barrier != null) {
                barrier.damage -= damage;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int getWaitTime() {
            return wait;
        }

        @Override
        public int getCooldownTime() {
            return cooldown;
        }
    }
}
