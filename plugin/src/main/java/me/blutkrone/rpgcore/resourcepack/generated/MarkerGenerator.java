package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.resourcepack.utils.ResourceUtil;
import me.blutkrone.rpgcore.resourcepack.utils.ResourcepackGeneratorMeasured;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MarkerGenerator {

    public static Map<String, IndexedTexture.GeneratedTexture> construct(File directory, ResourcepackGeneratorMeasured rules) {
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
                for (int i = 0; i < 48; i++) {
                    textures.put("marker_" + name + "_" + i, new IndexedTexture.GeneratedTexture(c, "minimap_marker", bi, rules.navigator_offset - 7 - i*2 + (bi.getHeight()/2)));
                    c += 1;
                }
            }
            for (File candidate : FileUtil.buildAllFiles(directory)) {
                if (!candidate.getName().endsWith(".png"))
                    continue;
                BufferedImage bi = ResourceUtil.imageCopyOpacity(ImageIO.read(candidate), 0.5d);
                // reduce to a simple name
                String name = candidate.getName();
                name = name.substring(0, name.indexOf("."));
                // 3 copies, one for each surface
                for (int i = 0; i < 48; i++) {
                    textures.put("marker_transparent_" + name + "_" + i, new IndexedTexture.GeneratedTexture(c, "minimap_marker_transparent", bi, rules.navigator_offset - 7 - i*2 + (bi.getHeight()/2)));
                    c += 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textures;
    }
}
