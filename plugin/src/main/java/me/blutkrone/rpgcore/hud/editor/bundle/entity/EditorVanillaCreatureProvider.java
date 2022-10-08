package me.blutkrone.rpgcore.hud.editor.bundle.entity;

import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.entity.providers.LivingProvider;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.EntityTypeConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorVanillaCreatureProvider extends AbstractEditorEntityProvider {

    @EditorWrite(name = "Type", constraint = EntityTypeConstraint.class)
    public EntityType type = EntityType.ZOMBIE;

    @Override
    public EntityProvider build() {
        return new LivingProvider(this.type);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.PLAYER_HEAD)
                .name("§fVanilla Entity Provider")
                .appendLore("§fEntity Type: " + type.name())
                .build();
    }

    @Override
    public String getName() {
        return "Entity Provider";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Vanilla Entity Provider");
        instruction.add("Spawns a plain vanilla creature.");
        return instruction;
    }
}
