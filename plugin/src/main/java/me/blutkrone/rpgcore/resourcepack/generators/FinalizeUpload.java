package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.resourcepack.upload.DBXUploader;
import me.blutkrone.rpgcore.resourcepack.upload.KeepUploader;
import me.blutkrone.rpgcore.resourcepack.upload.TempUploader;
import me.blutkrone.rpgcore.resourcepack.upload.TransferUploader;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.util.io.FileUtil;

import java.io.File;

public class FinalizeUpload implements IGenerator {
    private static final File OUTPUT_RESULT = FileUtil.file("resourcepack/output", "result.zip");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        // upload to dropbox
        String url = DBXUploader.upload(OUTPUT_RESULT);
        // upload to transfer.sh
        if (url.equals("error")) {
            url = TransferUploader.upload(OUTPUT_RESULT);
        }
        // upload to temp.sh
        if (url.equals("error")) {
            url = TempUploader.upload(OUTPUT_RESULT);
        }
        // upload to keep.sh
        if (url.equals("error")) {
            url = KeepUploader.upload(OUTPUT_RESULT);
        }
        // warn if no upload succeed
        if (url.equals("error")) {
            RPGCore.inst().getLogger().severe("The upload services failed, try again later or upload './resourcepack/output/result.zip' manually.");
        }

        generation.getManager().setUrl(url);
    }
}
