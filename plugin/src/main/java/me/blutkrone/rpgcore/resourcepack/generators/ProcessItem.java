package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.util.io.FileUtil;

import java.io.File;

public class ProcessItem implements IGenerator {
    private static final File WORKSPACE_ITEM = FileUtil.directory("resourcepack/working/assets/minecraft/models/item");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        generation.process().item(WORKSPACE_ITEM);
    }
}
