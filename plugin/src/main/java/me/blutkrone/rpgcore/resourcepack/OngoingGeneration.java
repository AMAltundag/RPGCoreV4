package me.blutkrone.rpgcore.resourcepack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.resourcepack.generation.component.FontEntry;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.AbstractTexture;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.CombinedTexture;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.GeneratedTexture;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.Item;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate.CustomModelPredicate;
import me.blutkrone.rpgcore.resourcepack.utils.CompileClock;
import me.blutkrone.rpgcore.resourcepack.utils.GenerationConfiguration;
import me.blutkrone.rpgcore.resourcepack.utils.ResourceUtil;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

public class OngoingGeneration {

    private final ResourcepackManager manager;

    // textures encoded into fonts
    final Map<String, AbstractTexture> hud_textures = new HashMap<>();
    // characters mapped to real size
    final Map<Character, Integer> char_to_size = new HashMap<>();
    // fonts that have been generated
    final Map<String, List<FontEntry>> font_generated = new HashMap<>();
    final Map<String, String> font_alias = new HashMap<>();
    // items that have been generated
    final Map<Material, Item> item_generated = new HashMap<>();
    // animated entity models that have been generated
    final Map<String, me.blutkrone.rpgcore.bbmodel.io.deserialized.Model> entities_generated = new HashMap<>();

    // allocation space for textures
    private int allocator_id;
    // configuration parameters for resourcepack
    private final GenerationConfiguration configuration;
    // clock used for timestamping
    private final CompileClock clock;

    public OngoingGeneration(ResourcepackManager manager, GenerationConfiguration configuration) {
        this.manager = manager;
        this.configuration = configuration;
        this.clock = new CompileClock();
    }

    /**
     * Write a json object into a file.
     *
     * @param file The file to write into
     * @param object The JSON object
     */
    public void write(File file, JsonObject object) throws IOException {
        file.getParentFile().mkdirs();

        String string;
        if (isCompressed()) {
            string = RPGCore.inst().getGsonUgly().toJson(object);
        } else {
            string = RPGCore.inst().getGsonPretty().toJson(object);
        }

        string = ResourceUtil.escapeMinecraft(string);
        try (PrintWriter out = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            out.write(string);
        }

        // try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
        //     this.gson().toJson(object, fw);
        // }
    }

    /**
     * A shared GSON instance to be used by all generators.
     *
     * @return GSON instance
     */
    public Gson gson() {
        return RPGCore.inst().getGsonUgly();
    }

    /**
     * If compressed we will shrink the resourcepack as much as we can
     * at the expense of performance.
     *
     * @return Compress the resourcepack
     */
    public boolean isCompressed() {
        return manager.isCompressed();
    }

    /**
     * A clock used to track how much time passed since the last
     * call to it, useful for compilation information.
     *
     * @return Compilation clock.
     */
    public CompileClock clock() {
        return clock;
    }

    /**
     * Retrieve the configuration for the generation process.
     *
     * @return Configuration for the generation process.
     */
    public GenerationConfiguration config() {
        return configuration;
    }

    /**
     * Resourcepack manager which the ongoing generation process.
     *
     * @return Resourcepack Manager
     */
    public ResourcepackManager getManager() {
        return manager;
    }

    /**
     * Handling for font generation.
     *
     * @return Font generation handling.
     */
    public HUD hud() {
        return new HUD();
    }

    /**
     * Handling for text generation.
     *
     * @return Text generation handling.
     */
    public Text text() {
        return new Text();
    }

    /**
     * Handling for special processing.
     *
     * @return Processing handling
     */
    public Process process() {
        return new Process();
    }

