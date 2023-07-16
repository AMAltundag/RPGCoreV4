package me.blutkrone.rpgcore.editor.bundle.npc;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.DialogueConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.npc.trait.impl.CoreDialogueTrait;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorDialogueTrait extends AbstractEditorNPCTrait {

    @EditorCategory(info = "Dialogue", icon = Material.BUNDLE)
    @EditorList(name = "Dialogue", constraint = DialogueConstraint.class, singleton = true)
    @EditorTooltip(tooltip = {"The dialogue to present."})
    public List<String> dialogue = new ArrayList<>();

    @EditorCategory(info = "Cortex", icon = Material.FURNACE)
    @EditorWrite(name = "Icon", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Symbol to show on the NPC menu", "Only relevant with multiple NPC traits."})
    public String symbol = "default";
    @EditorWrite(name = "Text", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Description of this trait", "§cThis is a language code, NOT plaintext."})
    public String lc_text = "NOTHINGNESS";
    @EditorWrite(name = "Unlock", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Flag needed to show the trait"})
    public String unlock = "none";

    public transient File file;

    public EditorDialogueTrait() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("§fDialogue Trait")
                .build();
    }

    @Override
    public String getName() {
        return "Dialogue";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Dialogue Trait");
        return instruction;
    }

    @Override
    public AbstractCoreTrait build() {
        return new CoreDialogueTrait(this);
    }

    @Override
    public String getCortexSymbol() {
        return this.symbol;
    }

    @Override
    public String getIconLC() {
        return this.lc_text;
    }

    @Override
    public String getUnlockFlag() {
        return this.unlock;
    }
}
