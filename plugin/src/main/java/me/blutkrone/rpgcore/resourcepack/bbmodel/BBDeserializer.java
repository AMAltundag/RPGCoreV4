package me.blutkrone.rpgcore.resourcepack.bbmodel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import me.blutkrone.rpgcore.resourcepack.bbmodel.editor.BBModel;

import java.lang.reflect.Type;

public class BBDeserializer implements JsonDeserializer<BBModel> {
    @Override
    public BBModel deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return new BBModel(json.getAsJsonObject());
    }
}
