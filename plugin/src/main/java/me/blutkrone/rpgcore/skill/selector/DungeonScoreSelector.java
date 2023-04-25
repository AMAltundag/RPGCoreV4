package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorDungeonScoreSelector;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DungeonScoreSelector extends AbstractCoreSelector {

    private final double minimum;
    private final double maximum;
    private final boolean party;
    private final List<String> scores;

    public DungeonScoreSelector(EditorDungeonScoreSelector editor) {
        this.minimum = editor.minimum;
        this.maximum = editor.maximum;
        this.party = editor.party;
        this.scores = new ArrayList<>(editor.scores);
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        List<IOrigin> updated = new ArrayList<>();

        for (IOrigin origin : previous) {
            World world = origin.getWorld();
            IDungeonInstance instance = RPGCore.inst().getDungeonManager().getInstance(world);
            if (instance instanceof ActiveDungeonInstance) {
                Map<String, Double> score_map = ((ActiveDungeonInstance) instance).getScore();
                double total = 0d;
                for (String score : scores) {
                    if (party) {
                        total += score_map.getOrDefault(score, 0d);
                    } else if (origin instanceof CorePlayer) {
                        total += score_map.getOrDefault(score + "_" + ((CorePlayer) origin).getUniqueId(), 0d);
                    }
                }

                if (total >= minimum && total <= maximum) {
                    updated.add(origin);
                }
            }
        }

        return updated;
    }
}
