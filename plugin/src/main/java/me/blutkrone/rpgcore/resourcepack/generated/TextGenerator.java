package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.Alphabet;
import me.blutkrone.rpgcore.resourcepack.component.ResourcePackFont;
import me.blutkrone.rpgcore.resourcepack.utils.ResourceUtil;
import me.blutkrone.rpgcore.resourcepack.utils.ResourcepackGeneratorMeasured;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Splice into rows, offset either on top
 * or bottom to generate the text.
 */
public class TextGenerator {

    /**
     * Cache the measurements of individual font sizes.
     *
     * @param files which files we want measured.
     * @return
     */
    public static Map<Character, Integer> measurement(File files) {
        Map<Character, Integer> output = new HashMap<>();
        File[] templates = files.listFiles();
        if (templates == null) {
            return output;
        }

        // create measurements used within the texture space
        for (Alphabet alphabet : Alphabet.REGISTERED) {
            File file = new File(files, alphabet.texture);
            BufferedImage bi;
            try {
                bi = ImageIO.read(file);
                List<String> symbols = alphabet.chars();
                for (int i = 0; i < symbols.size(); i++) {
                    char[] chars = symbols.get(i).toCharArray();
                    if (chars.length == 16) {
                        for (int j = 0; j < chars.length; j++) {
                            BufferedImage region = bi.getSubimage(alphabet.width * j, alphabet.height * i, alphabet.width, alphabet.height);
                            output.put(chars[j], getSizeCropped(region));
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        output.put(' ', 3);
        return output;
    }

    /**
     * Construct duplicates of relevant font textures
     *
     * @param files
     * @param rules
     * @param output_directory
     * @return
     */
    public static TextGeneratorResult construct(File files, ResourcepackGeneratorMeasured rules, File output_directory) {
        File[] templates = files.listFiles();
        if (templates == null) {
            throw new NullPointerException("Bad template files under " + files.getPath());
        }

        // transparency levels we do care about
        Set<Integer> transparencies = new HashSet<>();
        for (double opacity : rules.text_opacity.values()) {
            transparencies.add((int) (100 * opacity));
        }

        // create template texture files
        for (Alphabet alphabet : Alphabet.REGISTERED) {
            for (Integer transparency : transparencies) {
                File file = new File(files, alphabet.texture);
                BufferedImage bi;
                try {
                    bi = ImageIO.read(file);
                    bi = offset(bi, 240, transparency * 0.01d, alphabet.height, alphabet.width);
                    File output_file = FileUtil.file(output_directory, "generated_alphabet_" + transparency + "_" + alphabet.texture);
                    ImageIO.write(bi, "png", output_file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // reduce fonts by offset
        Map<Integer, List<String>> font_by_offset = new HashMap<>();
        rules.text_offset.forEach((font, offset) -> {
            font_by_offset.computeIfAbsent(offset, (k -> new ArrayList<>())).add(font);
        });

        TextGeneratorResult output = new TextGeneratorResult();
        font_by_offset.forEach((offset, fonts) -> {
            // each transparency generates one font
            Set<Integer> opacity = new HashSet<>();
            for (String font : fonts) {
                opacity.add((int) (100 * rules.text_opacity.getOrDefault(font, 1d)));
            }
            // generate realtime fonts that we want to use
            Map<Integer, String> transparency_to_font = new HashMap<>();
            for (Integer transparency : opacity) {
                String texture_path = "generated_alphabet_" + transparency + "_";
                List<ResourcePackFont> listed = newList();
                for (Alphabet alphabet : Alphabet.REGISTERED) {
                    String texture = "minecraft:font/" + texture_path + alphabet.texture;
                    int height = 240 + alphabet.height;
                    int ascent = offset + alphabet.ascent;
                    listed.add(new ResourcePackFont("bitmap", texture, ascent, height, alphabet.chars()));
                }
                // track the font we computed
                String unique_id = "generated_font_" + UUID.randomUUID();
                output.generated_fonts.put(unique_id, listed);
                transparency_to_font.put(transparency, unique_id);
            }
            // map the generated font to the alias
            for (String font : fonts) {
                int transparency = (int) (100 * rules.text_opacity.getOrDefault(font, 1d));
                output.generated_alias.put(font, transparency_to_font.get(transparency));
            }
        });
        return output;
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

    /*
     * Create a clean list of font textures to utilize.
     *
     * @return
     */
    public static List<ResourcePackFont> newList() {
        List<ResourcePackFont> generated = new ArrayList<>();

        generated.add(ResourcePackFont.getAsAmberSpace(-3, "\uF801"));
        generated.add(ResourcePackFont.getAsAmberSpace(-4, "\uF802"));
        generated.add(ResourcePackFont.getAsAmberSpace(-5, "\uF803"));
        generated.add(ResourcePackFont.getAsAmberSpace(-6, "\uF804"));
        generated.add(ResourcePackFont.getAsAmberSpace(-7, "\uF805"));
        generated.add(ResourcePackFont.getAsAmberSpace(-8, "\uF806"));
        generated.add(ResourcePackFont.getAsAmberSpace(-9, "\uF807"));
        generated.add(ResourcePackFont.getAsAmberSpace(-10, "\uF808"));
        generated.add(ResourcePackFont.getAsAmberSpace(-18, "\uF809"));
        generated.add(ResourcePackFont.getAsAmberSpace(-34, "\uF80a"));
        generated.add(ResourcePackFont.getAsAmberSpace(-66, "\uF80b"));
        generated.add(ResourcePackFont.getAsAmberSpace(-130, "\uF80c"));
        generated.add(ResourcePackFont.getAsAmberSpace(-258, "\uF80d"));
        generated.add(ResourcePackFont.getAsAmberSpace(-514, "\uF80e"));
        generated.add(ResourcePackFont.getAsAmberSpace(-1026, "\uF80f"));

        generated.add(ResourcePackFont.getAsAmberSpace(0, "\uF821"));
        generated.add(ResourcePackFont.getAsAmberSpace(1, "\uF822"));
        generated.add(ResourcePackFont.getAsAmberSpace(2, "\uF823"));
        generated.add(ResourcePackFont.getAsAmberSpace(3, "\uF824"));
        generated.add(ResourcePackFont.getAsAmberSpace(4, "\uF825"));
        generated.add(ResourcePackFont.getAsAmberSpace(5, "\uF826"));
        generated.add(ResourcePackFont.getAsAmberSpace(6, "\uF827"));
        generated.add(ResourcePackFont.getAsAmberSpace(7, "\uF828"));
        generated.add(ResourcePackFont.getAsAmberSpace(15, "\uF829"));
        generated.add(ResourcePackFont.getAsAmberSpace(31, "\uF82a"));
        generated.add(ResourcePackFont.getAsAmberSpace(63, "\uF82b"));
        generated.add(ResourcePackFont.getAsAmberSpace(127, "\uF82c"));
        generated.add(ResourcePackFont.getAsAmberSpace(255, "\uF82d"));
        generated.add(ResourcePackFont.getAsAmberSpace(511, "\uF82e"));
        generated.add(ResourcePackFont.getAsAmberSpace(1023, "\uF82f"));

        return generated;
    }

    /*
     * Compute the cropped size of an image, with the crop happening
     * only towards the right.
     *
     * @param image
     * @return
     */
    private static int getSizeCropped(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int global = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((image.getRGB(x, y) >> 24 & 0xff) != 0) {
                    global = Math.max(global, x);
                }
            }
        }

        return global + 1;
    }

    /**
     * The result when a font was generated
     */
    public static class TextGeneratorResult {
        // the real fonts that were generated
        public final Map<String, List<ResourcePackFont>> generated_fonts = new HashMap<>();
        // alias fonts that share the identifiers
        public final Map<String, String> generated_alias = new HashMap<>();
    }
}
