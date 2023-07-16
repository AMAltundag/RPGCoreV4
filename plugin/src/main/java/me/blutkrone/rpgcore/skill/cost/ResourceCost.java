package me.blutkrone.rpgcore.skill.cost;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.editor.bundle.cost.EditorResourceCost;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.resource.EntityResource;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

public class ResourceCost extends AbstractCoreCost {

    private final CoreModifierNumber cost;
    private final CoreModifierBoolean blood;
    private final CoreModifierBoolean stamina;

    public ResourceCost(EditorResourceCost editor) {
        cost = editor.cost.build();
        blood = editor.blood.build();
        stamina = editor.stamina.build();
    }

    @Override
    public boolean canAfford(IContext context) {
        CoreEntity entity = context.getCoreEntity();
        if (entity == null) {
            return false;
        }

        EntityResource resource = entity.getMana();
        if (stamina.evaluate(context)) {
            resource = entity.getStamina();
        }
        if (blood.evaluate(context)) {
            resource = entity.getMana();
        }
        return resource.getCurrentAmount() >= cost.evalAsDouble(context);
    }

    @Override
    public void consumeCost(IContext context) {
        CoreEntity entity = context.getCoreEntity();
        if (entity == null) {
            return;
        }

        EntityResource resource = entity.getMana();
        if (stamina.evaluate(context)) {
            resource = entity.getStamina();
        }
        if (blood.evaluate(context)) {
            resource = entity.getMana();
        }
        resource.damageBy(cost.evalAsDouble(context));
    }
}
