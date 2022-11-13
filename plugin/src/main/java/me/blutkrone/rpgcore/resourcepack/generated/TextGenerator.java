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
    public static Map<String, List<ResourcePackFont>> construct(File files, ResourcepackGeneratorMeasured rules, File output_directory) {
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

        // construct relevant fonts we want to utilize
        Map<String, List<ResourcePackFont>> output = new HashMap<>();
        rules.text_offset.forEach((id, offset) -> {
            // prefix of texture path
            int transparency = (int) (100 * rules.text_opacity.getOrDefault(id, 1d));
            String texture_path = "generated_alphabet_" + transparency + "_";
            // prepare a new font to be utilized
            List<ResourcePackFont> listed = newList();
            for (Alphabet alphabet : Alphabet.REGISTERED) {
                String texture = "minecraft:font/" + texture_path + alphabet.texture;
                int height = 240 + alphabet.height;
                int ascent = offset + alphabet.ascent;
                listed.add(new ResourcePackFont("bitmap", texture, ascent, height, alphabet.chars()));
            }
            // track the result we generated
            output.put("generated_text_" + id, listed);
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
    private static List<ResourcePackFont> newList() {
        List<ResourcePackFont> generated = new ArrayList<>();
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -3, Collections.singletonList("\uF801")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -4, Collections.singletonList("\uF802")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -5, Collections.singletonList("\uF803")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -6, Collections.singletonList("\uF804")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -7, Collections.singletonList("\uF805")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -8, Collections.singletonList("\uF806")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -9, Collections.singletonList("\uF807")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -10, Collections.singletonList("\uF808")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -18, Collections.singletonList("\uF809")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -34, Collections.singletonList("\uF80a")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -66, Collections.singletonList("\uF80b")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -130, Collections.singletonList("\uF80c")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -258, Collections.singletonList("\uF80d")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -514, Collections.singletonList("\uF80e")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -1026, Collections.singletonList("\uF80f")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, -0, Collections.singletonList("\uF821")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 1, Collections.singletonList("\uF821")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 2, Collections.singletonList("\uF822")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 3, Collections.singletonList("\uF823")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 3, Collections.singletonList("\uF824")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 4, Collections.singletonList("\uF825")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 5, Collections.singletonList("\uF826")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 6, Collections.singletonList("\uF827")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 7, Collections.singletonList("\uF828")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 15, Collections.singletonList("\uF829")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 31, Collections.singletonList("\uF82a")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 63, Collections.singletonList("\uF82b")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 127, Collections.singletonList("\uF82c")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 255, Collections.singletonList("\uF82d")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 511, Collections.singletonList("\uF82e")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 1023, Collections.singletonList("\uF82f")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32770, -32770, Collections.singletonList("\uF800")));
        generated.add(new ResourcePackFont("bitmap", "negative_space:font/pixel.png", -32768, 32767, Collections.singletonList("\uF820")));
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
                if (((image.getRGB(x, y) >> 24) & 0xff) != 0) {
                    global = Math.max(global, x);
                }
            }
        }

        return global + 1;
    }
}
