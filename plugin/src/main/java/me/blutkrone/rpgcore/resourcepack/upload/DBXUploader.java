package me.blutkrone.rpgcore.resourcepack.upload;

import com.dropbox.core.*;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkSettings;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DBXUploader {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * Upload the given file to dropbox.
     *
     * @param file the file to upload.
     * @return the URL to download from again.
     */
    public static String upload(File file) throws IOException {
        try {
            // ensure we have a token to build a client from
            DbxClientV2 client = Dropbox.getClient();
            if (client == null) {
                return "error";
            }
            // upload file to dropbox
            final String file_path = "/resourcepack/" + DATE_FORMAT.format(new Date()) + ".zip";
            try (InputStream in = new FileInputStream(file)) {
                client.files()
                        .uploadBuilder(file_path)
                        .uploadAndFinish(in);
            }
            // generate a link to the users
            SharedLinkSettings settings = SharedLinkSettings.newBuilder()
                    .withAllowDownload(true)
                    .withRequestedVisibility(RequestedVisibility.PUBLIC).build();
            SharedLinkMetadata share_result = client.sharing()
                    .createSharedLinkWithSettings(file_path, settings);
            // offer up the link that was generated
            return share_result.getUrl().replace("?dl=0", "?dl=1").replace("&dl=0", "&dl=1");
        } catch (Exception ex) {
            ex.printStackTrace();
            return "error";
        }
    }

    /**
     * Dropbox handling internals
     */
    public static class Dropbox {

        private static DbxAppInfo APP_INFO = new DbxAppInfo("jgar4q5tvdt3vh6");
        private static DbxPKCEWebAuth PKCE_AUTH = null;
        private static DbxRequestConfig REQUEST = DbxRequestConfig.newBuilder("rpgcore/resourcepack").build();

        private static final File USER_TOKEN = FileUtil.file("resourcepack/dropbox.rpgcore");
        private static final File DEV_TOKEN = new File(RPGCore.inst().getDataFolder().getParentFile() + File.separator + "__rpgcore"
                + File.separator + "dropbox.rpgcore");

        /**
         * Attempt to create a client.
         *
         * @return The client to be created.
         * @throws Exception
         */
        public static DbxClientV2 getClient() throws Exception {
            String token = null;

            if (USER_TOKEN.exists()) {
                try {
                    token = Files.readString(USER_TOKEN.toPath(), StandardCharsets.UTF_8);
                } catch (Exception ex) {
                    // ignored
                }
            }

            if (DEV_TOKEN.exists()) {
                try {
                    token = Files.readString(DEV_TOKEN.toPath(), StandardCharsets.UTF_8);
                } catch (Exception ex) {
                    // ignored
                }
            }

            if (token == null) {
                return null;
            }

            return new DbxClientV2(REQUEST, new DbxCredential("nil", 0L, token, APP_INFO.getKey()));
        }

        /**
         * Start the auth flow for dropbox.
         *
         * @throws Exception Should something go wrong.
         */
        public static void start() throws Exception {
            // Ensure no two auths run at once
            if (PKCE_AUTH != null) {
                Bukkit.getLogger().severe("An authorization flow has already been started!");
                return;
            }

            String uniqueId = getUniqueHostID();
            DbxRequestConfig requestConfig = new DbxRequestConfig(uniqueId);
            Dropbox.PKCE_AUTH = new DbxPKCEWebAuth(requestConfig, APP_INFO);

            DbxWebAuth.Request webAuthRequest =  DbxWebAuth.newRequestBuilder()
                    .withNoRedirect()
                    .withTokenAccessType(TokenAccessType.OFFLINE)
                    .build();

            String authorizeURL = Dropbox.PKCE_AUTH.authorize(webAuthRequest);

            try {
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(authorizeURL), null);
                Bukkit.getLogger().info("1. Go to " + authorizeURL + " (Added to clipboard!)");
            } catch (Throwable ignored) {
                Bukkit.getLogger().info("1. Go to " + authorizeURL);
            }

            Bukkit.getLogger().info("2. Click \"Allow\" (you might have to log in first).");
            Bukkit.getLogger().info("3. Copy the authorization code.");
            Bukkit.getLogger().info("4. Enter the authorization code with \"/rpg dropbox <code>\"");
        }

        /**
         * Finish the auth flow for dropbox.
         *
         * @param code Code provided by user
         */
        public static void finish(String code) throws Exception {
            DbxAuthFinish finish = Dropbox.PKCE_AUTH.finishFromCode(code);
            Dropbox.PKCE_AUTH = null;
            Files.writeString(USER_TOKEN.toPath(), finish.getRefreshToken(), StandardCharsets.UTF_8);
            Bukkit.getLogger().info("Your token was saved! (Do not share this token!)");
        }

        /**
         * Retrieve a distinctive ID for the host.
         *
         * @return A unique host identifier
         */
        public static String getUniqueHostID() throws SocketException, UnknownHostException {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
            byte[] hardwareAddress = ni.getHardwareAddress();
            String[] hexadecimal = new String[hardwareAddress.length];
            for (int i = 0; i < hardwareAddress.length; i++) {
                hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
            }
            return String.join("-", hexadecimal);
        }
    }
}
