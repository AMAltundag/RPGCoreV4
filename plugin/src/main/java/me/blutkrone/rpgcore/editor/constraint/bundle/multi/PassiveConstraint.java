package me.blutkrone.rpgcore.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.passive.*;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PassiveConstraint extends AbstractMultiConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("skillunlock", EditorPassiveUnlockSkill::new);
        id_to_constructor.put("entityattribute", EditorPassiveEntityAttribute::new);
        id_to_constructor.put("skillattribute", EditorPassiveSkillAttribute::new);
        id_to_constructor.put("socketentityattribute", EditorPassiveSocketEntityAttribute::new);
        id_to_constructor.put("socketskillattribute", EditorPassiveSocketSkillAttribute::new);
        id_to_constructor.put("socketskillreference", EditorPassiveSocketSkillReference::new);
        class_to_id.put(EditorPassiveEntityAttribute.class, "entityattribute");
        class_to_id.put(EditorPassiveSkillAttribute.class, "skillattribute");
        class_to_id.put(EditorPassiveSocketEntityAttribute.class, "socketentityattribute");
        class_to_id.put(EditorPassiveSocketSkillAttribute.class, "socketskillattribute");
        class_to_id.put(EditorPassiveSocketSkillReference.class, "socketskillreference");
        class_to_id.put(EditorPassiveUnlockSkill.class, "skillunlock");
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
        instruction.add("Passive");
        instruction.add("How a passive node affects an entity, depending on what");
        instruction.add("Source provided the tree, not all nodes will be active.");
        return instruction;
    }
}
