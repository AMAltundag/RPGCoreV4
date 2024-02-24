package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.CombinedTexture;

import java.awt.image.BufferedImage;

/**
 * Pixels used by the minimap
 */
public class GeneratorForMiniMap implements IGenerator {
    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();

        BufferedImage image = create(0xFFFFFFFF);
        for (int i = 0; i < 48; i++) {
            generation.hud().register("minimap_pixel_0_" + i, CombinedTexture.combine(allocator, image, generation.config().navigator_offset - 7 - i*2));
        }
        image = create(0x40FFFFFF);
        for (int i = 0; i < 48; i++) {
            generation.hud().register("minimap_pixel_1_" + i, CombinedTexture.combine(allocator, image, generation.config().navigator_offset - 7 - i*2));
        }
        image = create(0x20FFFFFF);
        for (int i = 0; i < 48; i++) {
            generation.hud().register("minimap_pixel_2_" + i, CombinedTexture.combine(allocator, image, generation.config().navigator_offset - 7 - i*2));
        }
    }

    /*
     * Create a 2x2 pixel with the appropriate ARGB color
     *
     * @param argb The ARGB parameter to use
     * @return Buffered image
     */
    private static BufferedImage create(int argb) {
        BufferedImage minimap_pixel = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        minimap_pixel.setRGB(0, 0, argb);
        minimap_pixel.setRGB(1, 0, argb);
        minimap_pixel.setRGB(0, 1, argb);
        minimap_pixel.setRGB(1, 1, argb);
        return minimap_pixel;
    }
}
