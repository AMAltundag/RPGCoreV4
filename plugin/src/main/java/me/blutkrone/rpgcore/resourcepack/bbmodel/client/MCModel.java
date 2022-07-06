package me.blutkrone.rpgcore.resourcepack.bbmodel.client;


import me.blutkrone.rpgcore.resourcepack.bbmodel.editor.BBDisplay;

import java.util.List;
import java.util.Map;

public class MCModel {
    private final String name;
    private final String parent;
    private final int[] texture_size;
    private final Map<String, String> textures;
    private final List<MCElement> elements;
    private final Map<String, BBDisplay> display;

    public MCModel(String name, String parent, int[] texture_size, Map<String, String> textures, List<MCElement> elements, Map<String, BBDisplay> display) {
        this.name = name;
        this.parent = parent;
        this.texture_size = texture_size;
        this.textures = textures;
        this.elements = elements;
        this.display = display;
    }
}