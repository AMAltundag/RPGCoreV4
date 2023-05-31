package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorDungeonScoreSelector;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DungeonScoreSelector extends AbstractCoreSelector {

    private final double minimum;
    private final double maximum;
    private final List<Range> exact;
    private final boolean party;
    private final List<String> scores;

    public DungeonScoreSelector(EditorDungeonScoreSelector editor) {
        this.minimum = editor.minimum;
        this.maximum = editor.maximum;
        this.party = editor.party;
        this.scores = new ArrayList<>(editor.scores);
        this.exact = new ArrayList<>();
        for (String score : editor.exact) {
            this.exact.add(new Range(score));
        }
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
                for (String score : this.scores) {
                    if (this.party) {
                        total += score_map.getOrDefault(score, 0d);
                    } else if (origin instanceof CorePlayer) {
                        total += score_map.getOrDefault(score + "_" + ((CorePlayer) origin).getUniqueId(), 0d);
                    }
                }

                if (!this.exact.isEmpty()) {
                    boolean overlap = false;
                    for (Range range : this.exact) {
                        overlap = overlap || range.contains(total);
                    }
                    if (overlap) {
                        updated.add(origin);
                    }
                } else if (total >= this.minimum && total <= this.maximum) {
                    updated.add(origin);
                }
            }
        }

        return updated;
    }

    /**
     * A number range to constrain the score to.
     */
    public class Range {
        public int start;
        public int finish;

        public Range(String string) {
            try {
                String[] split = string.split("\\~");
                this.start = Double.valueOf(split[0]).intValue();
                if (split.length == 2) {
                    this.finish = Double.valueOf(split[1]).intValue();
                } else {
                    this.finish = Double.valueOf(split[0]).intValue();
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Bad number range: " + string);
                this.start = 0;
                this.finish = 0;
            }
        }

        public boolean contains(double value) {
            return value >= start && value <= finish;
        }
    }
}
