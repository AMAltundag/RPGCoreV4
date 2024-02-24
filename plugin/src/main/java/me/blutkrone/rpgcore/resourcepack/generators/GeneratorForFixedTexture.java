package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;

import java.io.File;

/**
 * A collection of all static image references
 */
public class GeneratorForFixedTexture implements IGenerator {

    private static final File INPUT_PORTRAIT = FileUtil.directory("resourcepack/input/portrait");
    private static final File INPUT_SELFIE = FileUtil.directory("resourcepack/input/selfie");
    private static final File INPUT_QUEST = FileUtil.directory("resourcepack/input/quest");
    private static final File INPUT_FOCUS_SIGIL = FileUtil.directory("resourcepack/input/focus");
    private static final File INPUT_INTERFACE = FileUtil.directory("resourcepack/input/interface");
    private static final File INPUT_ACTIVITY_BACK = FileUtil.file(INPUT_INTERFACE, "activity_back.png");
    private static final File INPUT_ACTIVITY_FRONT = FileUtil.file(INPUT_INTERFACE, "activity_front.png");
    private static final File INPUT_PARTY_BACK = FileUtil.file(INPUT_INTERFACE, "party_back.png");
    private static final File INPUT_PARTY_FRONT = FileUtil.file(INPUT_INTERFACE, "party_front.png");
    private static final File INPUT_FOCUS_BACK = FileUtil.file(INPUT_INTERFACE, "focus_back.png");
    private static final File INPUT_FOCUS_FRONT = FileUtil.file(INPUT_INTERFACE, "focus_front.png");
    private static final File INPUT_NAVIGATOR_BACK = FileUtil.file(INPUT_INTERFACE, "navigator_back.png");
    private static final File INPUT_NAVIGATOR_OVERLAY = FileUtil.file(INPUT_INTERFACE, "navigator_overlay.png");
    private static final File INPUT_NAVIGATOR_FRONT_N = FileUtil.file(INPUT_INTERFACE, "navigator_front_N.png");
    private static final File INPUT_NAVIGATOR_FRONT_S = FileUtil.file(INPUT_INTERFACE, "navigator_front_S.png");
    private static final File INPUT_NAVIGATOR_FRONT_E = FileUtil.file(INPUT_INTERFACE, "navigator_front_E.png");
    private static final File INPUT_NAVIGATOR_FRONT_W = FileUtil.file(INPUT_INTERFACE, "navigator_front_W.png");
    private static final File INPUT_NAVIGATOR_FRONT_NE = FileUtil.file(INPUT_INTERFACE, "navigator_front_NE.png");
    private static final File INPUT_NAVIGATOR_FRONT_NW = FileUtil.file(INPUT_INTERFACE, "navigator_front_NW.png");
    private static final File INPUT_NAVIGATOR_FRONT_SE = FileUtil.file(INPUT_INTERFACE, "navigator_front_SE.png");
    private static final File INPUT_NAVIGATOR_FRONT_SW = FileUtil.file(INPUT_INTERFACE, "navigator_front_SW.png");
    private static final File INPUT_PLATE_BACK = FileUtil.file(INPUT_INTERFACE, "plate_back.png");
    private static final File INPUT_PLATE_FRONT = FileUtil.file(INPUT_INTERFACE, "plate_front.png");
    private static final File INPUT_PLATE_GLASS = FileUtil.file(INPUT_INTERFACE, "plate_glass.png");
    private static final File INPUT_SKILL_UNAFFORDABLE = FileUtil.file(INPUT_INTERFACE, "skill_unaffordable.png");
    private static final File INPUT_SKILL_COOLDOWN = FileUtil.file(INPUT_INTERFACE, "skill_cooldown.png");

    private static final int MENU_VERTICAL_OFFSET = 14 + 8;

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Bukkit.getLogger().severe("static generator should be split up");

        Allocator allocator = generation.hud().allocator();

        // activity is shown on the mainplate to hint at progress
        generation.hud().register(allocator, INPUT_ACTIVITY_BACK, generation.config().activity_offset);
        generation.hud().register(allocator, INPUT_ACTIVITY_FRONT, generation.config().activity_offset);
        // party member fallback textures
        for (int i = 0; i < 5; i++) {
            int displacement = generation.config().party_offset - (generation.config().party_distance * i);
            generation.hud().register(allocator, String.valueOf(i), INPUT_PARTY_BACK, displacement);
            generation.hud().register(allocator, String.valueOf(i), INPUT_PARTY_FRONT, displacement);
        }
        // you can look at another entity to focus the HUD on them
        generation.hud().register(allocator, INPUT_FOCUS_BACK, generation.config().focus_offset);
        generation.hud().register(allocator, INPUT_FOCUS_FRONT, generation.config().focus_offset);
        // use multiple navigators to offer directional hint
        generation.hud().register(allocator, INPUT_NAVIGATOR_BACK, generation.config().navigator_offset);
        generation.hud().register(allocator, INPUT_NAVIGATOR_OVERLAY, generation.config().navigator_offset);
        generation.hud().register(allocator, INPUT_NAVIGATOR_FRONT_N, generation.config().navigator_offset);
        generation.hud().register(allocator, INPUT_NAVIGATOR_FRONT_S, generation.config().navigator_offset);
        generation.hud().register(allocator, INPUT_NAVIGATOR_FRONT_E, generation.config().navigator_offset);
        generation.hud().register(allocator, INPUT_NAVIGATOR_FRONT_W, generation.config().navigator_offset);
        generation.hud().register(allocator, INPUT_NAVIGATOR_FRONT_NW, generation.config().navigator_offset);
        generation.hud().register(allocator, INPUT_NAVIGATOR_FRONT_NE, generation.config().navigator_offset);
        generation.hud().register(allocator, INPUT_NAVIGATOR_FRONT_SW, generation.config().navigator_offset);
        generation.hud().register(allocator, INPUT_NAVIGATOR_FRONT_SE, generation.config().navigator_offset);
        // mainplate that is on the center-bottom
        generation.hud().register(allocator, INPUT_PLATE_FRONT, generation.config().plate_offset);
        generation.hud().register(allocator, INPUT_PLATE_BACK, generation.config().plate_offset);
        generation.hud().register(allocator, INPUT_PLATE_GLASS, generation.config().plate_offset);
        // overlays for the skillbar
        generation.hud().register(allocator, INPUT_SKILL_COOLDOWN, generation.config().skillbar_offset);
        generation.hud().register(allocator, INPUT_SKILL_UNAFFORDABLE, generation.config().skillbar_offset);
        // portraits are used for iE character selector, passive tree selector
        allocator = generation.hud().allocator();
        for (File file : FileUtil.buildAllFiles(INPUT_PORTRAIT)) {
            generation.hud().register(allocator, "portrait", file, generation.config().portrait_offset);
        }
        // textures used by dialogue
        allocator = generation.hud().allocator();
        for (File file : FileUtil.buildAllFiles(INPUT_SELFIE)) {
            generation.hud().register(allocator, "selfie", file, MENU_VERTICAL_OFFSET);
        }
        // sigils are used on the focus bar
        allocator = generation.hud().allocator();
        for (File file : FileUtil.buildAllFiles(INPUT_FOCUS_SIGIL)) {
            generation.hud().register(allocator, "focus_sigil", file, generation.config().focus_sigil_offset);
        }
        // textures used by quests
        allocator = generation.hud().allocator();
        for (File file : FileUtil.buildAllFiles(INPUT_QUEST)) {
            generation.hud().register(allocator, "quest_icon", file, generation.config().quest_offset);
        }
    }
}