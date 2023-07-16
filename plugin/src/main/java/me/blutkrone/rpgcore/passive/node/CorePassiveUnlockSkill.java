package me.blutkrone.rpgcore.passive.node;

import me.blutkrone.rpgcore.editor.bundle.passive.EditorPassiveUnlockSkill;
import me.blutkrone.rpgcore.item.CoreItem;

import java.util.ArrayList;
import java.util.List;

public class CorePassiveUnlockSkill extends AbstractCorePassive {

    private List<String> skills;

    public CorePassiveUnlockSkill(EditorPassiveUnlockSkill editor) {
        this.skills = new ArrayList<>(editor.skill_unlocks);
    }

    /**
     * The hidden skills that unlock if we have this node.
     *
     * @return skills to unlock
     */
    public List<String> getSkills() {
        return skills;
    }

    @Override
    public boolean isSocket() {
        return false;
    }

    @Override
    public boolean canSocket(CoreItem stack) {
        return false;
    }
}
