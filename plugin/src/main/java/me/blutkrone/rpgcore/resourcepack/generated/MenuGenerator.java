package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuGenerator {

    public static final int MENU_VERTICAL_OFFSET = 14 + 8;

    public static Map<String, IndexedTexture> construct(File directory) {
        Map<String, IndexedTexture> textures = new HashMap<>();
        try {
            char c = 0xff;
            for (File candidate : FileUtil.buildAllFiles(directory)) {
                if (!candidate.getName().endsWith(".png"))
                    continue;
                String id = "menu_" + candidate.getName();
                id = id.substring(0, id.indexOf("."));

                BufferedImage bi = ImageIO.read(candidate);
                List<IndexedTexture.GeneratedTexture> sliced = new ArrayList<>();
                // slice the menu texture appropriately
                int w = 0;
                while (w < bi.getWidth()) {
                    // slice up the texture and pool it
                    BufferedImage slice = bi.getSubimage(w, 0, Math.min(128, bi.getWidth() - w), bi.getHeight());
                    sliced.add(new IndexedTexture.GeneratedTexture(c++, "menu_frame", slice.getWidth(), slice, MENU_VERTICAL_OFFSET));
                    // move our pointer ahead
                    w += Math.min(128, bi.getWidth());
                }
                // provide this symbol as our result
                textures.put(id, IndexedTexture.GeneratedCompoundTexture.build(sliced));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textures;
    }
}
