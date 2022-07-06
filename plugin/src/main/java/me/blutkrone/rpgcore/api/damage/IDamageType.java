package me.blutkrone.rpgcore.api.damage;

import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;

/**
 * A damage type decides how the damage is to be processed, do
 * note that a damage interaction will not have multiple types
 * of damage associated with it.
 */
public interface IDamageType {

    /**
     * Process the provided damage instance, writing the final damage to
     * apply into the relevant damage method.
     *
     * @param interaction the interaction to process
     */
    void process(DamageInteraction interaction);

    /**
     * Create a damage interaction, with the basic scaling rules setup
     * for it.
     *
     * @param defender involved entity
     * @param attacker involved entity
     * @return resulting interaction
     */
    DamageInteraction create(CoreEntity defender, CoreEntity attacker);
}
