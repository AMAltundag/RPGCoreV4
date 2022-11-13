package me.blutkrone.rpgcore.hud.editor.bundle;

import com.google.gson.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class EditorBundleGsonAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    private static Map<String, String> LEGACY_MAPPING = new HashMap<>();

    static {
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorAffixChance", "me.blutkrone.rpgcore.hud.editor.bundle.item.EditorAffixChance");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorAffixLimit", "me.blutkrone.rpgcore.hud.editor.bundle.item.EditorAffixLimit");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorLoot", "me.blutkrone.rpgcore.hud.editor.bundle.item.EditorLoot");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorAttributeAndFactor", "me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAttributeAndFactor");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorBankerTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorBankerTrait");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorCrafterTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorCrafterTrait");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorDialogueTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorDialogueTrait");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorEssenceTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorEssenceTrait");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorGateTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorGateTrait");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorMailTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorMailTrait");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorQuestTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorQuestTrait");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorRefinerTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorRefinerTrait");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorStorageTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorStorageTrait");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorTalkTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorTalkTrait");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.root.npc.EditorVendorTrait", "me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorVendorTrait");

        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorAudio", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectAudio");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorCircle", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectCircle");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorDirection", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectDirection");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorForward", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectForward");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorLine", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectLine");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorModel", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectModel");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorParticleBrush", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectBrush");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorParticlePoint", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectPoint");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorParticleSphere", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectSphere");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorRadiator", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectRadiator");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorRotor", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectRotor");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorWait", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectWait");

        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorAudio", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectAudio");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorCircle", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectCircle");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorDirection", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectDirection");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorForward", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectForward");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorLine", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectLine");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorModel", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectModel");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorParticleBrush", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectBrush");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorParticlePoint", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectPoint");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorParticleSphere", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectSphere");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorRadiator", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectRadiator");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorRotor", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectRotor");
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.EditorWait", "me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectWait");
    }

    @Override
    public final JsonElement serialize(final T object, final Type interfaceType, final JsonSerializationContext context) {
        final JsonObject member = new JsonObject();
        // track the type so we know how to deserialize it later
        member.addProperty("type", object.getClass().getName());
        // track the data so we know how to deserialize it later
        member.add("data", context.serialize(object));
        // offer up the deserialized info structure
        return member;
    }

    @Override
    public final T deserialize(final JsonElement elem, final Type interfaceType, final JsonDeserializationContext context) throws JsonParseException {
        // transform element to object
        JsonObject member = (JsonObject) elem;
        // retrieve the type to deserialize into
        if (!member.has("type"))
            throw new JsonParseException("no 'type' member found in json file.");
        JsonElement type = member.get("type");
        // retrieve the data to deserialize from
        if (!member.has("data"))
            throw new JsonParseException("no 'data' member found in json file.");
        JsonElement data = member.get("data");
        // deserialize the data into the type
        try {
            // safely wrap legacy class if we got one
            String safe_class = EditorBundleGsonAdapter.LEGACY_MAPPING.getOrDefault(type.getAsString(), type.getAsString());
            Class clazz = Class.forName(safe_class);
            return context.deserialize(data, clazz);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().severe("SERIALIZE:" + type.getAsString());
            throw new JsonParseException(e);
        }
    }
}