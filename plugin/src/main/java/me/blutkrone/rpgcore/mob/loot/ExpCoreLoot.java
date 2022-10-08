package me.blutkrone.rpgcore.mob.loot;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.loot.EditorLootExperience;
import org.bukkit.Bukkit;

import java.util.List;

public class ExpCoreLoot extends AbstractCoreLoot {

    private double experience;

    public ExpCoreLoot(EditorLootExperience editor) {
        this.experience = editor.experience;
    }

    @Override
    public void offer(CoreEntity killed, List<CorePlayer> killer) {
        Bukkit.getLogger().severe("Not implemented (exp loot)");
    }
}
