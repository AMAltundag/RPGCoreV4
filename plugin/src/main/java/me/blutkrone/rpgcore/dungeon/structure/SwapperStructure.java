package me.blutkrone.rpgcore.dungeon.structure;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.hud.editor.bundle.dungeon.EditorDungeonBlockSwapper;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Switch between blocks depending on the condition
 * that was met, if no condition is met it will be
 * air instead.
 * <p>
 * Hint: Use a command block trigger for block-replace
 */
public class SwapperStructure extends AbstractDungeonStructure<Boolean> {

    private final int interval;
    private final Material material_failure;
    private final Material material_success;
    private final List<AbstractCoreSelector> activation;
    private final boolean permanent;
    private final double range;
    private final boolean physics;

    public SwapperStructure(EditorDungeonBlockSwapper editor) {
        super(editor);
        this.interval = ((int) editor.interval);
        this.material_failure = editor.material_failure;
        this.material_success = editor.material_success;
        this.activation = AbstractEditorSelector.unwrap(editor.activation);
        this.permanent = editor.permanent;
        this.range = editor.range * editor.range;
        this.physics = editor.physics;
    }

    @Override
    public void clean(IDungeonInstance instance) {

    }

    @Override
    public void update(ActiveDungeonInstance instance, List<StructureData<Boolean>> where) {
        if (RPGCore.inst().getTimestamp() % this.interval != 0) {
            return;
        }

        where.removeIf((datum -> {
            boolean before = datum.data != null && datum.data;

            // only update while players are nearby
            List<Player> watching = RPGCore.inst().getEntityManager().getObserving(datum.where);
            watching.removeIf(player -> player.getLocation().distanceSquared(datum.where) > this.range);
            if (watching.isEmpty()) {
                return false;
            }

            // use failure material if condition has failed
            if (AbstractCoreSelector.doSelect(this.activation, datum.context, Collections.singletonList(new IOrigin.SnapshotOrigin(datum.where))).isEmpty()) {
                if (before) {
                    datum.where.getBlock().setType(this.material_failure, this.physics);
                    datum.data = false;
                }
                return false;
            }

            // use success material if condition has matched
            if (!before) {
                datum.where.getBlock().setType(this.material_success, this.physics);
                datum.data = true;

                if (this.permanent) {
                    return true;
                }
            }

            return false;
        }));
    }

    @Override
    public void update(EditorDungeonInstance instance, List<StructureData<Boolean>> where) {
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
