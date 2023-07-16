package me.blutkrone.rpgcore.editor.bundle.binding;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.skillbar.ISkillBind;

/**
 * A binding is intended for players to use for skills.
 */
public abstract class AbstractEditorSkillBinding implements IEditorBundle {

    public abstract ISkillBind build(CoreSkill skill);
}
