package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.InterruptMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorInterruptMechanic extends AbstractEditorMechanic {

    @Override
    public AbstractCoreMechanic build() {
        return new InterruptMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.SADDLE)
                .name("Interrupt Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Interrupt";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Interrupt Mechanic");
        instruction.add("Will interrupt the active casting of a player, if they");
        instruction.add("Are channeling the 'last tick' will still happen.");
        instruction.add("");
        instruction.add("Mobs are unaffected, if you want the possibility to interrupt");
        instruction.add("Their behaviour include it within their behavioural script.");
        return instruction;
    }
}
