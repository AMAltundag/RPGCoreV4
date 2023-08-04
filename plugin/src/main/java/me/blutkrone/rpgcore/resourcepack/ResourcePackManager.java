package me.blutkrone.rpgcore.resourcepack;

import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.resourcepack.bbmodel.BBExporter;
import me.blutkrone.rpgcore.resourcepack.component.ResourcePackFont;
import me.blutkrone.rpgcore.resourcepack.component.ResourcePackItem;
import me.blutkrone.rpgcore.resourcepack.generated.*;
import me.blutkrone.rpgcore.resourcepack.upload.KeepUploader;
import me.blutkrone.rpgcore.resourcepack.upload.TempUploader;
import me.blutkrone.rpgcore.resourcepack.upload.TransferUploader;
import me.blutkrone.rpgcore.resourcepack.utils.CompileClock;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.resourcepack.utils.ResourceUtil;
import me.blutkrone.rpgcore.resourcepack.utils.ResourcepackGeneratorMeasured;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Assists with creating a resourcepack
 */
public class ResourcePackManager implements Listener {

    private static final File TEMPLATE
            = FileUtil.directory("resourcepack/template");

    private static final File WORKSPACE_WORKING
            = FileUtil.directory("resourcepack/working");
    private static final File WORKSPACE_FONT
            = FileUtil.directory("resourcepack/working/assets/minecraft/font");
    private static final File WORKSPACE_ITEM
            = FileUtil.directory("resourcepack/working/assets/minecraft/models/item");
    private static final File WORKSPACE_SOUNDS
            = FileUtil.directory("resourcepack/working/assets/minecraft/sounds/custom");
    private static final File WORKSPACE_SOUND_FILE
            = FileUtil.file("resourcepack/working/assets/minecraft/sounds.json");

    private static final File INPUT_TEXTURE
            = FileUtil.directory("resourcepack/generated/item");
    private static final File INPUT_BBMODEL
            = FileUtil.directory("resourcepack/generated/bbmodel");
    private static final File INPUT_SOUND
            = FileUtil.directory("resourcepack/generated/sound");
    private static final File INPUT_TEXT
            = FileUtil.directory("resourcepack/generated/text");
    private static final File INPUT_MENU
            = FileUtil.directory("resourcepack/generated/menu");
    private static final File INPUT_MARKER
            = FileUtil.directory("resourcepack/generated/marker");
    private static final File INPUT_STATUS
            = FileUtil.directory("resourcepack/generated/status");
    private static final File INPUT_SKILLBAR
            = FileUtil.directory("resourcepack/generated/skillbar");
    private static final File INPUT_INTERFACE
            = FileUtil.directory("resourcepack/generated/interface");
    private static final File INPUT_ARMOR
            = FileUtil.directory("resourcepack/generated/armor");
    private static final File INPUT_PORTRAIT
            = FileUtil.directory("resourcepack/generated/portrait");
    private static final File INPUT_FRAME
            = FileUtil.directory("resourcepack/generated/frame");
    private static final File INPUT_LORE_STYLE
            = FileUtil.directory("resourcepack/generated/lore/style");
    private static final File INPUT_LORE_ICON
            = FileUtil.directory("resourcepack/generated/lore/icon");
    private static final File INPUT_LORE_JEWEL
            = FileUtil.directory("resourcepack/generated/lore/jewel");
    private static final File INPUT_ANIMATION_SLOT
            = FileUtil.directory("resourcepack/generated/animation/slot");
    private static final File INPUT_CORTEX_SMALL
            = FileUtil.directory("resourcepack/generated/cortex/small");
    private static final File INPUT_CORTEX_MEDIUM
            = FileUtil.directory("resourcepack/generated/cortex/medium");
    private static final File INPUT_CORTEX_LARGE
            = FileUtil.directory("resourcepack/generated/cortex/large");
    private static final File INPUT_HOLOGRAM
            = FileUtil.directory("resourcepack/generated/hologram");
    private static final File INPUT_SCROLLER
            = FileUtil.directory("resourcepack/generated/scroller");
    private static final File INPUT_CURRENCY
            = FileUtil.directory("resourcepack/generated/currency");
    private static final File INPUT_DIALOGUE
            = FileUtil.directory("resourcepack/generated/dialogue");
    private static final File INPUT_SELFIE
            = FileUtil.directory("resourcepack/generated/selfie");
    private static final File INPUT_QUEST
            = FileUtil.directory("resourcepack/generated/quest");
    private static final File INPUT_FOCUS_SIGIL
            = FileUtil.directory("resourcepack/generated/focus");
    private static final File INPUT_PASSIVE
            = FileUtil.directory("resourcepack/generated/passive");

    private static final File OUTPUT_FONT
            = FileUtil.directory("resourcepack/working/assets/minecraft/textures/font");
    private static final File OUTPUT_MODEL
            = FileUtil.directory("resourcepack/working/assets/minecraft/models/generated");
    private static final File OUTPUT_TEXTURE
            = FileUtil.directory("resourcepack/working/assets/minecraft/textures/generated");
    private static final File OUTPUT_ARMOR
            = FileUtil.directory("resourcepack/working/assets/minecraft/textures/models/armor");

