package me.blutkrone.rpgcore.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.quest.task.*;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class QuestTaskConstraint extends AbstractMultiConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("deliver", EditorQuestTaskDeliver::new);
        id_to_constructor.put("kill", EditorQuestTaskKill::new);
        id_to_constructor.put("talk", EditorQuestTaskTalk::new);
        id_to_constructor.put("visit", EditorQuestTaskVisit::new);
        id_to_constructor.put("collect", EditorQuestTaskCollect::new);
        id_to_constructor.put("logic", EditorQuestTaskLogic::new);

        class_to_id.put(EditorQuestTaskDeliver.class, "deliver");
        class_to_id.put(EditorQuestTaskKill.class, "kill");
        class_to_id.put(EditorQuestTaskTalk.class, "talk");
        class_to_id.put(EditorQuestTaskVisit.class, "visit");
        class_to_id.put(EditorQuestTaskCollect.class, "collect");
        class_to_id.put(EditorQuestTaskLogic.class, "logic");
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
        instruction.add("Quest Task");
        instruction.add("A task within a quest that has to be completed.");
        return instruction;
    }
}
