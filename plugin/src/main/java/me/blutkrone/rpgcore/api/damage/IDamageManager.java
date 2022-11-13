package me.blutkrone.rpgcore.api.damage;

import me.blutkrone.rpgcore.damage.ailment.AilmentSnapshot;
import me.blutkrone.rpgcore.damage.interaction.DamageElement;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;

import java.util.List;

/**
 * @see DamageInteraction contains information about damage dealt
 * @see CoreEntity only a core entity may be involved in damage
 */
public interface IDamageManager {

    /**
     * Process the damage interaction which was passed into this
     * damage manager.
     *
     * @param interaction the interaction we've received.
     */
    void damage(DamageInteraction interaction);

    void handleAilment(DamageInteraction interaction, AilmentSnapshot prepared_damage);

    DamageElement getElement(String element);

    List<DamageElement> getElements();

    List<DamageElement> getElements(List<String> elements);

    List<String> getElementIds();

    IDamageType getType(String type);

    List<IDamageType> getTypes();

    List<IDamageType> getTypes(List<String> types);

    List<String> getTypeIds();
}
