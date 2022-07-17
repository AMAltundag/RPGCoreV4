package me.blutkrone.rpgcore.hud.editor.root.npc;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.npc.trait.impl.CoreDialogueTrait;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorDialogueTrait extends AbstractEditorNPCTrait {

    @EditorCategory(info = "Cortex", icon = Material.FURNACE)
    @EditorWrite(name = "Icon", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Symbol to show on the NPC menu", "Only relevant with multiple NPC traits."})
    public String symbol = "default";
    @EditorWrite(name = "Text", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Description of this trait", "§cThis is a language code, NOT plaintext."})
    public String lc_text = "NOTHINGNESS";

    public transient File file;

    public EditorDialogueTrait() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("§fBanker Trait")
                .build();
    }

    @Override
    public String getName() {
        return "Banker";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fBanker Trait");
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
}
