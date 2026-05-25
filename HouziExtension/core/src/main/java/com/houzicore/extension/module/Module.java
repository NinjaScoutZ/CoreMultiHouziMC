package com.houzicore.extension.module;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.Config;
import com.houzicore.extension.config.setting.PermissionSetting;
import com.houzicore.extension.module.command.CommandModule;
import com.houzicore.extension.module.integration.IntegrationModule;
import com.houzicore.extension.module.message.MessageModule;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Module implements ModuleSimple {

    private final FileFacade fileFacade;

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return ModuleSimple.super.childrenBuilder().add(
                IntegrationModule.class,
                CommandModule.class,
                MessageModule.class
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.MODULE;
    }

    @Override
    public Config.Module config() {
        return fileFacade.config().module();
    }

    @Override
    public PermissionSetting permission() {
        return fileFacade.permission().module();
    }

}
