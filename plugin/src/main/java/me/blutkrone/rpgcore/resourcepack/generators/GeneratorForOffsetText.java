package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.utils.Alphabet;
import me.blutkrone.rpgcore.resourcepack.generation.component.FontEntry;
import me.blutkrone.rpgcore.resourcepack.utils.ResourceUtil;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

/**
 * Generate fonts that have a vertical offset.
 */
public class GeneratorForOffsetText implements IGenerator {
    private static final File INPUT_TEXT = FileUtil.directory("resourcepack/input/text");
    private static final File WORKSPACE_FONT = FileUtil.directory("resourcepack/working/assets/minecraft/textures/font");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        // ensure that we have our template fonts
        File[] templates = INPUT_TEXT.listFiles();
        if (templates == null) {
            throw new NullPointerException("Bad template files under " + INPUT_TEXT.getPath());
        }

        // transparency levels we do care about
        Set<Integer> transparencies = new HashSet<>();
        for (double opacity : generation.config().text_opacity.values()) {
            transparencies.add((int) (100 * opacity));
        }

        // create template texture files
        for (Alphabet alphabet : Alphabet.REGISTERED) {
            for (Integer transparency : transparencies) {
                File file = new File(INPUT_TEXT, alphabet.texture);
                BufferedImage bi = ImageIO.read(file);
                bi = offset(bi, 240, transparency * 0.01d, alphabet.height, alphabet.width);
                File output_file = FileUtil.file(WORKSPACE_FONT, "generated_alphabet_" + transparency + "_" + alphabet.texture);
                ImageIO.write(bi, "png", output_file);
            }
        }

        // minimize how many fonts we do generate
        Map<Integer, List<String>> font_by_offset = new HashMap<>();
        generation.config().text_offset.forEach((font, offset) -> {
            font_by_offset.computeIfAbsent(offset, (k -> new ArrayList<>())).add(font);
        });

        font_by_offset.forEach((offset, fonts) -> {
            // each transparency generates one font
            Set<Integer> opacity = new HashSet<>();
            for (String font : fonts) {
                opacity.add((int) (100 * generation.config().text_opacity.getOrDefault(font, 1d)));
            }

            // generate realtime fonts that we want to use
            Map<Integer, String> transparency_to_font = new HashMap<>();
            for (Integer transparency : opacity) {
                String texture_path = "generated_alphabet_" + transparency + "_";
                List<FontEntry> listed = generation.text().create();
                for (Alphabet alphabet : Alphabet.REGISTERED) {
                    String texture = "minecraft:font/" + texture_path + alphabet.texture;
                    int height = 240 + alphabet.height;
                    int ascent = offset + alphabet.ascent;
                    listed.add(new FontEntry("bitmap", texture, ascent, height, alphabet.chars()));
                }
                // track the font we computed
                String unique_id = "generated_font_" + UUID.randomUUID();
                generation.text().register(unique_id, listed);
                transparency_to_font.put(transparency, unique_id);
            }

            // we use aliases to not generate multiple copies of equivalent fonts
            for (String font : fonts) {
                int transparency = (int) (100 * generation.config().text_opacity.getOrDefault(font, 1d));
                generation.text().alias(font, transparency_to_font.get(transparency));
            }
        });
    }

    /*
     * Pad a texture so we can use it for relevant offsets.
     *
     * @param input_texture
     * @param offset
     * @param opacity
     * @param height
     * @param width
     * @return
     */
    private static BufferedImage offset(BufferedImage input_texture, int offset, double opacity, int height, int width) {
        // negative offset can just be done via ascent
        if (offset <= 0) {
            return input_texture;
        }
        // positive offset done by ascent=height, and raising height
        return ResourceUtil.fontCopyPaddedBottom(input_texture, offset, opacity, height, width);
    }
}
