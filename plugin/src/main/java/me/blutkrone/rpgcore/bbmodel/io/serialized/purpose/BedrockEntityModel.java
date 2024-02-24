package me.blutkrone.rpgcore.bbmodel.io.serialized.purpose;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.bbmodel.io.serialized.BBModel;
import me.blutkrone.rpgcore.bbmodel.io.serialized.animation.BBAnimation;
import me.blutkrone.rpgcore.bbmodel.io.serialized.animation.BBAnimator;
import me.blutkrone.rpgcore.bbmodel.io.serialized.animation.BBKeyFrame;
import me.blutkrone.rpgcore.bbmodel.io.serialized.geometry.BBCube;
import me.blutkrone.rpgcore.bbmodel.io.serialized.geometry.BBFace;
import me.blutkrone.rpgcore.bbmodel.io.serialized.geometry.BBGeometry;

import java.io.File;
import java.io.IOException;

public class BedrockEntityModel extends BBModel {

    /**
     * A java edition model is size limited into a 3x3x3 area, by applying
     * a shrinkage to the model we can ensure that the model will go beyond
     * the bounds.
     */
    public static final float EFFECTIVE_SCALE = 0.4f;

    public BedrockEntityModel(File bb_file, JsonObject bb_model) throws IOException {
        super(bb_file, bb_model);

        // normalize bones to respect java edition size limits
        this.normalize(bb_model, bb_file);
    }

    /**
     * Perform normalization
     * @param bb_model
     * @param bb_file
     */
    private void normalize(JsonObject bb_model, File bb_file) {
        // normalize the blockbench UV mappings to java UV mappings
        float uv_normalize_width = 16f / bb_model.getAsJsonObject("resolution").get("width").getAsFloat();
        float uv_normalize_height = 16f / bb_model.getAsJsonObject("resolution").get("height").getAsFloat();
        for (BBGeometry geometry : this.geometry) {
            for (BBCube cube : geometry.elements) {
                for (BBFace face : cube.faces.values()) {
                    face.normalize(uv_normalize_width, uv_normalize_height);
                }
            }
        }

        // apply shrinking on geometry
        for (BBGeometry geometry : this.geometry) {
            geometry.shrink(EFFECTIVE_SCALE);
        }

        // match animations to shrunken geometry
        for (BBAnimation animation : this.animations.values()) {
            for (BBAnimator animator : animation.animators) {
                for (BBKeyFrame keyframe : animator.kf_position) {
                    keyframe.data_point[0] *= EFFECTIVE_SCALE;
                    keyframe.data_point[1] *= EFFECTIVE_SCALE;
                    keyframe.data_point[2] *= EFFECTIVE_SCALE;
                }
            }
        }
    }
}
