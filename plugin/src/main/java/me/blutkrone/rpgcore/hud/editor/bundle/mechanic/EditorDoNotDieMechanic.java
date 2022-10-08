package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.MobDoNotDieMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDoNotDieMechanic extends AbstractEditorMechanic {

    @EditorBoolean(name = "Enabled")
    @EditorTooltip(tooltip = "Whether to prevent death.")
    public boolean enable = false;

    @Override
    public AbstractCoreMechanic build() {
        return new MobDoNotDieMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.GOLDEN_CHESTPLATE)
                .name("§fMob Do Not Die")
                .build();
    }

    @Override
    public String getName() {
        return "Mob Do Not Die";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Mob Do Not Die");
        instruction.add("Prevents death, but not damage. Life will not drop");
        instruction.add("Below a minimum threshold of 1.");
        return instruction;
    }
}
