package me.blutkrone.rpgcore.hud.editor.bundle.quest.reward;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.AbstractEditorQuestReward;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import me.blutkrone.rpgcore.quest.reward.impl.CoreTraitReward;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorQuestRewardTrait extends AbstractEditorQuestReward {

    @EditorWrite(name = "Trait", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Which trait tag to acquire.")
    public String trait = "none";

    public EditorQuestRewardTrait() {
    }

    @Override
    public AbstractQuestReward build() {
        return new CoreTraitReward(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aTrait Reward")
                .appendLore("§fTrait: " + this.trait)
                .build();
    }

    @Override
    public String getName() {
        return "Trait";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Trait Reward");
        instruction.add("Grants a trait tag which can unlock additional traits");
        instruction.add("on a NPC who wants that trait tag.");
        return instruction;
    }
}
