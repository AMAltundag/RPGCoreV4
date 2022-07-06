package me.blutkrone.rpgcore.skill.modifier;

import me.blutkrone.rpgcore.api.IContext;
import org.bukkit.Bukkit;

public class CoreModifierString {

    private final String string;

    public CoreModifierString(String string) {
        this.string = string;
    }

    public String evaluate(IContext context) {
        Bukkit.getLogger().severe("not implemented (core modifier string)");
        return string;
    }
}
