package me.blutkrone.rpgcore.hud.editor.bundle.binding;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.skillbar.ISkillBind;

/**
 * A binding is intended for players to use for skills.
 */
public abstract class AbstractEditorSkillBinding implements IEditorBundle {

    public abstract ISkillBind build(CoreSkill skill);
}
