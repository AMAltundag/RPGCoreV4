package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatusMenu {

    private Map<Integer, StatusLayout> layout = new HashMap<>();
    private Map<String, List<StatusInfo>> status = new HashMap<>();

    public StatusMenu() throws IOException {
        this.reload();
    }

    public void reload() throws IOException {
        this.layout.clear();
        this.status.clear();

        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "status.yml"));
        config.forEachUnder("status-slots", (path, root) -> {
            this.layout.put(Integer.parseInt(path), new StatusLayout( root.getSection(path)));
        });
    }

    public void open(Player player) {
        new me.blutkrone.rpgcore.menu.StatusMenu(this.layout).finish(player);
    }

    /**
     * Registered status effect modifiers.
     *
     * @return Status effect modifiers.
     */
    public Map<String, List<StatusInfo>> getStatusInfo() {
        return status;
    }

    /**
     * A pseudo modifier used in the status menu.
     */
    public static class StatusInfo {
        private final String category;
        private final ModifierStyle readable_style;
        private final String text_left;
        private final String text_right;
        private final String[] attributes;
        private final boolean compress;

        /**
         * Serialize a status info object from the given string.
         *
         * @param serialized Serialized state of the object
         * @throws Exception If anything goes wrong when parsing.
         */
        public StatusInfo(String serialized) throws Exception {
            String[] split = serialized.split("#");
            if (split[0].equalsIgnoreCase("HEADER")) {
                this.readable_style = ModifierStyle.HEADER;
                this.category = "";
                this.text_left = split[1];
                this.text_right = split[2];
                this.attributes = split[3].split(",");
                this.compress = false;
            } else if (split[0].equalsIgnoreCase("GENERIC")) {
                this.readable_style = ModifierStyle.GENERIC;
                this.category = split[1];
                this.text_left = split[2];
                this.text_right = split[3];
                this.attributes = split[4].split(",");
                this.compress = false;
            }  else if (split[0].equalsIgnoreCase("COMPRESS")) {
                this.readable_style = ModifierStyle.GENERIC;
                this.category = split[1];
                this.text_left = split[2];
                this.text_right = split[3];
                this.attributes = split[4].split(",");
                this.compress = true;
            } else {
                throw new IllegalArgumentException("bad status modifier: " + serialized);
            }
        }

        /*
         * Internal constructor when we create a copy.
         *
         * @param category
         * @param readable_style
         * @param text_left
         * @param text_right
         * @param attributes
         */
        private StatusInfo(String category, ModifierStyle readable_style, String text_left, String text_right, String[] attributes, boolean compress) {
            this.category = category;
            this.readable_style = readable_style;
            this.text_left = text_left;
            this.text_right = text_right;
            this.attributes = attributes;
            this.compress = compress;
        }

        /**
         * Write a copy of the modifier with the given player their attributes
         * parsed into it.
         *
         * @param player Whose attributes to parse
         * @return Copy of this with player attributes
         */
        public StatusInfo with(CorePlayer player) {
            // tree map to retain order for format
            TreeMap<String, Double> mapped = new TreeMap<>();
            boolean hasNonZero = false;
            for (int i = 0; i < this.attributes.length; i++) {
                double factor = player.evaluateAttribute(this.attributes[i]);
                mapped.put(String.valueOf(i), factor);
                if (Math.abs(factor) >= 0.000001) {
                    hasNonZero = true;
                }
            }
            // update the strings to reflect the attributes
            String text_left = RPGCore.inst().getLanguageManager().formatAsVersatile(this.text_left, mapped);
            String text_right = RPGCore.inst().getLanguageManager().formatAsVersatile(this.text_right, mapped);
            // offer up the copy
            return new StatusInfo(this.category, this.readable_style, text_left, text_right, this.attributes, this.compress && !hasNonZero);
        }

        public ModifierStyle getReadableStyle() {
            return readable_style;
        }

        public String getTextLeft() {
            return text_left;
        }

        public String getTextRight() {
            return text_right;
        }

        public String[] getAttributes() {
            return attributes;
        }

        public String getCategory() {
            return category;
        }

        public boolean isCompress() {
            return compress;
        }
    }

    /**
     * A layout object represented as an item in the final
     * menu design.
     */
    public static class StatusLayout {
        public String style;
        public String icon;
        public List<String> attributes;

        public StatusLayout(ConfigWrapper config) {
            this.style = config.getString("style");
            this.icon = config.getString("icon");
            this.attributes = config.getStringList("attributes");
        }
    }
}
