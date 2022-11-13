package me.blutkrone.rpgcore.passive.node;

import me.blutkrone.rpgcore.hud.editor.bundle.passive.EditorPassiveSocketSkillAttribute;
import me.blutkrone.rpgcore.item.CoreItem;

import java.util.List;
import java.util.stream.Collectors;

public class CorePassiveSocketSkillAttribute extends AbstractCorePassive {

    private final List<String> tags;

    public CorePassiveSocketSkillAttribute(EditorPassiveSocketSkillAttribute editor) {
        this.tags = editor.tags.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public boolean isSocket() {
        return true;
    }

    @Override
    public boolean canSocket(CoreItem item) {
        // compare against relevant tags
        for (String tag : item.getTags()) {
            if (this.tags.contains(tag)) {
                return true;
            }
        }
        // not a valid item
        return false;
    }
}
