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
 * Holograms can render images on demand.
 */
public class GeneratorForImageInHologram implements IGenerator {
    private static final File INPUT_HOLOGRAM = FileUtil.directory("resourcepack/input/hologram");
    private static final int MENU_VERTICAL_OFFSET = 14 + 8;

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();
        for (File candidate : FileUtil.buildAllFiles(INPUT_HOLOGRAM)) {
            if (candidate.getName().endsWith(".png"))  {
                String id = "dialogue_" + candidate.getName();
                id = id.substring(0, id.indexOf("."));
                BufferedImage image = ImageIO.read(candidate);
                generation.hud().register(id, CombinedTexture.combine(allocator, image, image.getHeight()));
            }
        }
    }
}
