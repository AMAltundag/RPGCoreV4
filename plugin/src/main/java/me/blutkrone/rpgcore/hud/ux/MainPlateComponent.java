package me.blutkrone.rpgcore.hud.ux;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.api.hud.IUXComponent;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.entity.resource.EntityWard;
import me.blutkrone.rpgcore.entity.resource.ResourceSnapshot;
import me.blutkrone.rpgcore.hud.UXWorkspace;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.skillbar.bound.SkillBindCast;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Optional;

public class MainPlateComponent implements IUXComponent<MainPlateComponent.Snapshot> {

    private static final DecimalFormat TWO_DIGIT_FORMAT = new DecimalFormat("#.00");

    private final int activity_frame_start_at;
    private final int activity_start_at;
    private int ward_start_at;
    private int stamina_start_at;
    private int health_start_at;
    private int mana_start_at;
    private int skillbar_start_at;
    private int exp_and_level_start_at;

    private int instant_animation_size;

    public MainPlateComponent(ConfigWrapper section) {
        activity_frame_start_at = section.getInt("interface-offset.activity-frame-start-at");
        activity_start_at = section.getInt("interface-offset.activity-start-at");
        ward_start_at = section.getInt("interface-offset.ward-start-at");
        stamina_start_at = section.getInt("interface-offset.stamina-start-at");
        health_start_at = section.getInt("interface-offset.health-start-at");
        mana_start_at = section.getInt("interface-offset.mana-start-at");
        skillbar_start_at = section.getInt("interface-offset.skills-start-at");
        exp_and_level_start_at = section.getInt("interface-offset.exp-and-level-start-at");

        for (int i = 0; i < 24; i++) {
            if (RPGCore.inst().getResourcePackManager().textures().containsKey("skillbar_instant_animation_" + i)) {
                instant_animation_size = i;
            } else {
                break;
            }
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Snapshot prepare(CorePlayer core_player, Player bukkit_player) {
        return new Snapshot(core_player);
    }

    @Override
    public void populate(CorePlayer core_player, Player bukkit_player, UXWorkspace workspace, Snapshot prepared) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        workspace.actionbar().shiftToExact(0);

        // draw the back plate
        workspace.actionbar().append(rpm.texture("static_plate_back"));
        workspace.actionbar().shiftToExact(0);

        // draw info about the current progress focus
        if (prepared.activity_text != null) {
            // write the progress % on the ux
            int state = Math.min(100, (int) (prepared.activity_ratio * 100));

            if (state >= 0) {
                workspace.actionbar().shiftToExact(activity_frame_start_at);
                workspace.actionbar().append(rpm.texture("static_activity_back"));
                workspace.actionbar().shiftToExact(activity_start_at);
                workspace.actionbar().append(rpm.texture("bar_activity_filling_" + state));
                workspace.actionbar().shiftToExact(activity_frame_start_at);
                workspace.actionbar().append(rpm.texture("static_activity_front"));
            }

            // write the text of the activity
            workspace.actionbar().shiftCentered(rpm.texture("static_plate_back").width / 2 + 1, Utility.measure(prepared.activity_text));
            workspace.actionbar().shadow(prepared.activity_text, "hud_progress_text");
            workspace.actionbar().shiftCentered(rpm.texture("static_plate_back").width / 2, Utility.measure(prepared.activity_text));
            workspace.actionbar().append(prepared.activity_text, "hud_progress_text");
        }

        // draw primary resources
        drawSpecificResource(workspace, "orb_self_health", "{REMAINING}/{MAXIMUM} health", prepared.health, "hud_lower_resource", health_start_at);
        drawSpecificResource(workspace, "orb_self_mana", "{REMAINING}/{MAXIMUM} mana", prepared.mana, "hud_lower_resource", mana_start_at);

        // layer the glass orb atop
        workspace.actionbar().shiftToExact(0);
        workspace.actionbar().append(rpm.texture("static_plate_glass"));

        // secondary secondary resources
        drawSpecificResource(workspace, "radial_self_stamina", "{REMAINING}/{MAXIMUM} stamina", prepared.stamina, "hud_upper_resource", stamina_start_at);
        if (prepared.ward != null)
            drawSpecificResource(workspace, "radial_self_ward", "{REMAINING}/{MAXIMUM} ward", prepared.ward, "hud_upper_resource", ward_start_at);

        // draw the front plate
        workspace.actionbar().shiftToExact(0);
        workspace.actionbar().append(rpm.texture("static_plate_front"));

        // drew the level/exp info
        String level = TWO_DIGIT_FORMAT.format(prepared.level);
        workspace.actionbar().shiftCentered(exp_and_level_start_at + 1, Utility.measure(level));
        workspace.actionbar().shadow(level, "hud_level_and_exp");
        workspace.actionbar().shiftCentered(exp_and_level_start_at, Utility.measure(level));
        workspace.actionbar().append(level, "hud_level_and_exp");

        // render skills unless skillbar locked
        for (int i = 1; i <= 6; i++) {
            // ensure we got a usable icon
            String icon = prepared.skill_icon[i - 1];
            if (icon == null || !rpm.textures().containsKey("skillbar_" + icon)) {
                continue;
            }

            // draw icon of skill
            workspace.actionbar().shiftToExact(skillbar_start_at + (i * 29) - 2);
            if (prepared.skill_selecting) {
                workspace.actionbar().append(rpm.texture("skillbar_" + icon));

                if (!prepared.skill_unaffordable[i - 1] && prepared.skill_cooldown[i - 1] <= 0) {
                    String key_hint = String.valueOf(i + 1);
                    workspace.actionbar().shiftToExact(skillbar_start_at + (i * 29) + 21 - 3 + 2 - Utility.measure(key_hint));
                    workspace.actionbar().shadow(key_hint, "hud_skill_text_keys");
                    workspace.actionbar().shiftToExact(skillbar_start_at + (i * 29) + 21 - 3 + 1 - Utility.measure(key_hint));
                    workspace.actionbar().append(key_hint, "hud_skill_text_keys", ChatColor.WHITE);
                }

                if (prepared.skill_instant[i - 1] && instant_animation_size != 0) {
                    int frame = (prepared.timestamp / 2) % instant_animation_size;
                    workspace.actionbar().shiftToExact(skillbar_start_at + (i * 29) - 2);
                    workspace.actionbar().append(rpm.texture("skillbar_instant_animation_" + frame));
                }
            } else {
                if (prepared.skill_highlight[i - 1]) {
                    // Bukkit.getLogger().severe("not implemented (highlight via animated frame)");
                    workspace.actionbar().append(rpm.texture("skillbar_" + icon));
                } else {
                    workspace.actionbar().append(rpm.texture("skillbar_bleached_" + icon));
                }

                if (prepared.skill_instant[i - 1] && instant_animation_size != 0) {
                    int frame = (prepared.timestamp / 2) % instant_animation_size;
                    workspace.actionbar().shiftToExact(skillbar_start_at + (i * 29) - 2);
                    workspace.actionbar().append(rpm.texture("skillbar_instant_animation_" + frame));
                }
            }

            // extra layer if cost unaffordable
            if (prepared.skill_unaffordable[i - 1]) {
                workspace.actionbar().shiftToExact(skillbar_start_at + (i * 29) - 2);
                workspace.actionbar().append(rpm.texture("static_skill_unaffordable"));
            }

            // extra layer if on cooldown
            if (prepared.skill_cooldown[i - 1] > 0) {
                workspace.actionbar().shiftToExact(skillbar_start_at + (i * 29) - 2);
                workspace.actionbar().append(rpm.texture("static_skill_cooldown"));

                String timer_info = RPGCore.inst().getLanguageManager().formatShortTicks(prepared.skill_cooldown[i - 1]);
                workspace.actionbar().shiftCentered(skillbar_start_at + (i * 29) + 12 - 2, Utility.measure(timer_info) + 2);
                workspace.actionbar().shadow(timer_info, "hud_skill_text_cooldown");
                workspace.actionbar().shiftCentered(skillbar_start_at + (i * 29) + 12 - 2, Utility.measure(timer_info) + 1);
                workspace.actionbar().append(timer_info, "hud_skill_text_cooldown", ChatColor.of("#FF8888"));
            }
        }
    }

    /*
     * Draw the info for a specific resource, including a graphical cue
     * on the state and a verbal cue on it.
     *
     * @param workspace        container for all UX info
     * @param graphic_         cue prefix to access the graphic cue
     * @param text_cue         pattern to substitute with parameters
     * @param resource         which resource is represented
     * @param text_cue         font the font page to shift the cue to
     * @param resource_maximum upper maximum of resource
     * @param render_point     the point to draw at
     */
    private void drawSpecificResource(UXWorkspace workspace, String graphic_cue, String text_cue, ResourceSnapshot snapshot, String text_cue_font, int render_point) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

        // draw the graphic cue first
        IndexedTexture graphic_cue_texture = rpm.texture(graphic_cue + "_" + ((int) (100 * snapshot.fraction)));
        workspace.actionbar().shiftToExact(render_point);
        workspace.actionbar().append(graphic_cue_texture);
        // draw the info text in-place
        String text_cue_parsed = text_cue.replace("{REMAINING}", String.valueOf(snapshot.current))
                .replace("{MAXIMUM}", String.valueOf(snapshot.maximum))
                .replace("{PERCENT}", String.valueOf((int) (snapshot.fraction * 100d)) + "%");

        workspace.actionbar().shiftCentered(render_point + (graphic_cue_texture.width / 2) + 1, Utility.measure(text_cue_parsed) + 1);
        workspace.actionbar().shadow(text_cue_parsed, text_cue_font);
        workspace.actionbar().shiftCentered(render_point + (graphic_cue_texture.width / 2), Utility.measure(text_cue_parsed) + 1);
        workspace.actionbar().append(text_cue_parsed, text_cue_font);
    }