    private static final File OUTPUT_RESULT
            = FileUtil.file("resourcepack/output", "result.zip");

    private static File INDEX_TEXTURE
            = FileUtil.file("resourcepack/output", "index-texture.rpgcore");

    private static File MEASUREMENT_FILE
            = FileUtil.file("resourcepack", "measurement.yml");

    // players which have been resourcepack initialized
    private Set<UUID> resourcepack_initialized = new HashSet<>();
    // a download link for the resourcepack
    private String download_link;
    // whether we are already compiling
    private boolean compiling;
    // index of all font-textures we got
    private Map<String, IndexedTexture.ConfigTexture> indexed_fonts = new HashMap<>();
    private Map<String, Double> indexed_parameter = new HashMap<>();
    private Map<String, String> indexed_alias = new HashMap<>();
    // base-line for symbol size tracking
    private Map<Character, Integer> chars_measurement = null;
    // cache of frequent requests
    private Map<String, Integer> measure_cache = new ConcurrentHashMap<>();
    // whether to apply image compression
    private boolean do_compression;

    public ResourcePackManager() {
        // basic configuration for the RP manager
        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("resourcepack", "config.yml"));
            do_compression = config.getBoolean("compress");

            download_link = config.getString("download-url");
            if (download_link != null && download_link.contains("dropbox")) {
                download_link = download_link.replace("?dl=0", "?dl=1");
            }

        } catch (Exception e) {
            throw new RuntimeException("Resource Pack Manager could not be loaded", e);
        }

        // font specific logic implementation
        try {
            // read up the textures
            if (ResourcePackManager.INDEX_TEXTURE.exists()) {
                byte[] bytes = FileUtils.readFileToByteArray(ResourcePackManager.INDEX_TEXTURE);
                BukkitObjectInputStream bois = new BukkitObjectInputStream(new ByteArrayInputStream(bytes));
                int total = bois.readInt();
                for (int i = 0; i < total; i++) {
                    String id = bois.readUTF();
                    String symbol = bois.readUTF();
                    String table = bois.readUTF();
                    int width = bois.readInt();
                    int height = bois.readInt();
                    this.indexed_fonts.put(id, new IndexedTexture.ConfigTexture(symbol, table, width, height));
                }

                total = bois.readInt();
                for (int i = 0; i < total; i++) {
                    String id = bois.readUTF();
                    double value = bois.readDouble();
                    this.indexed_parameter.put(id, value);
                }

                total = bois.readInt();
                for (int i = 0; i < total; i++) {
                    String alias_font = bois.readUTF();
                    String real_font = bois.readUTF();
                    this.indexed_alias.put(alias_font, real_font);
                }
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not parse indexed resourcepack parameters!");
        }

        // deal with resourcepack related events
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());

        // ensure URL is fine
        if (this.download_link == null || this.download_link.isEmpty()) {
            compile((file -> {
            }));
        } else {
            try {
                URL url = new URL(this.download_link);
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                int responseCode = huc.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    compile((file -> {
                    }));
                }
            } catch (Exception e) {
                compile((file -> {
                }));
            }
        }

        // wipe cache once per 5 minutes
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> this.measure_cache.clear(), 1, 6000);
    }

    /*
     * A copy that separates the data array
     */
    private static BufferedImage deepCopy(BufferedImage bi) {
        BufferedImage b = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.createGraphics();
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        return b;
    }

    /**
     * Translate an alias font to a real font.
     *
     * @param alias_font The alias font to look up
     * @return The real font backing the alias font
     */
    public String aliasToReal(String alias_font) {
        String real_font = this.indexed_alias.get(alias_font);
        if (real_font == null) {
            throw new IllegalArgumentException("The font " + alias_font + " is an unregistered alias!");
        }

        return real_font;
    }

    /**
     * Measure basic text, this will <b>NOT</b> accommodate
     *
     * @param text the base text to translate
     * @return the resulting length
     */
    public int measure(String text) {
        // build the character measurement table
        if (this.chars_measurement == null) {
            Map<Character, Integer> computed = new HashMap<>();
            this.indexed_parameter.forEach((key, value) -> {
                if (key.startsWith("char_length_")) {
                    char c = (char) Integer.parseInt(key.substring(12));
                    computed.put(c, value.intValue());
                }
            });
            this.chars_measurement = computed;
            this.measure_cache = new ConcurrentHashMap<>();
        }
        // fetch from cache if applicable
        text = ChatColor.translateAlternateColorCodes('&', text);
        return measure_cache.computeIfAbsent(text, (string -> {
            List<Integer> length = new ArrayList<>();
            // measure the text within our specifics
            int messagePxSize = 0;
            boolean previousCode = false;
            boolean isBold = false;
            for (char c : string.toCharArray()) {
                if (c == '§') {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    isBold = c == 'l' || c == 'L';
                } else {
                    messagePxSize += this.chars_measurement.getOrDefault(c, 0) + ((isBold && c == ' ') ? 1 : 0);
                    messagePxSize++;
                    length.add(this.chars_measurement.getOrDefault(c, 0));
                }
            }

            return messagePxSize;
        }));
    }

    /**
     * Update with a new download url.
     *
     * @param url updated download url.
     */
    public void setUrl(String url) {
        if (url.contains("dropbox")) {
            url = url.replace("?dl=0", "?dl=1");
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
     * Compile the resourcepack from our files, serving a quick and
     * easy method of generating relevant files.
     *
     * @param callback called once completed, may call with null if
     *                 failed.
     */
    public void compile(Consumer<File> callback) {
        if (compiling)
            throw new IllegalStateException("The compiler already is working!");
        compiling = true;

        // shared parameters for all workers
        ResourcepackGeneratorMeasured rules = new ResourcepackGeneratorMeasured(MEASUREMENT_FILE);
        Map<String, List<ResourcePackFont>> fonts = new HashMap<>();
        Map<Material, ResourcePackItem> items = new HashMap<>();
        Map<String, ResourcePackItem.ItemModel> generated_models = new HashMap<>();
        CompileClock clock = new CompileClock();
        Map<String, IndexedTexture> indexed_fonts = new HashMap<>();
        Map<String, IndexedTexture> self_indexed = new HashMap<>();
        Map<String, Double> indexed_parameter = new HashMap<>();
        Map<String, String> indexed_alias = new HashMap<>();

        // worker jobs to create the resourcepack
        ResourcePackThreadWorker worker = new ResourcePackThreadWorker();

        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // Clear our workspace, giving us a clean space to work within
            FileUtils.deleteDirectory(WORKSPACE_WORKING);
            // Copy the template into our working directory
            FileUtils.copyDirectory(TEMPLATE, WORKSPACE_WORKING);
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Preparing the working space in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            TextGenerator.measurement(INPUT_TEXT).forEach((c, len) -> {
                indexed_parameter.put("char_length_" + ((int) c), 0d + len);
            });
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Processed font measurements in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // each shader armor has two layers
            Map<Integer, BufferedImage> layer1 = new HashMap<>();
            Map<Integer, BufferedImage> layer2 = new HashMap<>();
            // load all shader armors on the disk
            for (File armor_files : INPUT_ARMOR.listFiles()) {
                int color = Integer.parseInt(armor_files.getName());
                if (color >= (2 << (8 * 3 - 1)) || color < 0) {
                    RPGCore.inst().getLogger().severe("Illegal Shader Armor ID: " + color);
                } else {
                    layer1.put(color, ImageIO.read(new File(armor_files, "layer_1.png")));
                    layer2.put(color, ImageIO.read(new File(armor_files, "layer_2.png")));
                }
            }
            int max_height = 0;
            for (BufferedImage image : layer1.values())
                max_height = Math.max(max_height, image.getHeight());
            for (BufferedImage image : layer2.values())
                max_height = Math.max(max_height, image.getHeight());
            // ensure file-path exists
            OUTPUT_ARMOR.mkdirs();
            // create empty overlay textures
            ImageIO.write(new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB), "png", new File(OUTPUT_ARMOR, "leather_layer_1_overlay.png"));
            ImageIO.write(new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB), "png", new File(OUTPUT_ARMOR, "leather_layer_2_overlay.png"));
            // concat all other textures
            BufferedImage joined1 = new BufferedImage(64 * layer1.size(), max_height, BufferedImage.TYPE_INT_ARGB);
            BufferedImage joined2 = new BufferedImage(64 * layer2.size(), max_height, BufferedImage.TYPE_INT_ARGB);
            // armor 0 is the default leather armor
            for (int i = 0; i < 64; i++) {
                for (int j = 0; j < 32; j++) {
                    joined1.setRGB(i, j, layer1.get(0).getRGB(i, j));
                    joined2.setRGB(i, j, layer2.get(0).getRGB(i, j));
                }
            }
            // drop the layer0 from further processing
            layer1.remove(0);
            layer2.remove(0);
            // everything else should simply be appended
            int n = 1;
            for (Map.Entry<Integer, BufferedImage> layer1_entry : layer1.entrySet()) {
                // fetch the texture which we are operating with
                BufferedImage layer1_texture = layer1_entry.getValue();
                BufferedImage layer2_texture = layer2.get(layer1_entry.getKey());
                // draw the respective textures
                for (int i = 0; i < 64; i++) {
                    for (int j = 0; j < Math.min(layer1_texture.getHeight(), layer2_texture.getHeight()); j++) {
                        joined1.setRGB(i + 64 * n, j, layer1_texture.getRGB(i, j));
                        joined2.setRGB(i + 64 * n, j, layer2_texture.getRGB(i, j));
                    }
                }
                // draw the identifying marker
                int marker = layer1_entry.getKey() | 0xFF000000;
                joined1.setRGB(64 * n, 0, marker);
                joined2.setRGB(64 * n, 0, marker);
                // increment our counter
                n += 1;
            }
            // save the joined layers on the disk
            ImageIO.write(joined1, "png", new File(OUTPUT_ARMOR, "leather_layer_1.png"));
            ImageIO.write(joined2, "png", new File(OUTPUT_ARMOR, "leather_layer_2.png"));
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Processed shader armors in %sms", clock.loop()));

        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // generate the sound specific rules
            Set<String> generated = new HashSet<>();
            JSONObject sounds = new JSONObject();
            for (File file : FileUtil.buildAllFiles(INPUT_SOUND)) {
                String name = file.getName().split("\\.")[0];
                // ensure no duplicate sounds
                if (!generated.add(name))
                    continue;
                // move to relevant directory
                FileUtils.copyFile(file, new File(WORKSPACE_SOUNDS, file.getName()));
                // offer the sound to the json structure
                JSONObject sound = new JSONObject();
                sound.put("category", "master");
                sound.put("sounds", new String[]{"custom/" + name});
                sounds.put("generated." + name, sound);
            }
            // register the sounds we have added
            ResourceUtil.saveToDisk(sounds, WORKSPACE_SOUND_FILE, do_compression);
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Processed audio files in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // pre-load every fonts offered via template
            for (File font_file : FileUtil.buildAllFiles(WORKSPACE_FONT)) {
                String filename = font_file.getName();
                if (!filename.endsWith(".json")) continue;
                filename = filename.substring(0, filename.length() - 5);
                fonts.put(filename, ResourceUtil.workingLoadFont(font_file));
            }
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Loading pre-existing fonts in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // compute all custom fonts with vertical offsets
            TextGenerator.TextGeneratorResult result = TextGenerator.construct(INPUT_TEXT, rules, OUTPUT_FONT);
            // prepare the respective font rendering pages
            result.generated_fonts.forEach((id, list) -> {
                fonts.computeIfAbsent(id, (k -> new ArrayList<>())).addAll(list);
            });
            // keep track of the alias mappings
            indexed_alias.putAll(result.generated_alias);
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Loading fonts with special offsets %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // generate the pixel used by the minimap
            int counter = 0;
            for (int j = 0; j < 3; j++) {
                BufferedImage minimap_pixel = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
                if (j == 0) {
                    minimap_pixel.setRGB(0, 0, 0xFFFFFFFF);
                    minimap_pixel.setRGB(1, 0, 0xFFFFFFFF);
                    minimap_pixel.setRGB(0, 1, 0xFFFFFFFF);
                    minimap_pixel.setRGB(1, 1, 0xFFFFFFFF);
                } else if (j == 1) {
                    minimap_pixel.setRGB(0, 0, 0x40FFFFFF);
                    minimap_pixel.setRGB(1, 0, 0x40FFFFFF);
                    minimap_pixel.setRGB(0, 1, 0x40FFFFFF);
                    minimap_pixel.setRGB(1, 1, 0x40FFFFFF);
                } else if (j == 2) {
                    minimap_pixel.setRGB(0, 0, 0x20FFFFFF);
                    minimap_pixel.setRGB(1, 0, 0x20FFFFFF);
                    minimap_pixel.setRGB(0, 1, 0x20FFFFFF);
                    minimap_pixel.setRGB(1, 1, 0x20FFFFFF);
                }

                for (int i = 0; i <= 48; i++) {
                    IndexedTexture.GeneratedTexture generated = new IndexedTexture.GeneratedTexture((char) (256 + counter++), "minimap_pixel", minimap_pixel, rules.navigator_offset - 7 - i*2);
                    indexed_fonts.put("minimap_pixel_" + j + "_" + i, generated);
                }
            }
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Generated 'maps' font textures in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            StaticGenerator.PooledSymbolSpace interface_base_space = new StaticGenerator.PooledSymbolSpace();
            // reset the clock used to measure time
            clock.loop();
            // basic textures for the menus
            indexed_fonts.putAll(MenuGenerator.construct(INPUT_MENU));
            RPGCore.inst().getLogger().info(String.format("Generated 'menu' font textures in %sms", clock.loop()));
            // marker textures for the compass
            indexed_fonts.putAll(MarkerGenerator.construct(INPUT_MARKER, rules));
            RPGCore.inst().getLogger().info(String.format("Generated 'markers' font textures in %sms", clock.loop()));
            // status effect icons for self
            indexed_fonts.putAll(StatusGenerator.construct(INPUT_STATUS, "self_upper", rules.status_self_upper_offset));
            indexed_fonts.putAll(StatusGenerator.construct(INPUT_STATUS, "self_lower", rules.status_self_lower_offset));
            RPGCore.inst().getLogger().info(String.format("Generated 'status' font textures in %sms", clock.loop()));
            // icons specific to skills
            indexed_fonts.putAll(SkillGenerator.construct(INPUT_SKILLBAR, rules.skillbar_offset, rules.focus_skillbar_offset));
            RPGCore.inst().getLogger().info(String.format("Generated 'skill' font textures in %sms", clock.loop()));
            // progress bar for player activity
            indexed_fonts.putAll(BarGenerator.construct(FileUtil.file("resourcepack/generated/interface", "activity_filling.png"), rules.activity_offset));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file("resourcepack/generated/interface", "activity_back.png"), "interface_base", rules.activity_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file("resourcepack/generated/interface", "activity_front.png"), "interface_base", rules.activity_offset, interface_base_space));
            RPGCore.inst().getLogger().info(String.format("Generated 'activity' font textures in %sms", clock.loop()));
            // resources of the shown player
            indexed_fonts.putAll(OrbGenerator.construct(FileUtil.file(INPUT_INTERFACE, "self_health.png"), rules.health_orb_offset));
            indexed_fonts.putAll(OrbGenerator.construct(FileUtil.file(INPUT_INTERFACE, "self_mana.png"), rules.mana_orb_offset));
            indexed_fonts.putAll(RadialGenerator.construct(FileUtil.file(INPUT_INTERFACE, "self_ward.png"), rules.ward_radial_offset));
            indexed_fonts.putAll(RadialGenerator.construct(FileUtil.file(INPUT_INTERFACE, "self_stamina.png"), rules.stamina_radial_offset));
            RPGCore.inst().getLogger().info(String.format("Generated 'resource' font textures in %sms", clock.loop()));
            // party member specifics
            for (int i = 0; i < 5; i++) {
                int displacement = rules.party_offset - (rules.party_distance * i);
                // basic interface design
                indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "party_back.png"), "interface_base", "member_" + i, displacement, interface_base_space));
                indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "party_front.png"), "interface_base", "member_" + i, displacement, interface_base_space));
                indexed_fonts.putAll(BarGenerator.construct(FileUtil.file(INPUT_INTERFACE, "party_health_filling.png"), String.valueOf(i), displacement + rules.party_health_offset));
                indexed_fonts.putAll(BarGenerator.construct(FileUtil.file(INPUT_INTERFACE, "party_ward_filling.png"), String.valueOf(i), displacement + rules.party_ward_offset));
            }
            RPGCore.inst().getLogger().info(String.format("Generated 'party' font textures in %sms", clock.loop()));
            // focused entity specifics
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "focus_back.png"), "interface_base", rules.focus_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "focus_front.png"), "interface_base", rules.focus_offset, interface_base_space));
            indexed_fonts.putAll(BarGenerator.construct(FileUtil.file(INPUT_INTERFACE, "focus_health_filling.png"), rules.focus_offset + rules.focus_health_offset));
            indexed_fonts.putAll(BarGenerator.construct(FileUtil.file(INPUT_INTERFACE, "focus_ward_filling.png"), rules.focus_offset + rules.focus_ward_offset));
            indexed_fonts.putAll(StatusGenerator.construct(INPUT_STATUS, "focus", rules.focus_offset + rules.focus_status_offset));
            RPGCore.inst().getLogger().info(String.format("Generated 'focus' font textures in %sms", clock.loop()));
            // generic item lore specific
            indexed_fonts.putAll(StatusGenerator.construct(INPUT_STATUS, "item_lore", 0));
            // basic interface components to be rendered
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "navigator_back.png"), "interface_base", rules.navigator_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "navigator_overlay.png"), "interface_base", rules.navigator_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "navigator_front_N.png"), "interface_base", rules.navigator_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "navigator_front_S.png"), "interface_base", rules.navigator_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "navigator_front_E.png"), "interface_base", rules.navigator_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "navigator_front_W.png"), "interface_base", rules.navigator_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "navigator_front_NE.png"), "interface_base", rules.navigator_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "navigator_front_NW.png"), "interface_base", rules.navigator_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "navigator_front_SE.png"), "interface_base", rules.navigator_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "navigator_front_SW.png"), "interface_base", rules.navigator_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "plate_back.png"), "interface_base", rules.plate_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "plate_front.png"), "interface_base", rules.plate_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "plate_glass.png"), "interface_base", rules.plate_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "skill_unaffordable.png"), "interface_base", rules.skillbar_offset, interface_base_space));
            indexed_fonts.putAll(StaticGenerator.construct(FileUtil.file(INPUT_INTERFACE, "skill_cooldown.png"), "interface_base", rules.skillbar_offset, interface_base_space));
            RPGCore.inst().getLogger().info(String.format("Generated 'misc' font textures in %sms", clock.loop()));
            // track symbols used to build the lore
            indexed_fonts.putAll(LoreGenerator.construct(INPUT_LORE_STYLE, INPUT_LORE_ICON, INPUT_LORE_JEWEL, INPUT_SKILLBAR, rules, new StaticGenerator.PooledSymbolSpace()));
            RPGCore.inst().getLogger().info(String.format("Generated 'lore' font textures in %sms", clock.loop()));
            // generate job portraits
            StaticGenerator.PooledSymbolSpace portrait_symbol_space = new StaticGenerator.PooledSymbolSpace();
            for (File file : FileUtil.buildAllFiles(INPUT_PORTRAIT)) {
                indexed_fonts.putAll(StaticGenerator.construct(file, "portrait", "portrait", rules.portrait_offset, portrait_symbol_space));
            }
            RPGCore.inst().getLogger().info(String.format("Generated 'portrait' font textures in %sms", clock.loop()));
            // generate frame wrappers
            // indexed_fonts.putAll(FrameGenerator.construct(FileUtil.file(INPUT_FRAME, "instruction.png"), "instruction", rules.instruction_offset));
            RPGCore.inst().getLogger().info(String.format("Generated 'frame' font textures in %sms", clock.loop()));
            // generate slot based menu animations
            indexed_fonts.putAll(AnimationGenerator.createSlotAnimation(INPUT_ANIMATION_SLOT));
            RPGCore.inst().getLogger().info(String.format("Generated 'slot animation' font textures in %sms", clock.loop()));
            // passive texture
            indexed_fonts.putAll(PassiveGenerator.createSlotAnimation(INPUT_PASSIVE));
            RPGCore.inst().getLogger().info(String.format("Generated 'passive' font textures in %sms", clock.loop()));
            // generate cortex menu textures
            indexed_fonts.putAll(CortexGenerator.constructSmall(INPUT_CORTEX_SMALL));
            indexed_fonts.putAll(CortexGenerator.constructMedium(INPUT_CORTEX_MEDIUM));
            indexed_fonts.putAll(CortexGenerator.constructLarge(INPUT_CORTEX_LARGE));
            RPGCore.inst().getLogger().info(String.format("Generated 'cortex' font textures in %sms", clock.loop()));
            // textures used by holograms
            indexed_fonts.putAll(HologramGenerator.construct(INPUT_HOLOGRAM));
            RPGCore.inst().getLogger().info(String.format("Generated 'hologram' font textures in %sms", clock.loop()));
            // textures used by scroller menu
            indexed_fonts.putAll(ScrollerGenerator.construct(INPUT_SCROLLER));
            RPGCore.inst().getLogger().info(String.format("Generated 'scroller' font textures in %sms", clock.loop()));
            // textures used by currencies
            indexed_fonts.putAll(CurrencyGenerator.construct(INPUT_CURRENCY));
            RPGCore.inst().getLogger().info(String.format("Generated 'currency' font textures in %sms", clock.loop()));
            // textures used by dialogue
            indexed_fonts.putAll(DialogueGenerator.construct(INPUT_DIALOGUE));
            RPGCore.inst().getLogger().info(String.format("Generated 'dialogue' font textures in %sms", clock.loop()));
            // textures used by dialogue
            StaticGenerator.PooledSymbolSpace selfie_symbol_space = new StaticGenerator.PooledSymbolSpace();
            for (File file : FileUtil.buildAllFiles(INPUT_SELFIE)) {
                indexed_fonts.putAll(StaticGenerator.construct(file, "dialogue_selfie", "selfie", MenuGenerator.MENU_VERTICAL_OFFSET, selfie_symbol_space));
            }
            RPGCore.inst().getLogger().info(String.format("Generated 'selfie' font textures in %sms", clock.loop()));
            // sigils are used on the focus bar
            StaticGenerator.PooledSymbolSpace sigil_space = new StaticGenerator.PooledSymbolSpace();
            for (File file : FileUtil.buildAllFiles(INPUT_FOCUS_SIGIL)) {
                indexed_fonts.putAll(StaticGenerator.construct(file, "focus_sigil", "focus_sigil", rules.focus_sigil_offset, sigil_space));
            }
            RPGCore.inst().getLogger().info(String.format("Generated 'sigil' font textures in %sms", clock.loop()));
            // textures used by quests
            StaticGenerator.PooledSymbolSpace quest_symbol_space = new StaticGenerator.PooledSymbolSpace();
            for (File file : FileUtil.buildAllFiles(INPUT_QUEST)) {
                indexed_fonts.putAll(StaticGenerator.construct(file, "quest_icon", "quest_icon", rules.quest_offset, quest_symbol_space));
            }
            RPGCore.inst().getLogger().info(String.format("Generated 'quest' font textures in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // unique identifier for the generic fonts
            int uid_track = 0;
            // transform generated entries into resourcepack data
            for (Map.Entry<String, IndexedTexture> entry : indexed_fonts.entrySet()) {
                IndexedTexture texture = entry.getValue();
                // pool all generated textures up (cuz some textures are compounded)
                List<IndexedTexture.GeneratedTexture> candidates = new ArrayList<>();
                if (texture instanceof IndexedTexture.GeneratedTexture) {
                    candidates.add(((IndexedTexture.GeneratedTexture) texture));
                } else if (texture instanceof IndexedTexture.GeneratedCompoundTexture) {
                    candidates.addAll(((IndexedTexture.GeneratedCompoundTexture) texture).getTextures());
                }
                // create all the textures we require
                for (IndexedTexture.GeneratedTexture candidate : candidates) {
                    // create basic header information
                    List<ResourcePackFont> font = fonts.computeIfAbsent(candidate.table, (k -> TextGenerator.newList()));
                    char symbol = candidate.symbol.charAt(0);
                    int ascent = candidate.offset;
                    int height = candidate.texture.getHeight();
                    BufferedImage image = deepCopy(candidate.texture); // todo: pool up texture
                    // adjust the displacement of the texture
                    if (ascent < 0) {
                        // create a padded copy with a padding above
                        image = ResourceUtil.imageCopyPadded(image, (-1 * ascent), 0, 0, 0);
                        height += -1 * ascent;
                        ascent = 0;
                    } else if (ascent > height) {
                        // create a padded copy with a padding below
                        image = ResourceUtil.imageCopyPadded(image, 0, ascent - height, 0, 0);
                        height += (ascent - height);
                        ascent = height;
                    }
                    // secure minimum opacity to maintain texture dimensions
                    int argb = image.getRGB(image.getWidth() - 1, image.getHeight() - 1);
                    int alpha = (argb >> 24) & 0xFF;
                    if (alpha < 2) {
                        image.setRGB(image.getWidth() - 1, image.getHeight() - 1, 0x16FFFFFF);
                    }
                    // save the image file we are to operate with
                    File exported_character = FileUtil.file(OUTPUT_FONT, "generated_" + uid_track + ".png");
                    ImageIO.write(image, "png", exported_character);
                    // note down our character that we've generated
                    font.add(new ResourcePackFont("bitmap", "minecraft:font/generated_" + uid_track + ".png", ascent, height, Collections.singletonList(String.valueOf(symbol))));
                    // increment the symbol we are backed up by
                    uid_track += 1;
                }
            }

            RPGCore.inst().getLogger().info(String.format("Built %s font textures in %sms", indexed_fonts.size(), clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // Load up items which have been specified previously already
            items.putAll(ResourceUtil.workingLoadItem(WORKSPACE_ITEM));
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Loading pre-existing items in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // Deal with the textures that are tied to our generator
            for (File file_custom_texture : FileUtil.buildAllFiles(INPUT_TEXTURE)) {
                // Transfer all textures that we are dealing with into our directory
                FileUtils.copyFile(file_custom_texture, FileUtil.file(OUTPUT_TEXTURE, file_custom_texture.getName()));
                // Should we be written in the format of material_number we expect generation
                String texture_name = file_custom_texture.getName();
                if (!texture_name.endsWith(".png")) continue;
                try {
                    texture_name = texture_name.replace(".png", "");
                    int split_point = texture_name.lastIndexOf('_');
                    if (split_point == -1) continue;
                    Material material = Material.valueOf(texture_name.substring(0, split_point).toUpperCase());
                    int model_data = Integer.parseInt(texture_name.substring(split_point + 1));
                    // mark down the item in our intended usage case
                    items.computeIfAbsent(material, (k -> ResourceUtil.createItemCompound(material)))
                            .overrides.add(new ResourcePackItem.ItemOverride(model_data, "minecraft:generated/" + texture_name));
                    generated_models.put(texture_name, new ResourcePackItem.ItemModel("minecraft:item/handheld", "minecraft:generated/" + texture_name));
                } catch (Exception ignored) {
                    // a failure means that we are a special texture that is referenced
                    // thorough a custom model, not requiring our dedicated generation.
                }
            }
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Generating texture based items in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // Deal with the blockbench models we operate with
            for (File file_bb_model : FileUtil.buildAllFiles(INPUT_BBMODEL)) {
                // identify which item backs up the given model
                String model_name = file_bb_model.getName();
                if (!model_name.endsWith(".bbmodel")) {
                    continue;
                }
                model_name = model_name.replace(".bbmodel", "");
                int split_point = model_name.lastIndexOf('_');
                if (split_point == -1) {
                    continue;
                }
                // transform the blockbench file into an acceptable workspace
                try {
                    BBExporter.Exported bb_export = BBExporter.export(file_bb_model);
                    // export the textures we got buffered to the resourcepack
                    bb_export.saveTextureToDirectory(OUTPUT_TEXTURE, file_bb_model);
                    // json export the working model we picked
                    bb_export.saveModelToFile(FileUtil.file(OUTPUT_MODEL, model_name + ".json"), do_compression);
                    // make the model we created accessible
                    Material material = Material.valueOf(model_name.substring(0, split_point).toUpperCase());
                    int model_data = Integer.parseInt(model_name.substring(split_point + 1));
                    items.computeIfAbsent(material, (k -> ResourceUtil.createItemCompound(material)))
                            .overrides.add(new ResourcePackItem.ItemOverride(model_data, "minecraft:generated/" + model_name));
                } catch (Exception ex) {
                    RPGCore.inst().getLogger().severe("BBModel could not parse: " + file_bb_model.getPath());
                    ex.printStackTrace();
                }
            }
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Generating bbmodel based items in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // Construct the font file which matters to our case
            for (Map.Entry<String, List<ResourcePackFont>> entry : fonts.entrySet()) {
                // extract basic parameters
                String table = entry.getKey();
                List<ResourcePackFont> letters = entry.getValue();
                // transform to json based data
                JSONObject font_output = new JSONObject();
                JSONArray font_providers = new JSONArray();
                font_providers.add(ResourcePackFont.getAsSpace(" ", 4));
                for (ResourcePackFont generated_character : letters)
                    font_providers.add(generated_character.transform());
                font_output.put("providers", font_providers);
                // identify the workspace we operate with
                File file = FileUtil.file(WORKSPACE_FONT, table + ".json");
                // construct the data we expect
                ResourceUtil.saveToDisk(font_output, file, do_compression);
            }
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Constructing the font registry in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // construct the models that were generated on-the-fly
            for (Map.Entry<String, ResourcePackItem.ItemModel> entry : generated_models.entrySet()) {
                File save_target = FileUtil.file(OUTPUT_MODEL, entry.getKey() + ".json");
                ResourceUtil.saveToDisk(entry.getValue().transform(), save_target, do_compression);
            }
            // construct the item configuration for all models
            for (Map.Entry<Material, ResourcePackItem> entry : items.entrySet()) {
                File save_target = FileUtil.file(WORKSPACE_ITEM, entry.getKey().name().toLowerCase() + ".json");
                ResourceUtil.saveToDisk(entry.getValue().transform(), save_target, do_compression);
            }
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Constructing the item registry in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // already processed data should be offered now
            indexed_fonts.putAll(self_indexed);
            // allow the server to access the new index
            this.indexed_fonts = new HashMap<>();
            indexed_fonts.forEach((k, v) -> this.indexed_fonts.put(k, new IndexedTexture.ConfigTexture(v)));
            this.indexed_parameter = indexed_parameter;
            this.indexed_alias = indexed_alias;
            try {
                // create index files if they do not exist
                ResourcePackManager.INDEX_TEXTURE.getParentFile().mkdirs();
                ResourcePackManager.INDEX_TEXTURE.createNewFile();
                // track relevant persistence information
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                BukkitObjectOutputStream boos = new BukkitObjectOutputStream(stream);
                // track data of the individual symbols we got
                boos.writeInt(this.indexed_fonts.size());
                this.indexed_fonts.forEach((id, value) -> {
                    try {
                        boos.writeUTF(id);
                        boos.writeUTF(value.symbol);
                        boos.writeUTF(value.table);
                        boos.writeInt(value.width);
                        boos.writeInt(value.height);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                // track data of the parameters we have got
                boos.writeInt(this.indexed_parameter.size());
                this.indexed_parameter.forEach((id, value) -> {
                    try {
                        boos.writeUTF(id);
                        boos.writeDouble(value);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                // track font aliases
                boos.writeInt(this.indexed_alias.size());
                this.indexed_alias.forEach((alias, real) -> {
                    try {
                        boos.writeUTF(alias);
                        boos.writeUTF(real);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                boos.close();
                FileUtils.writeByteArrayToFile(ResourcePackManager.INDEX_TEXTURE, stream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Building font index in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            if (do_compression) {
                File[] files = FileUtil.buildAllFiles(WORKSPACE_WORKING);
                long last_stamp = System.currentTimeMillis();
                clock.loop();

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
                    if (!file.getName().endsWith(".png")) {
                        continue;
                    }
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
                // Notify about what we have done
                RPGCore.inst().getLogger().info(String.format("Image compression pass done in %sms", clock.loop()));
            }
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
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
            try {
                FileUtils.deleteDirectory(WORKSPACE_WORKING);
            } catch (IOException ignored) {
            }
            // Notify about what we have done
            RPGCore.inst().getLogger().info(String.format("Finalizing workspace in %sms", clock.loop()));
        });
        worker.add(true, () -> {
            // reset the clock used to measure time
            clock.loop();
            // upload to transfer.sh
            String url = TransferUploader.upload(OUTPUT_RESULT);
            // upload to uguu.se
            if (url.equals("error")) {
                // url = UguuUploader.upload(OUTPUT_RESULT);
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
            this.setUrl(url);
            // Notify about what we've done
            RPGCore.inst().getLogger().info(String.format("Uploaded resourcepack in %sms", clock.loop()));
        });
        worker.add(false, () -> {
            callback.accept(OUTPUT_RESULT);
            compiling = false;
            chars_measurement = null;
        });

        worker.work();
    }

    /**
     * Verify if a given player has properly initialized their resourcepack,
     * this may require some delay before it works thoroughly.
     *
     * @param player the player who we are checking
     * @return true if they have our resourcepack
     */
    public boolean hasLoaded(Player player) {
        return resourcepack_initialized.contains(player.getUniqueId());
    }

    /**
     * Verify if a given player has properly initialized their resourcepack,
     * this may require some delay before it works thoroughly.
     *
     * @param player the player who we are checking
     * @return true if they have our resourcepack
     */
    public boolean hasLoaded(CorePlayer player) {
        return resourcepack_initialized.contains(player.getUniqueId());
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
    public Map<String, IndexedTexture.ConfigTexture> textures() {
        return indexed_fonts;
    }

    /**
     * Retrieve the texture mapped to a certain identifier.
     *
     * @param texture the texture we want to retrieve
     * @return the resulting texture
     * @throws NullPointerException fired instead of a null value
     */
    public IndexedTexture texture(String texture) throws NullPointerException {
        IndexedTexture result = indexed_fonts.get(texture);
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
    public IndexedTexture texture(String texture, String fallback) throws NullPointerException {
        IndexedTexture result = indexed_fonts.getOrDefault(texture, indexed_fonts.get(fallback));
        if (result == null)
            throw new NullPointerException("Could not find index of '" + texture + "' texture!");
        return result;
    }

    /**
     * Retrieve the parameter mapped to a certain identifier.
     *
     * @param parameter the parameter we want to retrieve
     * @return the resulting parameter
     * @throws NullPointerException fired instead of a null value
     */
    public Number parameter(String parameter) throws NullPointerException {
        Number result = indexed_parameter.get(parameter);
        if (result == null)
            throw new NullPointerException("Could not find index of '" + parameter + "' parameter!");
        return result;
    }

    /**
     * Check if a parameter exists.
     *
     * @param parameter the parameter to check
     * @return true if parameter exists
     */
    public boolean hasParameter(String parameter) {
        return indexed_parameter.containsKey(parameter);
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
        resourcepack_initialized.remove(event.getPlayer().getUniqueId());
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
            resourcepack_initialized.add(event.getPlayer().getUniqueId());
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
