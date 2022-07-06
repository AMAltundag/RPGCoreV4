package me.blutkrone.rpgcore.damage.interaction.types;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.damage.IDamageManager;
import me.blutkrone.rpgcore.api.damage.IDamageType;
import me.blutkrone.rpgcore.damage.ailment.AilmentSnapshot;
import me.blutkrone.rpgcore.damage.interaction.DamageElement;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.resource.EntityWard;

import java.util.HashMap;
import java.util.Map;

/**
 * Damage which was inflicted thorough a skill.
 */
public class SpellDamageType implements IDamageType {

    private void takeDamageAs(DamageInteraction interaction, Map<DamageElement, Double> damage_multi, Map<DamageElement, Double> damage_flat) {
        // managers which may be necessary to process damage
        IDamageManager damage_manager = RPGCore.inst().getDamageManager();

        // 5.) Apply rules to take all damage as one element
        Map<DamageElement, Double> damage_taken_as = new HashMap<>();
        double total_taken_as = 0d;
        for (DamageElement element : damage_manager.getElements()) {
            double taken_as = interaction.evaluateAttribute(element.getTakenAttribute(), interaction.getDefender());
            damage_taken_as.put(element, taken_as);
            total_taken_as += taken_as;
        }

        // transform damage if we got taken-as applying
        if (total_taken_as > 0d) {
            // normalize to not take more then a 100% damage
            if (total_taken_as > 1d) {
                for (Map.Entry<DamageElement, Double> taken : damage_taken_as.entrySet()) {
                    taken.setValue(taken.getValue() / total_taken_as);
                }
            }

            // transform the damage taken
            Map<DamageElement, Double> updated_damage_flat = new HashMap<>();
            Map<DamageElement, Double> updated_damage_multi = new HashMap<>();

            // retain damage if applicable
            if (total_taken_as < 1d) {
                for (Map.Entry<DamageElement, Double> entry_damage : damage_flat.entrySet())
                    updated_damage_flat.put(entry_damage.getKey(), entry_damage.getValue() * (1d - total_taken_as));
                for (Map.Entry<DamageElement, Double> entry_damage : damage_multi.entrySet())
                    updated_damage_multi.put(entry_damage.getKey(), entry_damage.getValue() * (1d - total_taken_as));
            }

            // compute total damage available
            double summed_flat = 0d;
            for (Map.Entry<DamageElement, Double> entry_damage : damage_flat.entrySet())
                summed_flat += entry_damage.getValue();
            double summed_multi = 0d;
            for (Map.Entry<DamageElement, Double> entry_damage : damage_flat.entrySet())
                summed_multi += entry_damage.getValue();

            // take the damage as that given element
            for (Map.Entry<DamageElement, Double> entry_taken : damage_taken_as.entrySet()) {
                DamageElement element = entry_taken.getKey();
                double ratio = entry_taken.getValue();
                updated_damage_flat.merge(element, ratio * summed_flat, (a, b) -> a + b);
                updated_damage_multi.merge(element, ratio * summed_multi, (a, b) -> a + b);
            }

            // apply the transformed damage
            damage_flat.clear();
            damage_flat.putAll(updated_damage_flat);
            damage_multi.clear();
            damage_multi.putAll(updated_damage_multi);
        }
    }

