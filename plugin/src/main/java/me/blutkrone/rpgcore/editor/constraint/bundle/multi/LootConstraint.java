package me.blutkrone.rpgcore.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.loot.EditorLootExperience;
import me.blutkrone.rpgcore.editor.bundle.loot.EditorLootItem;
import me.blutkrone.rpgcore.editor.bundle.loot.EditorLootSkill;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LootConstraint extends AbstractMultiConstraint {
    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("exp", EditorLootExperience::new);
        id_to_constructor.put("item", EditorLootItem::new);
        id_to_constructor.put("skill", EditorLootSkill::new);

        class_to_id.put(EditorLootExperience.class, "exp");
        class_to_id.put(EditorLootItem.class, "item");
        class_to_id.put(EditorLootSkill.class, "skill");
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
        instruction.add("Loot");
        instruction.add("A reward offered upon killing a mob.");
        return instruction;
    }
}
