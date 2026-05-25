package com.houzicore.extension.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.execution.pipeline.MessagePipeline;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.EventMetadata;
import com.houzicore.extension.model.event.VanishMetadata;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.module.integration.IntegrationModule;

import com.houzicore.extension.util.constant.MessageFlag;
import com.houzicore.extension.util.constant.ModuleName;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Sends formatted messages to external integrations (Discord, Telegram, Twitch, etc.)
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * IntegrationSender integrationSender = houzicorePulse.get(IntegrationSender.class);
 *
 * // Send chat message to external integrations
 * integrationSender.asyncSend(MessageType.CHAT, "<final_message>", eventMetadata);
 * }</pre>
 *
 * @author HouziCore Development
 * @since 1.5.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IntegrationSender {

    private static final Pattern FINAL_CLEAR_MESSAGE_PATTERN = Pattern.compile("[\\p{C}\\p{So}\\x{E0100}-\\x{E01EF}]+");

    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;
    private final TaskScheduler taskScheduler;

    /**
     * Sends a message to integrations asynchronously.
     *
     * @param moduleName the type of message being sent
     * @param format the message format string
     * @param eventMetadata the event metadata containing sender and message
     */
    public void asyncSend(ModuleName moduleName, String format, EventMetadata<?> eventMetadata) {
        taskScheduler.runAsync(() -> send(moduleName, format, eventMetadata), true);
    }

    /**
     * Sends a message to integrations
     *
     * @param moduleName the type of message being sent
     * @param format the message format string
     * @param eventMetadata the event metadata containing sender and message
     */
    public void send(ModuleName moduleName, String format, EventMetadata<?> eventMetadata) {
        UnaryOperator<String> integrationOperator = eventMetadata.integration();
        if (integrationOperator == null) return;
        if (!integrationModule.hasMessenger()) return;

        FEntity sender = eventMetadata.sender();
        if (eventMetadata instanceof VanishMetadata<?> vanishMetadata && !vanishMetadata.ignoreVanish() && integrationModule.isVanished(sender)) return;

        String plainFormat = plainSerialize(createFormat(format, eventMetadata));
        String plainMessage = plainSerialize(createMessage(eventMetadata));

        String finalMessage = Strings.CS.replace(
                plainFormat,
                "<message>",
                plainMessage
        );

        UnaryOperator<String> interfaceReplaceString = s -> {
            String input = integrationOperator.apply(s);
            if (StringUtils.isBlank(input)) return StringUtils.EMPTY;

            return StringUtils.replaceEach(
                    plainSerialize(createFormat(input, eventMetadata)),
                    new String[]{"<player>", "<message>", "<final_message>", "<final_clear_message>"},
                    new String[]{sender.name(), plainMessage, finalMessage, clearMessage(finalMessage)}
            );
        };

        for (String specificMessageName : createSpecificMessageNames(moduleName, eventMetadata)) {
            integrationModule.sendMessage(sender, specificMessageName, interfaceReplaceString);
        }

        integrationModule.sendMessage(sender, moduleName.name(), interfaceReplaceString);
    }

    private Component createFormat(String text, EventMetadata<?> eventMetadata) {
        FEntity sender = eventMetadata.sender();
        MessageContext context = messagePipeline.createContext(sender, FPlayer.UNKNOWN, text)
                .withFlags(eventMetadata.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.TRANSLATE_MODULE, MessageFlag.OBJECT_SPRITE_PROCESSING, MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING},
                        new boolean[]{false, false, false}
                )
                .addTagResolvers(eventMetadata.resolveTags(FPlayer.UNKNOWN));

        return messagePipeline.build(context);
    }

    private Component createMessage(EventMetadata<?> eventMetadata) {
        String message = eventMetadata.message();
        if (StringUtils.isEmpty(message)) return Component.empty();

        MessageContext context = messagePipeline.createContext(eventMetadata.sender(), FPlayer.UNKNOWN, message)
                .withFlags(eventMetadata.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.TRANSLATE_MODULE, MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.URL_PROCESSING},
                        new boolean[]{true, false, false, false, false, false}
                );

        return messagePipeline.build(context);
    }

    private String clearMessage(String finalMessage) {
        return RegExUtils.replaceAll(
                (CharSequence) finalMessage,
                FINAL_CLEAR_MESSAGE_PATTERN,
                StringUtils.EMPTY
        );
    }

    private String plainSerialize(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(GlobalTranslator.render(component, Locale.ROOT));
    }

    protected Collection<String> createSpecificMessageNames(ModuleName moduleName, EventMetadata<?> eventMetadata) {
        return Collections.emptyList();
    }

}
