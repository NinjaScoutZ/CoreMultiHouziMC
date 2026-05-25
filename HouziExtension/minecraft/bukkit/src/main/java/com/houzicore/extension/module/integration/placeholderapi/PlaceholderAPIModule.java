package com.houzicore.extension.module.integration.placeholderapi;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.Integration;
import com.houzicore.extension.config.Permission;
import com.houzicore.extension.config.setting.PermissionSetting;
import com.houzicore.extension.module.ModuleSimple;
import com.houzicore.extension.platform.registry.ListenerRegistry;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderAPIModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final Provider<PlaceholderAPIIntegration> placeholderAPIIntegrationProvider;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        placeholderAPIIntegrationProvider.get().hook();
        listenerRegistry.register(PlaceholderAPIIntegration.class);
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleSimple.super.permissionBuilder().add(permission().use());
    }

    @Override
    public void onDisable() {
        placeholderAPIIntegrationProvider.get().unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_PLACEHOLDERAPI;
    }

    @Override
    public Integration.Placeholderapi config() {
        return fileFacade.integration().placeholderapi();
    }

    @Override
    public Permission.Integration.Placeholderapi permission() {
        return fileFacade.permission().integration().placeholderapi();
    }
}
