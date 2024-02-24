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
 * Cortex is used to organize multiple images into a 9x6
 * top inventory. Example being NPC traits, Player Menu.
 */
public class GeneratorForCortexTile implements IGenerator {
    private static final int MENU_VERTICAL_OFFSET = 14 + 8 - 27;
    private static final File INPUT_CORTEX_SMALL = FileUtil.directory("resourcepack/input/cortex/small");
    private static final File INPUT_CORTEX_MEDIUM = FileUtil.directory("resourcepack/input/cortex/medium");
    private static final File INPUT_CORTEX_LARGE = FileUtil.directory("resourcepack/input/cortex/large");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();

        // width of 4.5, height of 2
        for (File file : FileUtil.buildAllFiles(INPUT_CORTEX_SMALL)) {
            if (file.getName().endsWith(".png")) {
                for (int i = 0; i < 3; i++) {
                    String id = "cortex_small_" + file.getName();
                    id = id.substring(0, id.indexOf(".")) + "_" + i;
                    BufferedImage texture = ImageIO.read(file);
                    generation.hud().register(id, CombinedTexture.combine(allocator, texture, MENU_VERTICAL_OFFSET - (36 * i)));
                }
            }
        }

        // width of 4.5, height of 3
        for (File file : FileUtil.buildAllFiles(INPUT_CORTEX_MEDIUM)) {
            if (file.getName().endsWith(".png")) {
                for (int i = 0; i < 2; i++) {
                    String id = "cortex_medium_" + file.getName();
                    id = id.substring(0, id.indexOf(".")) + "_" + i;
                    BufferedImage texture = ImageIO.read(file);
                    generation.hud().register(id, CombinedTexture.combine(allocator, texture, MENU_VERTICAL_OFFSET - (54 * i)));
                }
            }
        }

        // width of 9, height of 3
        for (File file : FileUtil.buildAllFiles(INPUT_CORTEX_LARGE)) {
            if (file.getName().endsWith(".png")) {
                for (int i = 0; i < 2; i++) {
                    String id = "cortex_large_" + file.getName();
                    id = id.substring(0, id.indexOf(".")) + "_" + i;
                    BufferedImage texture = ImageIO.read(file);
                    generation.hud().register(id, CombinedTexture.combine(allocator, texture, MENU_VERTICAL_OFFSET - (54 * i)));
                }
            }
        }
    }
}
