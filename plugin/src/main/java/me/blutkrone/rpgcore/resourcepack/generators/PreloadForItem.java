package me.blutkrone.rpgcore.resourcepack.generators;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.Item;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate.TrimPredicate;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Material;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;

public class PreloadForItem implements IGenerator {

    private static final Item ITEM_LEATHER_HORSE_ARMOR = new Item(Material.LEATHER_HORSE_ARMOR, Item.PARENT_GENERATED) {{
        // initialize default textures
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "minecraft:item/leather_horse_armor");
        this.template.add("textures", textures);
    }};
    private static final Item ITEM_LEATHER_HELMET = new Item(Material.LEATHER_HORSE_ARMOR, Item.PARENT_GENERATED) {{
        // initialize default textures
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "minecraft:item/leather_helmet");
        textures.addProperty("layer1", "minecraft:item/leather_helmet_overlay");
        this.template.add("textures", textures);
        // initialize default trims
        this.overrides.add(new ItemOverride(new TrimPredicate(0.1d), "minecraft:item/leather_helmet_quartz_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.2d), "minecraft:item/leather_helmet_iron_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.3d), "minecraft:item/leather_helmet_netherite_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.4d), "minecraft:item/leather_helmet_redstone_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.5d), "minecraft:item/leather_helmet_copper_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.6d), "minecraft:item/leather_helmet_gold_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.7d), "minecraft:item/leather_helmet_emerald_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.8d), "minecraft:item/leather_helmet_diamond_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.9d), "minecraft:item/leather_helmet_lapis_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(1.0d), "minecraft:item/leather_helmet_amethyst_trim"));
    }};
    private static final Item ITEM_LEATHER_CHESTPLATE = new Item(Material.LEATHER_HORSE_ARMOR, Item.PARENT_GENERATED) {{
        // initialize default textures
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "minecraft:item/leather_chestplate");
        textures.addProperty("layer1", "minecraft:item/leather_chestplate_overlay");
        this.template.add("textures", textures);
        // initialize default trims
        this.overrides.add(new ItemOverride(new TrimPredicate(0.1d), "minecraft:item/leather_chestplate_quartz_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.2d), "minecraft:item/leather_chestplate_iron_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.3d), "minecraft:item/leather_chestplate_netherite_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.4d), "minecraft:item/leather_chestplate_redstone_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.5d), "minecraft:item/leather_chestplate_copper_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.6d), "minecraft:item/leather_chestplate_gold_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.7d), "minecraft:item/leather_chestplate_emerald_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.8d), "minecraft:item/leather_chestplate_diamond_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.9d), "minecraft:item/leather_chestplate_lapis_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(1.0d), "minecraft:item/leather_chestplate_amethyst_trim"));
    }};
    private static final Item ITEM_LEATHER_LEGGINGS = new Item(Material.LEATHER_HORSE_ARMOR, Item.PARENT_GENERATED) {{
        // initialize default textures
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "minecraft:item/leather_leggings");
        textures.addProperty("layer1", "minecraft:item/leather_leggings_overlay");
        this.template.add("textures", textures);
        // initialize default trims
        this.overrides.add(new ItemOverride(new TrimPredicate(0.1d), "minecraft:item/leather_leggings_quartz_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.2d), "minecraft:item/leather_leggings_iron_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.3d), "minecraft:item/leather_leggings_netherite_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.4d), "minecraft:item/leather_leggings_redstone_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.5d), "minecraft:item/leather_leggings_copper_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.6d), "minecraft:item/leather_leggings_gold_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.7d), "minecraft:item/leather_leggings_emerald_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.8d), "minecraft:item/leather_leggings_diamond_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.9d), "minecraft:item/leather_leggings_lapis_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(1.0d), "minecraft:item/leather_leggings_amethyst_trim"));
    }};
    private static final Item ITEM_LEATHER_BOOTS = new Item(Material.LEATHER_HORSE_ARMOR, Item.PARENT_GENERATED) {{
        // initialize default textures
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "minecraft:item/leather_boots");
        textures.addProperty("layer1", "minecraft:item/leather_boots_overlay");
        this.template.add("textures", textures);
        // initialize default trims
        this.overrides.add(new ItemOverride(new TrimPredicate(0.1d), "minecraft:item/leather_boots_quartz_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.2d), "minecraft:item/leather_boots_iron_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.3d), "minecraft:item/leather_boots_netherite_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.4d), "minecraft:item/leather_boots_redstone_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.5d), "minecraft:item/leather_boots_copper_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.6d), "minecraft:item/leather_boots_gold_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.7d), "minecraft:item/leather_boots_emerald_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.8d), "minecraft:item/leather_boots_diamond_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(0.9d), "minecraft:item/leather_boots_lapis_trim"));
        this.overrides.add(new ItemOverride(new TrimPredicate(1.0d), "minecraft:item/leather_boots_amethyst_trim"));
    }};

    private static final File WORKSPACE_ITEM = FileUtil.directory("resourcepack/working/assets/minecraft/models/item");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        // preload items from configuration
        for (File item_file : FileUtil.buildAllFiles(WORKSPACE_ITEM)) {
            String item_name = item_file.getName();
            item_name = item_name.substring(0, item_name.length() - 5);
            Material material = Material.valueOf(item_name.toUpperCase());

            try (Reader reader = Files.newBufferedReader(item_file.toPath())) {
                JsonObject serialized = generation.gson().fromJson(reader, JsonObject.class);
                generation.model().register(new Item(material, serialized));
            }
        }
        // preload items that have special configurations
        generation.model().register(ITEM_LEATHER_HORSE_ARMOR);
        generation.model().register(ITEM_LEATHER_HELMET);
        generation.model().register(ITEM_LEATHER_CHESTPLATE);
        generation.model().register(ITEM_LEATHER_LEGGINGS);
        generation.model().register(ITEM_LEATHER_BOOTS);
    }
}
