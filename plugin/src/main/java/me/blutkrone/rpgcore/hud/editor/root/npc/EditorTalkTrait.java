package me.blutkrone.rpgcore.hud.editor.root.npc;

import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.npc.trait.impl.CoreTalkTrait;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorTalkTrait extends AbstractEditorNPCTrait {

    public transient File file;

    public EditorTalkTrait() {
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
        return new CoreTalkTrait(this);
    }

    @Override
    public String getCortexSymbol() {
        return "default"; // will not appear in cortex
    }

    @Override
    public String getIconLC() {
        return "default"; // will not appear in cortex
    }
}
