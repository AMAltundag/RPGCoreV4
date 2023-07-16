package me.blutkrone.rpgcore.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.npc.*;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TraitConstraint extends AbstractMultiConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("banker", EditorBankerTrait::new);
        id_to_constructor.put("crafter", EditorCrafterTrait::new);
        id_to_constructor.put("dialogue", EditorDialogueTrait::new);
        id_to_constructor.put("essence", EditorEssenceTrait::new);
        id_to_constructor.put("gate", EditorGateTrait::new);
        id_to_constructor.put("mail", EditorMailTrait::new);
        id_to_constructor.put("quest", EditorQuestTrait::new);
        id_to_constructor.put("refiner", EditorRefinerTrait::new);
        id_to_constructor.put("storage", EditorStorageTrait::new);
        id_to_constructor.put("vendor", EditorVendorTrait::new);
        id_to_constructor.put("travel", EditorTravelTrait::new);
        id_to_constructor.put("spawnpoint", EditorSpawnpointTrait::new);

        class_to_id.put(EditorBankerTrait.class, "banker");
        class_to_id.put(EditorCrafterTrait.class, "crafter");
        class_to_id.put(EditorDialogueTrait.class, "dialogue");
        class_to_id.put(EditorEssenceTrait.class, "essence");
        class_to_id.put(EditorGateTrait.class, "gate");
        class_to_id.put(EditorMailTrait.class, "mail");
        class_to_id.put(EditorQuestTrait.class, "quest");
        class_to_id.put(EditorRefinerTrait.class, "refiner");
        class_to_id.put(EditorStorageTrait.class, "storage");
        class_to_id.put(EditorVendorTrait.class, "vendor");
        class_to_id.put(EditorTravelTrait.class, "travel");
        class_to_id.put(EditorSpawnpointTrait.class, "spawnpoint");
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
        instruction.add("NPC Trait");
        instruction.add("Traits provide behaviour to NPC entities, only up to");
        instruction.add("6 traits will be applied.");
        return instruction;
    }
}
