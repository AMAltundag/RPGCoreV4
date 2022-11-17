package me.blutkrone.rpgcore.resourcepack.bbmodel;

import com.google.gson.*;
import me.blutkrone.rpgcore.resourcepack.bbmodel.client.MCModel;
import me.blutkrone.rpgcore.resourcepack.bbmodel.editor.BBModel;
import me.blutkrone.rpgcore.resourcepack.bbmodel.editor.BBTexture;
import me.blutkrone.rpgcore.resourcepack.utils.ResourceUtil;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BBExporter {

    // prepare a gson utility we can work with
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(BBModel.class, new BBDeserializer())
            .setPrettyPrinting()
            .create();

    /**
     * Transform the input array into an array of floats.
     *
     * @param array    a json array, expected to be floats
     * @param defaults default values if no entries available
     * @return an array of float numbers.
     */
    public static float[] asFloatArray(JsonArray array, float... defaults) {
        if (array == null)
            return defaults;
        float[] output = new float[array.size()];
        for (int i = 0; i < output.length; i++)
            output[i] = array.get(i).getAsFloat();
        return output;
    }

    /**
     * Send all entries in a json array thorough the matrix, to
     * transmute the given parameters.
     *
     * @param array  a json array of any given type.
     * @param matrix the matrix which we transmute off.
     * @return the elements of the array transmuted by the matrix.
     */
    public static <K> List<K> toArray(JsonArray array, Function<JsonElement, K> matrix) {
        List<K> output = new ArrayList<>();
        for (JsonElement element : array)
            output.add(matrix.apply(element));
        return output;
    }

    /**
     * Send all entries in a json object thorough the matrix, to
     * transmute the given parameters.
     *
     * @param object a json object with children
     * @param matrix the matrix which we transmute off.
     * @return the map of the array transmuted by the matrix.
     */
    public static <K> Map<String, K> toMap(JsonObject object, Function<JsonElement, K> matrix) {
        Map<String, K> output = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet())
            output.put(entry.getKey(), matrix.apply(entry.getValue()));
        return output;
    }

    /**
     * Automatically handle the exporting process of a <code>*.bbmodel</code> file
     * that can be merged into a resourcepack.
     *
     * @param file the input bbmodel to process
     * @return outputs a container holding a json-able instance plus textures.
     */
    public static Exported export(File file) throws BBException {
        // import the gson model standard
        BBModel model;
        try (FileReader r = new FileReader(file)) {
            model = gson.fromJson(r, BBModel.class);
        } catch (IOException e) {
            throw new BBException("JSON read failed", e);
        }
        // ensure that we do not violate contract
        if (model.isBoxUV())
            throw new BBException("BoxUV has not been implemented");
        // ensure that we have embedded textures
        if (!model.hasTexture())
            throw new BBException("Textures not embedded");
        // thorough-put our relevant data
        return new Exported(model.export(), model.texture());
    }

    public static class Exported {
        public final MCModel model;
        public final List<BBTexture> texture;

        public Exported(MCModel model, List<BBTexture> texture) {
            this.model = model;
            this.texture = texture;
        }

        public void saveModelToFile(File file, boolean compressed) throws IOException, ParseException {
            ResourceUtil.saveToDisk(gson.toJsonTree(model).getAsJsonObject(), file, compressed);
        }

        public void saveTextureToDirectory(File directory, File bbmodel) throws IOException {
            for (BBTexture texture : texture) {
                String texture_path = "bbmodel_" + texture.getFileId() + ".png";
                String mcmeta_path = texture.getMetaPath();

                // dump the texture into a file
                File dump_texture_at = FileUtil.file(directory, texture_path);
                ImageIO.write(texture.getTexture(), "png", dump_texture_at);

                // dump (optional) mcmeta texture
                if (mcmeta_path != null) {
                    File mcmeta_file = new File(bbmodel.getParentFile(), mcmeta_path);
                    if (mcmeta_file.exists()) {
                        File dump_mcmeta_at = FileUtil.file(directory, texture_path + ".mcmeta");
                        FileUtils.copyFile(mcmeta_file, dump_mcmeta_at);
                    }
                }
            }
        }
    }
}
