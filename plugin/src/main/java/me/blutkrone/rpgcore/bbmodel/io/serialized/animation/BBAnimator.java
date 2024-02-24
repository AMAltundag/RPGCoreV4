package me.blutkrone.rpgcore.bbmodel.io.serialized.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * Each animator is responsible for animating one geometry.
 */
public class BBAnimator {
    public String geometry_to_animate;

    public List<BBKeyFrame> kf_position;
    public List<BBKeyFrame> kf_rotation;
    public List<BBKeyFrame> kf_scale;

    BBAnimator(JsonObject bb_animator) {
        this.geometry_to_animate = bb_animator.get("name").getAsString();
        this.kf_position = new ArrayList<>();
        this.kf_rotation = new ArrayList<>();
        this.kf_scale = new ArrayList<>();

        for (JsonElement bb_keyframe : ((JsonArray) bb_animator.get("keyframes"))) {
            String channel = ((JsonObject) bb_keyframe).get("channel").getAsString();
            BBKeyFrame keyframe = new BBKeyFrame((JsonObject) bb_keyframe);

            switch (channel) {
                case "position" -> this.kf_position.add(keyframe);
                case "rotation" -> this.kf_rotation.add(keyframe);
                case "scale" -> this.kf_scale.add(keyframe);
                default -> Bukkit.getLogger().warning("Animator using channel %s is not supported!".formatted(channel));
            }
        }
    }
}
