package me.blutkrone.rpgcore.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.selector.*;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SelectorConstraint extends AbstractMultiConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("allies", EditorAlliesSelector::new);
        id_to_constructor.put("enemies", EditorEnemiesSelector::new);
        id_to_constructor.put("friendly", EditorFriendlySelector::new);
        id_to_constructor.put("hostile", EditorHostileSelector::new);
        id_to_constructor.put("radius", EditorRadiusSelector::new);
        id_to_constructor.put("flag", EditorFlagSelector::new);
        id_to_constructor.put("rage", EditorRageSelector::new);
        id_to_constructor.put("none", EditorNoneSelector::new);
        id_to_constructor.put("offset", EditorOffsetSelector::new);
        id_to_constructor.put("chance", EditorChanceSelector::new);
        id_to_constructor.put("looking", EditorLookingSelector::new);
        id_to_constructor.put("level", EditorLevelSelector::new);
        id_to_constructor.put("score", EditorDungeonScoreSelector::new);
        id_to_constructor.put("and", EditorAndSelector::new);
        id_to_constructor.put("or", EditorOrSelector::new);
        id_to_constructor.put("self", EditorSelfSelector::new);
        id_to_constructor.put("target", EditorTargetSelector::new);
        id_to_constructor.put("attribute", EditorAttributeSelector::new);
        id_to_constructor.put("health", EditorHealthSelector::new);
        id_to_constructor.put("stamina", EditorStaminaSelector::new);
        id_to_constructor.put("mana", EditorManaSelector::new);
        id_to_constructor.put("model", EditorModelSelector::new);
        id_to_constructor.put("profession", EditorProfessionSelector::new);

        class_to_id.put(EditorAlliesSelector.class, "allies");
        class_to_id.put(EditorEnemiesSelector.class, "enemies");
        class_to_id.put(EditorFriendlySelector.class, "friendly");
        class_to_id.put(EditorHostileSelector.class, "hostile");
        class_to_id.put(EditorRadiusSelector.class, "radius");
        class_to_id.put(EditorFlagSelector.class, "flag");
        class_to_id.put(EditorRageSelector.class, "rage");
        class_to_id.put(EditorNoneSelector.class, "none");
        class_to_id.put(EditorOffsetSelector.class, "offset");
        class_to_id.put(EditorChanceSelector.class, "chance");
        class_to_id.put(EditorLookingSelector.class, "looking");
        class_to_id.put(EditorLevelSelector.class, "level");
        class_to_id.put(EditorDungeonScoreSelector.class, "score");
        class_to_id.put(EditorAndSelector.class, "and");
        class_to_id.put(EditorOrSelector.class, "or");
        class_to_id.put(EditorSelfSelector.class, "self");
        class_to_id.put(EditorTargetSelector.class, "target");
        class_to_id.put(EditorAttributeSelector.class, "attribute");
        class_to_id.put(EditorHealthSelector.class, "health");
        class_to_id.put(EditorStaminaSelector.class, "stamina");
        class_to_id.put(EditorManaSelector.class, "mana");
        class_to_id.put(EditorModelSelector.class, "model");
        class_to_id.put(EditorProfessionSelector.class, "profession");
    }

    @Override
    protected Map<String, Supplier<IEditorBundle>> getIdToConstructor() {
        return id_to_constructor;
    }

    @Override
    protected Map<Class, String> getClassToId() {
        return class_to_id;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Selector");
        instruction.add("Used for targeting and filtering, selectors modify the");
        instruction.add("targets for subsequent selectors.");
        instruction.add("When a selector is used as a condition, the condition is");
        instruction.add("archived if the last selector leaves any target.");
        return instruction;
    }
}
