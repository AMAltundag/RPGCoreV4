package me.blutkrone.rpgcore.skin;

import me.blutkrone.external.inventive.mineskin.MineskinClient;
import me.blutkrone.external.inventive.mineskin.data.Skin;
import me.blutkrone.external.inventive.mineskin.data.Texture;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SkinPool {

    // MineSkin client implementation
    private MineskinClient skin_client;
    // all skins which we've pooled before
    private Map<String, CoreSkin> pooled = new HashMap<>();
    // query of all skins waiting
    private List<String> queried = new ArrayList<>();
    // true if we are busy
    private boolean working = false;

    public SkinPool() {
        this.skin_client = new MineskinClient("RPGCore/4");

        File directory = FileUtil.directory("editor/skin");
        directory.getParentFile().mkdirs();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        Reader reader = Files.newBufferedReader(file.toPath());
                        CoreSkin loaded = RPGCore.inst().getGsonPretty().fromJson(reader, CoreSkin.class);
                        reader.close();

                        this.pooled.put(loaded.id, loaded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            // ensure we are finished
            if (this.working) {
                return;
            }
            // ensure we got anything on our queue
            if (this.queried.isEmpty()) {
                return;
            }
            // ensure the client is available
            if (skin_client.getNextRequest() > System.currentTimeMillis()) {
                return;
            }
            // request the skin and block the worker
            this.working = true;
            try {
                String id = this.queried.remove(0);
                CompletableFuture<Skin> generated = skin_client.generateUrl(id);
                generated.thenAccept((skin -> {
                    // transform result into a core class
                    Texture texture = skin.data.texture;
                    CoreSkin tracked = new CoreSkin(id, texture.value, texture.signature, texture.url);
                    this.pooled.put(tracked.id, tracked);
                    // cache on disk to not recycle unnecessarily
                    File file = FileUtil.file("editor/skin", UUID.randomUUID() + ".skin");
                    file.getParentFile().mkdirs();

                    try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
                        RPGCore.inst().getGsonPretty().toJson(tracked, fw);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // notify about successful processing
                    RPGCore.inst().getLogger().info("Skin '" + tracked.id + "' was cached, " + this.queried.size() + " skins pending!");
                    // unlock our pool to process another skin
                    this.working = false;
                }));
            } catch (Throwable e) {
                this.working = false;
                e.printStackTrace();
            }
        }, 1, 1);
    }

    /**
     * Retrieve a skin which was pooled before, do note that
     * this may fail.
     *
     * @param url the URL of the pooled skin.
     * @return the skin that was pooled.
     */
    public CoreSkin get(String url) {
        return this.pooled.get(url);
    }

    /**
     * Query a skin from MineSkin.com, the processing of it
     * may finish at any given arbitrary time.
     *
     * @param url skin URL
     */
    public void query(String url) {
        if (!this.pooled.containsKey(url)) {
            this.queried.add(url);
        }
    }
}
