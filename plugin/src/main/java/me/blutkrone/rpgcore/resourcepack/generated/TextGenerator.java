package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.Alphabet;
import me.blutkrone.rpgcore.resourcepack.component.ResourcePackFont;
import me.blutkrone.rpgcore.resourcepack.utils.ResourceUtil;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;

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

    public static Map<Character, Integer> measurement(File files) {
        Map<Character, Integer> output = new HashMap<>();
        File[] templates = files.listFiles();
        if (templates == null) {
            return output;
        }

        for (File file : templates) {
            // unwrap the font file
            BufferedImage bi;
            try {
                bi = ImageIO.read(file);
            } catch (IOException e) {
                continue;
            }

            // identify the text area to use
            String[] symbols;
            int height;
            int width;
            if (file.getName().equalsIgnoreCase("ascii.png")) {
                symbols = Alphabet.CHARS_ASCII;
                height = Alphabet.HEIGHT_ASCII;
                width = Alphabet.WIDTH_ASCII;
            } else if (file.getName().equalsIgnoreCase("accented.png")) {
                symbols = Alphabet.CHARS_ACCENTED;
                height = Alphabet.HEIGHT_ACCENTED;
                width = Alphabet.WIDTH_ACCENTED;
            } else if (file.getName().equalsIgnoreCase("nonlatin_european.png")) {
                symbols = Alphabet.CHARS_NON_LATIN_EUROPEAN;
                height = Alphabet.HEIGHT_NON_LATIN_EUROPEAN;
                width = Alphabet.WIDTH_NON_LATIN_EUROPEAN;
            } else if (file.getName().startsWith("unicode_page_")) {
                int unicode_page = Integer.parseInt(file.getName().substring(13, 15), 16);
                symbols = new String[16];
                for (int i = 0; i < 16; i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < 16; j++) {
                        sb.append((char) (unicode_page * 0xff + (i*16+j)));
                    }
                    symbols[i] = sb.toString();
                }
                height = 8;
                width = 8;
            } else {
                Bukkit.getLogger().severe("Unknown language file: " + file.getPath());
                continue;
            }

            // construct measurements
            for (int i = 0; i < symbols.length; i++) {
                char[] chars = symbols[i].toCharArray();
                if (chars.length == 16) {
                    for (int j = 0; j < chars.length; j++) {
                        BufferedImage region = bi.getSubimage(width * j, height * i, width, height);
                        output.put(chars[j], getSizeCropped(region));
                    }
                }
            }
        }

        output.put(' ', 3);
        return output;
    }

    public static Map<String, List<ResourcePackFont>> construct(File files, Map<String, Integer> offsets, Map<String, Double> opacities, File OUTPUT_FONT) {
        File[] templates = files.listFiles();
        if (templates == null) {
            throw new NullPointerException("Bad template files under " + files.getPath());
        }

        Map<String, List<ResourcePackFont>> output = new HashMap<>();
        offsets.forEach((id, offset) -> {
            String keying = "generated_text_" + id;
            double opacity = opacities.getOrDefault(id, 1d);
            List<ResourcePackFont> listed = newList();

            // construct a font with appropriate padding
            for (File file : templates) {
                String file_id = "generated_font_" + UUID.randomUUID().toString() + ".png";

                // allocate the language if necessary
                String[] symbols;
                int height;
                int width;
                int ascent;
                if (file.getName().equalsIgnoreCase("ascii.png")) {
                    symbols = Alphabet.CHARS_ASCII;
                    height = Alphabet.HEIGHT_ASCII;
                    width = Alphabet.WIDTH_ASCII;
                    ascent = Alphabet.ASCENT_ASCII;
                } else if (file.getName().equalsIgnoreCase("accented.png")) {
                    symbols = Alphabet.CHARS_ACCENTED;
                    height = Alphabet.HEIGHT_ACCENTED;
                    width = Alphabet.WIDTH_ACCENTED;
                    ascent = Alphabet.ASCENT_ACCENTED;
                } else if (file.getName().equalsIgnoreCase("nonlatin_european.png")) {
                    symbols = Alphabet.CHARS_NON_LATIN_EUROPEAN;
                    height = Alphabet.HEIGHT_NON_LATIN_EUROPEAN;
                    width = Alphabet.WIDTH_NON_LATIN_EUROPEAN;
                    ascent = Alphabet.ASCENT_NON_LATIN_EUROPEAN;
                } else if (file.getName().startsWith("unicode_page_")) {
                    int unicode_page = Integer.parseInt(file.getName().substring(13, 15), 16);
                    symbols = new String[16];
                    for (int i = 0; i < 16; i++) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < 16; j++) {
                            sb.append((char) (unicode_page * 0xff + (i*16+j)));
                        }
                        symbols[i] = sb.toString();
                    }
                    height = 8;
                    width = 8;
                    ascent = 7;
                } else {
                    Bukkit.getLogger().severe("Unknown language file: " + file.getPath());
                    continue;
                }

                // load and prepare the texture
                BufferedImage bi;
                try {
                    bi = ImageIO.read(file);
                    bi = offset(bi, offset, opacity, height, width);
                    ImageIO.write(bi, "png", FileUtil.file(OUTPUT_FONT, file_id));
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                // mutate language according to our needs
                int h = bi.getHeight();
                if (offset <= 0) {
                    // lowering can just be done with ascent
                    listed.add(new ResourcePackFont("bitmap", "minecraft:font/" + file_id, offset + (height - ascent), height, Arrays.asList(symbols)));
                } else {
                    // increasing was done by the graphic being bumped
                    listed.add(new ResourcePackFont("bitmap", "minecraft:font/" + file_id, (h / symbols.length) - ascent, h / symbols.length, Arrays.asList(symbols)));
                }
            }
            // track the font we've generated
            output.put(keying, listed);
        });

        return output;
    }

    public static BufferedImage offset(BufferedImage input_texture, int offset, double opacity, int height, int width) {
        //if (Math.abs(offset) > 200)
        //    throw new IllegalArgumentException("Font offset caps at 200 pixels");

        // negative offset can just be done via ascent
        if (offset <= 0) {
            return input_texture;
        }
        // positive offset done by ascent=height, and raising height
        return ResourceUtil.fontCopyPaddedBottom(input_texture, offset, opacity, height, width);
    }
}
