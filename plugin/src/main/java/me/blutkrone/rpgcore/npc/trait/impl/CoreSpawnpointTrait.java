package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorSpawnpointTrait;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Update spawn location
 */
public class CoreSpawnpointTrait extends AbstractCoreTrait {

    private int cooldown;

    public CoreSpawnpointTrait(EditorSpawnpointTrait editor) {
        super(editor);
        this.cooldown = (int) (editor.cooldown * 72000);
    }

    @Override
    public void engage(Player player, CoreNPC npc) {
        LanguageManager lpm = RPGCore.inst().getLanguageManager();

        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        // check for cooldown and warn if necessary
        int cooldown = core_player.getCooldown("spawnpoint_cooldown");
        if (cooldown > 0) {
            String message = lpm.getTranslation("spawnpoint_cooldown");
            message = message.replace("{COOLDOWN}", lpm.formatShortTime(cooldown));
            player.sendMessage(message);
            return;
        }
        // update the spawn position
        Location updated = player.getLocation().clone();
        core_player.setRespawnPosition(updated);
        core_player.setCooldown("spawnpoint_cooldown", this.cooldown);
        // inform about success on updating
        String message = lpm.getTranslation("spawnpoint_updated");
        message = message.replace("{WHERE}", "%s/%s/%s".formatted(updated.getBlockX(), updated.getBlockY(), updated.getBlockZ()));
        player.sendMessage(message);
    }
}
