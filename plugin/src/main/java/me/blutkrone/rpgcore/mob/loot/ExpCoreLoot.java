package me.blutkrone.rpgcore.mob.loot;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.loot.EditorLootExperience;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.level.LevelManager;

import java.util.ArrayList;
import java.util.List;

public class ExpCoreLoot extends AbstractCoreLoot {

    private double experience;
    private List<String> attributes;

    public ExpCoreLoot(EditorLootExperience editor) {
        this.experience = editor.experience;
        this.attributes = new ArrayList<>(editor.scaling_attributes);
    }

    @Override
    public void offer(CoreEntity killed, List<CorePlayer> killer) {
        LevelManager manager = RPGCore.inst().getLevelManager();
        for (CorePlayer player : killer) {
            int self = player.getCurrentLevel();
            int other = killed.getCurrentLevel();
            double experience = this.experience * manager.getMultiplier(self, other);
            double multiplier = 1d;
            multiplier += player.evaluateAttribute("EXP_MULTI_KILL");
            for (String attribute : this.attributes) {
                multiplier += player.evaluateAttribute(attribute);
            }
            experience = experience * multiplier;
            if (experience > 0d) {
                player.setCurrentExp(player.getCurrentExp() + experience);
            }
        }
    }
}
