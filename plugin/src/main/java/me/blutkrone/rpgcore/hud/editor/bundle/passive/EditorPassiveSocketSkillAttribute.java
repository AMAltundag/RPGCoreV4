package me.blutkrone.rpgcore.hud.editor.bundle.passive;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.passive.node.AbstractCorePassive;
import me.blutkrone.rpgcore.passive.node.CorePassiveSocketSkillAttribute;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorPassiveSocketSkillAttribute extends AbstractEditorPassive {

    @EditorList(name = "Tags", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Item needs one of these tags to be socketed."})
    public List<String> tags = new ArrayList<>();

    @Override
    public AbstractCorePassive build() {
        return new CorePassiveSocketSkillAttribute(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.EXPERIENCE_BOTTLE)
                .name("§fSocket Passive")
                .lore("§fTags: " + this.tags)
                .build();
    }

    @Override
    public String getName() {
        return "Socket";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Socket Passive");
        instruction.add("");
        instruction.add("Socket items can be read like equipment, the modifiers will");
        instruction.add("Apply directly only to the associated skill.");
        instruction.add("");
        instruction.add("This will only affect the skill the tree belongs to.");
        instruction.add("");
        instruction.add("§cCompatible: Job, Skill");
        return instruction;
    }
}