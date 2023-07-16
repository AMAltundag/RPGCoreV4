package me.blutkrone.rpgcore.hud.initiator;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.roster.IRosterInitiator;
import me.blutkrone.rpgcore.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

public class AliasInitiator implements IRosterInitiator {

    private final ItemStack input_icon;

    public AliasInitiator(ConfigWrapper config) {
        this.input_icon = RPGCore.inst().getLanguageManager()
                .getAsItem("invisible")
                .meta(meta -> ((Repairable) meta).setRepairCost(-1))
                .build();
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean initiate(CorePlayer _player) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

        if (!"nothing".equalsIgnoreCase(_player.getAlias()))
            return false;

        ITextInput input = RPGCore.inst().getVolatileManager().createInput(_player.getEntity());
        input.setItemAt(0, ItemBuilder.of(this.input_icon.clone()).name("§f" + input.getViewer().getName()).build());
        // rebuild the menu when the text input changed
        input.setUpdating(current -> {
            input.setItemAt(0, ItemBuilder.of(this.input_icon.clone()).name("§f" + current).build());

            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-260);
            if (current.isBlank() || current.toLowerCase().equals(input.getViewer().getName().toLowerCase())) {
                msb.append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
            } else {
                msb.append(rpm.texture("menu_input_fine"), ChatColor.WHITE);
            }
            msb.shiftToExact(-45);
            msb.append(RPGCore.inst().getLanguageManager().getTranslation("alias_name_menu"), "text_menu_title");

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_alias"));
            instructions.apply(msb);

            input.setTitle(msb.compile());
        });
        // commit the input as the name of the character
        input.setResponse(current -> {
            if (!current.isBlank()) {
                CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(input.getViewer());
                player.setAlias(current);
            }
        });
        // open the menu to the given player
        input.open();
        // provide the basic title
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-260);
        msb.append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
        input.setTitle(msb.compile());
        return true;
    }
}