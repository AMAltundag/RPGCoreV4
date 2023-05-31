package me.blutkrone.rpgcore.resourcepack.upload;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class TransferUploader {

    private static String URL = "https://transfer.sh/rpgcore.zip";
    private static String CHARSET = "UTF-8";
    private static String CRLF = "\r\n";

    /**
     * Upload the given file to transfer.sh
     *
     * @param file the file to upload.
     * @return the URL to download from again.
     */
    public static String upload(File file) throws IOException {
        PrintWriter writer = null;
        ByteArrayOutputStream buffer = null;

        try {
            // an arbitrary random identifier
            String boundary = Long.toHexString(System.currentTimeMillis());
            // establish a connection to the upload service
            HttpURLConnection connection = (HttpURLConnection) new URL(TransferUploader.URL).openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            // attempt to push to the upload service
            OutputStream output = connection.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(output, TransferUploader.CHARSET), true);
            // write header information
            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"").append(file.getName()).append("\"").append(CRLF);
            writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(file.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();
            // write file information
            Files.copy(file.toPath(), output);
            output.flush();
            // write footer information
            writer.append(CRLF).flush();
            writer.append("--").append(boundary).append("--").append(CRLF).flush();
            // await the response from the connection
            if (connection.getResponseCode() != 200)
                throw new IllegalStateException("Bad connection response " + connection.getResponseCode());
            // retrieve the resulting download url
            buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = connection.getInputStream().read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            // adjust to be a direct download url
            String url = new String(byteArray, StandardCharsets.UTF_8);
            url = url.replace("transfer.sh/", "transfer.sh/get/");
            // offer up the download url for the resourcepack
            return url;
        } catch (Throwable e) {
            return "error";
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (buffer != null) {
                buffer.close();
            }
        }
    }
}