    class Snapshot {
        // snapshots of current resources we have
        private ResourceSnapshot health;
        private ResourceSnapshot mana;
        private ResourceSnapshot stamina;
        private ResourceSnapshot ward;
        // snapshot of skillbar information
        private String activity_text;
        private double activity_ratio;
        // snapshot of the skillbar
        private String[] skill_icon = new String[6];
        private int[] skill_cooldown = new int[6];
        private boolean[] skill_unaffordable = new boolean[6];
        private boolean[] skill_highlight = new boolean[6];
        private boolean[] skill_instant = new boolean[6];
        private boolean skill_selecting = false;
        // server interval of snapshot
        private int timestamp;
        // level and exp%
        private double level;

        Snapshot(CorePlayer entity) {
            this.timestamp = RPGCore.inst().getTimestamp();
            // level and experience
            this.level = entity.getCurrentLevel();
            double breakout = RPGCore.inst().getLevelManager().getExpToLevelUp(entity);
            this.level += breakout < 0d ? 0.0d : (entity.getCurrentExp() / breakout);
            // snapshot the resources
            this.health = entity.getHealth().snapshot();
            this.mana = entity.getMana().snapshot();
            this.stamina = entity.getStamina().snapshot();
            this.ward = Optional.ofNullable(entity.getWard())
                    .map(EntityWard::snapshot).orElse(null);
            // provide activity info
            IActivity activity = entity.getActivity();
            if (activity != null) {
                this.activity_text = activity.getInfoText();
                this.activity_ratio = activity.getProgress();
            } else if (entity.isSkillbarActive()) {
                this.activity_text = RPGCore.inst().getLanguageManager().getTranslation("ux_hint_skillbar_active");
                this.activity_ratio = -1d;
                this.skill_selecting = true;
            } else {
                this.activity_text = RPGCore.inst().getLanguageManager().getTranslation("ux_hint_skillbar_inactive");
                this.activity_ratio = -1d;
            }
            // provide skill info
            for (int i = 0; i <= 5; i++) {
                CoreSkill skill = entity.getSkillbar().getSkill(i);
                if (skill != null) {
                    SkillContext context = entity.createSkillContext(skill);
                    this.skill_icon[i] = skill.getBinding().getIcon(context);
                    this.skill_cooldown[i] = skill.getBinding().getCooldown(context);
                    this.skill_unaffordable[i] = !skill.getBinding().isAffordable(context);
                    this.skill_highlight[i] = activity != null && skill.getBinding().isCreatorOf(activity);
                    if (skill.getBinding() instanceof SkillBindCast) {
                        this.skill_instant[i] = entity.hasInstantCast(skill, false);
                    }
                }
            }
        }
    }
}
