package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.DialogueAsBundleConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDialogueChoice implements IEditorBundle {

    @EditorWrite(name = "Text", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"First line shows on menu, others as tooltip.", "§cThis is a language code, NOT plaintext."})
    public String lc_text = "NOTHINGNESS";
    @EditorBoolean(name = "Correct")
    @EditorTooltip(tooltip = {"Marks a dialogue quest task as complete.", "Will not apply if next dialogue exists."})
    public boolean correct = true;
    @EditorList(name = "Next", singleton = true, constraint = DialogueAsBundleConstraint.class)
    @EditorTooltip(tooltip = {"What dialogue to open after this one."})
    public List<IEditorBundle> dialogue = new ArrayList<>();

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aDialogue Choice")
                .appendLore("§fText: " + this.lc_text)
                .build();
    }

    @Override
    public String getName() {
        return "Choice";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fDialogue Choice");
        instruction.add("A choice for dialogue, do note that this is only meant");
        instruction.add("for narrative steering - choices in dialogue will not be");
        instruction.add("able to branch quest progress.");
        return instruction;
    }
}
