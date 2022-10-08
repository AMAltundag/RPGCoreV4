package me.blutkrone.rpgcore.hud.editor.root.other;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.EffectPartConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditorEffect implements IEditorRoot<CoreEffect> {

    @EditorList(name = "Parts", constraint = EffectPartConstraint.class)
    public List<IEditorBundle> effects = new ArrayList<>();

    public transient File file;

    public EditorEffect() {
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
            RPGCore.inst().getGson().toJson(this, fw);
        }
    }

    @Override
    public CoreEffect build(String id) {
        return new CoreEffect(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aComplex Effect")
                .appendLore("§fTotal: " + this.effects.size())
                .build();
    }

    @Override
    public String getName() {
        return "Effect";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Complex Effect");
        instruction.add("Chain together multiple effect components to.");
        instruction.add("build a visual effect.");
        return instruction;
    }

    /**
     * An interface providing a common access to construct
     * an effect part to work with.
     */
    public interface IEditorEffectBundle extends IEditorBundle {
        CoreEffect.IEffectPart build();
    }
}
