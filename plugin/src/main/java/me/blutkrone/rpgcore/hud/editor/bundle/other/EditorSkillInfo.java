package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.ModifierStyleConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;
import me.blutkrone.rpgcore.skill.info.CoreSkillInfo;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorSkillInfo implements IEditorBundle {

    @EditorWrite(name = "Readable", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Description of modifier, usage depends on style.", "§cThis is a language code, NOT plaintext."})
    public String lc_readable = "NOTHINGNESS";
    @EditorWrite(name = "Style", constraint = ModifierStyleConstraint.class)
    @EditorTooltip(tooltip = {"Readable Style", "How to present the readable information."})
    public ModifierStyle readable_style = ModifierStyle.GENERIC;
    @EditorWrite(name = "Category", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Organize non-header type descriptions.", "§cThis is a language code, NOT plaintext."})
    public String lc_category = "NOTHINGNESS";
    @EditorBundle(name = "Value")
    @EditorTooltip(tooltip = {"What custom value you want to be rendered."})
    public EditorModifierNumber value = new EditorModifierNumber(0d);

    public CoreSkillInfo build() {
        return new CoreSkillInfo(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aSkill Info")
                .appendLore("§fReadable: " + lc_readable)
                .appendLore("§fStyle: " + readable_style)
                .appendLore("§fCategory: " + lc_category)
                .appendLore("§fValue: " + value.base_value)
                .build();
    }

    @Override
    public String getName() {
        return "Info";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Skill Info");
        instruction.add("This is meant to quantify information about the skill, the");
        instruction.add("Information about the skill binding is automated.");
        instruction.add("");
        instruction.add("This is functionally identical to item modifiers.");
        return instruction;
    }
}
