package me.blutkrone.rpgcore.dungeon.structure;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.editor.bundle.dungeon.EditorDungeonSkillInvoker;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import me.blutkrone.rpgcore.skill.proxy.AbstractSkillProxy;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Invoke skill logic
 */
public class SkillStructure extends AbstractDungeonStructure<Boolean> {

    private final List<AbstractCoreSelector> activation;
    private final boolean permanent;
    private final int interval;
    private final MultiMechanic logic;
    private final boolean important;

    public SkillStructure(EditorDungeonSkillInvoker editor) {
        super(editor);
        this.activation = AbstractEditorSelector.unwrap(editor.activation);
        this.important = editor.important;
        this.permanent = editor.permanent;
        this.interval = ((int) editor.interval);
        this.logic = editor.logic.build();
    }

    @Override
    public void update(ActiveDungeonInstance instance, List<StructureData<Boolean>> where) {
        // match with the interval
        if (RPGCore.inst().getTimestamp() % this.interval != 0) {
            return;
        }

        where.removeIf((datum -> {
            // work off proxies/pipelines,
            datum.context.getProxies().removeIf(AbstractSkillProxy::update);
            datum.context.getPipelines().removeIf(CoreAction.ActionPipeline::update);
            if (datum.data != null && datum.data) {
                return datum.context.getProxies().isEmpty() && datum.context.getPipelines().isEmpty();
            }

            if (!important && !datum.activated) {
                return false;
            }

            // ensure condition has been archived
            if (AbstractCoreSelector.doSelect(activation, datum.context, Collections.singletonList(new IOrigin.SnapshotOrigin(datum.where))).isEmpty()) {
                return false;
            }

            // invoke the logic at the position
            this.logic.doMechanic(datum.context, Collections.singletonList(new IOrigin.SnapshotOrigin(datum.where)));
            // if permanent, we are done now
            if (this.permanent) {
                datum.data = true;
            }
            return permanent;
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