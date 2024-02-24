package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.AbstractTexture;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.CombinedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Generate a progress bar that renders from 0 to 100%
 */
public class GeneratorForProgressBar implements IGenerator {

    private static final File INPUT_INTERFACE = FileUtil.directory("resourcepack/input/interface");
    private static final File INPUT_PARTY_HEALTH_FILLING = FileUtil.file(INPUT_INTERFACE, "party_health_filling.png");
    private static final File INPUT_PARTY_WARD_FILLING = FileUtil.file(INPUT_INTERFACE, "party_ward_filling.png");
    private static final File INPUT_ACTIVITY_FILLING = FileUtil.file(INPUT_INTERFACE, "activity_filling.png");
    private static final File INPUT_FOCUS_HEALTH_FILLING = FileUtil.file(INPUT_INTERFACE, "focus_health_filling.png");
    private static final File INPUT_FOCUS_WARD_FILLING = FileUtil.file(INPUT_INTERFACE, "focus_ward_filling.png");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();

        // bars used for party members
        for (int i = 0; i < 5; i++) {
            int displacement = generation.config().party_offset - (generation.config().party_distance * i);
            generate(generation, allocator, INPUT_PARTY_HEALTH_FILLING, String.valueOf(i), displacement + generation.config().party_health_offset);
            generate(generation, allocator, INPUT_PARTY_WARD_FILLING, String.valueOf(i), displacement + generation.config().party_ward_offset);
        }
        // bar used on mainplate to inform about an activity
        generate(generation, allocator, INPUT_ACTIVITY_FILLING, "", generation.config().activity_offset);
        // bar used when focusing another entity
        generate(generation, allocator, INPUT_FOCUS_HEALTH_FILLING, "", generation.config().focus_offset +generation.config().focus_health_offset);
        generate(generation, allocator, INPUT_FOCUS_WARD_FILLING, "", generation.config().focus_offset + generation.config().focus_ward_offset);
    }

    /*
     * Generate a progressbar from the given texture.
     *
     * @param generation
     * @param allocator
     * @param texture
     * @param suffix
     * @param offset
     * @throws Exception
     */
    private void generate(OngoingGeneration generation, Allocator allocator, File file_texture, String suffix, int offset) throws Exception {
        // compute a more suitable name
        String name = file_texture.getName();
        name = name.substring(0, name.indexOf("."));
        if (!suffix.isEmpty()) {
            name += "_" + suffix;
        }

        // create the states of the given bar
        BufferedImage texture = ImageIO.read(file_texture);
        // a header makes for a nicer overall look
        BufferedImage header = deepCopy(texture.getSubimage(texture.getWidth() - 7, 0, 7, texture.getHeight()));

        // create 101 states (0-100%) for the image
        for (double i = 0; i <= 100; i++) {
            BufferedImage current_bar = deepCopy(texture);
            // cut away the part which is not being used
            int cutoff = (int) ((texture.getWidth() - 7) * (i / 100d));
            Graphics2D graphics = (Graphics2D) current_bar.getGraphics();
            graphics.setBackground(new Color(255, 255, 255, 0));
            graphics.clearRect(7 + cutoff, 0, current_bar.getWidth() - cutoff - 7, current_bar.getHeight());
            graphics.dispose();
            // overlap with our header copy
            for (int x = 0; x < header.getWidth(); x++) {
                for (int y = 0; y < header.getHeight(); y++) {
                    int header_color = header.getRGB(x, y);
                    int bar_color = current_bar.getRGB(cutoff + x, y);
                    Color blended = blend(new Color(header_color, true), new Color(bar_color, true), ((x + 1) * (1d / 7d)));
                    current_bar.setRGB(cutoff + x, y, blended.getRGB());
                }
            }
            // register the result texture
            AbstractTexture result = CombinedTexture.combine(allocator, current_bar, offset);
            generation.hud().register("bar_" + name + "_" + ((int) i), result);
        }
    }

    /*
     * Blend two colors by the given ratio, with 0.0 being closer to
     * the first color and 1.0 being closer to the second color.
     *
     * @param c0
     * @param c1
     * @param ratio
     * @return
     */
    private static Color blend(Color c0, Color c1, double ratio) {
        double weight0 = ratio;
        double weight1 = (1d - ratio);

        double r = weight0 * c0.getRed() + weight1 * c1.getRed();
        double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
        double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
        double a = weight0 * c0.getAlpha() + weight1 * c1.getAlpha();

        return new Color((int) r, (int) g, (int) b, (int) a);
    }

    /*
     * Create a fully isolated copy of the given image, which will
     * not affect the original image if modified.
     *
     * @param bi
     * @return
     */
    private static BufferedImage deepCopy(BufferedImage bi) {
        BufferedImage b = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.createGraphics();
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        return b;
    }
}
