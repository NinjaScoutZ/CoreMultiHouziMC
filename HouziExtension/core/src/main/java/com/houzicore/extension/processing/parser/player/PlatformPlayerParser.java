package com.houzicore.extension.processing.parser.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.module.integration.IntegrationModule;
import com.houzicore.extension.platform.adapter.PlatformPlayerAdapter;
import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.util.checker.PermissionChecker;
import com.houzicore.extension.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.jspecify.annotations.NonNull;

@Singleton
public class PlatformPlayerParser extends PlayerParser {

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final IntegrationModule integrationModule;
    private final FPlayerService fPlayerService;

    @Inject
    public PlatformPlayerParser(FPlayerService fPlayerService,
                                IntegrationModule integrationModule,
                                FileFacade fileFacade,
                                PlatformPlayerAdapter platformPlayerAdapter,
                                PermissionChecker permissionChecker) {
        super(fPlayerService, integrationModule, fileFacade, platformPlayerAdapter, permissionChecker);

        this.fPlayerService = fPlayerService;
        this.integrationModule = integrationModule;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return platformPlayerAdapter.getOnlinePlayers().stream()
                .map(fPlayerService::getFPlayer)
                .filter(player -> integrationModule.canSeeVanished(player, context.sender()))
                .filter(fPlayer -> isVisible(context.sender(), fPlayer))
                .map(FEntity::name)
                .toList();
    }
}
