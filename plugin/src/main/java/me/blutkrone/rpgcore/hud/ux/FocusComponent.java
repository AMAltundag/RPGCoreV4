package me.blutkrone.rpgcore.hud.ux;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.api.entity.IEntityEffect;
import me.blutkrone.rpgcore.api.hud.IUXComponent;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.entity.resource.EntityWard;
import me.blutkrone.rpgcore.entity.resource.ResourceSnapshot;
import me.blutkrone.rpgcore.hud.UXWorkspace;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.skill.activity.ISkillActivity;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class FocusComponent implements IUXComponent<FocusComponent.Snapshot> {

    private int focus_status_viewport;
    private int focus_health_offset;
    private int focus_ward_offset;
    private int focus_status_offset;
    private int focus_sigil_offset;
    private int focus_info_offset;

    public FocusComponent(ConfigWrapper section) {
        focus_info_offset = section.getInt("interface-offset.focus-info-offset");
        focus_health_offset = section.getInt("interface-offset.focus-health-offset");
        focus_ward_offset = section.getInt("interface-offset.focus-ward-offset");
        focus_status_offset = section.getInt("interface-offset.focus-status-offset");
        focus_status_viewport = section.getInt("interface-offset.focus-status-viewport");
        focus_sigil_offset = section.getInt("interface-offset.focus-sigil-offset");
    }

    @Override
    public int getPriority() {
        return 6;
    }

    @Override
    public Snapshot prepare(CorePlayer core_player, Player bukkit_player) {
        CoreEntity focused = core_player.getFocusTracker().getFocus();
        return focused == null ? null : new Snapshot(focused);
    }

    @Override
    public void populate(CorePlayer core_player, Player bukkit_player, UXWorkspace workspace, Snapshot prepared) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        // focus may not render anything if we have no snapshot
        if (prepared == null) {
            return;
        }
        // render the background frame
        int render_point = (core_player.getSettings().screen_width / 2) - (rpm.texture("static_focus_back").width / 2);
        workspace.bossbar().shiftToExact(render_point);
        workspace.bossbar().append(rpm.texture("static_focus_back"));
        // render the respective resources
        workspace.bossbar().shiftToExact(render_point + focus_health_offset);
        workspace.bossbar().append(rpm.texture("bar_focus_health_filling_" + ((int) (prepared.health.fraction * 100))));
        if (prepared.ward != null) {
            workspace.bossbar().shiftToExact(render_point + focus_ward_offset);
            workspace.bossbar().append(rpm.texture("bar_focus_ward_filling_" + ((int) (prepared.ward.fraction * 100))));
        }
        // render the sigil of the entity
        workspace.bossbar().shiftToExact(render_point + focus_sigil_offset);
        workspace.bossbar().append(rpm.texture("static_" + prepared.sigil + "_focus_sigil", "static_default_focus_sigil"));
        // render additional info of the mob
        if (prepared.info_text != null) {
            workspace.bossbar().shiftToExact(render_point + focus_info_offset);
            workspace.bossbar().append(prepared.info_text, "hud_focus_info");
        }
        // present the most recently updated status effects
        for (int i = 0; i < prepared.status_icons.size() && i < focus_status_viewport; i++) {
            // fetch info about the relevant icons
            String status_icon = prepared.status_icons.get(i);
            int status_time = prepared.status_time.get(i);
            int status_stack = prepared.status_stacks.get(i);
            // generate information about the effects
            drawStatus(workspace, status_icon, status_time, status_stack, i, render_point);
        }
        // draw the foreground frame
        workspace.bossbar().shiftToExact(render_point);
        workspace.bossbar().append(rpm.texture("static_focus_front"));
        // draw the name atop the focus bar
        workspace.bossbar().shiftCentered(core_player.getSettings().screen_width / 2 + 1, Utility.measure(prepared.name));
        workspace.bossbar().shadow(prepared.name, "hud_focus_name");
        workspace.bossbar().shiftCentered(core_player.getSettings().screen_width / 2, Utility.measure(prepared.name));
        workspace.bossbar().append(prepared.name, "hud_focus_name");
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
    private void drawStatus(UXWorkspace workspace, String icon, int time, int stack, int index, int renderpoint) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        // draw the icon of the effect
        workspace.bossbar().shiftToExact(renderpoint + this.focus_status_offset + (index * 26));
        workspace.bossbar().append(rpm.texture("status_focus_" + icon));
        // write duration if it did not exceed time limit
        if (time != Integer.MAX_VALUE) {
            String time_string = RPGCore.inst().getLanguageManager().formatShortTicks(time);
            workspace.bossbar().shiftCentered(renderpoint + this.focus_status_offset + (index * 26) + 12, Utility.measure(time_string));
            workspace.bossbar().append(time_string, "hud_status_time_focus");
        }
        // write stack count into the frame
        if (stack > 1) {
            String stack_string = RPGCore.inst().getLanguageManager().formatShortNumber(stack);
            workspace.bossbar().shiftCentered(renderpoint + this.focus_status_offset + (index * 26) + 12 + 1, Utility.measure(stack_string));
            workspace.bossbar().shadow(stack_string, "hud_status_stack_focus");
            workspace.bossbar().shiftCentered(renderpoint + this.focus_status_offset + (index * 26) + 12, Utility.measure(stack_string));
            workspace.bossbar().append(stack_string, "hud_status_stack_focus");
        }
    }

    class Snapshot {
        // last known name of the entity
        String name;
        // snapshots of current resources we have
        ResourceSnapshot health;
        ResourceSnapshot mana;
        ResourceSnapshot ward;
        // hint for a skill that is being cast
        String info_text;
        // listing of status effects
        List<String> status_icons = new LinkedList<>();
        List<Integer> status_stacks = new LinkedList<>();
        List<Integer> status_time = new LinkedList<>();
        // sigil used by the creature
        String sigil = "default";

        Snapshot(CoreEntity entity) {
            if (entity instanceof CoreMob) {
                this.name = ((CoreMob) entity).getTemplate().getName()
                        .replace("{LEVEL}", String.valueOf(entity.getCurrentLevel()));
            } else {
                this.name = entity.getEntity().getName();
            }
            // snapshot the resources
            this.health = entity.getHealth().snapshot();
            this.mana = entity.getMana().snapshot();
            this.ward = Optional.ofNullable(entity.getWard())
                    .map(EntityWard::snapshot).orElse(null);
            // generate a list of effects, sorted by their last update
            List<IEntityEffect> flatten = new ArrayList<>(entity.getStatusEffects().values());
            flatten.removeIf(e -> e.getIcon() == null);
            flatten.sort(Comparator.comparingLong(IEntityEffect::getLastUpdated));
            // track all the effects which are on the entity
            for (IEntityEffect effect : flatten) {
                this.status_icons.add(effect.getIcon());
                this.status_stacks.add(effect.getStacks());
                this.status_time.add(effect.getDuration());
            }
            // info specific for the focus component
            this.info_text = entity.getFocusHint();
            // info generated thorough skill usage
            if (this.info_text == null) {
                IActivity activity = entity.getActivity();
                if (activity instanceof ISkillActivity) {
                    ISkillActivity skill_activity = (ISkillActivity) activity;
                    this.info_text = skill_activity.getInfoText();
                }
            }
            // info on mob rage/sigils
            if (entity instanceof CoreMob) {
                // sigil to show on focus bar
                this.sigil = ((CoreMob) entity).getTemplate().focus_sigil;
                LivingEntity target = ((CoreMob) entity).getBase().getRageEntity();
                if (target instanceof Player && this.info_text == null) {
                    double rage = ((CoreMob) entity).getBase().getRageValue();
                    this.info_text = target.getName() + String.format(" > %.1f", rage);
                }
            }
        }
    }
}
