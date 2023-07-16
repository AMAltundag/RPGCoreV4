package me.blutkrone.rpgcore.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.dungeon.*;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMultiConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DungeonStructureConstraint extends AbstractMultiConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("skill", EditorDungeonSkillInvoker::new);
        id_to_constructor.put("block", EditorDungeonBlock::new);
        id_to_constructor.put("spawner", EditorDungeonSpawner::new);
        id_to_constructor.put("checkpoint", EditorDungeonCheckpoint::new);
        id_to_constructor.put("spawnpoint", EditorDungeonSpawnpoint::new);
        id_to_constructor.put("treasure", EditorDungeonTreasure::new);
        class_to_id.put(EditorDungeonSkillInvoker.class, "skill");
        class_to_id.put(EditorDungeonBlock.class, "block");
        class_to_id.put(EditorDungeonSpawner.class, "spawner");
        class_to_id.put(EditorDungeonCheckpoint.class, "checkpoint");
        class_to_id.put(EditorDungeonSpawnpoint.class, "spawnpoint");
        class_to_id.put(EditorDungeonTreasure.class, "treasure");
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
        instruction.add("Dungeon Structure");
        instruction.add("Special logic available within a dungeon.");
        return instruction;
    }
}
