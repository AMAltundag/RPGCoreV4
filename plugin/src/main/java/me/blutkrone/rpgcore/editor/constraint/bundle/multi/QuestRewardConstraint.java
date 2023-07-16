package me.blutkrone.rpgcore.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.quest.reward.*;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class QuestRewardConstraint extends AbstractMultiConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("exp", EditorQuestRewardExp::new);
        id_to_constructor.put("item", EditorQuestRewardItem::new);
        id_to_constructor.put("trait", EditorQuestRewardTrait::new);
        id_to_constructor.put("skill", EditorQuestRewardSkill::new);
        id_to_constructor.put("tag", EditorQuestRewardTag::new);
        id_to_constructor.put("advance", EditorQuestRewardAdvancement::new);

        class_to_id.put(EditorQuestRewardExp.class, "exp");
        class_to_id.put(EditorQuestRewardItem.class, "item");
        class_to_id.put(EditorQuestRewardTrait.class, "trait");
        class_to_id.put(EditorQuestRewardSkill.class, "skill");
        class_to_id.put(EditorQuestRewardTag.class, "tag");
        class_to_id.put(EditorQuestRewardAdvancement.class, "advance");
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
        instruction.add("Quest Reward");
        instruction.add("A reward offered upon quest completion.");
        return instruction;
    }
}
