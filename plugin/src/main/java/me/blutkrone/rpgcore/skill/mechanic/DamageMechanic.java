package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.api.damage.IDamageType;
import me.blutkrone.rpgcore.damage.DamageManager;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorDamageMechanic;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.editor.bundle.other.EditorAttributeAndModifier;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;

import java.util.*;

/**
 * Inflicts damage.
 */
public class DamageMechanic extends AbstractCoreMechanic {

    // type of damage to invoke
    private String type;
    // attributes for the damage instance
    private Map<String, CoreModifierNumber> modifiers_always = new HashMap<>();
    // tags for the damage instance
    private List<CoreModifierString> tags_always = new ArrayList<>();

    public DamageMechanic(EditorDamageMechanic editor) {
        this.type = editor.type;
        for (IEditorBundle bundle : editor.modifiers_always) {
            EditorAttributeAndModifier casted = (EditorAttributeAndModifier) bundle;
            this.modifiers_always.put(casted.attribute, casted.factor.build());
        }
        for (IEditorBundle bundle : editor.tags_always) {
            EditorModifierString casted = (EditorModifierString) bundle;
            this.tags_always.add(casted.build());
        }
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        DamageManager manager = RPGCore.inst().getDamageManager();
        // ensure the damage type exists
        IDamageType type = manager.getType(this.type);
        if (type == null) {
            RPGCore.inst().getLogger().severe("Unknown damage type: " + this.type);
            return;
        }
        // extract damage related things applicable to all entities
        Map<String, Double> attributes = new HashMap<>();
        Set<String> tags = new HashSet<>();
        modifiers_always.forEach((id, factor) -> {
            attributes.merge(id, factor.evalAsDouble(context), (a, b) -> a + b);
        });
        tags_always.forEach((tag) -> {
            tags.add(tag.evaluate(context));
        });

        for (IOrigin target : targets) {
            if (target instanceof CoreEntity) {
                DamageInteraction interaction = type.create(((CoreEntity) target), context.getCoreEntity());
                interaction.setSourceContext(context);
                if (context instanceof SkillContext) {
                    interaction.setDamageBlame("skill_" + ((SkillContext) context).getSkill().getId());
                }
                // offer up attributes into the interaction
                attributes.forEach((id, factor) -> interaction.getAttribute(id).create(factor));
                // provide tags for the interaction
                interaction.addTags(tags);
                // inflict the damage on the entity
                manager.damage(interaction);
            }
        }
    }
}
