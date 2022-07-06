package me.blutkrone.rpgcore.hud.editor.bundle;

import com.google.gson.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Type;

public class EditorBundleGsonAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

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
            Class clazz = Class.forName(type.getAsString());
            return context.deserialize(data, clazz);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }
}