package me.blutkrone.rpgcore.hud.editor.design.designs;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorColor;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.design.DesignElement;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.menu.EditorMenu;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

import java.lang.reflect.Field;

public class DesignColor implements IDesignFieldEditor {

    private final DesignElement element;
    private final Field field;
    private final String name;

    private ItemStack invisible = RPGCore.inst().getLanguageManager()
            .getAsItem("invisible")
            .meta(meta -> ((Repairable) meta).setRepairCost(-1))
            .build();

    public DesignColor(DesignElement element, Field field) {
        this.element = element;
        this.field = field;
        this.name = field.getAnnotation(EditorColor.class).name();
    }

    @Override
    public void edit(IEditorBundle bundle, Player viewer, EditorMenu editor) {
        // close previous menu
        viewer.closeInventory();
        // fetch resourcepack manager
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        // create input which we can work with
        ITextInput input = RPGCore.inst().getVolatileManager().createInput(viewer);
        input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name("FFFFFF").build());
        // present menu to help with color selection
        input.setUpdating((updated) -> {
            input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name("§f" + updated).build());
            MagicStringBuilder msb = new MagicStringBuilder();

            if (updated.length() == 6) {
                try {
                    ChatColor color = ChatColor.of("#" + updated);
                    msb.shiftToExact(-260).append(rpm.texture("menu_input_fine"), ChatColor.WHITE);
                    msb.shiftToExact(0).append("Color Preview", "anvil_input_hint_1", color);
                } catch (Exception e) {
                    msb.shiftToExact(-260).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                    msb.shiftToExact(0).append("Bad Color Format", "anvil_input_hint_1");
                }
            } else {
                msb.shiftToExact(-260).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                msb.shiftToExact(0).append("Bad Color Format", "anvil_input_hint_1");
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add("§fColor Selection");
            instructions.add("§fUse format 'RRGGBB' for your color!");
            instructions.apply(msb);

            msb.shiftToExact(-45).append(this.getName(), "text_menu_title");
            input.setTitle(msb.compile());
        });
        // update field to the given value
        input.setResponse((response) -> {
            // apply color if applicable
            if (response.length() == 6) {
                try {
                    // verify input integrity
                    ChatColor color = ChatColor.of("#" + response);
                    // track within space
                    this.field.set(bundle, response);
                    viewer.sendMessage("§aColor was updated to #" + response);
                } catch (Exception e) {
                    viewer.sendMessage("§cBad color string, no changes apply.");
                }
            } else {
                viewer.sendMessage("§cBad color string, no changes apply.");
            }

            // hop back to preceding menu
            input.stalled(() -> editor.getMenu().open());
        });
        // prepare default color setup
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-260).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
        msb.shiftToExact(-45).append(this.getName(), "text_menu_title");
        input.setTitle(msb.compile());
        // focus on this element
        input.open();
    }

    @Override
    public String getInfo(IEditorBundle bundle) throws Exception {
        try {
            return "#" + field.get(bundle);
        } catch (IllegalAccessException e) {
            return "???";
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ItemStack getIcon(IEditorBundle bundle) throws Exception {
        String color_string = (String) field.get(bundle);
        int raw_color = Integer.parseInt(color_string, 16);
        if (raw_color < 0) {
            raw_color = 0;
        }
        Color color = Color.fromRGB(raw_color);
        return ItemBuilder.of(Material.LEATHER_CHESTPLATE)
                .name("§f" + this.getName())
                .appendLore("§fColor: #" + color_string)
                .leatherColor(color)
                .build();
    }
}