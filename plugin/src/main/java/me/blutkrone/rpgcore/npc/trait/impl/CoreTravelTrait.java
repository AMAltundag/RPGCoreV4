package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.npc.EditorTravelTrait;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.menu.NavigationMenu;
import me.blutkrone.rpgcore.minimap.v2.MapInfo;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

/**
 * Acts like a pseudo-cortex, allowing scoped access to
 * the travel-tree defined in the travel file.
 */
public class CoreTravelTrait extends AbstractCoreTrait {

    // scope we pick travel target from
    private String minimap;

    public CoreTravelTrait(EditorTravelTrait editor) {
        super(editor);
        this.minimap = editor.minimap;
    }

    @Override
    public boolean isAvailable(CorePlayer player) {
        return super.isAvailable(player) && RPGCore.inst().getMinimapManager().getMapInfo(this.minimap) != null;
    }

    @Override
    public void engage(Player player, CoreNPC npc) {
        // filter everything within our bounds
        MapInfo region = RPGCore.inst().getMinimapManager().getMapInfo(this.minimap);

        try {
            NavigationMenu.TravelCartography cartography = new NavigationMenu.TravelCartography(region, this);
            cartography.finish(player);
        } catch (Exception ex) {
            player.kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cReferenced Illegal MiniMap");
        }
    }
}