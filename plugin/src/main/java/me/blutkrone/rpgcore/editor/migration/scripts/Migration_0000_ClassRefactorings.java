package me.blutkrone.rpgcore.editor.migration.scripts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.migration.AbstractMigration;
import me.blutkrone.rpgcore.util.io.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/*
 * Bundle paths need to be corrected.
 */
public class Migration_0000_ClassRefactorings extends AbstractMigration {

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
        LEGACY_MAPPING.put("me.blutkrone.rpgcore.hud.editor.bundle.dungeon.EditorDungeonBlockSwapper", "me.blutkrone.rpgcore.hud.editor.bundle.dungeon.EditorDungeonBlock");
    }

    @Override
    public void apply() {
        // deep scan of all *.rpgcore files with potential bundles
        Map<File, JsonObject> working = new HashMap<>();
        try {
            File[] files = FileUtil.buildAllFiles(FileUtil.directory("editor"));
            for (File file : files) {
                if (file.getName().endsWith(".rpgcore")) {
                    try (FileReader reader = new FileReader(file)) {
                        working.put(file, GSON.fromJson(reader, JsonObject.class));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // only migrate files if below a certain version
        working.entrySet().removeIf(entry -> {
            JsonElement version = entry.getValue().get("migration_version");
            return !(version == null || version.getAsInt() <= 0);
        });

        // perform the migration on the files
        if (!working.isEmpty()) {
            for (Map.Entry<File, JsonObject> working_entry : working.entrySet()) {
                // remap everything that is to be remapped
                Queue<JsonObject> objects = new LinkedList<>();
                objects.add(working_entry.getValue());

                while (!objects.isEmpty()) {
                    JsonObject object = objects.poll();
                    if (object.has("type") && object.has("data") && object.size() == 2) {
                        // correct class paths that were changed
                        if (object.get("type").getAsString().startsWith("me.blutkrone.rpgcore")) {
                            String corrected = LEGACY_MAPPING.get(object.get("type").getAsString());
                            if (corrected != null) {
                                object.addProperty("type", corrected);
                            }
                        }

                        // correct full path refactor
                        if (object.get("type").getAsString().startsWith("me.blutkrone.rpgcore.hud.editor.")) {
                            String corrected = object.get("type").getAsString().replace("me.blutkrone.rpgcore.hud.editor.", "me.blutkrone.rpgcore.editor.");
                            object.addProperty("type", corrected);
                        }

                        // explore deeper
                        objects.add((JsonObject) object.get("data"));
                    } else {
                        // explore deeper
                        for (JsonElement value : object.asMap().values()) {
                            if (value instanceof JsonObject) {
                                objects.add(((JsonObject) value));
                            } else if (value instanceof JsonArray) {
                                for (JsonElement _value : ((JsonArray) value)) {
                                    if (_value instanceof JsonObject) {
                                        objects.add(((JsonObject) _value));
                                    }
                                }
                            }
                        }
                    }
                }

                // mark the file as being corrected
                working_entry.getValue().addProperty("migration_version", 1);
            }

            // write the files back to the disk
            RPGCore.inst().getLogger().warning("Migrated " + working.size() + " files with " + this.getClass().getSimpleName() + "!");
            for (Map.Entry<File, JsonObject> entry : working.entrySet()) {
                try (FileWriter fw = new FileWriter(entry.getKey(), Charset.forName("UTF-8"))) {
                    GSON.toJson(entry.getValue(), fw);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
