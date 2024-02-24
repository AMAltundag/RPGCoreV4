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
 * Resource orb rendered on the bottom of the player HUD
 */
public class GeneratorForOrb implements IGenerator {
    private static final File INPUT_INTERFACE = FileUtil.directory("resourcepack/input/interface");
    private static final File INPUT_ORB_HEALTH = FileUtil.file(INPUT_INTERFACE, "self_health.png");
    private static final File INPUT_ORB_MANA = FileUtil.file(INPUT_INTERFACE, "self_mana.png");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();
        generate(generation, allocator, INPUT_ORB_HEALTH, generation.config().health_orb_offset);
        generate(generation, allocator, INPUT_ORB_MANA, generation.config().mana_orb_offset);
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
            BufferedImage current_orb = deepCopy(bi);
            // cut away the part which is not being used
            int cutoff = (int) (bi.getHeight() * (1d - (i / 100d)));
            Graphics2D graphics = (Graphics2D) current_orb.getGraphics();
            graphics.setBackground(new Color(255, 255, 255, 0));
            graphics.clearRect(0, 0, current_orb.getWidth(), cutoff);
            graphics.dispose();
            // register the remaining image
            generation.hud().register("orb_"+ name + "_" + ((int ) i ), CombinedTexture.combine(allocator, current_orb, offset));
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
