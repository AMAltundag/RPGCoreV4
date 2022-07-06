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

public class OrbGenerator {
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
                BufferedImage current_orb = deepCopy(bi);
                // cut away the part which is not being used
                int cutoff = (int) (bi.getHeight() * (1d - (i / 100d)));
                Graphics2D graphics = (Graphics2D) current_orb.getGraphics();
                graphics.setBackground(new Color(255, 255, 255, 0));
                graphics.clearRect(0, 0, current_orb.getWidth(), cutoff);
                graphics.dispose();
                // fracture the image if necessary
                if (current_orb.getWidth() > 128) {
                    int current = 0;
                    ArrayList<IndexedTexture.GeneratedTexture> pooled = new ArrayList<>();
                    while (current < current_orb.getWidth()) {
                        // slice up the texture and pool it
                        BufferedImage slice = current_orb.getSubimage(current, 0, Math.min(128, current_orb.getWidth() - current), current_orb.getHeight());
                        pooled.add(new IndexedTexture.GeneratedTexture(c++, "orb_" + name, slice.getWidth(), slice, offset));
                        // move our pointer ahead
                        current += Math.min(128, current_orb.getWidth());
                    }
                    output.put("orb_" + name + "_" + ((int) i), IndexedTexture.GeneratedCompoundTexture.build(pooled));
                } else {
                    // track the generated image
                    output.put("orb_" + name + "_" + ((int) i), new IndexedTexture.GeneratedTexture(c++, "orb_" + name, current_orb.getWidth(), current_orb, offset));
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
