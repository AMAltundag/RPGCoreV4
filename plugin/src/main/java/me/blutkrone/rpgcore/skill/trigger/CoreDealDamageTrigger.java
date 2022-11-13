package me.blutkrone.rpgcore.skill.trigger;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.damage.IDamageManager;
import me.blutkrone.rpgcore.api.damage.IDamageType;
import me.blutkrone.rpgcore.damage.interaction.DamageElement;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.hud.editor.bundle.trigger.EditorDealDamageTrigger;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Triggered when enough damage of an element is triggered
 */
public class CoreDealDamageTrigger extends AbstractCoreTrigger {

    // what element of damage to count
    private Set<DamageElement> elements;
    // what damage types to count
    private Set<IDamageType> types;
    // total damage to be accumulated
    private CoreModifierNumber damage;

    public CoreDealDamageTrigger(EditorDealDamageTrigger editor) {
        super(editor);

        IDamageManager manager = RPGCore.inst().getDamageManager();
        this.elements = editor.elements.stream().map(manager::getElement).collect(Collectors.toSet());
        this.types = editor.types.stream().map(manager::getType).collect(Collectors.toSet());
        this.damage = editor.damage.build();

        this.cooldown_recovery = editor.cooldown_recovery.build();
        this.cooldown_reduction = editor.cooldown_reduction.build();
        this.cooldown_time = editor.cooldown_time.build();
        this.cooldown_id = editor.cooldown_id.build();
    }

    @Override
    public boolean update(SkillContext context, Object event, TriggerInfo info) {
        // check if the kill is of the wanted type
        if (!(event instanceof DamageInteraction)) {
            return false;
        }
        DamageInteraction casted = (DamageInteraction) event;
        // check if we match with the wanted types
        boolean type_matched = this.types.isEmpty();
        for (IDamageType type : this.types) {
            if (casted.getType() == type) {
                type_matched = true;
            }
        }
        // failure if we do not match type-wise
        if (!type_matched) {
            return false;
        }
        // accumulate the damage we want to be taken
        if (this.elements.isEmpty()) {
            info.value += casted.getDamage();
        } else {
            for (DamageElement element : this.elements) {
                info.value += casted.getDamage(element);
            }
        }

        // check if enough damage has been accumulated
        return info.value >= this.damage.evalAsDouble(context);
    }
}
