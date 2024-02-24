package me.blutkrone.rpgcore.resourcepack.utils;

import java.awt.image.BufferedImage;
import java.io.StringWriter;

public class ResourceUtil {

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase();
    }

    /**
     * Create an exact copy of an image with transparency.
     *
     * @param image the image to copy
     * @return the copied image with an opacity multiplier
     */
    public static BufferedImage imageCopyOpacity(BufferedImage image, double opacity) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int argb = image.getRGB(x, y);
                int alpha = (int) (((0xFF000000 & argb) >> 24) * opacity);
                int rgb = image.getRGB(x, y) & 0xFFFFFF;
                copy.setRGB(x, y, rgb | (alpha << 24));
            }
        }
        return copy;
    }

    /**
     * Generate a sliced version of a font, where each symbol has a padding
     * on the top/bottom.
     *
     * @param input  the texture for a specific font
     * @param bottom how much spacing from below
     * @param height height of each symbol
     * @return the updated texture
     */
    public static BufferedImage fontCopyPaddedBottom(BufferedImage input, int bottom, double opacity, int height, int width) {
        BufferedImage out_texture = new BufferedImage(16 * width, (height + bottom) * (input.getHeight() / height), BufferedImage.TYPE_INT_ARGB);

        for (int charX = 0; charX < 16; charX++) {
            for (int charY = 0; charY < input.getHeight() / height; charY++) {
                // starting point to draw from
                int pixelStartX = charX * width;
                int pixelStartY = charY * (height + bottom);
                // copy the raw texture
                for (int pixelX = 0; pixelX < width; pixelX++) {
                    for (int pixelY = 0; pixelY < height; pixelY++) {
                        int raw = input.getRGB(charX * width + pixelX, charY * height + pixelY);
                        if ((raw & 0xFF000000) != 0) {
                            raw = raw & 0xFFFFFF | ((int) (255 * opacity) << 24);
                        }
                        out_texture.setRGB(pixelStartX + pixelX, pixelStartY + pixelY, raw);
                    }
                }
            }
        }

        return out_texture;
    }

    /**
     * Create a padded copy of an image, setup with minimum opacity
     * of 16/255
     *
     * @param image  the image to copy
     * @param top    padding on the top
     * @param bottom padding on the bottom
     * @param left   padding on the left
     * @param right  padding on the right
     * @return the copied image
     */
    public static BufferedImage imageCopyPadded(BufferedImage image, int top, int bottom, int left, int right) {
        BufferedImage copy = new BufferedImage(image.getWidth() + left + right, image.getHeight() + top + bottom, BufferedImage.TYPE_INT_ARGB);

        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    copy.setRGB(x + left, y + top, image.getRGB(x, y) | 0xFF000000);
                }
            }
        } else {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    copy.setRGB(x + left, y + top, image.getRGB(x, y));
                }
            }
        }

        return copy;
    }

    public static String escapeMinecraft(String str) {
        StringWriter out = new StringWriter(str.length() * 2);

        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (ch > 0xfff) {
                out.write("\\u" + hex(ch));
            } else if (ch > 0xff) {
                out.write("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                out.write("\\u00" + hex(ch));
            } else {
                out.write(ch);
            }
        }

        return out.toString();
    }

}