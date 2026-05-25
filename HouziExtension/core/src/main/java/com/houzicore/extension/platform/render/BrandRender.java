package com.houzicore.extension.platform.render;

import com.houzicore.extension.model.entity.FPlayer;
import net.kyori.adventure.text.Component;

/**
 * Renders server brand messages in player's client multiplayer screen.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * BrandRender brandRender = houzicorePulse.get(BrandRender.class);
 *
 * // Display custom server brand
 * brandRender.render(player, Component.text("My Awesome Server"));
 * }</pre>
 *
 * @author HouziCore Development
 * @since 1.7.0
 */
public interface BrandRender {

    /**
     * Renders a server brand message to a player's client.
     *
     * @param fPlayer the player to receive the brand message
     * @param component the brand component to display
     */
    void render(FPlayer fPlayer, Component component);

}
