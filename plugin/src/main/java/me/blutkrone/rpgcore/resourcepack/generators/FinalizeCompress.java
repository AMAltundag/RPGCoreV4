package me.blutkrone.rpgcore.resourcepack.generators;

import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.util.io.FileUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class FinalizeCompress implements IGenerator {
    private static final File WORKSPACE_WORKING = FileUtil.directory("resourcepack/working");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        if (generation.isCompressed()) {
            File[] files = FileUtil.buildAllFiles(WORKSPACE_WORKING);
            long last_stamp = System.currentTimeMillis();

            // count how many image files we have
            int total = 0;
            for (File file : files) {
                if (file.getName().endsWith(".png")) {
                    total += 1;
                }
            }

            int processed = 0;
            PngOptimizer optimizer = new PngOptimizer();
            for (File file : files) {
                // ensure we have a file that can be compressed
                if (file.getName().endsWith(".png")) {
                    // load as a PNG image
                    PngImage image;
                    try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
                        image = new PngImage(is);
                    }
                    // handle compression
                    optimizer.optimize(image);
                    // write back the image
                    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                        image.writeDataOutputStream(os);
                        image.export(file.getAbsolutePath(), os.toByteArray());
                    }
                    // provide info on what we've got
                    processed++;
                    if ((System.currentTimeMillis() - last_stamp) > 6000L) {
                        last_stamp = System.currentTimeMillis();
                        RPGCore.inst().getLogger().info("Compressing " + processed + " of " + total + " files!");
                    }
                }
            }
        }
    }
}
