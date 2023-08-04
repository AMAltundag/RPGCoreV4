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

public class RadialGenerator {
    public static Map<String, IndexedTexture> construct(File texture, int offset) {
        // reduce to a simple name
        String name = texture.getName();
        name = name.substring(0, name.indexOf("."));
        // create the states of the given orb
        Map<String, IndexedTexture> output = new HashMap<>();
        try {
            BufferedImage bi = ImageIO.read(texture);
            // create 101 states (0-100%) for the image
            char c = 0xff;
            for (double i = 0; i <= 100; i++) {
                BufferedImage current_radial = deepCopy(bi);
                // cut away the part which is not being used
                for (int x = 0; x < current_radial.getWidth(); x++) {
                    for (int y = 0; y < current_radial.getHeight(); y++) {
                        double dX = x - current_radial.getWidth() / 2d;
                        double dY = y - current_radial.getHeight() / 2d;
                        double radian = Math.atan2(dX, dY);
                        double degree = radian * (180d / Math.PI);
                        degree = (((int) degree) + 360) % 360;
                        if (degree > (360d * ((i + 0d) / 100d)))
                            current_radial.setRGB(x, y, 0x01FFFFFF);
                    }
                }
                // fracture the image if necessary
                if (current_radial.getWidth() > 128) {
                    int current = 0;
                    ArrayList<IndexedTexture.GeneratedTexture> pooled = new ArrayList<>();
                    while (current < current_radial.getWidth()) {
                        // slice up the texture and pool it
                        BufferedImage slice = current_radial.getSubimage(current, 0, Math.min(128, current_radial.getWidth() - current), current_radial.getHeight());
                        pooled.add(new IndexedTexture.GeneratedTexture(c++, "radial_" + name, slice, offset));
                        // move our pointer ahead
                        current += Math.min(128, current_radial.getWidth());
                    }
                    output.put("radial_" + name + "_" + ((int) i), IndexedTexture.GeneratedCompoundTexture.build(pooled));
                } else {
                    // track the generated image
                    output.put("radial_" + name + "_" + ((int) i), new IndexedTexture.GeneratedTexture(c++, "radial_" + name, current_radial, offset));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
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
