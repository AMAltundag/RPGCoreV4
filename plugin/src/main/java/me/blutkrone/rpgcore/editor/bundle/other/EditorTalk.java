package me.blutkrone.rpgcore.editor.bundle.other;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.reference.index.DialogueConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.NPCConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorTalk implements IEditorBundle {

    @EditorWrite(name = "Dialogue", constraint = DialogueConstraint.class)
    @EditorTooltip(tooltip = {"The dialogue to present.", "§cThis is a language code, NOT plaintext."})
    public String dialogue = "NOTHINGNESS";
    @EditorWrite(name = "NPC", constraint = NPCConstraint.class)
    @EditorTooltip(tooltip = {"Which npc offers the dialogue.", "§cThis is a language code, NOT plaintext."})
    public String npc = "NOTHINGNESS";

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aTalk")
                .appendLore("§fDialogue: " + this.dialogue)
                .appendLore("§fNPC: " + this.npc)
                .build();
    }

    @Override
    public String getName() {
        return "Talk";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Talk");
        instruction.add("Used by quests to show dialogue for a certain NPC.");
        return instruction;
    }

}
