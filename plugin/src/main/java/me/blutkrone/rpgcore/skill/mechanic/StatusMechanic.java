package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.api.entity.IEntityEffect;
import me.blutkrone.rpgcore.attribute.AttributeModifier;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorStatusMechanic;
import me.blutkrone.rpgcore.editor.bundle.other.EditorAction;
import me.blutkrone.rpgcore.editor.bundle.other.EditorAttributeAndModifier;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.*;

public class StatusMechanic extends AbstractCoreMechanic {

    private final String id;
    private final Map<String, CoreModifierNumber> attribute;
    private final List<String> scaling;
    private final boolean debuff;
    private final CoreModifierNumber duration;
    private final CoreModifierNumber stack;
    private final String icon;
    private final List<CoreAction> actions;
    private final CoreModifierNumber action_rate;

    public StatusMechanic(EditorStatusMechanic editor) {
        this.id = editor.id;
        this.attribute = EditorAttributeAndModifier.unwrap(editor.attribute);
        this.scaling = new ArrayList<>(editor.scaling);
        this.debuff = editor.debuff;
        this.duration = editor.duration.build();
        this.stack = editor.stack.build();
        this.icon = editor.icon;
        this.actions = new ArrayList<>();
        for (IEditorBundle action : editor.actions) {
            if (action instanceof EditorAction) {
                this.actions.add(((EditorAction) action).build());
            }
        }
        this.action_rate = editor.action_rate.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        for (IOrigin target : targets) {
            if (target instanceof CoreEntity) {
                IEntityEffect effect = ((CoreEntity) target).getEffect(id);
                if (effect != null) {
                    ((StatusEffect) effect).update(this.stack.evalAsInt(context), this.duration.evalAsInt(context));
                } else {
                    ((CoreEntity) target).addEffect(id, new StatusEffect(((CoreEntity) target), this, context));
                }
            }
        }
    }

    public static class StatusEffect implements IEntityEffect {

        private CoreEntity entity;
        private int stack;
        private int duration;
        private String icon;
        private long timestamp;
        private boolean debuff;
        private Map<String, Double> attribute;
        private List<String> scaling;
        private List<AttributeModifier> expired;
        private int last_stack;

        private int action_rate;
        private int action_cooldown;
        private List<CoreAction> actions;

        public StatusEffect(CoreEntity entity, Map<String, Double> attribute, int duration, boolean debuff) {
            this.entity = entity;
            this.stack = 1;
            this.duration = duration;
            this.icon = "none";
            this.timestamp = System.currentTimeMillis();
            this.debuff = debuff;
            this.attribute = new HashMap<>(attribute);
            this.expired = new ArrayList<>();
            this.last_stack = -1;
            this.scaling = new ArrayList<>();
            this.action_rate = 0;
            this.actions = new ArrayList<>();
            this.action_cooldown = 0;
        }

        StatusEffect(CoreEntity entity, StatusMechanic mechanic, IContext context) {
            this.entity = entity;
            this.stack = 1;
            this.duration = mechanic.duration.evalAsInt(context);
            this.icon = mechanic.icon;
            this.timestamp = System.currentTimeMillis();
            this.debuff = mechanic.debuff;
            this.attribute = new HashMap<>();
            this.expired = new ArrayList<>();
            this.last_stack = -1;
            this.scaling = mechanic.scaling;
            mechanic.attribute.forEach((id, factor) -> {
                this.attribute.put(id, factor.evalAsDouble(context));
            });
            this.action_rate = mechanic.action_rate.evalAsInt(context);
            this.actions = mechanic.actions;
            this.action_cooldown = 0;
        }

        /**
         * Gain a stack, so long we are below the given maximum.
         *
         * @param stack_maximum maximum stacks
         * @param duration      how long the effect lasts
         */
        public void update(int stack_maximum, int duration) {
            // gain 1 stack if below the maximum
            if (this.stack < stack_maximum) {
                this.stack = this.stack + 1;
            }
            // reset the duration
            this.duration = duration;
            // update the timestamp
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public boolean tickEffect(int delta) {
            this.duration -= delta;
            this.action_cooldown -= delta;

            if (this.duration < 0 || this.stack < 0) {
                this.expired.forEach(AttributeModifier::setExpired);
                return true;
            } else {
                if (this.last_stack != this.stack) {
                    // update ticking cooldown
                    if (this.action_cooldown < 0) {
                        for (CoreAction action : actions) {
                            CoreAction.ActionPipeline pipeline = action.pipeline(this.entity, Collections.singletonList(this.entity));
                            if (!pipeline.update()) {
                                this.entity.addPipeline(pipeline);
                            }
                        }
                        this.action_cooldown = this.action_rate;
                    }
                    // get rid of previous effects
                    this.expired.forEach(AttributeModifier::setExpired);
                    this.expired.clear();
                    this.last_stack = this.stack;
                    // compute new scaling factor
                    double scaling = 1d;
                    for (String attr : this.scaling) {
                        scaling += this.entity.evaluateAttribute(attr);
                    }
                    final double _scaling = scaling;
                    // apply the attributes
                    this.attribute.forEach((attribute, factor) -> {
                        this.expired.add(this.entity.getAttribute(attribute).create(factor * this.stack * _scaling));
                    });
                }

                // retain the effect
                return false;
            }
        }

        @Override
        public int getStacks() {
            return this.stack;
        }

        @Override
        public int getDuration() {
            return this.duration;
        }

        @Override
        public String getIcon() {
            return this.icon;
        }

        @Override
        public long getLastUpdated() {
            return this.timestamp;
        }

        @Override
        public boolean isDebuff() {
            return this.debuff;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void manipulate(int stack, int duration, boolean override) {
            if (override) {
                this.stack = stack;
                this.duration = duration;
            } else {
                this.stack = this.stack + stack;
                this.duration = this.duration + duration;
            }

            this.timestamp = System.currentTimeMillis();
        }
    }
}
