package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.utils.ResourceUtil;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.CombinedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Markers indicate locations on the minimap
 */
public class GeneratorForMarkerOnMap implements IGenerator {
    private static final File INPUT_MARKER = FileUtil.directory("resourcepack/input/marker");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();

        // opaque with small Y-axis difference
        for (File candidate : FileUtil.buildAllFiles(INPUT_MARKER)) {
            if (candidate.getName().endsWith(".png")) {
                String name = candidate.getName();
                name = name.substring(0, name.indexOf("."));

                BufferedImage texture = ImageIO.read(candidate);
                for (int i = 0; i < 48; i++) {
                    int offset = generation.config().navigator_offset - 7 - i*2 + (texture.getHeight()/2);
                    generation.hud().register("marker_" + name + "_" + i, CombinedTexture.combine(allocator, texture, offset));
                }
            }
        }

        // transparent with great Y-axis difference
        for (File candidate : FileUtil.buildAllFiles(INPUT_MARKER)) {
            if (candidate.getName().endsWith(".png")) {
                String name = candidate.getName();
                name = name.substring(0, name.indexOf("."));

                BufferedImage texture = ResourceUtil.imageCopyOpacity(ImageIO.read(candidate), 0.5d);
                for (int i = 0; i < 48; i++) {
                    int offset = generation.config().navigator_offset - 7 - i*2 + (texture.getHeight()/2);
                    generation.hud().register("marker_transparent_" + name + "_" + i, CombinedTexture.combine(allocator, texture, offset));
                }
            }
        }
    }
}
