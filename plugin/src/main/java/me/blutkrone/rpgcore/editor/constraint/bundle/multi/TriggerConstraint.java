package me.blutkrone.rpgcore.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.trigger.*;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TriggerConstraint extends AbstractMultiConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("attack", EditorAttackTrigger::new);
        id_to_constructor.put("cast", EditorCastTrigger::new);
        id_to_constructor.put("channel", EditorChannelTrigger::new);
        id_to_constructor.put("deal", EditorDealDamageTrigger::new);
        id_to_constructor.put("kill", EditorKillTrigger::new);
        id_to_constructor.put("move", EditorMoveTrigger::new);
        id_to_constructor.put("take", EditorTakeDamageTrigger::new);
        id_to_constructor.put("timer", EditorTimerTrigger::new);
        id_to_constructor.put("ward", EditorWardBreakTrigger::new);

        class_to_id.put(EditorAttackTrigger.class, "attack");
        class_to_id.put(EditorCastTrigger.class, "cast");
        class_to_id.put(EditorChannelTrigger.class, "channel");
        class_to_id.put(EditorDealDamageTrigger.class, "deal");
        class_to_id.put(EditorKillTrigger.class, "kill");
        class_to_id.put(EditorMoveTrigger.class, "move");
        class_to_id.put(EditorTakeDamageTrigger.class, "take");
        class_to_id.put(EditorTimerTrigger.class, "timer");
        class_to_id.put(EditorWardBreakTrigger.class, "ward");
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
        instruction.add("Trigger");
        instruction.add("Used to conditionally invoke a certain set of actions.");
        return instruction;
    }
}
