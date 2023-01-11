package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorTravelTrait;
import me.blutkrone.rpgcore.menu.NavigationMenu;
import me.blutkrone.rpgcore.minimap.MapMarker;
import me.blutkrone.rpgcore.minimap.MapRegion;
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
    // currency to pay cost in
    private String currency;
    // multiplier to the cost
    private double multiplier;

    public CoreTravelTrait(EditorTravelTrait editor) {
        super(editor);
        this.minimap = editor.minimap;
        this.currency = editor.currency;
        this.multiplier = editor.multiplier;
    }

    /**
     * What currency is used to pay for the travel.
     *
     * @return null or a currency.
     */
    public String getCurrency() {
        return currency.equalsIgnoreCase("nothingness") ? null : currency;
    }

    /**
     * Multiplies with distance to create the cost.
     *
     * @return the multiplier applied.
     */
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public boolean isAvailable(CorePlayer player) {
        return super.isAvailable(player) && RPGCore.inst().getMinimapManager().getRegion(this.minimap) != null;
    }

    @Override
    public void engage(Player player, CoreNPC npc) {
        // filter everything within our bounds
        MapRegion region = RPGCore.inst().getMinimapManager().getRegion(this.minimap);

        try {
            NavigationMenu.TravelCartography cartography = new NavigationMenu.TravelCartography(region, player.getLocation(), this);
            region.travel.forEach((id, position) -> {
                cartography.addMarker(new MapMarker(position.toLocation(player.getWorld()), "travel_" + id, 0d));
            });
            cartography.finish(player);
        } catch (Exception ex) {
            player.kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cReferenced Illegal MiniMap");
        }
    }
}