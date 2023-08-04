package me.blutkrone.rpgcore.dungeon.structure;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.editor.bundle.dungeon.EditorDungeonBlock;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Switch between blocks depending on the condition
 * that was met, if no condition is met it will be
 * air instead.
 * <br>
 * Hint: Use a command block trigger for block-replace
 */
public class BlockStructure extends AbstractDungeonStructure<Boolean> {

    private final int interval;
    private final Material material_failure;
    private final Material material_success;
    private final List<AbstractCoreSelector> activation;
    private final boolean permanent;
    private final boolean physics;
    private final int proliferate;

    private Map<BlockVector, List<BlockVector>> proliferated;

    public BlockStructure(EditorDungeonBlock editor) {
        super(editor);
        this.interval = ((int) editor.interval);
        this.material_failure = editor.material_failure;
        this.material_success = editor.material_success;
        this.activation = AbstractEditorSelector.unwrap(editor.activation);
        this.permanent = editor.permanent;
        this.physics = editor.physics;
        this.proliferate = (int) editor.proliferate;
        this.proliferated = new HashMap<>();
        for (EditorDungeonBlock.Proliferation proliferation : editor.proliferations) {
            this.proliferated.put(proliferation.source, proliferation.targets);
        }
    }

    /**
     * The radius we want to apply proliferation to.
     *
     * @return Proliferation radius.
     */
    public int getProliferateRadius() {
        return proliferate;
    }

    /**
     * A mapping of original location mapped to the
     * related proliferation targets.
     *
     * @return Proliferated locations
     */
    public Map<BlockVector, List<BlockVector>> getProliferated() {
        return proliferated;
    }

    @Override
    public void update(ActiveDungeonInstance instance, List<StructureData<Boolean>> where) {
        if (RPGCore.inst().getTimestamp() % this.interval != 0) {
            return;
        }

        where.removeIf((datum -> {
            // only update while players are nearby
            if (!datum.activated) {
                return false;
            }

            // do not update unless status has changed
            boolean after = !AbstractCoreSelector.doSelect(this.activation, datum.context, Collections.singletonList(new IOrigin.SnapshotOrigin(datum.where))).isEmpty();
            if (datum.data != null && datum.data == after) {
                return false;
            }

            // decide on material to apply
            Material material = after ? this.material_success : this.material_failure;
            List<BlockVector> vectors = this.proliferated.get(datum.where.toVector().toBlockVector());
            if (vectors != null) {
                for (BlockVector vector : vectors) {
                    Location block = vector.toLocation(instance.getWorld());
                    block.getBlock().setType(material, this.physics);
                }
            } else {
                datum.where.getBlock().setType(material, this.physics);
            }
            boolean nulled = datum.data == null;
            datum.data = after;

            // remove if instruction isn't permanent
            return !nulled && this.permanent;
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
