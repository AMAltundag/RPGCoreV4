package me.blutkrone.rpgcore.skill.modifier;

import me.blutkrone.rpgcore.api.IContext;
import org.bukkit.Bukkit;

public class CoreModifierBoolean {

    private final boolean bool;

    public CoreModifierBoolean(boolean bool) {
        this.bool = bool;
    }

    public boolean evaluate(IContext context) {
        Bukkit.getLogger().severe("not implemented (core modifier boolean)");
        return bool;
    }
}
