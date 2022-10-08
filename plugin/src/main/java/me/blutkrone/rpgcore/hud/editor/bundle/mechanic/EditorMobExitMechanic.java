package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.ExitMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorMobExitMechanic extends AbstractEditorMechanic {
    @Override
    public AbstractCoreMechanic build() {
        return new ExitMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.RED_BANNER)
                .name("§fMob Exit")
                .build();
    }

    @Override
    public String getName() {
        return "Mob Exit";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Mob Exit Mechanic");
        instruction.add("Exits the execution of a mob AI branch, do note that");
        instruction.add("This is intended for mobs only!");
        return instruction;
    }
}
