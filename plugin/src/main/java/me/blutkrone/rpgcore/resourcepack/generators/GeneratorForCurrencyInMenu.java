package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.CombinedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Currency symbols shown in vendors or bankers.
 */
public class GeneratorForCurrencyInMenu implements IGenerator {
    private static final int MENU_VERTICAL_OFFSET = 14 + 8;
    private static final File INPUT_CURRENCY = FileUtil.directory("resourcepack/input/currency");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();

        try {
            for (File candidate : FileUtil.buildAllFiles(INPUT_CURRENCY)) {
                if (candidate.getName().endsWith(".png")) {
                    String id = "currency_" + candidate.getName();
                    id = id.substring(0, id.indexOf("."));
                    BufferedImage texture = ImageIO.read(candidate);

                    generation.hud().register(id, CombinedTexture.combine(allocator, texture, 8));
                    for (int i = 0; i < 6; i++) {
                        generation.hud().register(id + "_menu_" + i, CombinedTexture.combine(allocator, texture, MENU_VERTICAL_OFFSET + -24 + 10 - (18 * i)));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
