package com.houzicore.extension.platform.render;

import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.util.Toast;
import net.kyori.adventure.text.Component;

/**
 * Renders toast notifications (advancement-style popups) to players.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * ToastRender toastRender = houzicorePulse.get(ToastRender.class);
 *
 * Toast toast = new Toast(
 *     Toast.Style.TASK,
 *     "minecraft:diamond"
 * );
 *
 * toastRender.render(player, Component.text("Achievement Unlocked!"), toast);
 * }</pre>
 *
 * @author HouziCore Development
 * @since 1.7.0
 */
public interface ToastRender {

    /**
     * Renders a toast notification to a player.
     *
     * @param fPlayer the player to receive the toast
     * @param title the title component to display
     * @param description the description component to display
     * @param toast the toast configuration including style and icon
     */
    void render(FPlayer fPlayer, Component title, Component description, Toast toast);

}
