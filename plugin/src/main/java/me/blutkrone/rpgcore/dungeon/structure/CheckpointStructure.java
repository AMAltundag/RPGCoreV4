package me.blutkrone.rpgcore.dungeon.structure;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.editor.bundle.dungeon.EditorDungeonCheckpoint;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CheckpointStructure extends AbstractDungeonStructure<Object> {

    protected final double range;
    private final List<AbstractCoreSelector> activation;

    public CheckpointStructure(EditorDungeonCheckpoint editor) {
        super(editor);

        this.range = editor.range * editor.range;
        this.activation = AbstractEditorSelector.unwrap(editor.activation);
    }

    @Override
    public void update(ActiveDungeonInstance instance, List<StructureData<Object>> where) {
        if (RPGCore.inst().getTimestamp() % 20 != 0) {
            return;
        }

        for (StructureData<Object> datum : where) {
            if (datum.activated) {
                // grab players within range
                List<Player> watching = RPGCore.inst().getEntityManager().getObserving(datum.where);
                watching.removeIf(player -> player.getLocation().distanceSquared(datum.where) > this.range);
                // ensure we have any players
                if (watching.isEmpty()) {
                    continue;
                }
                // ensure checkpoint can be activated
                if (AbstractCoreSelector.doSelect(activation, datum.context, Collections.singletonList(new IOrigin.SnapshotOrigin(datum.where))).isEmpty()) {
                    continue;
                }
                // apply a temporary dungeon respawn position
                for (Player player : watching) {
                    instance.setCheckpoint(player, datum.where);
                }
            }
        }
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