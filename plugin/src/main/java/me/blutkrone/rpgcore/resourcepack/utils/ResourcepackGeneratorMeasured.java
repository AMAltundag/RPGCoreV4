package me.blutkrone.rpgcore.resourcepack.utils;

import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResourcepackGeneratorMeasured {
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
    public int instruction_offset;

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

        // generate fonts for the instruction frame
        instruction_offset = config.getInt("interface-offset.instruction-offset");
        for (int i = 0; i < 24; i++) {
            text_offset.put("instruction_text_" + (i+1), (instruction_offset + (i*10)));
            text_opacity.put("instruction_text_" + (i+1), 1d);
        }
        // generate fonts for the nameplate text
        for (int i = 0; i < 10; i++) {
            text_offset.put("nameplate_" + i, 25+(i*10));
            text_opacity.put("nameplate_" + i, 1d);
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
            text_opacity.put(font + "_shadow", text_opacity.get(font));
        });
    }
}
