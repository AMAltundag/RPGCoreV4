package me.blutkrone.rpgcore.hud.editor.design.designs;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.FocusQueue;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.design.DesignElement;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DesignNumber implements IDesignFieldEditor {
    private final DesignElement element;
    private final Field field;
    private final String name;
    private final double minimum;
    private final double maximum;

    private ItemStack invisible = RPGCore.inst().getLanguageManager()
            .getAsItem("invisible")
            .meta(meta -> ((Repairable) meta).setRepairCost(-1))
            .build();

    public DesignNumber(DesignElement element, Field field) {
        this.element = element;
        this.field = field;
        this.name = field.getAnnotation(EditorNumber.class).name();
        this.minimum = field.getAnnotation(EditorNumber.class).minimum();
        this.maximum = field.getAnnotation(EditorNumber.class).maximum();
    }

    @Override
    public void edit(IEditorBundle bundle, Player viewer, IChestMenu editor, FocusQueue focus) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        editor.getViewer().closeInventory();

        try {
            double previous = (double) field.get(bundle);
            ITextInput input = RPGCore.inst().getVolatileManager().createInput(viewer);
            input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name(String.valueOf(previous)).build());
            input.setUpdating((updated) -> {
                input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name("§f" + updated).build());
                MagicStringBuilder msb = new MagicStringBuilder();

                List<String> hints = new ArrayList<>();
                if (updated.isBlank()) {
                    // failed since input value is too short
                    msb.shiftToExact(-260).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                } else if (this.isValidNumber(updated)) {
                    // success since we are within constraint
                    msb.shiftToExact(-260).append(rpm.texture("menu_input_fine"), ChatColor.WHITE);
                    hints.add("Value is fine");
                } else {
                    msb.shiftToExact(-260).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                    hints.add("Value is illegal");
                }

                // limit how many hints can be shown to size
                if (hints.size() > 4) {
                    hints.subList(4, hints.size()).clear();
                }

                // present the hints generated to the user
                for (int i = 0; i < hints.size(); i++) {
                    msb.shiftToExact(0).append(hints.get(i), "anvil_input_hint_" + (i + 1));
                }

                InstructionBuilder instructions = new InstructionBuilder();
                instructions.add("§fNumber Selection");
                instructions.add("§fMinimum Value: §c" + (this.minimum == Double.MIN_VALUE ? "None" : this.minimum));
                instructions.add("§fMaximum Value: §c" + (this.maximum == Double.MAX_VALUE ? "None" : this.maximum));
                instructions.add("§cDecimal numbers may not always be respected!");
                instructions.apply(msb);

                msb.shiftToExact(-45).append(this.getName(), "text_menu_title");
                input.setTitle(msb.compile());
            });
            input.setResponse((response) -> {
                // try adding the value to the list
                if (response.isBlank()) {
                    // warn about input value being too short
                    input.getViewer().sendMessage("§cInput too short, update discarded!");
                } else if (this.isValidNumber(response)) {
                    // add a clean value to the container
                    try {
                        this.field.set(bundle, Double.parseDouble(response));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    // inform about the successful operation
                    input.getViewer().sendMessage("§aA value was added to the list!");
                } else {
                    // warn about not finding any value
                    input.getViewer().sendMessage("§cInput value invalid, update discarded!");
                }
                // recover to the preceding page
                input.stalled(editor::open);
            });
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-260).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
            msb.shiftToExact(-45).append(this.getName(), "text_menu_title");
            input.setTitle(msb.compile());
            input.open();
        } catch (IllegalAccessException e) {
            viewer.sendMessage("§CAn unexpected error occurred");
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getInfo(IEditorBundle bundle) throws Exception {
        return String.valueOf(this.field.get(bundle));
    }

    @Override
    public ItemStack getIcon(IEditorBundle bundle) throws Exception {
        String val = String.valueOf(this.field.get(bundle));
        return ItemBuilder.of(Material.REDSTONE_TORCH)
                .name(this.name)
                .appendLore("§fValue: " + val)
                .build();
    }

    private boolean isValidNumber(String str) {
        try {
            double d = Double.parseDouble(str);
            return d <= this.maximum && d >= this.minimum;
        } catch (Exception e) {
            return false;
        }
    }
}