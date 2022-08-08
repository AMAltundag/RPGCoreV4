package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.roster.IRosterInitiator;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.initiator.AliasInitiator;
import me.blutkrone.rpgcore.hud.initiator.ClassInitiator;
import me.blutkrone.rpgcore.hud.initiator.SpawnInitiator;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A menu where the user can pick which character from
 * their roster they wish to play on, or create another
 * character on their roster.
 */
public class RosterMenu {

    // 9 pages with 3 slots each are available
    public Page[] roster_pages = new Page[9];
    // initiators that customize the character
    public List<IRosterInitiator> initiators = new ArrayList<>();

    /**
     * A menu where the user can pick which character from
     * their roster they wish to play on, or create another
     * character on their roster.
     *
     * @throws IOException should the config file be bad.
     */
    public RosterMenu() throws IOException {
        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "roster.yml"));

        // create the pages for the menu
        for (int i = 0; i < 9; i++) {
            this.roster_pages[i] = new Page(i, config.getSection("roster-page." + i));
        }

        // handle character configuration once spawned
        this.initiators.add(new ClassInitiator(config));
        this.initiators.add(new SpawnInitiator(config));
        this.initiators.add(new AliasInitiator(config));

        this.initiators.sort(Comparator.comparingInt(IRosterInitiator::priority));
    }

    /**
     * Check if the player has finished their initiation routine.
     *
     * @param player whose initiation status are we checking
     * @return true if we are finished
     */
    public boolean hasInitiated(CorePlayer player) {
        // ensure we haven't already initiated everything
        if (player.isInitiated()) {
            return true;
        }

        // wait until initiator can open a menu
        if (player.getEntity().getOpenInventory().getType() != InventoryType.CRAFTING) {
            return false;
        }

        // check if there is any initiator still pending
        for (IRosterInitiator initiator : getInitiators()) {
            if (initiator.initiate(player)) {
                return false;
            }
        }

        // otherwise we are done
        player.setInitiated();

        // move to the last known position
        player.moveToLoginPosition();

        return true;
    }

    /**
     * Open the roster menu for the given player.
     *
     * @param player who to present the roster menu to.
     */
    public void open(Player player) {
        new me.blutkrone.rpgcore.menu.RosterMenu(this).finish(player);
    }

    /**
     * A listing of all initiator rules for players.
     *
     * @return initiator rules
     */
    public List<IRosterInitiator> getInitiators() {
        return this.initiators;
    }

    /**
     * A page which can contain 3 characters, this is only intended
     * to be a visual approach to organize the 18 roster slots.
     */
    public class Page {
        // icon to show if the page is available
        public final ItemStack icon_available;
        // icon to show if the page is locked
        public final ItemStack icon_locked;
        // permission to gate the page behind
        public final String permission;

        Page(int i, ConfigWrapper config) {
            // icon to use while slot is unlocked
            this.icon_available = RPGCore.inst().getLanguageManager().getAsItem(config.getString("available"), i)
                    .persist("slot_id", i).build();
            this.icon_locked = RPGCore.inst().getLanguageManager().getAsItem(config.getString("locked"), i)
                    .persist("slot_id", i).build();
            // permission to gate page behind
            this.permission = i == 0 ? "" : config.getString("permission", "");
        }
    }

}