    private void scaleAsAttacker(DamageInteraction interaction, Map<DamageElement, Double> damage_multi, Map<DamageElement, Double> damage_flat) {
        // managers which may be necessary to process damage
        IDamageManager damage_manager = RPGCore.inst().getDamageManager();

        // decide if this damage is going to be processed as a critical hit
        if (interaction.getAttacker() != null && !interaction.checkForTag("CANNOT_CRITICAL", interaction.getAttacker())) {
            double crit_chance = 0d;
            crit_chance += interaction.evaluateAttribute("CRIT_CHANCE", interaction.getAttacker());
            crit_chance += interaction.evaluateAttribute("CRIT_CHANCE_SPELL", interaction.getAttacker());
            if (Math.random() <= crit_chance) {
                interaction.addTags("CRITICAL_HIT");
            }
        }

        // apply scaling rules of the attacker
        for (DamageElement element : damage_manager.getElements()) {
            // apply scaling for damage multipliers
            double multi = 0d;
            multi += interaction.evaluateAttribute("SPELL_" + element.getId() + "_MULTIPLIER", interaction.getAttacker());
            for (String attribute : element.getMultiplierAttribute()) {
                multi += interaction.evaluateAttribute(attribute, interaction.getAttacker());
            }
            damage_multi.merge(element, multi, (a, b) -> a + b);
            // apply scaling for damage base
            double base = 0d;
            base += interaction.evaluateAttribute("SPELL_" + element.getId() + "_BASE", interaction.getAttacker());
            damage_flat.merge(element, base, (a, b) -> a + b);
        }

        // apply rules to gain extra damage
        Map<DamageElement, Double> updated_damage_multi = new HashMap<>();
        Map<DamageElement, Double> updated_damage_flat = new HashMap<>();

        for (DamageElement element : damage_manager.getElements()) {
            // identify how much damage we gain from other elements
            double extra = 0d;
            for (String attribute : element.getExtraAttribute())
                extra += interaction.evaluateAttribute(attribute, interaction.getAttacker());
            // carry over the damage multi
            double extra_damage = extra;
            damage_multi.forEach((group, factor) -> {
                double ratio = group == element ? 1d : extra_damage;
                updated_damage_multi.merge(group, factor * ratio, (a, b) -> a + b);
            });
            // carry over the damage flat
            damage_flat.forEach((group, factor) -> {
                double ratio = group == element ? 1d : extra_damage;
                updated_damage_flat.merge(group, factor * ratio, (a, b) -> a + b);
            });
        }

        damage_multi.clear();
        damage_multi.putAll(updated_damage_multi);
        damage_flat.clear();
        damage_flat.putAll(updated_damage_flat);
    }

