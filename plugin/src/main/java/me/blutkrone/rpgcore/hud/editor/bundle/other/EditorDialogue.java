package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorName;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.DialogueChoiceConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.quest.dialogue.CoreDialogue;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@EditorName(name = "Dialogue")
@EditorTooltip(tooltip = "Dialogue for NPC/Quest")
public class EditorDialogue implements IEditorRoot<CoreDialogue> {

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

    public EditorDialogue() {
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
        instruction.add("§fDialogue");
        instruction.add("A dialogue meant for NPC/Quest, do note that while");
        instruction.add("Your dialogue can branch, the result is the same.");
        return instruction;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
            RPGCore.inst().getGson().toJson(this, fw);
        }
    }

    @Override
    public CoreDialogue build(String id) {
        return new CoreDialogue(this);
    }
}
