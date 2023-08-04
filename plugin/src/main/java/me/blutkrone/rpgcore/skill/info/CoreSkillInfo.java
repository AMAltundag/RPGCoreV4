package me.blutkrone.rpgcore.skill.info;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.other.EditorSkillInfo;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;
import me.blutkrone.rpgcore.item.styling.IDescriptionRequester;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A special wrapper which can provide us with information
 * on the modifiers of a skill.
 * <br>
 * Information about the binding is automatically generated, if
 * you wish for additional information the skill itself can host
 * arbitrary additional information.
 */
public class CoreSkillInfo {

    private String lc_category;
    private ModifierStyle readable_style;
    private String lc_readable;
    private CoreModifierNumber value;

    public CoreSkillInfo(EditorSkillInfo editor) {
        this.lc_category = editor.lc_category;
        this.readable_style = editor.readable_style;
        this.lc_readable = editor.lc_readable;
        this.value = editor.value.build();
    }

    public CoreSkillInfo(String lc_category, ModifierStyle readable_style, String lc_readable, CoreModifierNumber value) {
        this.lc_category = lc_category;
        this.readable_style = readable_style;
        this.lc_readable = lc_readable;
        this.value = value;
    }

    public String getLCCategory() {
        return this.lc_category;
    }

    public ModifierStyle getStyle() {
        return this.readable_style;
    }

    public List<String> getLCReadable(CorePlayer player) {
        // check if the value should be shown at all
        double value = this.value == null ? 1337d : this.value.evalAsDouble(player);
        if (Math.abs(value) <= 0.0001) {
            return new ArrayList<>();
        }
        // grab translation and format it to the value
        LanguageManager manager = RPGCore.inst().getLanguageManager();
        List<String> readables = manager.getTranslationList(this.lc_readable);
        readables.replaceAll((string -> {
            return manager.formatAsVersatile(string, Collections.singletonMap("auto", value));
        }));
        // render the specific modifier we have
        return readables;
    }

    public List<String> getLCReadable(IDescriptionRequester player) {
        if (player instanceof CorePlayer) {
            // translate as a player
            return getLCReadable(((CorePlayer) player));
        } else {
            // grab translation and format it to the value
            LanguageManager manager = RPGCore.inst().getLanguageManager();
            List<String> readables = manager.getTranslationList(this.lc_readable);
            readables.replaceAll(manager::formatAsVague);
            // render the specific modifier we have
            return readables;
        }
    }

    public static class CoreDamageInfo extends CoreSkillInfo {

        private final long lower;
        private final long upper;

        public CoreDamageInfo(String lc_category, ModifierStyle readable_style, String lc_readable, double lower, double upper) {
            super(lc_category, readable_style, lc_readable, null);
            this.lower = (long) lower;
            this.upper = (long) upper;
        }

        @Override
        public List<String> getLCReadable(CorePlayer player) {
            List<String> output = super.getLCReadable(player);
            output.replaceAll((string -> {
                if (this.lower == this.upper) {
                    string = string.replace("{DAMAGE_APPROXIMATION}", String.valueOf(this.lower));
                } else {
                    string = string.replace("{DAMAGE_APPROXIMATION}", this.lower + "-" + this.upper);
                }
                return string;
            }));
            return output;
        }
    }
}
