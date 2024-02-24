package me.blutkrone.rpgcore.bbmodel.io.serialized.animation;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an animation of the model.
 */
public class BBAnimation {

    public String name;
    public boolean override;
    public String loop;
    public float length;
    public List<BBAnimator> animators;

    /**
     * Represents an animation of the model.
     *
     * @param bb_animation JSON representation.
     */
    public BBAnimation(JsonObject bb_animation) {
        // pull generic parameters
        this.name = bb_animation.get("name").getAsString();
        this.override = bb_animation.get("override").getAsBoolean();
        this.loop = bb_animation.get("loop").getAsString();
        this.length = bb_animation.get("length").getAsFloat();
        // initialize animators
        this.animators = new ArrayList<>();
        JsonObject bb_animators = (JsonObject) bb_animation.get("animators");
        bb_animators.asMap().forEach((key, bb_animator) -> {
            this.animators.add(new BBAnimator((JsonObject) bb_animator));
        });
    }
}
