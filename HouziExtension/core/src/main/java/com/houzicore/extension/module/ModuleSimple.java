package com.houzicore.extension.module;

import com.google.common.collect.ImmutableSet;
import com.houzicore.extension.config.setting.EnableSetting;
import com.houzicore.extension.config.setting.PermissionSetting;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;

import java.util.function.BiPredicate;

public interface ModuleSimple {

    ModuleName name();

    EnableSetting config();

    PermissionSetting permission();

    default void onEnable() {
    }

    default void onDisable() {
    }

    default BiPredicate<FEntity, Boolean> disablePredicate() {
        return (fEntity, aBoolean) -> false;
    }

    default ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return ImmutableSet.builder();
    }

    default ImmutableSet.Builder<@NonNull PermissionSetting> permissionBuilder() {
        return ImmutableSet.<PermissionSetting>builder().add(permission());
    }

}
