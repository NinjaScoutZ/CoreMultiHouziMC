package com.houzicore.extension.processing.parser.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.module.integration.IntegrationModule;
import com.houzicore.extension.platform.adapter.PlatformPlayerAdapter;
import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.util.checker.PermissionChecker;
import com.houzicore.extension.util.constant.PotionUtil;
import com.houzicore.extension.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerParser implements ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {

    private final StringParser<FPlayer> stringParser = new StringParser<>(StringParser.StringMode.SINGLE);

    private final FPlayerService playerService;
    private final IntegrationModule integrationModule;
    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PermissionChecker permissionChecker;

    @Override
    public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return stringParser.parse(context, input);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return playerService.findOnlineFPlayers().stream()
                .filter(player -> integrationModule.canSeeVanished(player, context.sender()))
                .filter(fPlayer -> isVisible(context.sender(), fPlayer))
                .map(FEntity::name)
                .toList();
    }

    protected boolean isVisible(FPlayer sender, FPlayer fPlayer) {
        if (fileFacade.command().suggestInvisiblePlayers()) return true;
        if (!platformPlayerAdapter.hasPotionEffect(fPlayer, PotionUtil.INVISIBILITY_POTION_NAME)) return true;

        return permissionChecker.check(sender, fileFacade.permission().command().seeInvisiblePlayersInSuggest());
    }
}
