package com.houzicore.extension.config.setting;

import com.houzicore.extension.config.Permission;

/**
 * Configuration interface for settings that include sound permissions.
 *
 * @author HouziCore Development
 * @since 1.7.1
 */
public interface SoundPermissionSetting {

    /**
     * Gets the permission entry for playing sounds.
     *
     * @return the sound permission configuration
     * @see Permission.PermissionEntry
     */
    Permission.PermissionEntry sound();

}
