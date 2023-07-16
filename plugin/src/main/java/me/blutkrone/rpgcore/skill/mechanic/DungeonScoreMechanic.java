package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorDungeonScoreMechanic;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public class DungeonScoreMechanic extends AbstractCoreMechanic {

    private final double grant;
    private final boolean party;
    private final String score;

    public DungeonScoreMechanic(EditorDungeonScoreMechanic editor) {
        this.grant = editor.grant;
        this.party = editor.party;
        this.score = editor.score;
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        for (IOrigin origin : targets) {
            World world = origin.getWorld();
            IDungeonInstance instance = RPGCore.inst().getDungeonManager().getInstance(world);
            if (instance instanceof ActiveDungeonInstance) {
                Map<String, Double> score_map = ((ActiveDungeonInstance) instance).getScore();
                if (party) {
                    score_map.merge(this.score, grant, (a, b) -> a + b);
                } else if (origin instanceof CorePlayer) {
                    score_map.merge(this.score + "_" + ((CorePlayer) origin).getUniqueId(), grant, (a, b) -> a + b);
                }
            }
        }
    }
}
