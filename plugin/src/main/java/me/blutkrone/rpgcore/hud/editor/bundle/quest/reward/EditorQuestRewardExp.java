package me.blutkrone.rpgcore.hud.editor.bundle.quest.reward;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.AbstractEditorQuestReward;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import me.blutkrone.rpgcore.quest.reward.impl.CoreExpReward;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorQuestRewardExp extends AbstractEditorQuestReward {

    @EditorWrite(name = "Type", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "What type of experience is awarded")
    public String type = "player";
    @EditorNumber(name = "Exp", minimum = 0d)
    @EditorTooltip(tooltip = "Grants a flat amount of experience.")
    public double exp = 0d;
    @EditorNumber(name = "Levels", minimum = 0d)
    @EditorTooltip(tooltip = "Grants levels (including partials.)")
    public double level = 0d;
    @EditorNumber(name = "Scaling", minimum = 0d)
    @EditorTooltip(tooltip = {"Applies penalty based on player level", "Use 0 for no penalty."})
    public double scaling = 0d;


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
                .appendLore("§fExp: " + ((int) this.exp))
                .appendLore(String.format("§fLevel: %.2f%%", (this.level * 100d)))
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
        instruction.add("Grants either levels or experience, flat experience will");
        instruction.add("be applied first.");
        return instruction;
    }
}
