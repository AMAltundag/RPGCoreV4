package me.blutkrone.rpgcore.damage.ailment;

import me.blutkrone.rpgcore.damage.interaction.DamageElement;

import java.util.Map;

/**
 * An early snapshot of damage inflicted, used to scale the
 * ailment without causing any significant double-dipping.
 */
public class AilmentSnapshot {
    public Map<DamageElement, Double> damage_flat;
    public Map<DamageElement, Double> damage_multi;

    public AilmentSnapshot(Map<DamageElement, Double> damage_flat, Map<DamageElement, Double> damage_multi) {
        this.damage_flat = damage_flat;
        this.damage_multi = damage_multi;
    }
}
