package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.api.entity.IEntityEffect;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorInstantMechanic;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InstantMechanic extends AbstractCoreMechanic {

    private Set<String> tags;
    private CoreModifierNumber duration;
    private String icon;
    private String id;

    public InstantMechanic(EditorInstantMechanic editor) {
        this.tags = editor.tags.stream().map(String::toLowerCase).collect(Collectors.toSet());
        this.duration = editor.duration.build();
        this.icon = editor.icon;
        this.id = editor.id;
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);
        for (IOrigin target : targets) {
            if (target instanceof CorePlayer) {
                ((CoreEntity) target).addEffect(id, new Effect(duration, this.tags, this.icon));
            }
        }
    }

    /**
     * A ticket used to track instant casts,
     */
    public static class Effect implements IEntityEffect {
        private int duration;
        private Set<String> tags;
        private String icon;
        private long timestamp;
        private boolean consumed;

        Effect(int duration, Set<String> tags, String icon) {
            this.duration = duration;
            this.tags = tags;
            this.icon = icon;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * Check if we do match with the given skill.
         *
         * @param skill which skill we want to instant-cast
         * @return whether we can instant cast the skill
         */
        public boolean doesMatch(CoreSkill skill) {
            // search for any tag we do match with
            for (String tag : skill.getTags()) {
                if (tags.contains(tag.toLowerCase())) {
                    return true;
                }
            }
            // check against the ID of the skill
            return this.tags.contains(skill.getId().toLowerCase());
        }

        /**
         * Marks the instant cast as consumed, making it no longer
         * a valid listing.
         */
        public void setConsumed() {
            this.consumed = true;
        }

        @Override
        public boolean tickEffect(int delta) {
            this.duration -= delta;
            return this.duration <= 0;
        }

        @Override
        public int getStacks() {
            return 1;
        }

        @Override
        public int getDuration() {
            return this.duration;
        }

        @Override
        public String getIcon() {
            if ("none".equalsIgnoreCase(this.icon)) {
                return null;
            }

            return this.icon;
        }

        @Override
        public long getLastUpdated() {
            return this.timestamp;
        }

        @Override
        public boolean isDebuff() {
            return false;
        }

        @Override
        public boolean isValid() {
            return this.duration > 0 && !this.consumed;
        }

        @Override
        public void manipulate(int stack, int duration, boolean override) {
            if (override) {
                this.duration = duration;
            } else {
                this.duration += duration;
            }
        }
    }
}
