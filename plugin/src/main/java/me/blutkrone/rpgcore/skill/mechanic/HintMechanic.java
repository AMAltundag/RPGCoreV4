package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorHintMechanic;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.List;

public class HintMechanic extends AbstractCoreMechanic {

    private String hint;
    private CoreModifierNumber duration;

    public HintMechanic(EditorHintMechanic editor) {
        this.hint = RPGCore.inst().getLanguageManager().getTranslation(editor.lc_hint);
        this.duration = editor.duration.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        CoreEntity entity = context.getCoreEntity();
        entity.giveFocusHint(hint, this.duration.evalAsInt(context));
    }
}
