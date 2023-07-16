package me.blutkrone.rpgcore.editor.bundle.entity;

import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.other.EditorItemModel;
import me.blutkrone.rpgcore.editor.constraint.enums.EntityTypeConstraint;
import me.blutkrone.rpgcore.entity.providers.LivingProvider;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorVanillaCreatureProvider extends AbstractEditorEntityProvider {

    @EditorWrite(name = "Type", constraint = EntityTypeConstraint.class)
    public EntityType type = EntityType.ZOMBIE;
    @EditorBundle(name = "Helmet")
    public EditorItemModel helmet = new EditorItemModel();
    @EditorBundle(name = "Boots")
    public EditorItemModel boots = new EditorItemModel();
    @EditorBundle(name = "Leggings")
    public EditorItemModel leggings = new EditorItemModel();
    @EditorBundle(name = "Chestplate")
    public EditorItemModel chestplate = new EditorItemModel();
    @EditorBundle(name = "Main-Hand")
    public EditorItemModel main_hand = new EditorItemModel();
    @EditorBundle(name = "Off-Hand")
    public EditorItemModel off_hand = new EditorItemModel();
    @EditorBundle(name = "Hidden")
    public boolean hidden = false;

    @Override
    public EntityProvider build() {
        return new LivingProvider(this);
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
        instruction.add("Equipment cannot be dropped.");
        return instruction;
    }
}
