package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SkillGenerator {

    public static Map<String, IndexedTexture.GeneratedTexture> construct(File directory, int offset, int focus_offset) {
        Map<String, IndexedTexture.GeneratedTexture> textures = new HashMap<>();
        try {
            char c = 0xff;
            for (File candidate : FileUtil.buildAllFiles(directory)) {
                if (!candidate.getName().endsWith(".png"))
                    continue;
                BufferedImage bi = ImageIO.read(candidate);
                // reduce to a simple name
                String name = candidate.getName();
                name = name.substring(0, name.indexOf("."));
                // basic skillbar texture
                textures.put("skillbar_" + name, new IndexedTexture.GeneratedTexture(c, "skillbar", bi.getWidth(), bi, offset));
                // item lore texture
                textures.put("skillbar_item_lore_" + name, new IndexedTexture.GeneratedTexture(c, "skillbar_item_lore", bi.getWidth(), bi, 0));
                // skillbar icon on focus
                textures.put("skillbar_focused_" + name, new IndexedTexture.GeneratedTexture(c, "skillbar_focused", bi.getWidth(), bi, focus_offset));
                // bleached texture
                bi = bleach(bi);
                textures.put("skillbar_bleached_" + name, new IndexedTexture.GeneratedTexture(c, "skillbar_bleached", bi.getWidth(), bi, offset));
                // increment the identifier
                c += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textures;
    }

    private static BufferedImage bleach(BufferedImage image) {
        // bleach the image of some color
        BufferedImage copied = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color c = new Color(image.getRGB(x, y));
                float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                copied.setRGB(x, y, 0xff000000 | Color.HSBtoRGB(hsv[0], hsv[1] - hsv[1] * 0.7f, hsv[2] - hsv[2] * 0.7f));
            }
        }
        return copied;
    }
}