    /**
     * Handling for item generation.
     *
     * @return Item generation handling.
     */
    public Model model() {
        return new Model();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //--------------------------------------------------------------------------------------------------------------//
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class Model {

        /**
         * Register an item container which can hold custom items.<br>
         * <br>
         * Should the item already be registered, this method will do
         * nothing.
         *
         * @param item Item we want to register
         * @return Could register item?
         */
        public boolean register(Item item) {
            if (item_generated.containsKey(item.material)) {
                return false;
            }

            item_generated.put(item.material, item);
            return true;
        }

        /**
         * Register an item container which can hold custom items.<br>
         * <br>
         * Should the item already be registered, we retrieve the previously
         * registered item.
         *
         * @param material Material to register.
         * @return Item that was registered.
         */
        public Item register(Material material) {
            Item item = item_generated.get(material);
            if (item != null) {
                return item_generated.get(material);
            }

            // anything else should show as a 'held' item
            item = new Item(material, Item.PARENT_HANDHELD);
            // initialize default texture
            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", "minecraft:item/" + material.name().toLowerCase());
            item.template.add("textures", textures);

            item_generated.put(material, item);
            return item;
        }

        /**
         * Scan the given override for the next available custom model data.
         *
         * @param material Material to scan
         * @return The next unoccupied model data
         */
        public int getNextCustomModelData(Material material) {
            Item item = register(material);
            int available = 0;
            for (Item.ItemOverride override : item.overrides) {
                if (override.predicate instanceof CustomModelPredicate) {
                    available = Math.max(((CustomModelPredicate) override.predicate).getCustomModelData(), available);
                }
            }
            return available + 1;
        }

        /**
         * Register an animated model based on items.
         *
         * @param id Unique ID of the model
         * @param model The model that was generated
         */
        public void register(String id, me.blutkrone.rpgcore.bbmodel.io.deserialized.Model model) {
            entities_generated.put(id.toLowerCase(), model);
        }
    }

    public class Process {

        /**
         * Finalize items that were created, do note that this expects that our
         * overrides are already in-place. This will ONLY save the item itself.
         *
         * @param output Directory to dump items into.
         * @throws Exception Should something go wrong.
         */
        public void item(File output) throws Exception {
            for (Map.Entry<Material, Item> entry : item_generated.entrySet()) {
                Material material = entry.getKey();
                Item item = entry.getValue();

                write(new File(output, material.name().toLowerCase() + ".json"), item.export());
            }
        }

        /**
         * Finalize textures encoded into custom fonts, by building those
         * fonts and embedding them appropriately, will also dump fonts.
         *
         * @param output_texture Where to put PNG files
         * @param output_json Where to put JSON files
         * @throws Exception Should something go wrong.
         */
        public void text(File output_texture, File output_json) throws Exception {
            // unique identifier for the generic fonts
            int uid_track = 0;
            // transform generated entries into resourcepack data
            for (Map.Entry<String, AbstractTexture> entry : hud_textures.entrySet()) {
                AbstractTexture texture = entry.getValue();
                // pool all generated textures up (cuz some textures are compounded)
                List<GeneratedTexture> candidates = new ArrayList<>();
                if (texture instanceof GeneratedTexture) {
                    candidates.add(((GeneratedTexture) texture));
                } else if (texture instanceof CombinedTexture) {
                    candidates.addAll(((CombinedTexture) texture).getTextures());
                }

                // create all the textures we require
                for (GeneratedTexture candidate : candidates) {
                    // create basic header information
                    List<FontEntry> font = font_generated.computeIfAbsent(candidate.table, (k -> OngoingGeneration.this.text().create()));
                    char symbol = candidate.symbol.charAt(0);
                    int ascent = candidate.offset;
                    int height = candidate.texture.getHeight();
                    BufferedImage image = deepCopy(candidate.texture);

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
                    File exported_character = FileUtil.file(output_texture, "generated_" + uid_track + ".png");
                    ImageIO.write(image, "png", exported_character);
                    // note down our character that we've generated
                    font.add(new FontEntry("bitmap", "minecraft:font/generated_" + uid_track + ".png", ascent, height, Collections.singletonList(String.valueOf(symbol))));
                    // increment the symbol we are backed up by
                    uid_track += 1;
                }
            }

            // dump all fonts in memory on disk
            for (Map.Entry<String, List<FontEntry>> font_entry : font_generated.entrySet()) {
                String id = font_entry.getKey();
                List<FontEntry> entries = font_entry.getValue();

                JsonArray array = new JsonArray();
                array.add(FontEntry.getAsSpace(" ", 4));
                for (FontEntry entry : entries) {
                    array.add(entry.transform());
                }

                JsonObject container = new JsonObject();
                container.add("providers", array);

                write(new File(output_json, id + ".json"), container);
            }
        }
    }

    public class Text {

        /**
         * Define a measurement for the given character, this is used when
         * we generate custom user interfaces to properly compute a layout
         * without incorrect offsets.
         *
         * @param character The character to measured
         * @param size the size in pixels
         */
        public void measurement(char character, int size) {
            char_to_size.put(character, size);
        }

        /**
         * Register a font by an unstable ID, the unstable ID will always be
         * mapped to an alias which can be expected to be stable.
         * <br>
         * Fonts are keyed 'unstable' so multiple aliases can just point to a
         * real font, instead of maintaining multiple fonts which will consume
         * large amounts of RAM.
         * <br>
         * Being unstable means that generating the resourcepack again will not
         * result in the same ID.
         *
         * @param unstable The real ID of the font
         * @param font Entries making up the font
         */
        public void register(String unstable, List<FontEntry> font) {
            font_generated.put(unstable, font);
        }

        /**
         * Fonts are keyed 'unstable' so multiple aliases can just point to a
         * real font, instead of maintaining multiple fonts which will consume
         * large amounts of RAM.
         * <br>
         * An alias is stable (iE re-generating the resourcepack will still mean
         * that our alias will give us the same font.)
         *
         * @param alias
         * @param unstable
         */
        public void alias(String alias, String unstable) {
            font_alias.put(alias, unstable);
        }

        /**
         * Create a clean font listing.
         *
         * @return Font list with minimum setup.
         */
        public List<FontEntry> create() {
            List<FontEntry> generated = new ArrayList<>();

            generated.add(FontEntry.getAsAmberSpace(-3, "\uF801"));
            generated.add(FontEntry.getAsAmberSpace(-4, "\uF802"));
            generated.add(FontEntry.getAsAmberSpace(-5, "\uF803"));
            generated.add(FontEntry.getAsAmberSpace(-6, "\uF804"));
            generated.add(FontEntry.getAsAmberSpace(-7, "\uF805"));
            generated.add(FontEntry.getAsAmberSpace(-8, "\uF806"));
            generated.add(FontEntry.getAsAmberSpace(-9, "\uF807"));
            generated.add(FontEntry.getAsAmberSpace(-10, "\uF808"));
            generated.add(FontEntry.getAsAmberSpace(-18, "\uF809"));
            generated.add(FontEntry.getAsAmberSpace(-34, "\uF80a"));
            generated.add(FontEntry.getAsAmberSpace(-66, "\uF80b"));
            generated.add(FontEntry.getAsAmberSpace(-130, "\uF80c"));
            generated.add(FontEntry.getAsAmberSpace(-258, "\uF80d"));
            generated.add(FontEntry.getAsAmberSpace(-514, "\uF80e"));
            generated.add(FontEntry.getAsAmberSpace(-1026, "\uF80f"));

            generated.add(FontEntry.getAsAmberSpace(0, "\uF821"));
            generated.add(FontEntry.getAsAmberSpace(1, "\uF822"));
            generated.add(FontEntry.getAsAmberSpace(2, "\uF823"));
            generated.add(FontEntry.getAsAmberSpace(3, "\uF824"));
            generated.add(FontEntry.getAsAmberSpace(4, "\uF825"));
            generated.add(FontEntry.getAsAmberSpace(5, "\uF826"));
            generated.add(FontEntry.getAsAmberSpace(6, "\uF827"));
            generated.add(FontEntry.getAsAmberSpace(7, "\uF828"));
            generated.add(FontEntry.getAsAmberSpace(15, "\uF829"));
            generated.add(FontEntry.getAsAmberSpace(31, "\uF82a"));
            generated.add(FontEntry.getAsAmberSpace(63, "\uF82b"));
            generated.add(FontEntry.getAsAmberSpace(127, "\uF82c"));
            generated.add(FontEntry.getAsAmberSpace(255, "\uF82d"));
            generated.add(FontEntry.getAsAmberSpace(511, "\uF82e"));
            generated.add(FontEntry.getAsAmberSpace(1023, "\uF82f"));

            return generated;
        }
    }

    public class HUD {

        /**
         * Create an allocator to assist us with handling textures that
         * were encoded into a font.
         * <br>
         *
         * @return Allocator instance.
         */
        public Allocator allocator() {
            return new Allocator("rpgcore_" + allocator_id++);
        }

        /**
         * Register as a static font, the ID will be file name without
         * the file ending.
         * <br>
         *
         * @param file
         * @param offset
         */
        public void register(Allocator allocator, File file, int offset) throws IOException {
            // compute what ID to associate with
            String id = file.getName();
            id = id.substring(0, id.indexOf("."));
            // generate the texture
            AbstractTexture texture = CombinedTexture.combine(allocator, ImageIO.read(file), offset);
            register("static_" + id, texture);
        }

        /**
         * Register as a static font, the ID will be file name without
         * the file ending.
         * <br>
         * Additionally, the suffix will be appended to the ID.
         *
         * @param suffix
         * @param file
         * @param offset
         */
        public void register(Allocator allocator, String suffix, File file, int offset) throws IOException {
            // compute what ID to associate with
            String id = file.getName();
            id = id.substring(0, id.indexOf("."));
            if (!suffix.isEmpty()) {
                id += "_" + suffix;
            }
            // generate the texture
            AbstractTexture texture = CombinedTexture.combine(allocator, ImageIO.read(file), offset);
            register("static_" + id, texture);
        }

        /**
         * Register a font texture into our ongoing generator process.
         * <br>
         *
         * @param id      Stable identifier that can be used to load the equivalent
         *                of this font texture even if the backing resourcepack has
         *                been updated.
         * @param texture The resulting texture.
         */
        public void register(String id, AbstractTexture texture) {
            texture = hud_textures.put(id, texture);
            if (texture != null) {
                Bukkit.getLogger().warning("Font texture %s does overlap!".formatted(id));
            }
        }

        /**
         * Retrieve a texture that was written during the ongoing generation.
         *
         * @param id Stable identifier
         * @return Texture that was generated
         */
        public Collection<GeneratedTexture> fetch(String id) {
            AbstractTexture texture = hud_textures.get(id);
            if (texture instanceof CombinedTexture) {
                return ((CombinedTexture) texture).getTextures();
            } else if (texture instanceof GeneratedTexture) {
                return Collections.singletonList(((GeneratedTexture) texture));
            } else if (texture == null) {
                return Collections.emptyList();
            } else {
                throw new IllegalStateException("Unexpected type: " + texture.getClass());
            }
        }
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
}
