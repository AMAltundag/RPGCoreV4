package me.blutkrone.rpgcore.bbmodel.active.component;

import java.util.ArrayList;
import java.util.List;

public class AnimationResult {

    public List<SubResult> results;

    public AnimationResult() {
        this.results = new ArrayList<>();
    }

    public float[] position() {
        if (results.isEmpty()) {
            return new float[3];
        }

        float x = 0f;
        float y = 0f;
        float z = 0f;
        float w = 0f;
        for (SubResult result : this.results) {
            x += result.position[0] * result.weight;
            y += result.position[1] * result.weight;
            z += result.position[2] * result.weight;
            w += result.weight;
        }
        return new float[] { x/w, y/w, z/w };
    }

    public float[] rotation() {
        if (results.isEmpty()) {
            return new float[3];
        }
        float x = 0f;
        float y = 0f;
        float z = 0f;
        float w = 0f;
        for (SubResult result : this.results) {
            x += result.rotation[0] * result.weight;
            y += result.rotation[1] * result.weight;
            z += result.rotation[2] * result.weight;
            w += result.weight;
        }
        return new float[] { x/w, y/w, z/w };
    }

    public float[] scale() {
        if (results.isEmpty()) {
            return new float[] { 1f, 1f, 1f };
        }

        float x = 0f;
        float y = 0f;
        float z = 0f;
        float w = 0f;
        for (SubResult result : this.results) {
            x += result.scale[0] * result.weight;
            y += result.scale[1] * result.weight;
            z += result.scale[2] * result.weight;
            w += result.weight;
        }
        return new float[] { x/w, y/w, z/w };
    }

    /**
     * A sub-result that will be blended
     */
    public static class SubResult {
        float[] position;
        float[] rotation;
        float[] scale;
        float weight;

        public SubResult(float[] position, float[] rotation, float[] scale, float weight) {
            this.position = position;
            this.rotation = rotation;
            this.scale = scale;
            this.weight = weight;
        }
    }
}
