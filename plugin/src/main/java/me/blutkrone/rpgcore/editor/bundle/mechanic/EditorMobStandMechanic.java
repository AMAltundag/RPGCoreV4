package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.MobStandMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorMobStandMechanic extends AbstractEditorMechanic {

    @Override
    public AbstractCoreMechanic build() {
        return new MobStandMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ANVIL)
                .name("§fMob Stand")
                .build();
    }

    @Override
    public String getName() {
        return "Mob Stand";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Mob Stand");
        instruction.add("Stop the current pathing of the mob, does not");
        instruction.add("new requests to pathing.");
        return instruction;
    }
}
