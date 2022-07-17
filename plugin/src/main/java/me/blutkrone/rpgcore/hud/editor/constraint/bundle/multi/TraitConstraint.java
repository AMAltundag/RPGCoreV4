package me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.root.npc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TraitConstraint implements IEditorConstraint {

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
    }

    @Override
    public List<String> getHint(String value) {
        List<String> hints = new ArrayList<>();
        id_to_constructor.forEach((k, v) -> {
            if (k.startsWith(value.toLowerCase())) {
                hints.add(k);
            }
        });
        return hints;
    }

    @Override
    public boolean isDefined(String value) {
        return id_to_constructor.containsKey(value);
    }

    @Override
    public void extend(String value) {
        // unsupported
    }

    @Override
    public boolean canExtend() {
        return false;
    }

    @Override
    public String getConstraintAt(List container, int index) {
        Object o = container.get(index);
        String value = class_to_id.get(o.getClass());
        if (value == null) {
            throw new IllegalArgumentException("Unknown class: " + o.getClass());
        }
        return value;
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        Supplier<IEditorBundle> constructor = id_to_constructor.get(value);
        if (constructor == null) {
            throw new IllegalArgumentException("Bad type " + value);
        }
        container.set(index, constructor.get());
    }

    @Override
    public void addElement(List container, String value) {
        Supplier<IEditorBundle> constructor = id_to_constructor.get(value);
        if (constructor == null) {
            throw new IllegalArgumentException("Bad type " + value);
        }
        container.add(constructor.get());
    }

    @Override
    public Object asTypeOf(String value) {
        Supplier<IEditorBundle> constructor = id_to_constructor.get(value);
        if (constructor == null) {
            throw new IllegalArgumentException("Bad type " + value);
        }
        return constructor.get();
    }

    @Override
    public String toTypeOf(Object o) {
        String value = class_to_id.get(o.getClass());
        if (value == null) {
            throw new IllegalArgumentException("Unknown class: " + o.getClass());
        }
        return value;
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> preview = new ArrayList<>();

        if (list.size() <= 16) {
            for (int i = 0; i < list.size(); i++) {
                preview.add(i + ": " + toTypeOf(list.get(i)));
            }
        } else {
            for (int i = 0; i < 16; i++) {
                preview.add(i + ": " + toTypeOf(list.get(i)));
            }

            preview.add("... And " + (list.size() - 16) + " More!");
        }

        return preview;
    }

    @Override
    public String getPreview(Object object) {
        return toTypeOf(object);
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fNPC Trait");
        instruction.add("§fTraits provide behaviour to NPC entities, only up to");
        instruction.add("§f6 traits will be applied.");
        return instruction;
    }
}
