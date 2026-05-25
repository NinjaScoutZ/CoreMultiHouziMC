package com.houzicore.extension.platform.registry;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.processing.parser.integer.ColorParser;
import com.houzicore.extension.processing.parser.integer.DurationReasonParser;
import com.houzicore.extension.processing.parser.player.PlayerParser;
import com.houzicore.extension.processing.parser.string.MessageParser;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.parser.standard.StringParser;

public interface BrigadierCommandRegistry extends CommandRegistry {

    /**
     * Configures Brigadier mappings for custom argument parsers.
     * This method sets up mappings between HouziExtension parsers and Brigadier argument types.
     *
     * @param brigadierManager the CloudBrigadierManager to configure
     */
    default void setupBrigadierManager(CloudBrigadierManager<FPlayer, ?> brigadierManager) {
        brigadierManager.setNativeSuggestions(new TypeToken<StringParser<FPlayer>>() {}, true);

        brigadierManager.registerMapping(new TypeToken<PlayerParser>() {},
                builder -> builder.cloudSuggestions()
                        .to(argument -> StringArgumentType.string())
        );

        brigadierManager.registerMapping(new TypeToken<DurationReasonParser>() {},
                builder -> builder.cloudSuggestions()
                        .to(argument -> StringArgumentType.greedyString())
        );

        brigadierManager.registerMapping(new TypeToken<ColorParser>() {},
                builder -> builder.cloudSuggestions()
                        .to(argument -> StringArgumentType.greedyString())
        );

        brigadierManager.registerMapping(new TypeToken<MessageParser>() {},
                builder -> builder.cloudSuggestions()
                        .to(argument -> StringArgumentType.greedyString())
        );

        brigadierManager.settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);
    }

}
