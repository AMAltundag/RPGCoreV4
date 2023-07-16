package me.blutkrone.rpgcore.resourcepack.upload;

import me.blutkrone.rpgcore.RPGCore;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UguuUploader {

    private static final String URL = "https://uguu.se/upload.php";

    /**
     * Upload the given file to transfer.sh
     *
     * @param file the file to upload.
     * @return the URL to download from again.
     */
    public static String upload(File file) throws IOException {
        try {
            Process exec = Runtime.getRuntime().exec("curl -i -F files[]=@" + file.getAbsolutePath() + " https://uguu.se/upload.php");
            String response = IOUtils.toString(exec.getInputStream(), StandardCharsets.UTF_8);
            exec.destroy();
            RPGCore.inst().getLogger().severe(response);
            return "error";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "error";
        }
    }
}
