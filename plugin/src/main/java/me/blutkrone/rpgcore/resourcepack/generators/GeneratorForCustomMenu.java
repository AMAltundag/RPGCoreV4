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
 * Overlay for custom menu
 */
public class GeneratorForCustomMenu implements IGenerator {
    public static final int MENU_VERTICAL_OFFSET = 14 + 8;
    private static final File INPUT_MENU = FileUtil.directory("resourcepack/input/menu");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();

        // custom override for the basic chest menu
        for (File candidate : FileUtil.buildAllFiles(INPUT_MENU)) {
            if (candidate.getName().endsWith(".png")) {
                String id = "menu_" + candidate.getName();
                id = id.substring(0, id.indexOf("."));

                BufferedImage image = ImageIO.read(candidate);
                generation.hud().register(id, CombinedTexture.combine(allocator, image, MENU_VERTICAL_OFFSET));
            }
        }
    }
}
