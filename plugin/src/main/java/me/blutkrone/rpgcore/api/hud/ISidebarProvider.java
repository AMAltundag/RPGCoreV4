package me.blutkrone.rpgcore.api.hud;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;

import java.util.List;

/**
 * A provider for the sidebar, the contents are only written
 * should we have enough space for the whole content.
 */
public interface ISidebarProvider {

    /**
     * The priority which we do have, the lower priority will
     * be drawn first.
     *
     * @return the priority which we have.
     */
    int getPriority();

    /**
     * The content which is to be appended, do note that these
     * are NOT using the default font table but instead will be
     * generating a customized one.
     *
     * @return lines of text for the sidebar.
     */
    List<String> getContent(CorePlayer player);
}
