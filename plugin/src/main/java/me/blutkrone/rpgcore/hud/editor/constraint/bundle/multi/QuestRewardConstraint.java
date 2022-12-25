package me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.reward.EditorQuestRewardExp;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.reward.EditorQuestRewardItem;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.reward.EditorQuestRewardSkill;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.reward.EditorQuestRewardTrait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class QuestRewardConstraint implements IEditorConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("exp", EditorQuestRewardExp::new);
        id_to_constructor.put("item", EditorQuestRewardItem::new);
        id_to_constructor.put("trait", EditorQuestRewardTrait::new);
        id_to_constructor.put("skill", EditorQuestRewardSkill::new);

        class_to_id.put(EditorQuestRewardExp.class, "exp");
        class_to_id.put(EditorQuestRewardItem.class, "item");
        class_to_id.put(EditorQuestRewardTrait.class, "trait");
        class_to_id.put(EditorQuestRewardSkill.class, "skill");
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
        instruction.add("Quest Reward");
        instruction.add("A reward offered upon quest completion.");
        return instruction;
    }
}
