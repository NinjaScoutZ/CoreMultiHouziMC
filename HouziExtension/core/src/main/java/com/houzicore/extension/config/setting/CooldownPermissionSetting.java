package com.houzicore.extension.config.setting;

import com.houzicore.extension.config.Permission;

/**
 * Configuration interface for settings that include cooldown bypass permissions.
 *
 * @author HouziCore Development
 * @since 1.7.1
 */
public interface CooldownPermissionSetting {

    /**
     * Gets the permission entry for bypassing cooldowns.
     *
     * @return the cooldown bypass permission configuration
     * @see Permission.PermissionEntry
     */
    Permission.PermissionEntry cooldownBypass();

}
