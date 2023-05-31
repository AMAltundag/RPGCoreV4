package me.blutkrone.rpgcore.resourcepack.upload;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TempUploader {

    /**
     * Upload the given file to transfer.sh
     *
     * @param file the file to upload.
     * @return the URL to download from again.
     */
    public static String upload(File file) throws IOException {
        try {
            Process exec = Runtime.getRuntime().exec("curl -T " + file.getAbsolutePath() + " https://temp.sh");
            String response = IOUtils.toString(exec.getInputStream(), StandardCharsets.UTF_8);
            exec.destroy();
            if (!response.startsWith("http")) {
                Thread.sleep(5000);
                exec = Runtime.getRuntime().exec("curl -T " + file.getAbsolutePath() + " https://temp.sh");
                response = IOUtils.toString(exec.getInputStream(), StandardCharsets.UTF_8);
                exec.destroy();
            }
            return response.startsWith("http") ? response : "error";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "error";
        }
    }
}
