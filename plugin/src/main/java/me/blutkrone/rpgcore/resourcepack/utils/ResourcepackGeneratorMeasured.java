package me.blutkrone.rpgcore.resourcepack.utils;

import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResourcepackGeneratorMeasured {

    private static void warnClientRAM(String message, boolean showing) {
        if (showing) {
            Bukkit.getLogger().severe(message);
        }
    }

    public Map<String, Integer> text_offset = new HashMap<>();
    public Map<String, Double> text_opacity = new HashMap<>();

    public int status_self_upper_offset;
    public int status_self_lower_offset;
    public int party_offset;
    public int party_distance;
    public int party_health_offset;
    public int party_ward_offset;
    public int party_name_offset;
    public int focus_offset;
    public int focus_health_offset;
    public int focus_ward_offset;
    public int focus_status_offset;
    public int focus_skillbar_offset;
    public int skillbar_offset;
    public int activity_offset;
    public int health_orb_offset;
    public int mana_orb_offset;
    public int stamina_radial_offset;
    public int ward_radial_offset;
    public int navigator_offset;
    public int plate_offset;
    public int minimap_offset;
    public int marker_offset;
    public int portrait_offset;
    public int focus_sigil_offset;

    public int hud_notification;
    public int hud_sidebar;

    public int quest_offset;

    /**
     * Rules on how to generate the resourcepack.
     */
    public ResourcepackGeneratorMeasured(File file) {
        // load up the offset parameters for each texture
        ConfigWrapper config;
        try {
            config = FileUtil.asConfigYML(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        status_self_lower_offset = config.getInt("interface-offset.status-self-lower-offset");
        status_self_upper_offset = config.getInt("interface-offset.status-self-upper-offset");
        party_offset = config.getInt("interface-offset.party-offset");
        party_distance = config.getInt("interface-offset.party-distance");
        party_health_offset = config.getInt("interface-offset.party-health-offset");
        party_name_offset = config.getInt("interface-offset.party-name-offset");
        party_ward_offset = config.getInt("interface-offset.party-ward-offset");
        focus_offset = config.getInt("interface-offset.focus-offset");
        focus_health_offset = config.getInt("interface-offset.focus-health-offset");
        focus_ward_offset = config.getInt("interface-offset.focus-ward-offset");
        focus_status_offset = config.getInt("interface-offset.focus-status-offset");
        focus_skillbar_offset = config.getInt("interface-offset.focus-skillbar-offset");
        focus_sigil_offset = config.getInt("interface-offset.focus-sigil-offset");
        skillbar_offset = config.getInt("interface-offset.skillbar-offset");
        activity_offset = config.getInt("interface-offset.activity-offset");
        health_orb_offset = config.getInt("interface-offset.health-orb-offset");
        mana_orb_offset = config.getInt("interface-offset.mana-orb-offset");
        stamina_radial_offset = config.getInt("interface-offset.stamina-radial-offset");
        ward_radial_offset = config.getInt("interface-offset.ward-radial-offset");
        navigator_offset = config.getInt("interface-offset.navigator-offset");
        plate_offset = config.getInt("interface-offset.plate-offset");
        minimap_offset = config.getInt("interface-offset.minimap-offset");
        marker_offset = config.getInt("interface-offset.marker-offset");
        portrait_offset = config.getInt("interface-offset.portrait-offset");
        hud_notification = config.getInt("interface-offset.hud-notification");
        hud_sidebar = config.getInt("interface-offset.hud-sidebar");
        quest_offset = config.getInt("interface-offset.quest-offset");

        warnClientRAM("Offset 'hud-sidebar' should be a multiple of 10 (will reduce RAM usage!)", hud_sidebar % 10 != 0);
        warnClientRAM("Offset 'hud-notification' should be a multiple of 10 (will reduce RAM usage!)", hud_sidebar % 10 != 0);

        // generate fonts we can benefit
        for (int i = 0; i < 24; i++) {
            addFont("instruction_text_" + (i + 1), (i * (-10)) - 5, 1d);
            addFont("menu_text_" + (i + 1), (i * (-10)) - 5, 1d);
            addFont("hud_sidebar_" + (i + 1), hud_sidebar - (i * 10) - 5, 1d);
        }

        addFont("text_menu_title", 5, 1d);

        addFont("hud_notification_1", hud_notification, 1d);
        addFont("hud_notification_2", hud_notification + 10, 1d);
        addFont("hud_notification_3", hud_notification + 20, 1d);
        addFont("hud_notification_4", hud_notification + 30, 0.75d);
        addFont("hud_notification_5", hud_notification + 40, 0.50d);
        addFont("hud_notification_6", hud_notification + 50, 0.25d);

        addFont("dialogue_choice_question", -19, 1d);
        addFont("dialogue_choice_1", -54, 1d);
        addFont("dialogue_choice_2", -72, 1d);
        addFont("dialogue_choice_3", -90, 1d);
        addFont("dialogue_choice_4", -108, 1d);

        addFont("editor_viewport_1", -54, 1d);
        addFont("editor_viewport_2", -72, 1d);
        addFont("editor_viewport_3", -90, 1d);
        addFont("editor_viewport_4", -108, 1d);

        addFont("scroller_text_1", -18, 1d);
        addFont("scroller_text_2", -36, 1d);
        addFont("scroller_text_3", -54, 1d);
        addFont("scroller_text_4", -72, 1d);
        addFont("scroller_text_5", -90, 1d);
        addFont("scroller_text_6", -108, 1d);

        addFont("roster_create_info_1", -27, 1d);
        addFont("roster_create_info_2", -36, 1d);
        addFont("roster_create_info_3", -45, 1d);
        addFont("roster_create_info_4", -54, 1d);

        addFont("anvil_input_hint_1", -32, 1d);
        addFont("anvil_input_hint_2", -42, 1d);
        addFont("anvil_input_hint_3", -52, 1d);
        addFont("anvil_input_hint_4", -62, 1d);

        addFont("banked_text_0", -16, 1d);
        addFont("banked_text_1", -34, 1d);
        addFont("banked_text_2", -52, 1d);
        addFont("banked_text_3", -70, 1d);
        addFont("banked_text_4", -88, 1d);
        addFont("banked_text_5", -106, 1d);

        addFont("passive_tree_info", 5, 1d);
        addFont("default_fixed", 0, 1d);

        for (int i = 0; i < 5; i++) {
            int displacement = party_offset - (party_distance * i) - party_name_offset;
            addFont("hud_party_name_" + (i + 1), displacement, 1d);
        }

        // load up font specific information
        ConfigWrapper font_configs = config.getSection("font-permutation");
        for (String identifier : font_configs.getKeys(false)) {
            if (font_configs.isSection(identifier)) {
                text_offset.put(identifier, font_configs.getInt(identifier + ".offset"));
                text_opacity.put(identifier, font_configs.getDouble(identifier + ".opacity", 1d));
            } else {
                text_offset.put(identifier, font_configs.getInt(identifier));
                text_opacity.put(identifier, 1d);
            }
        }

        // dupe each entry with their shadow
        Map<String, Integer> duped = new HashMap<>(this.text_offset);
        duped.forEach((font, offset) -> {
            text_offset.put(font + "_shadow", offset - 1);
            text_opacity.put(font + "_shadow", text_opacity.get(font) * 0.8);
        });
    }

    /*
     * Add a font permutation to operate with.
     *
     * @param font
     * @param offset
     * @param opacity
     */
    private void addFont(String font, int offset, double opacity) {
        text_offset.put(font, offset);
        text_opacity.put(font, opacity);
    }
}
