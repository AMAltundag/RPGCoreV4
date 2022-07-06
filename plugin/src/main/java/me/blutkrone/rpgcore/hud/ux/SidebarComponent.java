package me.blutkrone.rpgcore.hud.ux;

import me.blutkrone.rpgcore.api.hud.ISidebarProvider;
import me.blutkrone.rpgcore.api.hud.IUXComponent;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.UXWorkspace;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SidebarComponent implements IUXComponent<List<String>> {

    // how many lines are to be shown
    private final int sidebar_line_total;
    // providers for text to be shown on the sidebar
    private List<ISidebarProvider> side_text_provider = new ArrayList<>();

    public SidebarComponent(ConfigWrapper section) {
        // how many lines are to be shown
        sidebar_line_total = section.getInt("sidebar-line-total");

        addProvider(new ISidebarProvider() {
            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public List<String> getContent(CorePlayer player) {
                return Arrays.asList(
                        "§a§l[1] Rat slaying in the sewer",
                        "§f  Slay §c3/10§f stinking rats",
                        "§f  Slay §c0/1§f giant stinking rat",
                        "§a§l[2] Treasure hunting",
                        "§f  Find §a3/3§f hidden treasures",
                        "§f  Find §c0/1§f lost treasure"
                );
            }
        });
    }

    /**
     * Register a provider which can supply us with text.
     *
     * @param provider the provider we operate with.
     */
    public void addProvider(ISidebarProvider provider) {
        this.side_text_provider.add(provider);
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public List<String> prepare(CorePlayer core_player, Player bukkit_player) {
        List<String> out = new ArrayList<>();
        int remaining = this.sidebar_line_total;
        // provide the content by the text options
        for (ISidebarProvider provider : this.side_text_provider) {
            List<String> content = provider.getContent(core_player);
            if (remaining < content.size()) break;
            out.addAll(content);
            remaining -= content.size();
        }
        return out;
    }

    @Override
    public void populate(CorePlayer core_player, Player bukkit_player, UXWorkspace workspace, List<String> prepared) {
        // measure the widest line we have
        int widest = 0;
        for (String s : prepared) {
            widest = Math.max(widest, Utility.measureWidthExact(s));
        }
        // write contents anchored to the left
        for (int i = 0, size = prepared.size(); i < size; i++) {
            String line = prepared.get(i);
            workspace.bossbar().shiftToExact(core_player.getSettings().screen_width - widest - 10);
            workspace.bossbar().append(line, "hud_sidebar_" + (i + 1));
        }
    }
}
