package me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.dungeon.*;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMultiConstraint;
import org.bukkit.Bukkit;

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
        id_to_constructor.put("block", EditorDungeonBlockSwapper::new);
        id_to_constructor.put("spawner", EditorDungeonSpawner::new);
        id_to_constructor.put("checkpoint", EditorDungeonCheckpoint::new);
        id_to_constructor.put("spawnpoint", EditorDungeonSpawnpoint::new);
        class_to_id.put(EditorDungeonSkillInvoker.class, "skill");
        class_to_id.put(EditorDungeonBlockSwapper.class, "block");
        class_to_id.put(EditorDungeonSpawner.class, "spawner");
        class_to_id.put(EditorDungeonCheckpoint.class, "checkpoint");
        class_to_id.put(EditorDungeonSpawnpoint.class, "spawnpoint");

        Bukkit.getLogger().severe("not implemented (treasure structure)");
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
