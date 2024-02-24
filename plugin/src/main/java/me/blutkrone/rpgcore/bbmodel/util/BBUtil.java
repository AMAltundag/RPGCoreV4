package me.blutkrone.rpgcore.bbmodel.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.MatrixUtil;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.bbmodel.io.serialized.geometry.BBCube;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.util.Transformation;
import org.joml.Math;
import org.joml.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BBUtil {

    /**
     * Threshold for fuzzy equals check
     */
    public static final float EPSILON = 0.000001f;

    /**
     * Transform a float array into a readable string, with
     * a precision of up to three digits.
     *
     * @param array Array to transform.
     * @return Output
     */
    public static String toString(float[] array) {
        List<Float> floats = new ArrayList<>();
        for (float f : array) {
            floats.add(f);
        }
        return "[" + floats.stream().map(f -> String.format(Locale.US, "%.2f", f)).collect(Collectors.joining(" ")) + "]";
    }

    /**
     * Multiply an array with a factor.
     *
     * @param array The array to multiply.
     * @param factor The factor to multiply by.
     */
    public static void multiply(float[] array, float factor) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] * factor;
        }
    }

    /**
     * Add the floats from one array to another.
     *
     * @param to   Target array
     * @param from Source array
     */
    public static void add(float[] to, float[] from) {
        for (int i = 0; i < from.length; i++) {
            to[i] += from[i];
        }
    }

    /**
     * Subtract the floats from one array to another.
     *
     * @param to   Target array
     * @param from Source array
     */
    public static void subtract(float[] to, float[] from) {
        for (int i = 0; i < from.length; i++) {
            to[i] -= from[i];
        }
    }

    /**
     * Clamp a value between a minimum and maximum.
     *
     * @param value   Value to clamp
     * @param minimum Minimum value
     * @param maximum Maximum value
     * @return minimum <= value <= maximum
     */
    public static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(value, maximum));
    }

    /**
     * Find minimum value amongst all values
     *
     * @param values Values to compare
     * @return Maximum among values
     */
    public static float max(float... values) {
        float maximum = Float.MIN_VALUE;
        for (float value : values) {
            maximum = Math.max(value, maximum);
        }
        return maximum;
    }

    /**
     * Find minimum value amongst all values
     *
     * @param values Values to compare
     * @return Minimum among values
     */
    public static float min(float... values) {
        float minimum = Float.MAX_VALUE;
        for (float value : values) {
            minimum = Math.min(value, minimum);
        }
        return minimum;
    }

    /**
     * Difference between the first and second array, this is
     * assuming that first and second have the same length.
     *
     * @param first  First array
     * @param second Second array
     * @return [a1-a2, b1-b2, ..., z1-z2]
     */
    public static float[] diff(float[] first, float[] second) {
        float[] output = new float[first.length];
        for (int i = 0; i < first.length; i++) {
            output[i] = first[i] - second[i];
        }
        return output;
    }

    /**
     * Find the maximum corner of the cubes.
     *
     * @param cubes Cubes to compare
     * @return Maximum XYZ of cubes
     */
    public static float[] max(List<BBCube> cubes) {
        float x = Float.MIN_VALUE, y = Float.MIN_VALUE, z = Float.MIN_VALUE;
        for (BBCube cube : cubes) {
            float[] from = cube.getFrom();
            float[] to = cube.getTo();
            x = max(x, from[0], to[0]);
            y = max(y, from[1], to[1]);
            z = max(z, from[2], to[2]);
        }
        return new float[]{x, y, z};
    }

    /**
     * Find the minimum corner of the cubes.
     *
     * @param cubes Cubes to compare
     * @return Minimum XYZ of model
     */
    public static float[] min(List<BBCube> cubes) {
        float x = Float.MAX_VALUE, y = Float.MAX_VALUE, z = Float.MAX_VALUE;
        for (BBCube cube : cubes) {
            float[] from = cube.getFrom();
            float[] to = cube.getTo();
            x = min(x, from[0], to[0]);
            y = min(y, from[1], to[1]);
            z = min(z, from[2], to[2]);
        }
        return new float[]{x, y, z};
    }

    /**
     * Transfer an element from one JSON object to another, the
     * transferred element will always be a deep copy.
     *
     * @param from    Where to read from
     * @param to      Where to write to
     * @param element What to transfer
     */
    public static void transfer(JsonObject from, JsonObject to, String element) {
        if (from.has(element)) {
            to.add(element, from.get(element).deepCopy());
        }
    }

    /**
     * Transform base64 encoded string into a texture.
     *
     * @param source Base64 encoded image data
     * @return Output texture
     */
    public static BufferedImage toImage(String source) {
        byte[] bytes = Base64.getDecoder().decode(source.split(",")[1]);
        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transform JSON data into a POJO.
     *
     * @param data     Data to be transformed
     * @param importer Method that transforms JSON to POJO
     * @return A string-pojo map holding the data
     */
    public static <K> Map<String, K> importFrom(JsonObject data, Function<JsonObject, K> importer) {
        Map<String, K> output = new HashMap<>();
        data.asMap().forEach((string, json) -> {
            output.put(string, importer.apply(((JsonObject) json)));
        });
        return output;
    }

    /**
     * Transform POJO data into JSON
     *
     * @param data     Data to be transformed
     * @param exporter Method that transforms POJO to JSON
     * @return A json object representing the data
     */
    public static <K> JsonObject exportTo(Map<String, K> data, Function<K, JsonObject> exporter) {
        JsonObject output = new JsonObject();
        data.forEach((string, pojo) -> {
            output.add(string, exporter.apply(pojo));
        });
        return output;
    }

    /**
     * Transform json array into a primitive float array.
     *
     * @param array JSON Array with floats.
     * @return Primitive array with floats
     */
    public static float[] toFloatArray(JsonArray array, int length) {
        float[] output = new float[length];
        for (int i = 0; i < length && array != null; i++) {
            output[i] = array.get(i).getAsFloat();
        }
        return output;
    }

    /**
     * Transform a primitive float array to a JSON array.
     *
     * @param array Primitive array with floats
     * @return JSON array with floats
     */
    public static JsonArray toJsonArray(float[] array) {
        JsonArray output = new JsonArray();
        for (float f : array) {
            output.add(f);
        }
        return output;
    }

    /**
     * Compute the size of the geometry, with the first value being
     * the XZ (width), and the second value being the Y (height) of
     * the element.
     *
     * @param elements The elements to process
     * @return Bounding size
     */
    public static float[] sizeOf(List<BBCube> elements) {
        if (elements.isEmpty()) {
            return new float[] { 0.0f, 0.0f };
        }

        float min_x = Float.MAX_VALUE, max_x = Float.MIN_VALUE;
        float min_y = Float.MAX_VALUE, max_y = Float.MIN_VALUE;
        float min_z = Float.MAX_VALUE, max_z = Float.MIN_VALUE;

        for (BBCube element : elements) {
            min_x = BBUtil.min(element.from[0], element.to[0], min_x);
            max_x = BBUtil.max(element.from[0], element.to[0], max_x);
            min_y = BBUtil.min(element.from[1], element.to[1], min_y);
            max_y = BBUtil.max(element.from[1], element.to[1], max_y);
            min_z = BBUtil.min(element.from[2], element.to[2], min_z);
            max_z = BBUtil.max(element.from[2], element.to[2], max_z);
        }

        float diff_x = Math.abs(max_x - min_x);
        float diff_y = Math.abs(max_y - min_y);
        float diff_z = Math.abs(max_z - min_z);

        return new float[] { Math.max(diff_x, diff_z), diff_y };
    }

    /**
     * Transform the contents of a file to a JSON object
     *
     * @param file File to read
     * @return JSON object representing file
     */
    public static JsonObject fileToJson(File file) throws IOException {
        JsonObject bb_model;
        try (FileReader reader = new FileReader(file)) {
            bb_model = RPGCore.inst().getGsonUgly().fromJson(reader, JsonObject.class);
        }
        return bb_model;
    }

    public static class Quaternion {

        /**
         * Translate euler angle into quaternion.
         *
         * @param euler
         * @return EulerAngle represented as Quaternion
         * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm">Credit</a>
         */
        public static Quaternionf fromEuler(float[] euler) {
            // transform degree to radian
            final float yaw = Math.toRadians(euler[0]);
            final float pitch = Math.toRadians(euler[1]);
            final float roll = Math.toRadians(euler[2]);
            // transform to quaternion
            float x = Math.sin(roll / 2) * Math.cos(pitch / 2) * Math.cos(yaw / 2) - Math.cos(roll / 2) * Math.sin(pitch / 2) * Math.sin(yaw / 2);
            float y = Math.cos(roll / 2) * Math.sin(pitch / 2) * Math.cos(yaw / 2) + Math.sin(roll / 2) * Math.cos(pitch / 2) * Math.sin(yaw / 2);
            float z = Math.cos(roll / 2) * Math.cos(pitch / 2) * Math.sin(yaw / 2) - Math.sin(roll / 2) * Math.sin(pitch / 2) * Math.cos(yaw / 2);
            float w = Math.cos(roll / 2) * Math.cos(pitch / 2) * Math.cos(yaw / 2) + Math.sin(roll / 2) * Math.sin(pitch / 2) * Math.sin(yaw / 2);
            // output the normlaized quaternion
            return new Quaternionf(x, y, z, w).normalize();
        }
    }

    public static class Matrix {

        public static Matrix4f transform(float[] position, float[] rotation, float scale) {
            Matrix4f result = new Matrix4f();
            // apply offset
            result.translate(position[0], position[1], position[2]);
            // apply rotation
            Quaternionf quaternion = new Quaternionf();
            quaternion.identity();
            quaternion.rotateX(Math.toRadians(rotation[0]));
            quaternion.rotateY(Math.toRadians(rotation[1]));
            quaternion.rotateZ(Math.toRadians(rotation[2]));
            result.rotate(quaternion);
            // apply scale
            result.scale(scale, scale, scale);
            return result;
        }

        public static Transformation transformFromMatrix(Matrix4f matrix) {
            float factor = 1.0F / matrix.m33();
            Triple<Quaternionf, Vector3f, Quaternionf> extracted = MatrixUtil.a((new Matrix3f(matrix)).scale(factor));
            Vector3f translation = matrix.getTranslation(new Vector3f()).mul(factor);
            Quaternionf left = new Quaternionf(extracted.getLeft());
            Vector3f scale = new Vector3f(extracted.getMiddle());
            Quaternionf right = new Quaternionf(extracted.getRight());
            return new Transformation(translation, left, scale, right);
        }

        public static Matrix4f transformToMatrix(Vector3f translation, Quaternionf rotation_left, Vector3f scale, Quaternionf rotation_right) {
            Matrix4f matrix = new Matrix4f();
            if (translation != null) {
                matrix.translation(translation);
            }
            if (rotation_left != null) {
                matrix.rotate(rotation_left);
            }
            if (scale != null) {
                matrix.scale(scale);
            }
            if (rotation_right != null) {
                matrix.rotate(rotation_right);
            }
            return matrix;
        }
    }
}
