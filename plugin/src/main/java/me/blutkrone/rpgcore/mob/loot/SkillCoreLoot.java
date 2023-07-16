package me.blutkrone.rpgcore.mob.loot;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.loot.EditorLootSkill;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.root.passive.EditorPassiveTree;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.passive.CorePassiveTree;
import me.blutkrone.rpgcore.skill.CoreSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SkillCoreLoot extends AbstractCoreLoot {

    private double multi_chance;
    private int maximum;

    public SkillCoreLoot(EditorLootSkill editor) {
        this.multi_chance = editor.multi_chance;
        this.maximum = (int) editor.maximum;
    }

    @Override
    public void offer(CoreEntity killed, List<CorePlayer> killer) {
        EditorIndex<CorePassiveTree, EditorPassiveTree> tree_index = RPGCore.inst().getPassiveManager().getTreeIndex();

        for (CorePlayer player : killer) {
            // look up skills on skillbar
            List<CoreSkill> skills = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                CoreSkill skill = player.getSkillbar().getSkill(i);
                if (skill != null && !skills.contains(skill)) {
                    skills.add(skill);
                }
            }
            // do nothing if we have no skills
            if (skills.isEmpty()) {
                continue;
            }
            // offer one guaranteed point
            CoreSkill skill = skills.remove(ThreadLocalRandom.current().nextInt(skills.size()));
            String point_type = tree_index.get("skill_" + skill.getId()).getPoint();
            int point_have = player.getPassivePoints().getOrDefault(point_type, 0);
            if (point_have < this.maximum) {
                player.getPassivePoints().merge(point_type, 1, (a, b) -> a + b);
            }
            // chance for any other skill to gain a point
            for (CoreSkill _skill : skills) {
                point_type = tree_index.get("skill_" + _skill.getId()).getPoint();
                point_have = player.getPassivePoints().getOrDefault(point_type, 0);
                if (point_have < this.maximum && Math.random() <= this.multi_chance) {
                    player.getPassivePoints().merge(point_type, 1, (a, b) -> a + b);
                }
            }
        }
    }
}
