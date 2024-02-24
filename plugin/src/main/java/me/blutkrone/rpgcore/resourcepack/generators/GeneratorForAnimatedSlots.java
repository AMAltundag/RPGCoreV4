package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.AbstractTexture;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.CombinedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Flip-book style animation which can be played on any
 * of the six rows of the top inventory.
 */
public class GeneratorForAnimatedSlots implements IGenerator {

    private static final int MENU_VERTICAL_OFFSET = 14 + 8;
    private static final File INPUT_ANIMATION_SLOT = FileUtil.directory("resourcepack/input/animation/slot");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        for (File folder : INPUT_ANIMATION_SLOT.listFiles()) {
            if (folder.isDirectory()) {
                Allocator allocator = generation.hud().allocator();
                String key = "animation_slot_" + folder.getName();

                for (File file : folder.listFiles()) {
                    // only process image files
                    if (!file.getName().endsWith(".png")) {
                        continue;
                    }
                    // dupe the animation textures
                    for (int i = 0; i < 6; i++) {
                        // identify the base path of the animation
                        String id = key + "_" + i + "_" + file.getName();
                        id = id.substring(0, id.indexOf("."));
                        // create the texture object
                        BufferedImage image = ImageIO.read(file);
                        int offset = MENU_VERTICAL_OFFSET - (18 * i) - 3;
                        AbstractTexture texture = CombinedTexture.combine(allocator, image, offset);
                        // register it to our generation effort
                        generation.hud().register(id, texture);
                    }
                }
            }
        }
    }
}
