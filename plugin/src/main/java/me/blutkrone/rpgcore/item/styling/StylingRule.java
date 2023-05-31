package me.blutkrone.rpgcore.item.styling;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class StylingRule {
    // what renderer to utilize
    private String render;
    // what type of texture style to use
    private String style;
    // text colors follow a left-to-right gradient
    private Map<String, ChatColor> left_color = new HashMap<>();
    private Map<String, ChatColor> right_color = new HashMap<>();

    public StylingRule(ConfigWrapper config) {
        this.render = config.getString("render");
        this.style = config.getString("style");
        config.forEachUnder("color", (path, root) -> {
            String[] raw = root.getString(path).split(" ");
            ChatColor left = ChatColor.of(raw[0]);
            this.left_color.put(path, left);
            ChatColor right = raw.length == 2 ? ChatColor.of(raw[1]) : left;
            this.right_color.put(path, right);
        });
    }

    /**
     * What approach to use on rendering if this is the
     * style of the item.
     *
     * @return what style to render in.
     */
    public String getRender() {
        return render;
    }

    /**
     * Retrieve a texture which is defined by the styling rule.
     *
     * @param pattern  which texture to retrieve
     * @param position position of the texture
     * @return the texture we found
     */
    public IndexedTexture texture(String pattern, int position) {
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
        return resource_manager.texture("lore_" + pattern + "_" + this.style + "_" + position);
    }

    /**
     * Color a text within color specifications.
     *
     * @param pattern how to color the text
     * @param text    the text to be colored
     * @return the colored text
     */
    public String color(String pattern, String text) {
        // remove previous color rules from text
        text = ChatColor.stripColor(text);
        // identify the colors we are working with
        ChatColor left = this.left_color.getOrDefault(pattern, ChatColor.WHITE);
        ChatColor right = this.right_color.getOrDefault(pattern, ChatColor.WHITE);
        // same colors need no gradient
        if (left.equals(right) || text.length() <= 1) {
            return left + text;
        }
        // build a gradient for the text
        ChatColor[] gradient = gradient(left.getColor(), right.getColor(), text.length());
        // merge gradient with text
        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sb.append(gradient[i].toString()).append(chars[i]);
        }
        // offer up merged text
        return sb.toString();
    }

    /*
     * Generate a gradient for n letters between two colors.
     *
     * @param left first color to blend
     * @param right second color to blend
     * @param size how many steps to blend
     * @return one color per letter
     */
    private ChatColor[] gradient(Color left, Color right, int size) {
        // one color per symbol
        ChatColor[] colors = new ChatColor[size];
        // starting color to blend with
        int step_red = Math.abs(left.getRed() - right.getRed()) / (size - 1);
        int step_green = Math.abs(left.getGreen() - right.getGreen()) / (size - 1);
        int step_blue = Math.abs(left.getBlue() - right.getBlue()) / (size - 1);
        // directional increments to use
        int direction_red = left.getRed() < right.getRed() ? +1 : -1;
        int direction_green = left.getGreen() < right.getGreen() ? +1 : -1;
        int direction_blue = left.getBlue() < right.getBlue() ? +1 : -1;
        // blend the colors for each symbol
        for (int i = 0; i < size; i++) {
            int r = left.getRed() + ((step_red * i) * direction_red);
            int g = left.getGreen() + ((step_green * i) * direction_green);
            int b = left.getBlue() + ((step_blue * i) * direction_blue);
            colors[i] = ChatColor.of(new Color(r, g, b));
        }
        // offer up the blended color
        return colors;
    }
}