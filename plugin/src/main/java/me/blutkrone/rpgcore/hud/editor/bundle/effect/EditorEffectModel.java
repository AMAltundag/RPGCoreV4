package me.blutkrone.rpgcore.hud.editor.bundle.effect;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectModel;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorItemModel;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectModel implements EditorEffect.IEditorEffectBundle {
    @EditorBundle(name = "Model")
    @EditorTooltip(tooltip = "Which model to render")
    public EditorItemModel model = new EditorItemModel();
    @EditorNumber(name = "Duration")
    @EditorTooltip(tooltip = "Ticks before the model is removed")
    public double duration = 0d;
    @EditorNumber(name = "Scatter")
    @EditorTooltip(tooltip = "Applies random offset on XYZ axis")
    public double scatter = 0d;

    public EditorEffectModel() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aModel Effect")
                .appendLore("§fModel: " + model.material + ":" + ((int) model.model))
                .appendLore("§fDuration: " + duration)
                .appendLore("§fScatter: " + scatter)
                .build();
    }

    @Override
    public String getName() {
        return "Model";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Model Effect");
        instruction.add("Item model at location for X ticks");
        return instruction;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectModel(this);
    }
}
