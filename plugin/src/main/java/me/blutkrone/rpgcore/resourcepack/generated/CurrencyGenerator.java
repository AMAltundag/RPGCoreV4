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

public class CurrencyGenerator {
    private static final int MENU_VERTICAL_OFFSET = 14 + 8;

    public static Map<String, IndexedTexture> construct(File directory) {
        Map<String, IndexedTexture> textures = new HashMap<>();
        try {
            char c = 0xff;
            for (File candidate : FileUtil.buildAllFiles(directory)) {
                if (!candidate.getName().endsWith(".png"))
                    continue;
                BufferedImage bi = ImageIO.read(candidate);

                String id = "currency_" + candidate.getName();
                id = id.substring(0, id.indexOf("."));

                textures.putAll(buildSymbol(bi, c++, id + "_menu_0", -24 + 10 - (18 * 0)));
                textures.putAll(buildSymbol(bi, c++, id + "_menu_1", -24 + 10 - (18 * 1)));
                textures.putAll(buildSymbol(bi, c++, id + "_menu_2", -24 + 10 - (18 * 2)));
                textures.putAll(buildSymbol(bi, c++, id + "_menu_3", -24 + 10 - (18 * 3)));
                textures.putAll(buildSymbol(bi, c++, id + "_menu_4", -24 + 10 - (18 * 4)));
                textures.putAll(buildSymbol(bi, c++, id + "_menu_5", -24 + 10 - (18 * 5)));
                textures.putAll(buildSymbol(bi, c++, id, 8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textures;
    }

    private static Map<String, IndexedTexture> buildSymbol(BufferedImage bi, char symbol, String key, int offset) {
        Map<String, IndexedTexture> textures = new HashMap<>();

        List<IndexedTexture.GeneratedTexture> sliced = new ArrayList<>();
        // slice the menu texture appropriately
        int w = 0;
        while (w < bi.getWidth()) {
            // slice up the texture and pool it
            BufferedImage slice = bi.getSubimage(w, 0, Math.min(128, bi.getWidth() - w), bi.getHeight());
            sliced.add(new IndexedTexture.GeneratedTexture(symbol, "currency_symbols", slice, MENU_VERTICAL_OFFSET + offset));
            // move our pointer ahead
            w += Math.min(128, bi.getWidth());
        }
        // provide this symbol as our result
        textures.put(key, IndexedTexture.GeneratedCompoundTexture.build(sliced));

        return textures;
    }
}
