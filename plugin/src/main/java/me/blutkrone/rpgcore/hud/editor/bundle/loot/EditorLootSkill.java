package me.blutkrone.rpgcore.hud.editor.bundle.loot;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.mob.loot.AbstractCoreLoot;
import me.blutkrone.rpgcore.mob.loot.SkillCoreLoot;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorLootSkill extends AbstractEditorLoot {

    @EditorNumber(name = "Maximum", minimum = 0d)
    @EditorTooltip(tooltip = {"Only gain a point if skill has less points"})
    public double maximum = 0d;
    @EditorNumber(name = "Chance", minimum = 0d)
    @EditorTooltip(tooltip = {"One skill is guaranteed, others are chance"})
    public double multi_chance = 0d;

    @Override
    public AbstractCoreLoot build() {
        return new SkillCoreLoot(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aSkill Reward")
                .appendLore("§fMaximum: " + ((int) this.maximum))
                .appendLore("§fChance: " + this.multi_chance)
                .build();
    }

    @Override
    public String getName() {
        return "Loot Skill";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Skill Loot");
        instruction.add("Passive points for skills on the players skillbar, points are");
        instruction.add("Only granted if the total is below the maximum. One skill will");
        instruction.add("Always receive a point, others roll a 0-100% chance at it.");
        return instruction;
    }
}
