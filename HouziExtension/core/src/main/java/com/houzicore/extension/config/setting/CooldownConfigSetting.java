package com.houzicore.extension.config.setting;

import com.houzicore.extension.model.util.Cooldown;

/**
 * Configuration interface for settings that include a cooldown.
 *
 * @author HouziCore Development
 * @since 1.7.1
 */
public interface CooldownConfigSetting {

    /**
     * Gets the cooldown configuration.
     *
     * @return the cooldown configuration
     * @see Cooldown
     */
    Cooldown cooldown();

}
