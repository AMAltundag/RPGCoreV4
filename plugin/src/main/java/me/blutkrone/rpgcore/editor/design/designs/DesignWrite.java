package me.blutkrone.rpgcore.editor.design.designs;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.IEditorConstraint;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.design.DesignElement;
import me.blutkrone.rpgcore.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.menu.AbstractPickListMenu;
import me.blutkrone.rpgcore.menu.EditorMenu;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import me.blutkrone.rpgcore.resourcepack.ResourcepackManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DesignWrite implements IDesignFieldEditor {
    private final DesignElement element;
    private final Field field;
    private final String name;
    private IEditorConstraint constraint;

    private ItemStack invisible = RPGCore.inst().getLanguageManager()
            .getAsItem("invisible")
            .meta(meta -> ((Repairable) meta).setRepairCost(-1))
            .build();

    public DesignWrite(DesignElement element, Field field) {
        this.element = element;
        this.field = field;
        this.name = field.getAnnotation(EditorWrite.class).name();
        try {
            Class<? extends IEditorConstraint> clazz = field.getAnnotation(EditorWrite.class).constraint();
            this.constraint = clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void edit(IEditorBundle bundle, Player viewer, EditorMenu editor, boolean wasShiftFocus) {
        ResourcepackManager rpm = RPGCore.inst().getResourcepackManager();
        editor.getMenu().getViewer().closeInventory();

        if (wasShiftFocus) {
            Map<String, ItemStack> options = this.constraint.getAllForList();
            if (options != null) {
                // prepare picklist that can pick from the constraint
                AbstractPickListMenu menu = new AbstractPickListMenu() {
                    @Override
                    public boolean onPick(String choice) {
                        // insert the element
                        return false;
                    }

                    @Override
                    public boolean highlight(String choice) {
                        // no need for highlights in this menu
                        return false;
                    }

                    @Override
                    public void close(InventoryCloseEvent event) {
                        // recover to the preceding page
                        this.getMenu().stalled(() -> editor.getMenu().open());
                    }
                };
                // append options to picklist
                options.forEach((id, icon) -> menu.addToList(icon, id));
                // present picklist to player
                menu.finish(viewer);
                return;
            }
        }

        // allow to pick from a text list
        try {
            String previous = this.constraint.toTypeOf(this.field.get(bundle));
            ITextInput input = RPGCore.inst().getVolatileManager().createInput(viewer);
            input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name(previous).build());
            input.setUpdating((updated) -> {
                input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name("§f" + updated).build());
                MagicStringBuilder msb = new MagicStringBuilder();

                List<String> hints = new ArrayList<>();
                if (updated.isBlank()) {
                    // failed since input value is too short
                    msb.shiftToExact(-260).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                    hints.addAll(this.constraint.getHint(""));
                } else if (this.constraint.isDefined(updated)) {
                    // success since we are within constraint
                    msb.shiftToExact(-260).append(rpm.texture("menu_input_fine"), ChatColor.WHITE);
                } else if (this.constraint.canExtend()) {
                    // success since we are creating a new value
                    msb.shiftToExact(-260).append(rpm.texture("menu_input_maybe"), ChatColor.WHITE);
                    hints.addAll(this.constraint.getHint(updated));
                } else {
                    msb.shiftToExact(-260).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                    hints.addAll(this.constraint.getHint(updated));
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
                instructions.add(this.constraint.getInstruction().stream()
                        .map(ln -> "§f" + ln).collect(Collectors.toList()));
                instructions.apply(msb);

                msb.shiftToExact(-45).append(this.getName(), "text_menu_title");
                input.setTitle(msb.compile());
            });
            input.setResponse((response) -> {
                // try adding the value to the list
                if (response.isBlank()) {
                    // warn about input value being too short
                    input.getViewer().sendMessage("§cInput too short, update discarded!");
                } else if (this.constraint.isDefined(response)) {
                    // add a clean value to the container
                    try {
                        this.field.set(bundle, this.constraint.asTypeOf(response));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    // inform about the successful operation
                    input.getViewer().sendMessage("§aA value was added to the list!");
                } else if (this.constraint.canExtend()) {
                    // extend constraint by the value
                    this.constraint.extend(response);
                    // add a clean value to the container
                    try {
                        this.field.set(bundle, this.constraint.asTypeOf(response));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    // inform about the successful operation
                    input.getViewer().sendMessage("§aA value was created and added to the list!");
                } else {
                    // warn about not finding any value
                    input.getViewer().sendMessage("§cInput value invalid, update discarded!");
                }
                // recover to the preceding page
                input.stalled(() -> editor.getMenu().open());
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
        return "";
    }

    @Override
    public ItemStack getIcon(IEditorBundle bundle) throws Exception {
        Object o = this.field.get(bundle);
        return ItemBuilder.of(Material.WRITABLE_BOOK)
                .name(this.name)
                .appendLore("§fValue: " + o)
                .appendLore("§cLeft Click: Pick by typing")
                .appendLore("§cRight Click: Pick from list (if possible)")
                .build();
    }
}
