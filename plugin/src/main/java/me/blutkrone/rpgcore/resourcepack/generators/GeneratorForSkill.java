package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.CombinedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Textures for skills, iE in the skillbar
 */
public class GeneratorForSkill implements IGenerator {

    private static final File INPUT_SKILLBAR = FileUtil.directory("resourcepack/input/skillbar");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();
        for (File candidate : FileUtil.buildAllFiles(INPUT_SKILLBAR)) {
            if (candidate.getName().endsWith(".png")) {
                // reduce to a simple name
                String name = candidate.getName();
                name = name.substring(0, name.indexOf("."));
                // register skillbar variants
                BufferedImage texture = ImageIO.read(candidate);
                generation.hud().register("skillbar_" + name, CombinedTexture.combine(allocator, texture, generation.config().skillbar_offset));
                generation.hud().register("skillbar_focused_" + name, CombinedTexture.combine(allocator, texture, generation.config().focus_skillbar_offset));
                generation.hud().register("skillbar_bleached_" + name, CombinedTexture.combine(allocator, bleach(texture), generation.config().skillbar_offset));
            }
        }
    }

    /*
     * Transform image into a 'bleached' version, reducing saturation and
     * brightness by a large portion but not going full grayscale.
     *
     * @param image
     * @return
     */
    private static BufferedImage bleach(BufferedImage image) {
        BufferedImage copied = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color c = new Color(image.getRGB(x, y));
                float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                copied.setRGB(x, y, 0xff000000 | Color.HSBtoRGB(hsv[0], hsv[1] - hsv[1] * 0.5f, hsv[2] - hsv[2] * 0.5f));
            }
        }
        return copied;
    }
}
