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
 * Textures used by the passive skill tree
 */
public class GeneratorForPassive implements IGenerator {
    private static final File INPUT_PASSIVE = FileUtil.directory("resourcepack/input/passive");
    private static final int MENU_VERTICAL_OFFSET = 14 + 8;

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();
        for (File folder : INPUT_PASSIVE.listFiles()) {
            if (folder.isDirectory()) {
                String key = "passive_" + folder.getName();
                for (File file : folder.listFiles()) {
                    if (file.getName().endsWith(".png")) {
                        // dupe the animation textures
                        for (int i = 0; i < 6; i++) {
                            String id = key + "_" + i + "_" + file.getName();
                            id = id.substring(0, id.indexOf("."));
                            BufferedImage texture = ImageIO.read(file);
                            generation.hud().register(id, CombinedTexture.combine(allocator, texture, MENU_VERTICAL_OFFSET - (18 * i) - 2 - 24));
                        }
                    }
                }
            }
        }
    }
}
