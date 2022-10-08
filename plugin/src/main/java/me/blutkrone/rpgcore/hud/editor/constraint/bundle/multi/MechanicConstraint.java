package me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.*;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MechanicConstraint extends AbstractMultiConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("branch", EditorLogicBranchMechanic::new);
        id_to_constructor.put("multi", EditorLogicMultiMechanic::new);
        id_to_constructor.put("exit", EditorMobExitMechanic::new);
        id_to_constructor.put("sleep", EditorMobSleepMechanic::new);
        id_to_constructor.put("stall", EditorMobStallMechanic::new);
        id_to_constructor.put("barrier", EditorMobBarrierMechanic::new);
        id_to_constructor.put("flag", EditorLogicFlagMechanic::new);
        id_to_constructor.put("limit", EditorDoNotDieMechanic::new);
        id_to_constructor.put("status", EditorStatusMechanic::new);
        id_to_constructor.put("potion", EditorPotionMechanic::new);
        id_to_constructor.put("gravity", EditorGravityMechanic::new);
        id_to_constructor.put("taunt", EditorTauntMechanic::new);
        id_to_constructor.put("thrust", EditorThrustMechanic::new);
        id_to_constructor.put("velocity", EditorVelocityMechanic::new);
        id_to_constructor.put("damage", EditorDamageMechanic::new);
        id_to_constructor.put("anchor", EditorAnchorMechanic::new);
        id_to_constructor.put("area", EditorAreaMechanic::new);
        id_to_constructor.put("beam", EditorBeamMechanic::new);
        id_to_constructor.put("blast", EditorBlastMechanic::new);
        id_to_constructor.put("bolt", EditorBoltMechanic::new);
        id_to_constructor.put("chain", EditorChainMechanic::new);
        id_to_constructor.put("totem", EditorTotemMechanic::new);
        id_to_constructor.put("trap", EditorTrapMechanic::new);
        id_to_constructor.put("stroll", EditorStrollMechanic::new);
        id_to_constructor.put("walk", EditorWalkMechanic::new);
        id_to_constructor.put("engage", EditorMobEngage::new);
        id_to_constructor.put("stand", EditorMobStandMechanic::new);
        id_to_constructor.put("face", EditorFaceMechanic::new);
        id_to_constructor.put("effect", EditorEffectMechanic::new);
        id_to_constructor.put("hint", EditorHintMechanic::new);

        class_to_id.put(EditorLogicBranchMechanic.class, "branch");
        class_to_id.put(EditorLogicMultiMechanic.class, "multi");
        class_to_id.put(EditorMobExitMechanic.class, "exit");
        class_to_id.put(EditorMobSleepMechanic.class, "sleep");
        class_to_id.put(EditorMobStallMechanic.class, "stall");
        class_to_id.put(EditorMobBarrierMechanic.class, "barrier");
        class_to_id.put(EditorLogicFlagMechanic.class, "flag");
        class_to_id.put(EditorDoNotDieMechanic.class, "limit");
        class_to_id.put(EditorStatusMechanic.class, "status");
        class_to_id.put(EditorPotionMechanic.class, "potion");
        class_to_id.put(EditorGravityMechanic.class, "gravity");
        class_to_id.put(EditorTauntMechanic.class, "taunt");
        class_to_id.put(EditorThrustMechanic.class, "thrust");
        class_to_id.put(EditorVelocityMechanic.class, "velocity");
        class_to_id.put(EditorDamageMechanic.class, "damage");
        class_to_id.put(EditorAnchorMechanic.class, "anchor");
        class_to_id.put(EditorAreaMechanic.class, "area");
        class_to_id.put(EditorBeamMechanic.class, "beam");
        class_to_id.put(EditorBlastMechanic.class, "blast");
        class_to_id.put(EditorBoltMechanic.class, "bolt");
        class_to_id.put(EditorChainMechanic.class, "chain");
        class_to_id.put(EditorTotemMechanic.class, "totem");
        class_to_id.put(EditorTrapMechanic.class, "trap");
        class_to_id.put(EditorStrollMechanic.class, "stroll");
        class_to_id.put(EditorWalkMechanic.class, "walk");
        class_to_id.put(EditorMobEngage.class, "engage");
        class_to_id.put(EditorMobStandMechanic.class, "stand");
        class_to_id.put(EditorFaceMechanic.class, "face");
        class_to_id.put(EditorEffectMechanic.class, "effect");
        class_to_id.put(EditorHintMechanic.class, "hint");
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
        instruction.add("Mechanic");
        instruction.add("Actions invoked by players or mobs.");
        return instruction;
    }
}
