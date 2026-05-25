package com.houzicore.extension.config.setting;

import com.houzicore.extension.model.util.Sound;

/**
 * Configuration interface for settings that include sound.
 *
 * @author HouziCore Development
 * @since 1.7.1
 */
public interface SoundConfigSetting {

    /**
     * Gets the sound configuration.
     *
     * @return the sound configuration
     * @see Sound
     */
    Sound sound();

}
