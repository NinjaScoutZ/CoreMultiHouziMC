package com.houzicore.extension.platform.registry;

import com.houzicore.extension.config.Permission;
import com.houzicore.extension.config.setting.PermissionSetting;

public interface PermissionRegistry extends Registry {

    void register(String name, Permission.Type type);

    default void register(PermissionSetting permissionSetting) {
        if (permissionSetting == null) return;

        register(permissionSetting.name(), permissionSetting.type());
    }

}
