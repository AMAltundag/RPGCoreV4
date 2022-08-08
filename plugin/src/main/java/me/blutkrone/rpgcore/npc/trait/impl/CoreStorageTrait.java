package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorStorageTrait;
import me.blutkrone.rpgcore.menu.StorageMenu;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.StoragePage;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage for items shared across the roster.
 */
public class CoreStorageTrait extends AbstractCoreTrait {

    public List<StoragePage> pages = new ArrayList<>();

    public CoreStorageTrait(EditorStorageTrait editor) {
        super(editor);
        for (String s : editor.stored) {
            StoragePage storage = RPGCore.inst().getNPCManager().getStorage(s);
            if (storage != null) {
                this.pages.add(storage);
            }
        }
    }

    @Override
    public void engage(Player player, CoreNPC npc) {
        if (RPGCore.inst().getEntityManager().getPlayer(player) == null) {
            return;
        }

        if (this.pages.isEmpty()) {
            player.sendMessage("§cINTERNAL ERROR - STORAGE PAGES NOT CONFIGURED!");
            return;
        }

        new StorageMenu(this, npc).finish(player);
    }
}
