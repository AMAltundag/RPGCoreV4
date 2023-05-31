package me.blutkrone.rpgcore.quest.dialogue;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorDialogueBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorDialogueChoice;

import java.util.ArrayList;
import java.util.List;

/**
 * One way to complete the talking task, this allows
 * to have multiple NPCs to steer the narrative.
 */
public class CoreDialogue {
    // the entire dialogue we are getting (works with textures.)
    public String lc_dialogue_text;
    // the question to show atop the choices
    public String lc_dialogue_question;
    // choices to give to the user (maximum 4)
    public List<CoreDialogueChoice> choices = new ArrayList<>();

    public CoreDialogue(EditorDialogueBundle editor) {
        this.lc_dialogue_text = editor.lc_text;
        this.lc_dialogue_question = editor.lc_question;
        for (IEditorBundle bundle : editor.choices) {
            EditorDialogueChoice choice = ((EditorDialogueChoice) bundle);
            this.choices.add(new CoreDialogueChoice(choice));
        }
    }
}
