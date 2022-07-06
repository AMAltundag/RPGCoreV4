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
import org.bukkit.entity.Player;

import java.util.*;

public class FocusComponent implements IUXComponent<FocusComponent.Snapshot> {

    private int focus_status_viewport;
    private int focus_name_offset;
    private int focus_skill_offset;
    private int focus_health_offset;
    private int focus_ward_offset;
    private int focus_status_offset;

    public FocusComponent(ConfigWrapper section) {
        focus_skill_offset = section.getInt("interface-offset.focus-skill-offset");
        focus_health_offset = section.getInt("interface-offset.focus-health-offset");
        focus_ward_offset = section.getInt("interface-offset.focus-ward-offset");
        focus_status_offset = section.getInt("interface-offset.focus-status-offset");
        focus_status_viewport = section.getInt("interface-offset.focus-status-viewport");
        focus_name_offset = section.getInt("interface-offset.focus-name-offset");
    }

    @Override
    public int getPriority() {
        return 6;
    }

    @Override
    public Snapshot prepare(CorePlayer core_player, Player bukkit_player) {
        return new ExampleData();
        //CoreEntity focused = core_player.getFocusTracker().getFocus();
        //return focused == null ? null : new Snapshot(focused);
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
        // if we are casting a skill, give hint for that
        if (prepared.skill_activity_icon != null) {
            int progress = (int) Math.max(0d, Math.min(99d, prepared.skill_activity_progress * 100));

            workspace.bossbar().shiftToExact(render_point + focus_skill_offset);
            workspace.bossbar().append(rpm.texture("skillbar_focused_" + prepared.skill_activity_icon));

            workspace.bossbar().shiftCentered(render_point + focus_skill_offset + 12 + 1, Utility.measureWidthExact(progress + "%"));
            workspace.bossbar().shadow(progress + "%", "focus_cast_progress");
            workspace.bossbar().shiftCentered(render_point + focus_skill_offset + 12, Utility.measureWidthExact(progress + "%"));
            workspace.bossbar().append(progress + "%", "focus_cast_progress");
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
        workspace.bossbar().shiftCentered(core_player.getSettings().screen_width / 2 + 1, Utility.measureWidthExact(prepared.name));
        workspace.bossbar().shadow(prepared.name, "focus_target_name");
        workspace.bossbar().shiftCentered(core_player.getSettings().screen_width / 2, Utility.measureWidthExact(prepared.name));
        workspace.bossbar().append(prepared.name, "focus_target_name");
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
        String time_string = RPGCore.inst().getLanguageManager().formatShortTicks(time);
        //workspace.bossbar().shiftCentered(renderpoint + this.focus_status_offset + (index*26)+12 + 1, Utility.measureWidthExact(time_string));
        //workspace.bossbar().shadow(time_string, "hud_status_time_focus");
        workspace.bossbar().shiftCentered(renderpoint + this.focus_status_offset + (index * 26) + 12, Utility.measureWidthExact(time_string));
        workspace.bossbar().append(time_string, "hud_status_time_focus");
        // write stack count into the frame
        if (stack > 1) {
            String stack_string = RPGCore.inst().getLanguageManager().formatShortNumber(stack);
            workspace.bossbar().shiftCentered(renderpoint + this.focus_status_offset + (index * 26) + 12 + 1, Utility.measureWidthExact(stack_string));
            workspace.bossbar().shadow(stack_string, "hud_status_stack_focus");
            workspace.bossbar().shiftCentered(renderpoint + this.focus_status_offset + (index * 26) + 12, Utility.measureWidthExact(stack_string));
            workspace.bossbar().append(stack_string, "hud_status_stack_focus");
        }
    }

    private class ExampleData extends Snapshot {
        public ExampleData() {
            this.name = "Focus Test";
            this.health = new ResourceSnapshot(224, 581, 224d / 581d);
            this.mana = new ResourceSnapshot(332, 771, 332d / 771d);
            this.ward = new ResourceSnapshot(55, 133, 55d / 133d);
            this.skill_activity_icon = "divine_ward";
            this.skill_activity_progress = 0.34d;
            this.status_icons.add("placeholder");
            this.status_icons.add("placeholder");
            this.status_icons.add("placeholder");
            this.status_stacks.add(1);
            this.status_stacks.add(25812);
            this.status_stacks.add(13);
            this.status_time.add(582);
            this.status_time.add(3331);
            this.status_time.add(12571257);
            this.rage_info = "Blutkrone > 348.5k";
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
        String skill_activity_icon;
        double skill_activity_progress;
        // listing of status effects
        List<String> status_icons = new LinkedList<>();
        List<Integer> status_stacks = new LinkedList<>();
        List<Integer> status_time = new LinkedList<>();
        // info string about rage (empty for players)
        String rage_info = "";

        Snapshot() {

        }

        Snapshot(CoreEntity entity) {
            this.name = entity.getEntity().getCustomName();
            if (this.name == null) {
                this.name = entity.getEntity().getName();
            }
            // snapshot the resources
            this.health = entity.getHealth().snapshot();
            this.mana = entity.getMana().snapshot();
            this.ward = Optional.ofNullable(entity.getWard())
                    .map(EntityWard::snapshot).orElse(null);
            // snapshot of the activity
            IActivity activity = entity.getActivity();
            if (activity instanceof ISkillActivity) {
                ISkillActivity skill_activity = (ISkillActivity) activity;
                this.skill_activity_icon = skill_activity.getSkill().getBinding().getIcon(skill_activity.getContext());
                this.skill_activity_progress = skill_activity.getProgress();
            }
            // generate a list of effects, sorted by their last update
            List<IEntityEffect> flatten = new ArrayList<>();
            for (Map<String, IEntityEffect> effect_map : entity.getStatusEffects().values()) {
                flatten.addAll(effect_map.values());
            }
            flatten.removeIf(e -> e.getIcon() == null);
            flatten.sort(Comparator.comparingLong(IEntityEffect::getLastUpdated));
            // track all the effects which are on the entity
            for (IEntityEffect effect : flatten) {
                this.status_icons.add(effect.getIcon());
                this.status_stacks.add(effect.getStacks());
                this.status_time.add(effect.getDuration());
            }
            // info text about rage if we are a mob
            if (entity instanceof CoreMob) {

            }
        }
    }
}
