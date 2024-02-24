package me.blutkrone.rpgcore.hud.ux;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.hud.IUXComponent;
import me.blutkrone.rpgcore.api.social.IPartySnapshot;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.entity.resource.EntityWard;
import me.blutkrone.rpgcore.entity.resource.ResourceSnapshot;
import me.blutkrone.rpgcore.hud.UXWorkspace;
import me.blutkrone.rpgcore.resourcepack.ResourcepackManager;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.AbstractTexture;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PartyComponent implements IUXComponent<List<PartyComponent.Snapshot>> {

    private int party_name_offset;
    private int party_offset;
    private int party_health_offset;
    private int party_ward_offset;

    public PartyComponent(ConfigWrapper section) {
        party_offset = section.getInt("interface-offset.party-offset");
        party_health_offset = section.getInt("interface-offset.party-health-offset");
        party_ward_offset = section.getInt("interface-offset.party-ward-offset");
        party_name_offset = section.getInt("interface-offset.party-name-offset");
    }

    /*
     * Draw the info for a specific resource, including a graphical cue
     * on the state and a verbal cue on it.
     *
     * @param workspace        container for all UX info
     * @param graphic_cue      cue prefix to access the graphic cue
     * @param resource         which resource is represented
     * @param render_point     the point to draw at
     */
    private static void drawSpecificResource(UXWorkspace workspace, String graphic_cue, ResourceSnapshot resource, int render_point) {
        ResourcepackManager rpm = RPGCore.inst().getResourcepackManager();
        AbstractTexture graphic_cue_texture = rpm.texture(graphic_cue + "_" + (int) (100 * resource.fraction));
        workspace.bossbar().shiftToExact(render_point);
        workspace.bossbar().append(graphic_cue_texture);
    }

    @Override
    public int getPriority() {
        return 7;
    }

    @Override
    public List<Snapshot> prepare(CorePlayer core_player, Player bukkit_player) {
        List<Snapshot> snapshots = new ArrayList<>();

        IPartySnapshot party = RPGCore.inst().getSocialManager().getGroupHandler().getPartySnapshot(core_player);
        if (party != null) {
            List<OfflinePlayer> players = new ArrayList<>(party.getAllMembers());
            players.sort(Comparator.comparingDouble(player -> {
                if (player instanceof Player) {
                    return Utility.distanceSqOrWorld(bukkit_player, ((Player) player));
                } else {
                    return Double.MAX_VALUE;
                }
            }));
            if (players.size() != 1) {
                players.removeIf(player -> player.getUniqueId().equals(bukkit_player.getUniqueId()));
            }

            for (OfflinePlayer player : players) {
                CorePlayer party_player = RPGCore.inst().getEntityManager().getPlayer(player.getUniqueId());
                if (party_player != null) {
                    snapshots.add(new Snapshot(party_player));
                } else {
                    snapshots.add(new Snapshot(player));
                }
            }

        }

        // snapshots.add(new Snapshot("§aBilly And His Book", new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5)));
        // snapshots.add(new Snapshot("§aBobby And His Sword", new ResourceSnapshot(100, 300, 0.33), new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5)));
        // snapshots.add(new Snapshot("§aMicky And His Shoe", new ResourceSnapshot(100, 400, 0.25), new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5)));
        // snapshots.add(new Snapshot("§8Dobby And His Sock", new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5)));
        // snapshots.add(new Snapshot("§8Ricky And His Pickle", new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5), new ResourceSnapshot(100, 200, 0.5)));

        return snapshots;
    }

    @Override
    public void populate(CorePlayer core_player, Player bukkit_player, UXWorkspace workspace, List<Snapshot> prepared) {
        ResourcepackManager rpm = RPGCore.inst().getResourcepackManager();

        for (int i = 0, size = prepared.size(); i < size && i < 5; i++) {
            Snapshot party_member = prepared.get(i);
            // draw the background frame
            workspace.bossbar().shiftToExact(party_offset);
            workspace.bossbar().append(rpm.texture("static_party_back_member_" + i));
            // draw resources if available
            if (party_member.health != null) {
                drawSpecificResource(workspace, "bar_party_health_filling_" + i, party_member.health, party_offset + party_health_offset);
            }
            if (party_member.ward != null) {
                drawSpecificResource(workspace, "bar_party_ward_filling_" + i, party_member.ward, party_offset + party_ward_offset);
            }
            // draw the name of the party member
            workspace.bossbar().shiftToExact(party_offset + party_name_offset);
            workspace.bossbar().append(party_member.name, "hud_party_name_" + (i + 1));
            // draw the foreground frame
            workspace.bossbar().shiftToExact(party_offset);
            workspace.bossbar().append(rpm.texture("static_party_front_member_" + i));
        }
    }

    class Snapshot {

        String name;
        ResourceSnapshot health;
        ResourceSnapshot mana;
        ResourceSnapshot stamina;
        ResourceSnapshot ward;

        Snapshot(String name, ResourceSnapshot health, ResourceSnapshot mana, ResourceSnapshot stamina, ResourceSnapshot ward) {
            this.name = name;
            this.health = health;
            this.mana = mana;
            this.stamina = stamina;
            this.ward = ward;
        }

        Snapshot(CorePlayer online) {
            this.name = "§a" + online.getEntity().getName();
            this.health = online.getHealth().snapshot();
            this.mana = online.getMana().snapshot();
            this.stamina = online.getStamina().snapshot();
            EntityWard ward = online.getWard();
            if (ward != null) {
                this.ward = ward.snapshot();
            }
        }

        Snapshot(OfflinePlayer offline) {
            this.name = "§8" + offline.getName();
        }
    }

}
