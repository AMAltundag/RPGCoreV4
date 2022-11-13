package me.blutkrone.rpgcore.damage.ailment.ailments;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.damage.IDamageManager;
import me.blutkrone.rpgcore.api.damage.IDamageType;
import me.blutkrone.rpgcore.attribute.IExpiringModifier;
import me.blutkrone.rpgcore.damage.ailment.AbstractAilment;
import me.blutkrone.rpgcore.damage.ailment.AilmentSnapshot;
import me.blutkrone.rpgcore.damage.ailment.AilmentTracker;
import me.blutkrone.rpgcore.damage.interaction.DamageElement;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;

import java.util.*;

public class DamageAilment extends AbstractAilment {

    // maximum concurrent ticking damage instances
    private List<String> maximum_stack;
    // duration which the ailment will last
    private List<String> duration_attribute;
    // chance to inflict the ailment
    private List<String> chance_attribute;
    // % of damage inherited by element
    private Map<String, List<String>> contribution = new HashMap<>();
    // accelerates how quick the ailment damages
    private List<String> faster_attribute;
    // % chance to fail gaining the ailment
    private List<String> avoid_attribute;
    // base duration of inflicted ailment
    private int base_duration;
    // tags held while any ailment is active
    private List<String> tag_list;

    /**
     * An ailment is a secondary effect caused by non-DOT damage.
     *
     * @param id     identifier of this ailment
     * @param config how to setup the ailment.
     */
    public DamageAilment(String id, ConfigWrapper config) {
        super(id, config);
        maximum_stack = config.getStringList("maximum-stack");
        base_duration = config.getInt("base-duration");
        duration_attribute = config.getStringList("percent-duration");
        chance_attribute = config.getStringList("chance");
        config.forEachUnder("contribution", (path, root) -> {
            contribution.put(path, root.getStringList(path));
        });
        faster_attribute = config.getStringList("faster");
        avoid_attribute = config.getStringList("avoid-chance");
        tag_list = config.getStringList("tags-while-active");
    }

    @Override
    public AilmentTracker createTracker(CoreEntity holder) {
        return new Tracker(this, holder);
    }

    public class Tracker extends AilmentTracker {

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
        public boolean acquireAilment(DamageInteraction interaction, AilmentSnapshot damage) {
            // non-crit damage may fail to inflict the ailment
            if (!interaction.checkForTag("CRITICAL_HIT", interaction.getAttacker())) {
                double chance = 0d;
                for (String attribute : chance_attribute) {
                    chance += interaction.evaluateAttribute(attribute, interaction.getAttacker());
                }
                if (!(Math.random() <= chance))
                    return false;
            }

            // defense layer to just avoid the ailment
            double avoid_chance = 0d;
            for (String attribute : avoid_attribute)
                avoid_chance += Math.max(0d, interaction.evaluateAttribute(attribute, interaction.getDefender()));
            if (Math.random() <= avoid_chance)
                return false;

            // compute the % multiplier to ailment duration
            double duration = 1d;
            for (String attribute : duration_attribute)
                duration += interaction.evaluateAttribute(attribute, interaction.getAttacker());
            if (duration <= 0d)
                return false;

            // how many concurrent instances can deal damage
            double concurrent = 0d;
            for (String attribute : maximum_stack)
                concurrent += interaction.evaluateAttribute(attribute, interaction.getAttacker());
            if (concurrent <= 0d)
                return false;

            // drop a stack if we are at the limit
            if (active.size() >= concurrent)
                active.remove(active.size() - 1);

            // accumulate the damage to inherit
            Map<DamageElement, Double> flat_damage = new HashMap<>();
            damage.damage_flat.forEach((element, flat) -> {
                double inheritance = 0d;
                for (String attribute : contribution.getOrDefault(element.getId(), new ArrayList<>()))
                    inheritance += interaction.evaluateAttribute(attribute, interaction.getAttacker());
                if (inheritance <= 0d)
                    return;
                flat_damage.put(element, inheritance * flat);
            });

            // ensure we got any damage to inflict at all
            double summed = 0d;
            for (Double inherited : flat_damage.values())
                summed += inherited;
            if (summed <= 0d)
                return false;

            // accelerates rate at which we deal damage
            double faster = 1d;
            for (String attribute : faster_attribute) {
                faster += interaction.evaluateAttribute(attribute, interaction.getAttacker());
            }

            // multiplier carried from alternative source
            double inherited_multi = interaction.evaluateAttribute("DOT_MULTI_INHERIT", interaction.getAttacker());

            // track a new damage instance
            active.add(new ActiveAilment(interaction.getAttacker(), (int) (base_duration * duration),
                    Math.max(0.1d, faster), flat_damage, inherited_multi));
            // re-sort again by strongest ailments
            active.sort(Comparator.comparingDouble(ailment -> ailment.cumulative));
            return true;
        }

        @Override
        public boolean tick(int interval) {
            IDamageManager damage_manager = RPGCore.inst().getDamageManager();
            IDamageType damage_type_DOT = damage_manager.getType("DOT");

            // purge all tag instances which we have
            List<IExpiringModifier> expiring_modifiers = getHolder().getExpiringModifiers("TAG_AILMENT_" + identifier);
            // abandon all modifiers which we had before
            expiring_modifiers.forEach(IExpiringModifier::setExpired);
            expiring_modifiers.clear();

            // eat up the appropriate amount of duration
            active.removeIf(ailment -> {
                // consume some of the duration, relative to % faster damage
                ailment.duration -= interval * ailment.faster;
                // ditch the ailment if we ran out of time
                return ailment.duration <= 0;
            });

            // no DOT inflicted if no active instances left
            if (active.isEmpty())
                return true;

            // invoke all ailments which are active
            for (ActiveAilment ailment : active) {
                // generate a damage instance for the DOT
                DamageInteraction interaction = damage_type_DOT.create(this.getHolder(), ailment.attacker);
                // adjust damage inheritance via time ratio
                double ratio = (interval * ailment.faster) / 20d;
                // inherit relevant flat damage
                ailment.flat_damage.forEach((element, damage) -> interaction.setDamage(element, damage * ratio));
                // track inherited multiplier
                interaction.getAttribute("DOT_MULTIPLIER").create(ailment.multi_inherited);
                // put blame on a secondary ailment
                interaction.setDamageBlame("ailment");
                // inflict the damage
                damage_manager.damage(interaction);
            }

            // acquire the appropriate tags
            for (String tag : tag_list)
                expiring_modifiers.add(getHolder().grantTag(tag));

            // retain the collection and re-check next time
            return false;
        }
    }

    private class ActiveAilment {
        // who inflicted the ailment
        CoreEntity attacker;
        // ticks the instance will last
        int duration;
        // damage inheritance
        Map<DamageElement, Double> flat_damage;
        // generic % damage inherited
        double multi_inherited;
        // snapshot of cumulative damage
        double cumulative;
        // accelerated rate of dealing damage
        double faster;

        private ActiveAilment(CoreEntity attacker, int duration, double faster, Map<DamageElement, Double> flat_damage, double multi_inherited) {
            this.attacker = attacker;
            this.duration = duration;
            this.flat_damage = flat_damage;
            this.faster = faster;
            this.multi_inherited = multi_inherited;
            flat_damage.forEach((element, flat) -> {
                this.cumulative += flat;
            });
        }
    }
}