package me.blutkrone.rpgcore.dungeon.structure;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.hud.editor.bundle.dungeon.EditorDungeonSpawnpoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class SpawnpointStructure extends AbstractDungeonStructure<Object> {

    public SpawnpointStructure(EditorDungeonSpawnpoint editor) {
        super(editor);
    }

    @Override
    public void clean(IDungeonInstance instance) {

    }

    @Override
    public void update(ActiveDungeonInstance instance, List<StructureData<Object>> where) {
        // processed on dungeon creation
    }

    @Override
    public void update(EditorDungeonInstance instance, List<StructureData<Object>> where) {
        if (RPGCore.inst().getTimestamp() % 20 == 0) {
            for (StructureData<?> structure : where) {
                if (structure.highlight == null) {
                    int x = structure.where.getBlockX();
                    int y = structure.where.getBlockY();
                    int z = structure.where.getBlockZ();
                    structure.highlight = RPGCore.inst().getVolatileManager().getPackets().highlight(x, y, z);
                }

                List<Player> watching = RPGCore.inst().getEntityManager().getObserving(structure.where);
                watching.removeIf(player -> player.getLocation().distance(structure.where) > 32);
                for (Player player : watching) {
                    structure.highlight.enable(player);
                    Bukkit.getScheduler().runTaskLater(RPGCore.inst(), () -> {
                        structure.highlight.disable(player);
                    }, 10);
                }
            }
        }
    }
}