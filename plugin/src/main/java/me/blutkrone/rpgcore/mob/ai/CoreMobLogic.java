package me.blutkrone.rpgcore.mob.ai;

import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorMobLogic;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.nms.api.mob.AbstractEntityRoutine;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.mechanic.BarrierMechanic;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.ArrayList;
import java.util.List;

public class CoreMobLogic extends CoreAction {

    // the group to share among other logic
    private String group;
    // cooldown after being invoked
    private int cooldown;
    // priority used for sorting
    private double priority;
    // logic only starts after meeting conditions
    private List<AbstractCoreSelector> start_when_found = new ArrayList<>();

    public CoreMobLogic(EditorMobLogic editor) {
        super(editor);
        this.group = editor.group;
        this.cooldown = (int) editor.cooldown;
        this.priority = editor.priority;
        for (IEditorBundle bundle : editor.start_when_found) {
            if (bundle instanceof AbstractEditorSelector) {
                this.start_when_found.add(((AbstractEditorSelector) bundle).build());
            }
        }
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
        // cooldown before being triggered again
        private int cooldown;

        public SmartEntityRoutine(IEntityBase entity, CoreMob core_entity) {
            super(entity);

            this.core_entity = core_entity;
        }

        @Override
        public boolean doStart() {
            // cannot start if no steps defined
            if (mechanics.isEmpty() || cooldown-- > 0) {
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
            pipeline = pipeline(core_entity);
            // allow the task to start
            return true;
        }

        @Override
        public boolean doUpdate() {
            if (this.pipeline.update()) {
                // put on cooldown since we finished
                cooldown = CoreMobLogic.this.cooldown;
                // we are finished
                return true;
            } else {
                // we haven't finished yet
                return false;
            }
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
    }
}
