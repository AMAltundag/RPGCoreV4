package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.utils.Alphabet;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Generate measurements for basic characters
 */
public class GeneratorForTextMeasurement implements IGenerator {

    private static final File INPUT_TEXT = FileUtil.directory("resourcepack/input/text");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        File[] templates = INPUT_TEXT.listFiles();
        if (templates == null) {
            throw new IllegalStateException("Missing font template files!");
        } else {
            // create measurements used within the texture space
            for (Alphabet alphabet : Alphabet.REGISTERED) {
                File file = new File(INPUT_TEXT, alphabet.texture);
                BufferedImage bi = ImageIO.read(file);
                List<String> symbols = alphabet.chars();
                for (int i = 0; i < symbols.size(); i++) {
                    char[] chars = symbols.get(i).toCharArray();
                    if (chars.length == 16) {
                        for (int j = 0; j < chars.length; j++) {
                            BufferedImage region = bi.getSubimage(alphabet.width * j, alphabet.height * i, alphabet.width, alphabet.height);
                            generation.text().measurement(chars[j], getSizeCropped(region));
                        }
                    }
                }
            }
            generation.text().measurement(' ', 3);
        }
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
}
