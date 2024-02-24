package me.blutkrone.rpgcore.resourcepack;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.AbstractTexture;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.ConfiguredTexture;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.Model;
import me.blutkrone.rpgcore.resourcepack.generators.*;
import me.blutkrone.rpgcore.resourcepack.utils.GenerationConfiguration;
import me.blutkrone.rpgcore.resourcepack.utils.GenerationWorker;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.util.Consumer;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ResourcepackManager implements Listener {
    private static final File TEMPLATE = FileUtil.directory("resourcepack/template");

    private static final File WORKSPACE_WORKING = FileUtil.directory("resourcepack/working");

    private static final File WORKSPACE_FONT = FileUtil.directory("resourcepack/working/assets/minecraft/font");
    private static final File OUTPUT_MODEL = FileUtil.directory("resourcepack/working/assets/minecraft/models/generated");
    private static final File OUTPUT_TEXTURE = FileUtil.directory("resourcepack/working/assets/minecraft/textures/generated");

    @Deprecated
    private static final File INPUT_FRAME = FileUtil.directory("resourcepack/input/frame");

    private static final File OUTPUT_RESULT = FileUtil.file("resourcepack/output", "result.zip");
    private static final File MEASUREMENT_FILE = FileUtil.file("resourcepack", "measurement.yml");
    private static final File GENERATION_INDEX = FileUtil.file("resourcepack/output", "index.rpgcore");

    // all generators that contribute to the resourcepack
    private List<IGenerator> generators;
    // write resourcepack in a compressed fashion
    private boolean do_compression;
    // indexed parameters from last generation
    private GenerationIndexFile index;
    // a download link for the resourcepack
    private String download_link;
    // players which have been resourcepack initialized
    private Set<UUID> initialized = new HashSet<>();
    // whether we are already compiling
    private boolean compiling;
    public ResourcepackManager() {
        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("resourcepack", "config.yml"));
            // need to compress as much as possible
            do_compression = config.getBoolean("compress");
            // where to download from
            download_link = config.getString("download-url");
            if (download_link != null && download_link.contains("dropbox")) {
                download_link = download_link.replace("?dl=0", "?dl=1").replace("&dl=0", "&dl=1");
            }
            // load generation index if present
            if (GENERATION_INDEX.exists()) {
                byte[] bytes = FileUtils.readFileToByteArray(GENERATION_INDEX);
                BukkitObjectInputStream bois = new BukkitObjectInputStream(new ByteArrayInputStream(bytes));
                try {
                    index = new GenerationIndexFile(bois);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bois.close();
            }

            // register all contributors to resourcepack
            generators = new ArrayList<>();
            generators.add(new PreloadForText());
            generators.add(new PreloadForItem());
            generators.add(new GeneratorForTextMeasurement());
            generators.add(new GeneratorForArmor());
            generators.add(new GeneratorForSound());
            generators.add(new GeneratorForOffsetText());
            generators.add(new GeneratorForMiniMap());
            generators.add(new GeneratorForAnimatedSlots());
            generators.add(new GeneratorForCortexTile());
            generators.add(new GeneratorForCurrencyInMenu());
            generators.add(new GeneratorForCustomMenu());
            generators.add(new GeneratorForFixedTexture());
            generators.add(new GeneratorForImageInDialogue());
            generators.add(new GeneratorForImageInHologram());
            generators.add(new GeneratorForLore());
            generators.add(new GeneratorForMarkerOnMap());
            generators.add(new GeneratorForOrb());
            generators.add(new GeneratorForPassive());
            generators.add(new GeneratorForProgressBar());
            generators.add(new GeneratorForRadial());
            generators.add(new GeneratorForScrollerMenu());
            generators.add(new GeneratorForSkill());
            generators.add(new GeneratorForStatusIcon());
            generators.add(new GeneratorForItemFromImage());
            generators.add(new GeneratorForModelDecoration());
            generators.add(new GeneratorForModelEntity());
            generators.add(new ProcessText());
            generators.add(new ProcessItem());
            generators.add(new FinalizeCompress());
            generators.add(new FinalizeZipper());
            generators.add(new FinalizeUpload());

            // re-compile if resourcepack cannot be found
            if (download_link == null || download_link.isEmpty() || !GENERATION_INDEX.exists() || index == null) {
                compile((file) -> {});
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                    try {
                        URL url = new URL(download_link);
                        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                        int responseCode = huc.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            compile((file) -> {});
                        }
                    } catch (Exception e) {
                        compile((file) -> {});
                    }
                });
            }

            // wipe measuring cache all 5 minutes
            Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
                if (index != null) {
                    index.measuring_cache.clear();
                }
            }, 1, 6000);

            // deal with resourcepack related events
            Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
        } catch (Exception e) {
            throw new RuntimeException("Resource Pack Manager could not be loaded", e);
        }
    }

    /**
     * Invoke the compilation process for a resourcepack.
     *
     * @param callback Called when finished
     */
    public void compile(Consumer<File> callback) {
        // ensure we do not compile multiple times at once
        if (compiling)
            throw new IllegalStateException("The compiler already is working!");
        compiling = true;

        // initialize the compilation process
        GenerationConfiguration config = new GenerationConfiguration(MEASUREMENT_FILE);
        OngoingGeneration ongoing = new OngoingGeneration(this, config);
        GenerationWorker worker = new GenerationWorker();
        // clean up the workspace
        worker.add(true, () -> {
            ongoing.clock().loop();
            FileUtils.deleteDirectory(WORKSPACE_WORKING);
            FileUtils.copyDirectory(TEMPLATE, WORKSPACE_WORKING);
            Bukkit.getLogger().info("Prepared workspace: %sms".formatted(ongoing.clock().loop()));
        });
        // query up every generation task
        for (int i = 0; i < this.generators.size(); i++) {
            int pos = i;
            IGenerator generator = this.generators.get(i);
            worker.add(true, () -> {
                ongoing.clock().loop();
                generator.generate(ongoing);
                Bukkit.getLogger().info("[%s/%s] Generator %s: %sms".formatted(pos+1, this.generators.size(), generator.getClass().getSimpleName(), ongoing.clock().loop()));
            });
        }
        // finish the operation
        worker.add(false, () -> {
            // minimize into configured textures
            for (Map.Entry<String, AbstractTexture> entry : ongoing.hud_textures.entrySet()) {
                entry.setValue(new ConfiguredTexture(entry.getValue()));
            }
            // finalize our index
            index = new GenerationIndexFile(ongoing);
            index.save(GENERATION_INDEX);
            // accept callback
            callback.accept(OUTPUT_RESULT);
            // mark as finished
            compiling = false;
            // clean up working space
            FileUtils.deleteDirectory(WORKSPACE_WORKING);
            // inform about completion
            Bukkit.getLogger().info("Generator has finished!");
        });
        // start the generation process
        worker.work();
    }

    /**
     * Update with a new download url.
     *
     * @param url updated download url.
     */
    public void setUrl(String url) {
        if (url.contains("dropbox")) {
            url = url.replace("?dl=0", "?dl=1").replace("&dl=0", "&dl=1");
        }

        this.download_link = url;

        try {
            File config_file = FileUtil.file("resourcepack", "config.yml");
            ConfigWrapper configuration = FileUtil.asConfigYML(config_file);
            configuration.set("download-url", url);
            FileUtil.saveToDirectory(((YamlConfiguration) configuration.getHandle()), config_file);
        } catch (Exception e) {
            throw new RuntimeException("Resource Pack Manager could not be updated", e);
        }
    }

    /**
     * Translate an alias font to a real font.
     *
     * @param alias_font The alias font to look up
     * @return The real font backing the alias font
     */
    public String aliasToReal(String alias_font) {
        String real_font = this.index.font_alias.get(alias_font);
        if (real_font == null) {
            throw new IllegalArgumentException("The font " + alias_font + " is an unregistered alias!");
        }

        return real_font;
    }

    /**
     * Retrieve the model backing a certain ID
     *
     * @param model Model ID to retrieve
     * @return Model we retrieved
     */
    public Model getModel(String model) {
        return this.index.entities_generated.get(model.toLowerCase());
    }

    /**
     * All models that were generated in the last resourcepack compilation.
     *
     * @return All RPG models
     */
    public Map<String, Model> getModels() {
        return this.index.entities_generated;
    }

    /**
     * Measure basic text, this will <b>NOT</b> accommodate
     *
     * @param text the base text to translate
     * @return the resulting length
     */
    public int measure(String text) {
        return this.index.measure(text);
    }

    /**
     * If compressed we will shrink the resourcepack as much as we can
     * at the expense of performance.
     *
     * @return Compress the resourcepack
     */
    public boolean isCompressed() {
        return this.do_compression;
    }

    /**
     * Verify if a given player has properly initialized their resourcepack,
     * this may require some delay before it works thoroughly.
     *
     * @param player the player who we are checking
     * @return true if they have our resourcepack
     */
    public boolean hasLoaded(Player player) {
        return initialized.contains(player.getUniqueId());
    }

    /**
     * Verify if a given player has properly initialized their resourcepack,
     * this may require some delay before it works thoroughly.
     *
     * @param player the player who we are checking
     * @return true if they have our resourcepack
     */
    public boolean hasLoaded(CorePlayer player) {
        return initialized.contains(player.getUniqueId());
    }

    /**
     * A download link for the resourcepack meant to be conveyed
     * to players.
     *
     * @return the resourcepack we are using
     */
    public String getDownloadLink() {
        return download_link;
    }

    /**
     * All textures registered to the manager.
     *
     * @return the registered textures
     */
    public Map<String, AbstractTexture> textures() {
        return this.index.hud_textures;
    }

    /**
     * Retrieve the texture mapped to a certain identifier.
     *
     * @param texture the texture we want to retrieve
     * @return the resulting texture
     * @throws NullPointerException fired instead of a null value
     */
    public AbstractTexture texture(String texture) throws NullPointerException {
        AbstractTexture result = this.index.hud_textures.get(texture);
        if (result == null)
            throw new NullPointerException("Could not find index of '" + texture + "' texture!");
        return result;
    }

    /**
     * Retrieve the texture mapped to a certain identifier.
     *
     * @param texture the texture we want to retrieve
     * @return the resulting texture
     * @throws NullPointerException fired instead of a null value
     */
    public AbstractTexture texture(String texture, String fallback) throws NullPointerException {
        AbstractTexture result = this.index.hud_textures.getOrDefault(texture, this.index.hud_textures.get(fallback));
        if (result == null)
            throw new NullPointerException("Could not find index of '" + texture + "' texture!");
        return result;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(AsyncPlayerPreLoginEvent e) {
        if (this.download_link == null) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Resourcepack is still being generated!!");
        } else if (this.download_link.equals("error")) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Server could not upload resourcepack!");
        }
    }

    /**
     * When a player connects, send a resourcepack on them.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RPGCore.inst(), () -> {
            event.getPlayer().setResourcePack(download_link);
            event.getPlayer().sendMessage(RPGCore.inst().getLanguageManager().getTranslation("loading_resourcepack"));
        }, 20);
    }

    /**
     * When a player quits, they no longer have the resourcepack
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerQuitEvent event) {
        // clean up to lower memory wastage of our server
        initialized.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Ensure that the player always got the resourcepack
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerResourcePackStatusEvent event) {
        if (download_link == null || "error".equals(download_link)) {
            return;
        }

        PlayerResourcePackStatusEvent.Status i = event.getStatus();
        if (i == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            // we loaded the resourcepack and can play now
            event.getPlayer().sendMessage(RPGCore.inst().getLanguageManager().getTranslation("loaded_resourcepack"));
            initialized.add(event.getPlayer().getUniqueId());
        } else if (i == PlayerResourcePackStatusEvent.Status.DECLINED) {
            // we rejected downloading the resourcepack linked
            event.getPlayer().kickPlayer(RPGCore.inst().getLanguageManager().getTranslation("failed_resourcepack"));
        } else if (i == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            // we failed downloading the resourcepack linked
            event.getPlayer().kickPlayer(RPGCore.inst().getLanguageManager().getTranslation("failed_resourcepack"));
        } else if (i == PlayerResourcePackStatusEvent.Status.ACCEPTED) {
            // the resourcepack is now being actively downloaded
            event.getPlayer().sendMessage(RPGCore.inst().getLanguageManager().getTranslation("accepted_resourcepack"));
        }
    }

}