package me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.binding.EditorCastBind;
import me.blutkrone.rpgcore.hud.editor.bundle.binding.EditorChannelBind;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SkillBindingConstraint extends AbstractMultiConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("cast", EditorCastBind::new);
        id_to_constructor.put("channel", EditorChannelBind::new);
        class_to_id.put(EditorCastBind.class, "cast");
        class_to_id.put(EditorChannelBind.class, "channel");
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
        instruction.add("Binding");
        instruction.add("Used by players to control their skills.");
        return instruction;
    }
}
