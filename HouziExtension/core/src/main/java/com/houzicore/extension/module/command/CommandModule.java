package com.houzicore.extension.module.command;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.Command;
import com.houzicore.extension.config.Permission;
import com.houzicore.extension.config.setting.PermissionSetting;
import com.houzicore.extension.module.ModuleSimple;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CommandModule implements ModuleSimple {

    private final FileFacade fileFacade;

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return ModuleSimple.super.childrenBuilder();
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleSimple.super.permissionBuilder().add(permission().seeInvisiblePlayersInSuggest());
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND;
    }

    @Override
    public Command config() {
        return fileFacade.command();
    }

    @Override
    public Permission.Command permission() {
        return fileFacade.permission().command();
    }
}
