package com.houzicore.extension.module.integration;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.module.ModuleSimple;

import com.houzicore.extension.platform.adapter.PlatformServerAdapter;
import com.houzicore.extension.platform.controller.ModuleController;
import com.houzicore.extension.processing.resolver.ReflectionResolver;
import com.houzicore.extension.util.file.FileFacade;
import com.houzicore.extension.util.logging.FLogger;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jspecify.annotations.NonNull;

public abstract class MinecraftIntegrationModule extends IntegrationModule {

    private final PlatformServerAdapter platformServerAdapter;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final FLogger fLogger;
    private final Injector injector;

    protected MinecraftIntegrationModule(FileFacade fileFacade,
                                         FLogger fLogger,
                                         PlatformServerAdapter platformServerAdapter,
                                         ReflectionResolver reflectionResolver,
                                         ModuleController moduleController,
                                         Injector injector) {
        super(fileFacade, platformServerAdapter, moduleController, injector);

        this.platformServerAdapter = platformServerAdapter;
        this.reflectionResolver = reflectionResolver;
        this.moduleController = moduleController;
        this.fLogger = fLogger;
        this.injector = injector;
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return super.childrenBuilder();
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        return false;
    }

    public String getTextureUrl(FEntity sender) {
        return null;
    }

    public PlayerHeadObjectContents.ProfileProperty getProfileProperty(FEntity sender) {
        return null;
    }

}
