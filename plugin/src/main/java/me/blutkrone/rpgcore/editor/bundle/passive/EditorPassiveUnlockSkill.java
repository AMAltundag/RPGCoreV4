package me.blutkrone.rpgcore.editor.bundle.passive;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.constraint.reference.index.SkillConstraint;
import me.blutkrone.rpgcore.passive.node.AbstractCorePassive;
import me.blutkrone.rpgcore.passive.node.CorePassiveUnlockSkill;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorPassiveUnlockSkill extends AbstractEditorPassive {

    @EditorList(name = "Skills", constraint = SkillConstraint.class)
    @EditorTooltip(tooltip = {"Hidden skills will be unlocked from this"})
    public List<String> skill_unlocks = new ArrayList<>();

    @Override
    public AbstractCorePassive build() {
        return new CorePassiveUnlockSkill(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ENCHANTED_BOOK)
                .name("§fSkill Passive")
                .lore("§fTotal Skills: " + this.skill_unlocks.size() + "X")
                .build();
    }

    @Override
    public String getName() {
        return "Skill";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Skill Unlock Passive");
        instruction.add("");
        instruction.add("Unlocks a skill that was marked as hidden.");
        instruction.add("");
        instruction.add("§cCompatible: Job, Skill");
        return instruction;
    }
}