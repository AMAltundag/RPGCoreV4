package me.blutkrone.rpgcore.bbmodel.io.serialized.purpose;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.bbmodel.io.serialized.BBModel;

import java.io.File;
import java.io.IOException;

public class JavaItemModel extends BBModel {

    public JavaItemModel(File bb_file, JsonObject bb_model) throws IOException {
        super(bb_file, bb_model);
    }
}
