package me.blutkrone.rpgcore.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.effect.*;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EffectPartConstraint extends AbstractMultiConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("sound", EditorEffectAudio::new);
        id_to_constructor.put("particle", EditorEffectBrush::new);
        id_to_constructor.put("circle", EditorEffectCircle::new);
        id_to_constructor.put("direction", EditorEffectDirection::new);
        id_to_constructor.put("forward", EditorEffectForward::new);
        id_to_constructor.put("line", EditorEffectLine::new);
        id_to_constructor.put("model", EditorEffectModel::new);
        id_to_constructor.put("point", EditorEffectPoint::new);
        id_to_constructor.put("radiator", EditorEffectRadiator::new);
        id_to_constructor.put("rotate", EditorEffectRotate::new);
        id_to_constructor.put("rotor", EditorEffectRotor::new);
        id_to_constructor.put("sphere", EditorEffectSphere::new);
        id_to_constructor.put("wait", EditorEffectWait::new);
        id_to_constructor.put("repeat", EditorEffectRepeat::new);
        id_to_constructor.put("block", EditorEffectBlock::new);

        class_to_id.put(EditorEffectAudio.class, "sound");
        class_to_id.put(EditorEffectBrush.class, "particle");
        class_to_id.put(EditorEffectCircle.class, "circle");
        class_to_id.put(EditorEffectDirection.class, "direction");
        class_to_id.put(EditorEffectForward.class, "forward");
        class_to_id.put(EditorEffectLine.class, "line");
        class_to_id.put(EditorEffectModel.class, "model");
        class_to_id.put(EditorEffectPoint.class, "point");
        class_to_id.put(EditorEffectRadiator.class, "radiator");
        class_to_id.put(EditorEffectRotate.class, "rotate");
        class_to_id.put(EditorEffectRotor.class, "rotor");
        class_to_id.put(EditorEffectSphere.class, "sphere");
        class_to_id.put(EditorEffectWait.class, "wait");
        class_to_id.put(EditorEffectRepeat.class, "repeat");
        class_to_id.put(EditorEffectBlock.class, "block");
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
        instruction.add("Complex Effect");
        instruction.add("Chain together multiple effect components to.");
        instruction.add("build a visual effect.");
        return instruction;
    }
}
