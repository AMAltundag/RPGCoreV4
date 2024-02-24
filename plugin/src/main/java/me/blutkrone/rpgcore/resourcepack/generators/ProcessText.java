package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.util.io.FileUtil;

import java.io.File;

/**
 * Transform in-memory font textures to real text fonts.
 */
public class ProcessText implements IGenerator {
    private static final File WORKSPACE_FONT_TEXTURE = FileUtil.directory("resourcepack/working/assets/minecraft/textures/font");
    private static final File WORKSPACE_FONT_JSON = FileUtil.directory("resourcepack/working/assets/minecraft/font");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        generation.process().text(WORKSPACE_FONT_TEXTURE, WORKSPACE_FONT_JSON);
    }
}
