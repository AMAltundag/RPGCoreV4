package me.blutkrone.rpgcore.quest.dialogue;

import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorDialogueBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorDialogueChoice;

/**
 * A choice made by a player from within the dialogue, which
 * continues to another dialogue after.
 */
public class CoreDialogueChoice {
    // first line is shown while 2+ is a tooltip
    public String lc_choice_text;
    // follow up dialogue (blocks any rewards.)
    public CoreDialogue dialogue_next;
    // if last in branch and false will not count as completed
    public boolean correct;

    public CoreDialogueChoice(EditorDialogueChoice editor) {
        this.lc_choice_text = editor.lc_text;
        if (!editor.dialogue.isEmpty()) {
            this.dialogue_next = new CoreDialogue(((EditorDialogueBundle) editor.dialogue.get(0)));
        }
        this.correct = editor.correct;
    }
}
