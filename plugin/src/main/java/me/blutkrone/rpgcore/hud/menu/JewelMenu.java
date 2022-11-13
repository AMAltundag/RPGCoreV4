package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectManager;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

public class JewelMenu {
    public ItemStack invisible;
    public List<Integer> jewel_position;
    public ItemStack locked;
    public ItemStack unlocked;
    public ItemStack await;
    public List<String> jewel_shatter_effect;
    public List<String> jewel_embed_effect;

    public JewelMenu() throws IOException {
        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "jewel.yml"));
        this.jewel_position = config.getIntegerList("jewel-position");
        this.locked = RPGCore.inst().getLanguageManager().getAsItem("jewel_socket_locked").build();
        this.unlocked = RPGCore.inst().getLanguageManager().getAsItem("jewel_socket_unlocked").build();
        this.await = RPGCore.inst().getLanguageManager().getAsItem("jewel_socket_await_item").build();
        this.invisible = RPGCore.inst().getLanguageManager().getAsItem("invisible").build();
        this.jewel_shatter_effect = config.getStringList("jewel-shatter-effect");
        this.jewel_embed_effect = config.getStringList("jewel-embed-effect");
    }

    /**
     * When this method is called, we assume that the associated item has
     * been written to 'jewel_inspection' - when the menu closes the item
     * will simply be added to the player inventory. If there is no space
     * to do so, we will instead drop it at the player their feet.
     *
     * @param player who are we inspecting.
     */
    public void open(Player player) {
        new me.blutkrone.rpgcore.menu.JewelMenu(this).finish(player);
    }

    /**
     * An effect meant to play when a player shattered a
     * jewel on one of their items.
     *
     * @param player who has shattered the jewel.
     */
    public void playShatterEffect(Player player) {
        EffectManager manager = RPGCore.inst().getEffectManager();
        for (String effect : jewel_shatter_effect) {
            CoreEffect core_effect = manager.getIndex().get(effect);
            core_effect.show(player.getLocation(), player);
        }
    }

    /**
     * An effect meant to play once a player successfully
     * embedded a jewel upon an item.
     *
     * @param player who has embedded the jewel.
     */
    public void playEmbedEffect(Player player) {
        EffectManager manager = RPGCore.inst().getEffectManager();
        for (String effect : jewel_embed_effect) {
            CoreEffect core_effect = manager.getIndex().get(effect);
            core_effect.show(player.getLocation());
        }
    }
}
