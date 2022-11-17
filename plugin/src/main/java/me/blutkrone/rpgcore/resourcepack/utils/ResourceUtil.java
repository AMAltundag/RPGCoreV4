package me.blutkrone.rpgcore.resourcepack.utils;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.resourcepack.component.ResourcePackFont;
import me.blutkrone.rpgcore.resourcepack.component.ResourcePackItem;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Material;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceUtil {
    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase();
    }

    /**
     * Create an exact copy of an image, processed on a CPU
     * level instead.
     *
     * @param image the image to copy
     * @return the copied image
     */
    public static BufferedImage imageCopyExact(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                copy.setRGB(x, y, image.getRGB(x, y));
            }
        }
        return copy;
    }

    /**
     * Generate a sliced version of a font, where each symbol has a padding
     * on the top/bottom.
     *
     * @param input  the texture for a specific font
     * @param bottom how much spacing from below
     * @param height height of each symbol
     * @return the updated texture
     */
    public static BufferedImage fontCopyPaddedBottom(BufferedImage input, int bottom, double opacity, int height, int width) {
        BufferedImage out_texture = new BufferedImage(16 * width, (height + bottom) * (input.getHeight() / height), BufferedImage.TYPE_INT_ARGB);

        for (int charX = 0; charX < 16; charX++) {
            for (int charY = 0; charY < input.getHeight() / height; charY++) {
                // starting point to draw from
                int pixelStartX = charX * width;
                int pixelStartY = charY * (height + bottom);
                // copy the raw texture
                for (int pixelX = 0; pixelX < width; pixelX++) {
                    for (int pixelY = 0; pixelY < height; pixelY++) {
                        int raw = input.getRGB(charX * width + pixelX, charY * height + pixelY);
                        if ((raw & 0xFF000000) != 0) {
                            raw = raw & 0xFFFFFF | ((int) (255 * opacity) << 24);
                        }
                        out_texture.setRGB(pixelStartX + pixelX, pixelStartY + pixelY, raw);
                    }
                }
            }
        }

        return out_texture;
    }

    /**
     * Generate a sliced version of a font, where each symbol has a padding
     * on the top/bottom.
     *
     * @param input the texture for a specific font
     * @param top   how much spacing from above
     * @return the updated texture
     */
    public static BufferedImage fontCopyPaddedTop(BufferedImage input, int top, double opacity) {
        BufferedImage out_texture = new BufferedImage(128, (8 + top) * 16, BufferedImage.TYPE_INT_ARGB);

        for (int charX = 0; charX < 16; charX++) {
            for (int charY = 0; charY < 16; charY++) {
                // starting point to draw from
                int pixelStartX = charX * 8;
                int pixelStartY = charY * (8 + top) + top;
                // copy the raw texture
                for (int pixelX = 0; pixelX < 8; pixelX++) {
                    for (int pixelY = 0; pixelY < 8; pixelY++) {
                        int raw = input.getRGB(charX * 8 + pixelX, charY * 8 + pixelY);
                        if ((raw & 0xFF000000) != 0) {
                            raw = raw & 0xFFFFFF | ((int) (255 * opacity) << 24);
                        }
                        out_texture.setRGB(pixelStartX + pixelX, pixelStartY + pixelY, raw);
                    }
                }
            }
        }

        return out_texture;
    }

    /**
     * Create a padded copy of an image, setup with minimum opacity
     * of 16/255
     *
     * @param image  the image to copy
     * @param top    padding on the top
     * @param bottom padding on the bottom
     * @param left   padding on the left
     * @param right  padding on the right
     * @return the copied image
     */
    public static BufferedImage imageCopyPadded(BufferedImage image, int top, int bottom, int left, int right) {
        BufferedImage copy = new BufferedImage(image.getWidth() + left + right, image.getHeight() + top + bottom, BufferedImage.TYPE_INT_ARGB);

        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    copy.setRGB(x + left, y + top, image.getRGB(x, y) | 0xFF000000);
                }
            }
        } else {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    copy.setRGB(x + left, y + top, image.getRGB(x, y));
                }
            }
        }

        return copy;
    }

    /**
     * Create the base for an item within the generator
     *
     * @param material the material we are operating on
     * @return the resourcepack we are working with
     */
    public static ResourcePackItem createItemCompound(Material material) {
        switch (material) {
            case LEATHER_HELMET:
                ;
            case LEATHER_CHESTPLATE:
                ;
            case LEATHER_LEGGINGS:
                ;
            case LEATHER_BOOTS: {
                Map<String, String> textures = new HashMap<>();
                textures.put("layer0", "item/" + material.name().toLowerCase());
                textures.put("layer1", "item/" + material.name().toLowerCase() + "_overlay");
                return new ResourcePackItem("item/generated", textures);
            }
            case LEATHER_HORSE_ARMOR: {
                Map<String, String> textures = new HashMap<>();
                textures.put("layer0", "item/" + material.name().toLowerCase());
                return new ResourcePackItem("item/generated", textures);
            }
            default: {
                Map<String, String> textures = new HashMap<>();
                textures.put("layer0", "item/" + material.name().toLowerCase());
                return new ResourcePackItem("item/handheld", textures);
            }
        }
    }

    /**
     * A utility method which can write escaped strings to our backing
     * writer, this only serves to cleanly avoid a case of "\\uFFFF"
     *
     * @param str the string to export
     * @param out the writer to work with
     */
    private static void writeToStream(String str, OutputStreamWriter out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        } else if (str != null) {
            int sz = str.length();

            for (int i = 0; i < sz; ++i) {
                char ch = str.charAt(i);
                if (ch > 4095) {
                    out.write("\\u" + hex(ch));
                } else if (ch > 255) {
                    out.write("\\u0" + hex(ch));
                } else if (ch > 127) {
                    out.write("\\u00" + hex(ch));
                } else if (ch < ' ') {
                    switch (ch) {
                        case '\b':
                            out.write('\b');
                            break;
                        case '\t':
                            out.write('\t');
                            break;
                        case '\n':
                            out.write('\n');
                            break;
                        case '\u000b':
                        default:
                            if (ch > 15) {
                                out.write("\\u00" + hex(ch));
                            } else {
                                out.write("\\u000" + hex(ch));
                            }
                            break;
                        case '\f':
                            out.write('\f');
                            break;
                        case '\r':
                            out.write('\r');
                    }
                } else {
                    out.write(ch);
                }
            }
        }
    }

    /**
     * Export a json construct, writing it to our disk
     *
     * @param object the object we wish to export
     * @param file   the file that shall contain it
     */
    public static void saveToDisk(JSONObject object, File file, boolean compressed) throws IOException, ParseException {
        file.getParentFile().mkdirs();

        String string;
        if (compressed) {
            string = RPGCore.inst().getGsonUgly().toJson(object);
        } else {
            string = RPGCore.inst().getGsonPretty().toJson(object);
        }

        string = escapeMinecraft(string);
        try (PrintWriter out = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            out.write(string);
        }
    }

    /**
     * Export a json construct, writing it to our disk
     *
     * @param object the object we wish to export
     * @param file   the file that shall contain it
     */
    public static void saveToDisk(JsonObject object, File file, boolean compressed) throws IOException, ParseException {
        file.getParentFile().mkdirs();

        String string;
        if (compressed) {
            string = RPGCore.inst().getGsonUgly().toJson(object);
        } else {
            string = RPGCore.inst().getGsonPretty().toJson(object);
        }

        string = escapeMinecraft(string);
        try (PrintWriter out = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            out.write(string);
        }
    }

    private static String escapeMinecraft(String str) {
        StringWriter out = new StringWriter(str.length() * 2);

        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (ch > 0xfff) {
                out.write("\\u" + hex(ch));
            } else if (ch > 0xff) {
                out.write("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                out.write("\\u00" + hex(ch));
            } else {
                out.write(ch);
            }
        }

        return out.toString();
    }

    /**
     * Load the already existing font information from our resourcepack, transforming
     * it into a readable information which can be safely merged afterwards.
     *
     * @return the resourcepack information that we already have
     */
    public static List<ResourcePackFont> workingLoadFont(File workspace) throws IOException, ParseException {
        // a listing representing the font information we have
        List<ResourcePackFont> template = new ArrayList<>();
        JSONObject object;

        try (
                FileInputStream fis = new FileInputStream(workspace);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        ) {
            object = (JSONObject) new JSONParser().parse(isr);
        }

        JSONArray array = (JSONArray) object.get("providers");
        for (Object raw_provider : array) {
            JSONObject json_provider = ((JSONObject) raw_provider);
            // the type of the font
            String type = (String) json_provider.getOrDefault("type", "bitmap");
            // file for the character table
            String file = (String) json_provider.getOrDefault("file", "undefined");
            // dimension parameters for the font
            long ascent = (long) json_provider.getOrDefault("ascent", 0);
            long height = (long) json_provider.getOrDefault("height", -1);
            // the characters that are to be occupied
            JSONArray raw_chars = (JSONArray) json_provider.get("chars");
            List<String> chars = new ArrayList<>();
            for (Object raw_char : raw_chars)
                chars.add((String) raw_char);
            // store for later retrieval of the computing
            template.add(new ResourcePackFont(type, file, ascent, height, chars));
        }
        return template;
    }

    /**
     * Load the already existing font information from our resourcepack, transforming
     * it into a readable information which can be safely merged afterwards.
     *
     * @return the resourcepack information that we already have
     */
    public static Map<Material, ResourcePackItem> workingLoadItem(File workspace) throws IOException, ParseException {
        Map<Material, ResourcePackItem> template = new HashMap<>();
        for (File item_file : FileUtil.buildAllFiles(workspace)) {
            // the material that we are operating on (remove file ending)
            Material material = Material.valueOf(item_file.getName().substring(0, item_file.getName().length() - 5).toUpperCase());
            JSONObject object;

            try (
                    FileInputStream fis = new FileInputStream(item_file);
                    InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            ) {
                object = (JSONObject) new JSONParser().parse(isr);
            }

            JSONArray array = (JSONArray) object.get("overrides");
            String parent = (String) object.get("parent");
            Map<String, String> textures = new HashMap<>();
            JSONObject raw_textures = (JSONObject) object.get("textures");
            for (Object id : textures.keySet())
                textures.put(id.toString(), ((String) raw_textures.get(id)));
            ResourcePackItem item = new ResourcePackItem(parent, textures);
            for (Object raw_override : array) {
                JSONObject json_override = ((JSONObject) raw_override);
                JSONObject raw_predicate = (JSONObject) json_override.get("predicate");
                // the custom model data we are using for the item
                int custom_model_data = (int) (long) raw_predicate.get("custom_model_data");
                // the model we are using for this item
                String model = (String) json_override.get("model");
                item.overrides.add(new ResourcePackItem.ItemOverride(custom_model_data, model));
            }
            template.put(material, item);
        }
        return template;
    }
}