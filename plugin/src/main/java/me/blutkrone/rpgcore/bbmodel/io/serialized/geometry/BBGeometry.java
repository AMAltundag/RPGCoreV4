package me.blutkrone.rpgcore.bbmodel.io.serialized.geometry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.bbmodel.io.serialized.BBModel;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;
import me.blutkrone.rpgcore.bbmodel.util.NestedIterator;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * Represents the geometry of a model
 *
 * rotation: relative (inherit from nested geometry)
 * pivot: absolute (isolated from other geometry)
 *
 * Collapsed model should be placed so that the pivot
 * stays in the relative same position to it
 *
 * Need to compute size of the model for this, so we
 * can perfectly position the element
 */
public class BBGeometry implements Iterable<BBGeometry> {

    public String name;
    public float[] pivot;
    public float[] rotation;
    public boolean visible;
    public List<BBCube> elements = new ArrayList<>();
    public Map<String, BBGeometry> children = new HashMap<>();
    public int render_data;

    /**
     * A geometry object holds other geometry objects, additionally a number
     * of cubes is offered.<br>
     * <br>
     * A geometry contains a number of cubes under it, other geometry may also
     * be nested under it.<br>
     * <br>
     * Use this constructor if we have no proper geometry layout.
     */
    public BBGeometry() {
        this.name = "rpgcore_root";
        this.pivot = new float[3];
        this.rotation = new float[3];
        this.visible = true;
        this.render_data = -1;
    }

    /**
     * A geometry contains a number of cubes under it, other geometry may also
     * be nested under it.
     *
     * @param bb_outline JSON Array of 'outline'
     * @param cubes Serialized cubes, identified by UUID
     */
    public BBGeometry(JsonArray bb_outline, Map<UUID, BBCube> cubes) {
        this.name = "rpgcore_root";
        this.pivot = new float[3];
        this.rotation = new float[3];
        this.visible = true;
        this.render_data = -1;
        bb_outline.forEach((bb_child) -> {
            if (bb_child.isJsonObject()) {
                // nested geometry always is an object
                BBGeometry child_geometry = new BBGeometry(bb_child.getAsJsonObject(), cubes);
                this.children.put(child_geometry.name, child_geometry);
            } else {
                // cubes are always a uuid represented as a string
                this.elements.add(cubes.get(UUID.fromString(bb_child.getAsString())));
            }
        });
    }

    /*
     * Internal constructor to create nested geometry.
     *
     * @param bb_outline JSON Array of 'outline'
     * @param cubes Serialized cubes, identified by UUID
     */
    private BBGeometry(JsonObject bb_outline, Map<UUID, BBCube> cubes) {
        this.name = bb_outline.get("name").getAsString();
        this.pivot = BBUtil.toFloatArray(bb_outline.getAsJsonArray("origin"), 3);
        this.rotation = BBUtil.toFloatArray(bb_outline.getAsJsonArray("rotation"), 3);
        this.visible = bb_outline.get("visibility").getAsBoolean();
        this.render_data = -1;
        bb_outline.getAsJsonArray("children")
                .forEach((bb_child) -> {
                    if (bb_child.isJsonObject()) {
                        // nested geometry always is an object
                        BBGeometry child_geometry = new BBGeometry(bb_child.getAsJsonObject(), cubes);
                        this.children.put(child_geometry.name, child_geometry);
                    } else {
                        // cubes are always a uuid represented as a string
                        this.elements.add(cubes.get(UUID.fromString(bb_child.getAsString())));
                    }
                });
    }

