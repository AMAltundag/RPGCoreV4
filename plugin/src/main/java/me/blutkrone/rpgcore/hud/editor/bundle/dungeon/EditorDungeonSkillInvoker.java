package me.blutkrone.rpgcore.hud.editor.bundle.dungeon;

import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.dungeon.structure.SkillStructure;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorLogicMultiMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDungeonSkillInvoker extends AbstractEditorDungeonStructure {

    @EditorCategory(icon = Material.EXPERIENCE_BOTTLE, info = "Skill")
    @EditorBoolean(name = "Important")
    @EditorTooltip(tooltip = "Ignore range condition")
    public boolean important = false;
    @EditorBoolean(name = "Single")
    @EditorTooltip(tooltip = "Will only invoke once, rather then repeatedly")
    public boolean permanent = false;
    @EditorNumber(name = "Interval")
    @EditorTooltip(tooltip = "Interval to check activation condition.")
    public double interval = 60d;
    @EditorCategory(info = "Skill", icon = Material.EXPERIENCE_BOTTLE)
    @EditorBundle(name = "Logic")
    @EditorTooltip(tooltip = {"Mechanic to invoke when activated"})
    public EditorLogicMultiMechanic logic = new EditorLogicMultiMechanic();

    @Override
    public AbstractDungeonStructure build() {
        return new SkillStructure(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.EXPERIENCE_BOTTLE)
                .name("Skill Structure")
                .appendLore("§cID: " + this.sync_id)
                .appendLore("§fActivation: X" + this.activation.size())
                .appendLore("§fRange: " + this.range)
                .appendLore("§fHidden: " + this.hidden)
                .appendLore("§fPermanent: " + this.permanent)
                .appendLore("§fInterval: " + this.interval)
                .appendLore("§fLogic: X" + this.logic.actions.size())
                .build();
    }

    @Override
    public String getName() {
        return "Skill";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Skill Structure");
        instruction.add("Invokes the given skill logic.");
        instruction.add("");
        instruction.add("The initial target will be the location");
        instruction.add("The context will be the location");
        return instruction;
    }
}