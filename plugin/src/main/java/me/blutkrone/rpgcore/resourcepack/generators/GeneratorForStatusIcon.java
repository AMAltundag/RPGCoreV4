package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.CombinedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Status effect icons
 */
public class GeneratorForStatusIcon implements IGenerator {
    private static final File INPUT_STATUS = FileUtil.directory("resourcepack/input/status");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();
        generate(generation, allocator, "self_upper", generation.config().status_self_upper_offset);
        generate(generation, allocator, "self_lower", generation.config().status_self_lower_offset);
        generate(generation, allocator, "focus", generation.config().focus_offset + generation.config().focus_status_offset);
        generate(generation, allocator, "item_lore", 0);
    }

    /*
     * Generate a copy of all status effect icons at the given offset.
     *
     * @param generation
     * @param allocator
     * @param space
     * @param offset
     */
    private static void generate(OngoingGeneration generation, Allocator allocator, String space, int offset) throws Exception {
        for (File candidate : FileUtil.buildAllFiles(INPUT_STATUS)) {
            if (candidate.getName().endsWith(".png")) {
                // reduce to a simple name
                String name = candidate.getName();
                name = name.substring(0, name.indexOf("."));
                // 3 copies, one for each surface
                BufferedImage texture = ImageIO.read(candidate);
                generation.hud().register("status_" + space + "_" + name, CombinedTexture.combine(allocator, texture, offset));
            }
        }
    }
}