    /**
     * Export only the local geometry, ignoring the geometry of elements that
     * have been nested into this.<br>
     * <br>
     * Used by entity models, so that each geometry object can be animated as
     * its own geometry.
     *
     * @param bb_model Model that owns this geometry.
     * @return Output result.
     */
    public JsonObject exportLocal(BBModel bb_model, File bb_file) {
        // export the cubes of the geometry, disregarding pivot
        List<BBCube> cubes = new ArrayList<>();
        for (BBCube cube : this.elements) {
            if (cube.visible) {
                cube = cube.copy();
                cube.translate(-pivot[0], -pivot[1], -pivot[2]);
                cubes.add(cube);
            }
        }

        if (cubes.isEmpty()) {
            return null;
        }

        float[] min = BBUtil.min(cubes);
        float[] max = BBUtil.max(cubes);
        float[] dif = BBUtil.diff(max, min);

        if ((dif[0] <= 48f) && (dif[1] <= 48f) && (dif[2] <= 48f)) {
            for (BBCube cube : cubes) {
                float dX = 0.0F, dY = 0.0F, dZ = 0.0F;
                dX += (min[0] < -24.0F) ? (-24.0F - min[0]) : 0.0F;
                dX += (max[0] > +24.0F) ? (+24.0F - max[0]) : 0.0F;
                dY += (min[1] < -24.0F) ? (-24.0F - min[1]) : 0.0F;
                dY += (max[1] > +24.0F) ? (+24.0F - max[1]) : 0.0F;
                dZ += (min[2] < -24.0F) ? (-24.0F - min[2]) : 0.0F;
                dZ += (max[2] > +24.0F) ? (+24.0F - max[2]) : 0.0F;

                // align in center
                cube.translate(dX, dY, dZ);
                // dX dY dZ also offset in display?
                cube.translate(8f, 8f, 8f);
            }
        } else {
            // warn about model being too large
            Bukkit.getLogger().warning("Geometry is too large: '%s'".formatted(bb_file.getName()));
        }

        // serialize the cubes
        JsonArray model_elements = new JsonArray();
        cubes.forEach(cube -> model_elements.add(cube.export()));

        // offer up JSON object
        JsonObject output = new JsonObject();
        output.add("textures", bb_model.template.get("textures"));
        output.add("elements", model_elements);
        output.add("display", bb_model.template.get("display"));
        return output;
    }

    /**
     * Export all geometry, including the geometry that is nested into
     * the children of this.<br>
     * <br>
     * Used by decoration and item models, so they can be shown as only
     * one element.
     *
     * @param bb_model Model that owns this geometry.
     * @return Output result.
     */
    public JsonObject exportGlobal(BBModel bb_model, File bb_file) {
        // export the cubes of the geometry
        List<BBCube> cubes = new ArrayList<>();
        for (BBGeometry geometry : this) {
            for (BBCube cube : geometry.elements) {
                if (cube.visible) {
                    cubes.add(cube.copy());
                }
            }
        }

        if (cubes.isEmpty()) {
            return null;
        }

        float[] minimum = BBUtil.min(cubes);
        float[] maximum = BBUtil.max(cubes);
        float[] diff = BBUtil.diff(maximum, minimum);

        if ((diff[0] > 48f) && (diff[1] > 48f) && (diff[2] > 48f)) {
            // warn about model being too large
            Bukkit.getLogger().warning("Geometry is too large: '%s'".formatted(bb_file.getName()));
        }

        // serialize the cubes
        JsonArray model_elements = new JsonArray();
        cubes.forEach(cube -> model_elements.add(cube.export()));

        // offer up JSON object
        JsonObject output = new JsonObject();
        output.add("textures", bb_model.template.get("textures"));
        output.add("elements", model_elements);
        output.add("display", bb_model.template.get("display"));
        return output;
    }

    /**
     * Shrink the cube to the given factor.
     *
     * @param shrink
     */
    public void shrink(float shrink) {
        // shrink rotation pivot
        this.pivot[0] = this.pivot[0] * shrink;
        this.pivot[1] = this.pivot[1] * shrink;
        this.pivot[2] = this.pivot[2] * shrink;
        // shrink cubes we hold
        for (BBCube cube : this.elements) {
            cube.shrink(shrink);
        }
    }

    /**
     * Offset the pivot of the geometry, do note that this does
     * not offset the actual cubes.
     *
     * @param x
     * @param y
     * @param z
     */
    public void translate(float x, float y, float z) {
        // translate pivot point
        this.pivot[0] = this.pivot[0] + x;
        this.pivot[1] = this.pivot[1] + y;
        this.pivot[2] = this.pivot[2] + z;
        // translate child elements
        for (BBCube cube : this.elements) {
            cube.translate(x, y, z);
        }
    }

    @NotNull
    @Override
    public Iterator<BBGeometry> iterator() {
        return new NestedIterator<>(this) {
            @Override
            public Collection<BBGeometry> getNestedFrom(BBGeometry current) {
                return current.children.values();
            }
        };
    }
}
