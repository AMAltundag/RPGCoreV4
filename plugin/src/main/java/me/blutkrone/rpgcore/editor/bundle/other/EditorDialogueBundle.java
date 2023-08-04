package me.blutkrone.rpgcore.editor.bundle.other;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.DialogueChoiceConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.quest.dialogue.CoreDialogue;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a special case where-in the implementation must be available as either
 * a bundle or a root.
 * <br>
 * We need the bundle in-case we want to nest dialogue
 * <br>
 * We need the editor in-case we want to access the dialogue
 */
public class EditorDialogueBundle implements IEditorBundle {

    @EditorWrite(name = "Text", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Contents of the dialogue", "Longer lines are automatically split", "Using a # will do a line-break", "Write 'portrait_?' to change portrait", "Write 'dialogue_?' to show a dialogue image", "§cThis is a language code, NOT plaintext."})
    public String lc_text = "NOTHINGNESS";
    @EditorWrite(name = "Question", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Question presented when we got choices", "§cThis is a language code, NOT plaintext."})
    public String lc_question = "NOTHINGNESS";
    @EditorList(name = "Choices", constraint = DialogueChoiceConstraint.class)
    @EditorTooltip(tooltip = {"Choices within the dialogue.", "This is only for narrative, cannot branch progress."})
    public List<IEditorBundle> choices = new ArrayList<>();

    public transient File file;

    public EditorDialogueBundle() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aDialogue")
                .appendLore("§fText: " + this.lc_text)
                .appendLore("§fQuestion: " + this.lc_question)
                .appendLore("§fChoices: " + this.choices.size())
                .build();
    }

    @Override
    public String getName() {
        return "Dialogue";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Dialogue");
        instruction.add("A dialogue meant for NPC/Quest, do note that while");
        instruction.add("Your dialogue can branch, the result is the same.");
        return instruction;
    }

    public CoreDialogue build() {
        return new CoreDialogue(this);
    }
}
