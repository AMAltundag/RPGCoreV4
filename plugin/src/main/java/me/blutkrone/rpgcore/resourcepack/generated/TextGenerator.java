package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.utils.ResourceUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Splice into rows, offset either on top
 * or bottom to generate the text.
 */
public class TextGenerator {

    public static BufferedImage construct(File file, int offset, double opacity) {
        //if (Math.abs(offset) > 200)
        //    throw new IllegalArgumentException("Font offset caps at 200 pixels");

        try {
            // retrieve the texture we operate with
            BufferedImage input_texture = ImageIO.read(file);
            // negative offset can just be done via ascent
            if (offset <= 0) {
                return input_texture;
            }
            // positive offset done by ascent=height, and raising height
            return ResourceUtil.fontCopyPaddedBottom(input_texture, offset, opacity);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
