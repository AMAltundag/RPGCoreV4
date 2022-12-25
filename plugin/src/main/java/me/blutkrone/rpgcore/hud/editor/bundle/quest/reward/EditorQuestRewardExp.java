package me.blutkrone.rpgcore.hud.editor.bundle.quest.reward;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.AbstractEditorQuestReward;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.AttributeConstraint;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import me.blutkrone.rpgcore.quest.reward.impl.CoreExpReward;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorQuestRewardExp extends AbstractEditorQuestReward {

    @EditorNumber(name = "Amount", minimum = 0d)
    @EditorTooltip(tooltip = {"Grants an amount of experience"})
    public double experience = 0d;
    @EditorNumber(name = "Source LVL", minimum = 0d)
    @EditorTooltip(tooltip = {"Which level to scale from, 0 to not scale"})
    public double scaling_level = 0d;
    @EditorList(name = "Scaling", constraint = AttributeConstraint.class)
    @EditorTooltip(tooltip = {"Attributes to increase exp gained", "Attribute 'exp_multi_quest' works for all quests"})
    public List<String> scaling_attributes = new ArrayList<>();

    public EditorQuestRewardExp() {
    }

    @Override
    public AbstractQuestReward build() {
        return new CoreExpReward(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aExp Reward")
                .appendLore("§fAmount: " + ((int) this.experience))
                .appendLore("§fSource Level: " + ((int) this.scaling_level))
                .build();
    }

    @Override
    public String getName() {
        return "Exp";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Exp Reward");
        instruction.add("Grants experience to the player completing a quest.");
        return instruction;
    }
}
