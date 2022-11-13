package me.blutkrone.rpgcore.passive.node;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.bundle.passive.EditorPassiveSocketSkillReference;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.skill.CoreSkill;

import java.util.List;
import java.util.stream.Collectors;

public class CorePassiveSocketSkillReference extends AbstractCorePassive {

    private final List<String> tags;

    public CorePassiveSocketSkillReference(EditorPassiveSocketSkillReference editor) {
        this.tags = editor.tags.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public boolean isSocket() {
        return true;
    }

    @Override
    public boolean canSocket(CoreItem stack) {
        // ensure we are dealing with a skill
        String skill = stack.getHidden("skill").orElse(null);
        if (skill == null) {
            return false;
        }
        // if tag filter is empty, any skill works
        if (tags.isEmpty()) {
            return true;
        }
        // compare against relevant tags
        CoreSkill core_skill = RPGCore.inst().getSkillManager().getIndex().get(skill);
        for (String tag : core_skill.getTags()) {
            if (this.tags.contains(tag)) {
                return true;
            }
        }
        // false if no tags matched up
        return false;
    }
}
