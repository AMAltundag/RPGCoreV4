package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MarkerGenerator {

    public static Map<String, IndexedTexture.GeneratedTexture> construct(File directory, int offset) {
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
                // 3 copies, one for each surface
                textures.put("marker_" + name, new IndexedTexture.GeneratedTexture(c, "minimap_marker", bi.getWidth(), bi, offset));
                c += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textures;
    }
}
