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
 * Resource display on the bottom of the HUD, rendered as a radial
 */
public class GeneratorForRadial implements IGenerator {
    private static final File INPUT_INTERFACE = FileUtil.directory("resourcepack/input/interface");
    private static final File INPUT_RADIAL_WARD = FileUtil.file(INPUT_INTERFACE, "self_ward.png");
    private static final File INPUT_RADIAL_STAMINA = FileUtil.file(INPUT_INTERFACE, "self_stamina.png");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();
        generate(generation, allocator, INPUT_RADIAL_WARD, generation.config().ward_radial_offset);
        generate(generation, allocator, INPUT_RADIAL_STAMINA, generation.config().stamina_radial_offset);
    }

    private static void generate(OngoingGeneration generation, Allocator allocator, File texture, int offset) throws Exception {
        // reduce to a simple name
        String name = texture.getName();
        name = name.substring(0, name.indexOf("."));
        // create the states of the given orb
        BufferedImage bi = ImageIO.read(texture);
        // create 101 states (0-100%) for the image
        char c = 0xff;
        for (double i = 0; i <= 100; i++) {
            BufferedImage current_radial = deepCopy(bi);
            // cut away the part which is not being used
            for (int x = 0; x < current_radial.getWidth(); x++) {
                for (int y = 0; y < current_radial.getHeight(); y++) {
                    double dX = x - current_radial.getWidth() / 2d;
                    double dY = y - current_radial.getHeight() / 2d;
                    double radian = Math.atan2(dX, dY);
                    double degree = radian * (180d / Math.PI);
                    degree = (((int) degree) + 360) % 360;
                    if (degree > (360d * ((i + 0d) / 100d)))
                        current_radial.setRGB(x, y, 0x01FFFFFF);
                }
            }
            // register the radial texture
            generation.hud().register("radial_" + name + "_" + ((int) i), CombinedTexture.combine(allocator, current_radial, offset));
        }
    }

    /*
     * A copy that separates the data array
     */
    private static BufferedImage deepCopy(BufferedImage bi) {
        BufferedImage b = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.createGraphics();
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        return b;
    }

}
