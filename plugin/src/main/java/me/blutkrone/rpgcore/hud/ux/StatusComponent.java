package me.blutkrone.rpgcore.hud.ux;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.entity.IEntityEffect;
import me.blutkrone.rpgcore.api.hud.IUXComponent;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.UXWorkspace;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class StatusComponent implements IUXComponent<List<IEntityEffect>> {

    private final int status_start_at;
    private final int status_viewport;

    public StatusComponent(ConfigWrapper section) {
        status_start_at = section.getInt("interface-offset.status-start-at");
        status_viewport = section.getInt("interface-offset.status-viewport");
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public List<IEntityEffect> prepare(CorePlayer core_player, Player bukkit_player) {
        List<IEntityEffect> flatten = new ArrayList<>();
        for (Map<String, IEntityEffect> effectMap : core_player.getStatusEffects().values()) {
            flatten.addAll(effectMap.values());
        }

        flatten.add(new Test(1112, 332, "placeholder", true));
        flatten.add(new Test(4432, 62145, "placeholder", true));
        flatten.add(new Test(1251, 1233, "placeholder", true));
        flatten.add(new Test(1, 4412, "placeholder", true));
        flatten.add(new Test(25221, 125812, "placeholder", true));
        flatten.add(new Test(1, 125125, "placeholder", true));
        flatten.add(new Test(22134, 4423, "placeholder", true));
        flatten.add(new Test(3221, 5522, "placeholder", false));
        flatten.add(new Test(52, 124124, "placeholder", false));
        flatten.add(new Test(14444, 2255, "placeholder", false));
        flatten.add(new Test(13, 1114, "placeholder", false));
        flatten.add(new Test(1, 6643, "placeholder", false));
        flatten.add(new Test(55, 214122, "placeholder", false));
        flatten.add(new Test(2, 44321, "placeholder", false));

        return flatten;
    }

    @Override
    public void populate(CorePlayer core_player, Player bukkit_player, UXWorkspace workspace, List<IEntityEffect> prepared) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

        // abandon all buffs without an attached icon
        prepared.removeIf(effect -> effect.getIcon() == null);
        // sort buffs by their intended purpose
        prepared.sort(Comparator.comparingLong(IEntityEffect::getLastUpdated));
        // split into left/right list for buff/debuff
        List<IEntityEffect> buff_effects = new ArrayList<>();
        List<IEntityEffect> debuff_effects = new ArrayList<>();
        for (IEntityEffect effect : prepared) {
            if (effect.isDebuff()) {
                debuff_effects.add(effect);
            } else {
                buff_effects.add(effect);
            }
        }
        // buffs written to the left, relative to center
        for (int i = 0; i < buff_effects.size(); i++) {
            IEntityEffect effect = buff_effects.get(i);
            int renderpoint = rpm.texture("static_plate_back").width / 2 + this.status_start_at + (i * 26);
            drawStatus(workspace, effect.getIcon(), effect.getDuration(), effect.getStacks(), renderpoint);
        }
        // debuffs written to the right, relative to center
        for (int i = 0; i < debuff_effects.size(); i++) {
            IEntityEffect effect = debuff_effects.get(i);
            int renderpoint = rpm.texture("static_plate_back").width / 2 - this.status_start_at - ((i + 1) * 26);
            drawStatus(workspace, effect.getIcon(), effect.getDuration(), effect.getStacks(), renderpoint);
        }
    }

    /*
     * Draw a status effect of an entity on the UX
     *
     * @param workspace which workspace to draw on
     * @param icon icon of the effect
     * @param time ticks the effect lasts
     * @param stack total stacks of the effect
     * @param index position of the effect
     * @param renderpoint point to render status at
     */
    private void drawStatus(UXWorkspace workspace, String icon, int time, int stack, int renderpoint) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        // draw the icon of the effect
        workspace.actionbar().shiftToExact(renderpoint);
        workspace.actionbar().append(rpm.texture("status_self_lower_" + icon));
        // write duration if it did not exceed time limit
        String time_string = RPGCore.inst().getLanguageManager().formatShortTicks(time);
        //workspace.actionbar().shiftCentered(renderpoint + this.focus_status_offset + (index*26)+12 + 1, Utility.measureWidthExact(time_string));
        //workspace.actionbar().shadow(time_string, "hud_status_time_focus");
        workspace.actionbar().shiftCentered(renderpoint + 12, Utility.measureWidthExact(time_string));
        workspace.actionbar().append(time_string, "hud_status_time_lower");
        // write stack count into the frame
        if (stack > 1) {
            String stack_string = RPGCore.inst().getLanguageManager().formatShortNumber(stack);
            workspace.actionbar().shiftCentered(renderpoint + 12 + 1, Utility.measureWidthExact(stack_string));
            workspace.actionbar().shadow(stack_string, "hud_status_stack_lower");
            workspace.actionbar().shiftCentered(renderpoint + 12, Utility.measureWidthExact(stack_string));
            workspace.actionbar().append(stack_string, "hud_status_stack_lower");
        }
    }

    private class Test implements IEntityEffect {

        private final int stacks;
        private final int duration;
        private final String icon;
        private final boolean debuff;

        private Test(int stacks, int duration, String icon, boolean debuff) {
            this.stacks = stacks;
            this.duration = duration;
            this.icon = icon;
            this.debuff = debuff;
        }

        @Override
        public boolean tickEffect(int delta) {
            return false;
        }

        @Override
        public int getStacks() {
            return stacks;
        }

        @Override
        public int getDuration() {
            return duration;
        }

        @Override
        public String getIcon() {
            return icon;
        }

        @Override
        public long getLastUpdated() {
            return 0;
        }

        @Override
        public boolean isDebuff() {
            return debuff;
        }
    }
}
