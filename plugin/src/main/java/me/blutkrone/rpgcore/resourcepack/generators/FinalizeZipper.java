package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.File;

public class FinalizeZipper implements IGenerator {
    private static final File WORKSPACE_WORKING = FileUtil.directory("resourcepack/working");
    private static final File OUTPUT_RESULT = FileUtil.file("resourcepack/output", "result.zip");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        // initialize the zipping task
        ZipFile zip_file = new ZipFile(OUTPUT_RESULT);

        // accumulate to zipping operation
        for (File file : WORKSPACE_WORKING.listFiles()) {
            try {
                if (file.isDirectory())
                    zip_file.addFolder(file);
                else zip_file.addFile(file);
            } catch (ZipException ex) {
                RPGCore.inst().getLogger().severe("Zip Conflict: " + file.getPath() + ", skipped!");
            }
        }

        // stall until threaded zipper has finished working
        while (zip_file.getProgressMonitor().getState() == ProgressMonitor.State.BUSY) {
            Thread.sleep(10L);
        }

        // clean up our working space that may be dirty from the prior loop
        //FileUtils.deleteDirectory(WORKSPACE_WORKING);
    }
}
