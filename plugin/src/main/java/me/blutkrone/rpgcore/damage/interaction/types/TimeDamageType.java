package me.blutkrone.rpgcore.damage.interaction.types;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.damage.IDamageManager;
import me.blutkrone.rpgcore.api.damage.IDamageType;
import me.blutkrone.rpgcore.damage.interaction.DamageElement;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;

/**
 * To accommodate for performance issues, a DOT is unaffected by conversion
 * and partially the element scaling. Please handle that scaling before a
 * DOT instance is created.
 * <p>
 * <ul>
 * <li>DOT_TAKEN_MULTIPLIER (Multiplier, Defender)</li>
 * </ul>
 */
public class TimeDamageType implements IDamageType {

    @Override
    public void process(DamageInteraction interaction) {
        // managers which may be necessary to process damage
        IDamageManager damage_manager = RPGCore.inst().getDamageManager();

        // prevent damage of certain elements being applied at all
        for (DamageElement element : damage_manager.getElements()) {
            if (interaction.checkForTag("TAKE_NO_" + element.getId() + "_DAMAGE", interaction.getDefender()))
                interaction.setDamage(element, 0d);
            if (interaction.checkForTag("DEAL_NO_" + element.getId() + "_DAMAGE", interaction.getDefender()))
                interaction.setDamage(element, 0d);
        }

        // compute modifiers specific to dot damage
        double received_dot = interaction.evaluateAttribute("DOT_TAKEN_MULTIPLIER", interaction.getDefender());
        double multi_dot = 0d;
        if (interaction.getAttacker() != null)
            multi_dot = interaction.evaluateAttribute("DOT_MULTIPLIER", interaction.getAttacker());

        // calculate the total damage inflicted
        for (DamageElement element : damage_manager.getElements()) {
            double damage = interaction.getDamage(element);
            // skip if we got no damage of this element
            if (damage <= 0d) {
                continue;
            }
            // apply DOT specific scaling rules
            damage *= Math.max(0d, 1d + multi_dot);
            damage *= Math.max(0d, 1d + received_dot);
            // write the damage inflicted to the interaction
            interaction.setDamage(element, damage);
        }
    }

    @Override
    public DamageInteraction create(CoreEntity defender, CoreEntity attacker) {
        return new DamageInteraction(this, defender, attacker);
    }
}