    @Override
    public void process(DamageInteraction interaction) {
        // managers which may be necessary to process damage
        IDamageManager damage_manager = RPGCore.inst().getDamageManager();

        // the elements that are dealt damage thorough
        Map<DamageElement, Double> damage_multi = new HashMap<>();
        Map<DamageElement, Double> damage_flat = new HashMap<>();

        // accumulate the flat damage we are to deal
        for (DamageElement element : damage_manager.getElements()) {
            damage_flat.put(element, interaction.getDamage(element));
        }

        // handle scaling rules based on the attacker
        if (interaction.getAttacker() != null) {
            scaleAsAttacker(interaction, damage_multi, damage_flat);
        }

        // shift the damage to another element
        takeDamageAs(interaction, damage_multi, damage_flat);

        // prevent damage of certain elements being applied at all
        for (DamageElement element : damage_manager.getElements()) {
            if (interaction.checkForTag("TAKE_NO_" + element.getId() + "_DAMAGE", interaction.getDefender()))
                interaction.setDamage(element, 0d);
            if (interaction.checkForTag("DEAL_NO_" + element.getId() + "_DAMAGE", interaction.getDefender()))
                interaction.setDamage(element, 0d);
        }

        // upscale according to critical damage
        double crit_damage = 1d;
        if (interaction.getAttacker() != null && interaction.checkForTag("CRITICAL_HIT", interaction.getAttacker())) {
            crit_damage += Math.max(0d, interaction.evaluateAttribute("CRIT_DAMAGE", interaction.getAttacker()));
            crit_damage += Math.max(0d, interaction.evaluateAttribute("CRIT_DAMAGE_SPELL", interaction.getAttacker()));
        }

        // get an approximation of the damage inflicted
        double approx_damage = 0d;
        for (DamageElement element : damage_manager.getElements()) {
            // identify the total damage
            double approximation = damage_flat.getOrDefault(element, 0d);
            approximation *= 1d + damage_multi.getOrDefault(element, 0d);
            approximation *= crit_damage;
            // sum up approximation available
            approx_damage += approximation;
        }
        // compute modifiers specific to spell damage
        double received_spell = interaction.evaluateAttribute("SPELL_TAKEN_MULTIPLIER", interaction.getDefender());
        double resistance_spell = interaction.evaluateAttribute("SPELL_RESISTANCE", interaction.getDefender());
        double multi_spell = 0d;
        if (interaction.getAttacker() != null)
            multi_spell = interaction.evaluateAttribute("SPELL_MULTIPLIER", interaction.getAttacker());
        // apply armor specific damage reduction
        double armor_spell = interaction.evaluateAttribute("SPELL_ARMOR", interaction.getDefender());
        if (approx_damage > 0) {
            // reduce armor based on armor break
            double armor_break = 0d;
            if (interaction.getAttacker() != null) {
                armor_break = Math.max(0d, interaction.evaluateAttribute("SPELL_ARMOR_BREAK", interaction.getAttacker()));
            }
            // acquire resistance relative to damage
            resistance_spell += Math.log(1d + (armor_spell / approx_damage)) / Math.log(2d + Math.max(0d, armor_break)) * 0.1d;
        }

        // calculate the total damage inflicted
        for (Map.Entry<DamageElement, Double> entry_flat_damage : damage_flat.entrySet()) {
            DamageElement element = entry_flat_damage.getKey();
            double damage = entry_flat_damage.getValue();

            // damage multiplier of the attacker
            damage *= Math.max(0d, 1d + damage_multi.getOrDefault(element, 0d) + multi_spell);

            // damage multiplier of the defender
            double received = 1d;
            for (String attribute : element.getReceived())
                received += interaction.evaluateAttribute(attribute, interaction.getDefender());
            damage *= Math.max(0d, received + received_spell);

            // damage multiplier of resistance
            double resistance = resistance_spell;
            for (String attribute : element.getResistanceAttribute())
                resistance += interaction.evaluateAttribute(attribute, interaction.getDefender());

            // cap resistance to maximum
            double max_resistance = interaction.evaluateAttribute(element.getMaxReductionAttribute(), interaction.getDefender());
            // penetration to lower the resistance
            double penetration = 0d;
            if (interaction.getAttacker() != null) {
                for (String attribute : element.getPenetrationAttribute())
                    penetration += interaction.evaluateAttribute(attribute, interaction.getAttacker());
            }

            // damage multiplier thorough resistance/penetration
            damage *= Math.max(0d, 1d - Math.min(resistance, max_resistance) + penetration);
            // apply crit multiplier to the damage
            damage *= crit_damage;
            // write the damage inflicted to the interaction
            interaction.setDamage(element, damage);

            // leech % of the damage inflicted
            if (interaction.getAttacker() != null) {
                EntityWard ward = interaction.getAttacker().getWard();
                if (ward != null) {
                    ward.addLeech(damage * ward.leech_rate);
                    double leech_health = interaction.evaluateAttribute("SPELL_LEECH_" + element.getId() + "_AS_LIFE", interaction.getAttacker());
                    interaction.getAttacker().getHealth().addLeech(damage * leech_health);
                    double leech_mana = interaction.evaluateAttribute("SPELL_LEECH_" + element.getId() + "_AS_MANA", interaction.getAttacker());
                    interaction.getAttacker().getMana().addLeech(damage * leech_mana);
                }
            }
        }

        // allow to inflict ailments with spell based damage
        damage_manager.handleAilment(interaction, new AilmentSnapshot(damage_flat, damage_multi));
    }

    @Override
    public DamageInteraction create(CoreEntity defender, CoreEntity attacker) {
        return new DamageInteraction(this, defender, attacker);
    }
}
