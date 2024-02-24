package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.CombinedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Special menu textures for scrolling interfaces.
 */
public class GeneratorForScrollerMenu implements IGenerator {

    private static final int MENU_VERTICAL_OFFSET = 14 + 8;

    private static final File INPUT_SCROLLER = FileUtil.directory("resourcepack/input/scroller");
    private static final File INPUT_GRID = FileUtil.file(INPUT_SCROLLER, "grid.png");
    private static final File INPUT_MONO = FileUtil.file(INPUT_SCROLLER, "mono.png");
    private static final File INPUT_DUAL = FileUtil.file(INPUT_SCROLLER, "dual.png");
    private static final File INPUT_NAVIGATED = FileUtil.file(INPUT_SCROLLER, "navigated.png");
    private static final File INPUT_QUESTION = FileUtil.file(INPUT_SCROLLER, "question.png");
    private static final File INPUT_POINTER_TINY = FileUtil.file(INPUT_SCROLLER, "pointer_tiny.png");
    private static final File INPUT_POINTER_SMALL = FileUtil.file(INPUT_SCROLLER, "pointer_small.png");
    private static final File INPUT_POINTER_MEDIUM = FileUtil.file(INPUT_SCROLLER, "pointer_medium.png");
    private static final File INPUT_POINTER_HUGE = FileUtil.file(INPUT_SCROLLER, "pointer_huge.png");
    private static final File INPUT_HIGHLIGHT = FileUtil.file(INPUT_SCROLLER, "highlight.png");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();

        generateMenu(generation, allocator, INPUT_GRID);
        generateMenu(generation, allocator, INPUT_MONO);
        generateMenu(generation, allocator, INPUT_DUAL);
        generateMenu(generation, allocator, INPUT_NAVIGATED);
        generateMenu(generation, allocator, INPUT_QUESTION);

        generatePointers(generation, allocator, INPUT_POINTER_TINY);
        generatePointers(generation, allocator, INPUT_POINTER_SMALL);
        generatePointers(generation, allocator, INPUT_POINTER_MEDIUM);
        generatePointers(generation, allocator, INPUT_POINTER_HUGE);

        generateHighlight(generation, allocator, INPUT_HIGHLIGHT);
    }

    /*
     * Generate a menu for the given file.
     *
     * @param generation
     * @param allocator
     * @param file
     * @throws Exception
     */
    private static void generateMenu(OngoingGeneration generation, Allocator allocator, File file) throws Exception {
        String id = file.getName();
        id = id.substring(0, id.indexOf("."));
        BufferedImage texture = ImageIO.read(file);
        generation.hud().register("menu_scroller_" + id, CombinedTexture.combine(allocator, texture, MENU_VERTICAL_OFFSET));
    }

    /*
     * Generate the scrollbar at various offsets.
     *
     * @param generation
     * @param allocator
     * @param file
     * @throws Exception
     */
    private static void generatePointers(OngoingGeneration generation, Allocator allocator, File file) throws Exception {
        // identifier we are using
        String id = file.getName();
        id = id.substring(0, id.indexOf("."));
        // texture we are using
        BufferedImage texture = ImageIO.read(file);
        int spacing = 78 - texture.getHeight();
        // generate 100 slices for 0-100% progress
        for (int i = 0; i <= 100; i++) {
            // identify the exact offset we want to use
            double ratio = (0d + i) / 100.0d;
            // generate the scrollbar pointer
            generation.hud().register(id + "_" + i, CombinedTexture.combine(allocator, texture, (int) (MENU_VERTICAL_OFFSET - 41 - (ratio * spacing))));
        }
    }

    /*
     * Generate a highlighted slot
     *
     * @param generation
     * @param allocator
     * @param file
     * @throws Exception
     */
    private static void generateHighlight(OngoingGeneration generation, Allocator allocator, File file) throws Exception {
        BufferedImage texture = ImageIO.read(file);
        for (int i = 0; i < 6; i++) {
            generation.hud().register("scroller_highlight_" + i, CombinedTexture.combine(allocator, texture, MENU_VERTICAL_OFFSET - 25 - (18 * i)));
        }
    }
}
