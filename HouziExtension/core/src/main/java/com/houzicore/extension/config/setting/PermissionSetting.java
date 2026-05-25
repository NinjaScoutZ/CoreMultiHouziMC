package com.houzicore.extension.config.setting;

import com.houzicore.extension.config.Permission;

/**
 * Configuration interface for permission settings.
 *
 * @author HouziCore Development
 * @since 1.7.1
 */
public interface PermissionSetting {

    /**
     * Gets the name of the permission.
     *
     * @return the permission name
     */
    String name();

    /**
     * Gets the type of the permission.
     *
     * @return the permission type
     * @see Permission.Type
     */
    Permission.Type type();

}
