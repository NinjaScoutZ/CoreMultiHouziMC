package com.houzicore.extension.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.Localization;
import com.houzicore.extension.execution.pipeline.MessagePipeline;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.model.util.ExternalModeration;
import com.houzicore.extension.model.util.Moderation;
import com.houzicore.extension.module.integration.IntegrationModule;

import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.service.ModerationService;
import com.houzicore.extension.util.checker.MuteChecker;
import com.houzicore.extension.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationMessageFormatter {

    private final FileFacade fileFacade;
    private final TimeFormatter timeFormatter;
    private final ModerationService moderationService;
    private final Provider<IntegrationModule> integrationModuleProvider;

    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;

    public String replacePlaceholders(String message,
                                      String moderationId,
                                      String reason,
                                      String date,
                                      String time,
                                      String timeLeft) {
        return StringUtils.replaceEach(message,
                new String[]{"<id>", "<reason>", "<date>", "<time>", "<time_left>"},
                new String[]{moderationId, reason, date, time, timeLeft}
        );
    }

    public String replacePlaceholders(String message, FPlayer fReceiver, Moderation moderation) {
        Localization localization = fileFacade.localization(fReceiver);

        Localization.ReasonMap constantReasons = switch (moderation.type()) {
            case BAN -> localization.command().ban().reasons();
            case MUTE -> localization.command().mute().reasons();
            case WARN -> localization.command().warn().reasons();
            case KICK -> localization.command().kick().reasons();
        };

        String reason = constantReasons.getConstant(moderation.reason());
        String date = timeFormatter.formatDate(moderation.date());
        String time = moderation.isPermanent()
                ? localization.time().permanent()
                : timeFormatter.format(fReceiver, moderation.getOriginalTime());
        String timeLeft = moderation.isPermanent()
                ? localization.time().permanent()
                : timeFormatter.format(fReceiver, moderation.getRemainingTime());

        return replacePlaceholders(message, String.valueOf(moderation.id()), reason, date, time, timeLeft);
    }

    public String replacePlaceholders(String message, FPlayer fReceiver, ExternalModeration moderation) {
        Localization localization = fileFacade.localization(fReceiver);

        String date = timeFormatter.formatDate(moderation.date());
        String time = moderation.permanent()
                ? localization.time().permanent()
                : timeFormatter.format(fReceiver, moderation.time());
        String timeLeft = moderation.permanent()
                ? localization.time().permanent()
                : timeFormatter.format(fReceiver, moderation.time() - System.currentTimeMillis());

        return replacePlaceholders(message, String.valueOf(moderation.moderationId()), moderation.reason(), date, time, timeLeft);
    }

    public Optional<MessageContext> createMuteContext(FPlayer fPlayer, MuteChecker.Status status) {
        String format = fileFacade.localization(fPlayer).command().mute().person();

        return switch (status) {
            case LOCAL -> {
                List<Moderation> mutes = moderationService.getValidMutes(fPlayer);
                if (mutes.isEmpty()) yield Optional.empty();

                Moderation mute = mutes.getFirst();

                MessageContext muteContext = messagePipeline.createContext(fPlayer, replacePlaceholders(format, fPlayer, mute))
                        .addTagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getFPlayer(mute.moderator())));

                yield Optional.of(muteContext);
            }
            case EXTERNAL -> {
                ExternalModeration mute = integrationModuleProvider.get().getMute(fPlayer);
                if (mute == null) yield Optional.empty();

                MessageContext muteContext = messagePipeline.createContext(fPlayer, replacePlaceholders(format, fPlayer, mute))
                        .addTagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getFPlayer(mute.moderatorName())));

                yield Optional.of(muteContext);
            }

            default -> Optional.empty();
        };
    }
}
