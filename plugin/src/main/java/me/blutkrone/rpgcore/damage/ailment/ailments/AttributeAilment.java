package me.blutkrone.rpgcore.damage.ailment.ailments;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.attribute.IExpiringModifier;
import me.blutkrone.rpgcore.damage.ailment.AbstractAilment;
import me.blutkrone.rpgcore.damage.ailment.AilmentTracker;
import me.blutkrone.rpgcore.damage.interaction.DamageElement;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeAilment extends AbstractAilment {

    // what element we are associated with (does nothing, needed for attribute initialisation)
    public String element;
    // maximum stacks of this attribute ailment
    public List<String> maximum_stack;
    // reduces the intensity of the inflicted ailment
    public List<String> reduced_effect;
    // the ratio at which attributes will be affected
    public Map<String, Double> attribute_ratio = new HashMap<>();
    public List<String> tag_list;
    // % of damage inherited by element
    public Map<String, String> contribution = new HashMap<>();
    // base duration of inflicted ailment
    public int base_duration;
    // duration which the ailment will last
    public List<String> percent_duration;
    // chance to inflict the ailment
    public List<String> chance;

    /**
     * An ailment is a secondary effect caused by non-DOT damage.
     *
     * @param id     identifier of this ailment
     * @param config how to setup the ailment.
     */
    public AttributeAilment(String id, ConfigWrapper config) {
        super(id, config);
        element = config.getString("element");
        maximum_stack = config.getStringList("maximum-stack");
        base_duration = config.getInt("base-duration");
        percent_duration = config.getStringList("percent-duration");
        chance = config.getStringList("chance");
        config.forEachUnder("contribution", (path, root) -> {
            contribution.put(path, root.getString(path));
        });
        reduced_effect = config.getStringList("reduced-effect");
        config.forEachUnder("attribute-ratio", (path, root) -> {
            attribute_ratio.put(path, root.getDouble(path));
        });
        tag_list = config.getStringList("tags-while-active");
    }

    @Override
    public AilmentTracker createTracker(CoreEntity holder) {
        return new Tracker(this, holder);
    }

    /*
     * A tracker which applies an attribute effect, based on the
     * strongest ailment inflicted.
     */
    private class Tracker extends AilmentTracker {

        // all active instances of the ailment
        private List<ActiveAilment> active = new ArrayList<>();

        /**
         * A tracker who is tracking a specific ailment for one
         * single entity.
         *
         * @param ailment the ailment which we are tracking
         * @param holder  who was afflicted with the ailment
         */
        public Tracker(AbstractAilment ailment, CoreEntity holder) {
            super(ailment, holder);
        }

        @Override
        public boolean acquireAilment(DamageInteraction interaction) {
            // non-crit damage may fail to inflict the ailment
            if (!interaction.checkForTag("CRITICAL_HIT", interaction.getAttacker())) {
                double chance = 0d;
                for (String attribute : AttributeAilment.this.chance) {
                    chance += interaction.evaluateAttribute(attribute, interaction.getAttacker());
                }
                if (!(Math.random() <= chance))
                    return false;
            }

            // compute the % multiplier to ailment duration
            double duration = 1d;
            for (String attribute : percent_duration)
                duration += interaction.evaluateAttribute(attribute, interaction.getAttacker());
            if (duration <= 0d)
                return false;

            // check how much damage counts to our ailment threshold
            double total_power = 0d;
            for (DamageElement element : RPGCore.inst().getDamageManager().getElements()) {
                // count how much damage was dealt in total
                double inflicted = interaction.getDamage(element);
                if (inflicted <= 0d) continue;
                // identify % of damage which counts
                String attribute = contribution.get(element.getId());
                if (attribute == null) continue;
                double ratio = interaction.evaluateAttribute(attribute, interaction.getAttacker());
                // skip if ratio is less-equal zero
                if (ratio <= 0d)
                    continue;
                // count damage to the threshold
                total_power += ratio * inflicted;
            }

            // identify if the ailment is strong enough to be inflicted
            double maximum_health = interaction.getDefender().getHealth().getSnapshotMaximum();
            double threshold = interaction.evaluateAttribute("AILMENT_THRESHOLD", interaction.getDefender());
            total_power = Math.min(1d, total_power / Math.max(0.01d, maximum_health * threshold));

            // count how many stacks we will inflict
            int stacks = 0;
            for (String attribute : maximum_stack)
                stacks += (int) Math.max(0d, interaction.evaluateAttribute(attribute, interaction.getAttacker()));
            stacks = (int) (total_power * stacks);

            // defense against the ailment
            double defense = 1d;
            for (String attribute : reduced_effect)
                defense -= Math.max(0d, interaction.evaluateAttribute(attribute, interaction.getDefender()));
            stacks = (int) (stacks * defense);

            // no ailment to be gained if less then one stack
            if (stacks <= 0)
                return false;

            // maximum of 50 concurrent instances
            if (active.size() > 50)
                active.remove(active.size() - 1);

            // inflict an instance of the ailment
            active.add(new ActiveAilment(stacks, (int) (base_duration * duration)));

            return false;
        }

        @Override
        public boolean tick(int interval) {
            // fetch the modifier index tying to this group
            List<IExpiringModifier> expiring_modifiers = getHolder().getExpiringModifiers("AILMENT_" + identifier);
            // abandon all modifiers which we had before
            expiring_modifiers.forEach(IExpiringModifier::setExpired);
            expiring_modifiers.clear();
            // drop all instances which have expired
            active.removeIf(ailment -> {
                ailment.duration -= interval;
                return ailment.duration <= 0;
            });
            // done if we have no active instance
            if (active.isEmpty())
                return true;
            // tick and search for strongest instance
            int[] strongest = {0};
            for (ActiveAilment ailment : active)
                strongest[0] = Math.max(ailment.stack, strongest[0]);
            // apply the attributes gained thorough the ailment
            attribute_ratio.forEach((attribute, factor) -> {
                expiring_modifiers.add(getHolder().getAttribute(attribute).create(factor * strongest[0]));
            });
            // note down relevant tags we've gained
            for (String tag : tag_list)
                expiring_modifiers.add(getHolder().grantTag(tag));
            // retain the ailment handler we have
            return false;
        }
    }

    private class ActiveAilment {
        // how intense the ailment is
        int stack;
        // how many ticks it has left
        int duration;

        public ActiveAilment(int stack, int duration) {
            this.stack = stack;
            this.duration = duration;
        }
    }
}
