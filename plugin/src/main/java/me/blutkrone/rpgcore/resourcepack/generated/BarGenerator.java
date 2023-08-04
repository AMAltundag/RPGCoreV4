package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BarGenerator {

    public static Map<String, IndexedTexture> construct(File texture, int offset) {
        return construct(texture, "", offset);
    }

    public static Map<String, IndexedTexture> construct(File texture, String suffix, int offset) {
        // reduce to a simple name
        String name = texture.getName();
        name = name.substring(0, name.indexOf("."));
        if (!suffix.isEmpty()) name += "_" + suffix;
        // create the states of the given bar
        Map<String, IndexedTexture> output = new HashMap<>();
        try {
            BufferedImage bi = ImageIO.read(texture);
            // a header makes for a nicer overall look
            BufferedImage header = deepCopy(bi.getSubimage(bi.getWidth() - 7, 0, 7, bi.getHeight()));
            // create 101 states (0-100%) for the image
            char c = 0xff;
            for (double i = 0; i <= 100; i++) {
                BufferedImage current_bar = deepCopy(bi);
                // cut away the part which is not being used
                int cutoff = (int) ((bi.getWidth() - 7) * (i / 100d));
                Graphics2D graphics = (Graphics2D) current_bar.getGraphics();
                graphics.setBackground(new Color(255, 255, 255, 0));
                graphics.clearRect(7 + cutoff, 0, current_bar.getWidth() - cutoff - 7, current_bar.getHeight());
                graphics.dispose();
                // overlap with our header copy
                for (int x = 0; x < header.getWidth(); x++) {
                    for (int y = 0; y < header.getHeight(); y++) {
                        int header_color = header.getRGB(x, y);
                        int bar_color = current_bar.getRGB(cutoff + x, y);
                        Color blended = blend(new Color(header_color, true), new Color(bar_color, true), ((x + 1) * (1d / 7d)));
                        current_bar.setRGB(cutoff + x, y, blended.getRGB());
                    }
                }
                // fracture the image if necessary
                if (current_bar.getWidth() > 128) {
                    int current = 0;
                    ArrayList<IndexedTexture.GeneratedTexture> pooled = new ArrayList<>();
                    while (current < current_bar.getWidth()) {
                        // slice up the texture and pool it
                        BufferedImage slice = current_bar.getSubimage(current, 0, Math.min(128, current_bar.getWidth() - current), current_bar.getHeight());
                        pooled.add(new IndexedTexture.GeneratedTexture(c++, "bar_" + name, slice, offset));
                        // move our pointer ahead
                        current += Math.min(128, current_bar.getWidth());
                    }
                    output.put("bar_" + name + "_" + ((int) i), IndexedTexture.GeneratedCompoundTexture.build(pooled));
                } else {
                    // track the generated image
                    output.put("bar_" + name + "_" + ((int) i), new IndexedTexture.GeneratedTexture(c++, "bar_" + name, current_bar, offset));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    private static Color blend(Color c0, Color c1, double ratio) {
        double weight0 = ratio;
        double weight1 = (1d - ratio);

        double r = weight0 * c0.getRed() + weight1 * c1.getRed();
        double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
        double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
        double a = weight0 * c0.getAlpha() + weight1 * c1.getAlpha();

        return new Color((int) r, (int) g, (int) b, (int) a);
    }

    /*
     * A copy that separates the data array
     */
    private static BufferedImage deepCopy(BufferedImage bi) {
        BufferedImage b = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.createGraphics();
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        return b;
    }
}
