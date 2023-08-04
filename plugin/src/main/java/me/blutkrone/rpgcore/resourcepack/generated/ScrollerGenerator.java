package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScrollerGenerator {
    private static final int MENU_VERTICAL_OFFSET = 14 + 8;

    public static Map<String, IndexedTexture> construct(File directory) {
        Map<String, IndexedTexture> textures = new HashMap<>();
        textures.putAll(constructMenu(new File(directory, "grid.png")));
        textures.putAll(constructMenu(new File(directory, "mono.png")));
        textures.putAll(constructMenu(new File(directory, "dual.png")));
        textures.putAll(constructMenu(new File(directory, "navigated.png")));
        textures.putAll(constructMenu(new File(directory, "question.png")));
        textures.putAll(constructPointers(new File(directory, "pointer_tiny.png")));
        textures.putAll(constructPointers(new File(directory, "pointer_small.png")));
        textures.putAll(constructPointers(new File(directory, "pointer_medium.png")));
        textures.putAll(constructPointers(new File(directory, "pointer_huge.png")));
        textures.putAll(constructHighlight(new File(directory, "highlight.png")));
        return textures;
    }

    private static Map<String, IndexedTexture> constructMenu(File file) {
        Map<String, IndexedTexture> textures = new HashMap<>();
        try {
            char c = 0xff;
            // identifier we are using
            String id = file.getName();
            id = id.substring(0, id.indexOf("."));

            BufferedImage bi = ImageIO.read(file);
            List<IndexedTexture.GeneratedTexture> sliced = new ArrayList<>();
            // slice the menu texture appropriately
            int w = 0;
            while (w < bi.getWidth()) {
                // slice up the texture and pool it
                BufferedImage slice = bi.getSubimage(w, 0, Math.min(128, bi.getWidth() - w), bi.getHeight());
                sliced.add(new IndexedTexture.GeneratedTexture(c++, "menu_scroller_frame_" + id, slice, MENU_VERTICAL_OFFSET));
                // move our pointer ahead
                w += Math.min(128, bi.getWidth());
            }
            // provide this symbol as our result
            textures.put("menu_scroller_" + id, IndexedTexture.GeneratedCompoundTexture.build(sliced));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textures;
    }

    private static Map<String, IndexedTexture> constructPointers(File file) {
        Map<String, IndexedTexture> textures = new HashMap<>();
        try {
            // symbol pointer
            char c = 0xff;
            // identifier we are using
            String id = file.getName();
            id = id.substring(0, id.indexOf("."));
            // texture we are using
            BufferedImage bi = ImageIO.read(file);
            int spacing = 78 - bi.getHeight();
            // generate 100 slices for 0-100% progress
            for (int i = 0; i <= 100; i++) {
                // identify the exact offset we want to use
                double ratio = (0d + i) / 100.0d;

                List<IndexedTexture.GeneratedTexture> sliced = new ArrayList<>();
                // slice the menu texture appropriately
                int w = 0;
                while (w < bi.getWidth()) {
                    // slice up the texture and pool it
                    BufferedImage slice = bi.getSubimage(w, 0, Math.min(128, bi.getWidth() - w), bi.getHeight());
                    sliced.add(new IndexedTexture.GeneratedTexture(c++, "pointer_" + id, slice, (int) (MENU_VERTICAL_OFFSET - 41 - (ratio * spacing))));
                    // move our pointer ahead
                    w += Math.min(128, bi.getWidth());
                }

                // provide this symbol as our result
                textures.put(id + "_" + i, IndexedTexture.GeneratedCompoundTexture.build(sliced));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textures;

    }

    private static Map<String, IndexedTexture> constructHighlight(File file) {
        Map<String, IndexedTexture> textures = new HashMap<>();
        try {
            char c = 0xff;

            BufferedImage bi = ImageIO.read(file);

            for (int i = 0; i < 6; i++) {
                List<IndexedTexture.GeneratedTexture> sliced = new ArrayList<>();
                // slice the menu texture appropriately
                int w = 0;
                while (w < bi.getWidth()) {
                    // slice up the texture and pool it
                    BufferedImage slice = bi.getSubimage(w, 0, Math.min(128, bi.getWidth() - w), bi.getHeight());
                    sliced.add(new IndexedTexture.GeneratedTexture(c++, "scroller_highlight", slice, MENU_VERTICAL_OFFSET - 25 - 18 * i));
                    // move our pointer ahead
                    w += Math.min(128, bi.getWidth());
                }
                // provide this symbol as our result
                textures.put("scroller_highlight_" + i, IndexedTexture.GeneratedCompoundTexture.build(sliced));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textures;
    }
}
