package com.houzicore.extension.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.execution.dispatcher.EventDispatcher;
import com.houzicore.extension.execution.pipeline.MessagePipeline;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.message.MessageSendEvent;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.platform.formatter.ModerationMessageFormatter;
import com.houzicore.extension.util.checker.MuteChecker;
import com.houzicore.extension.util.constant.ModuleName;
import net.kyori.adventure.text.Component;

import java.util.Optional;

/**
 * Sends mute notifications to players when they attempt to chat while muted.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * MuteSender muteSender = houzicorePulse.get(MuteSender.class);
 *
 * // Check if player is muted and send notification
 * if (muteSender.sendIfMuted(player)) {
 *     // Player is muted, message sent
 * }
 * }</pre>
 *
 * @author HouziCore Development
 * @since 1.6.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MuteSender {

    private final MuteChecker muteChecker;
    private final MessagePipeline messagePipeline;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final EventDispatcher eventDispatcher;

    /**
     * Checks if a player is muted and sends a mute notification.
     * Only sends messages to players, not other entities.
     *
     * @param entity the entity to check
     * @return true if player is muted and notification was sent, false otherwise
     */
    public boolean sendIfMuted(FEntity entity) {
        // skip message for entity
        if (!(entity instanceof FPlayer fPlayer)) return false;

        MuteChecker.Status status = muteChecker.check(fPlayer);
        if (status == MuteChecker.Status.NONE) return false;

        Optional<MessageContext> muteContext = moderationMessageFormatter.createMuteContext(fPlayer, status);
        if (muteContext.isEmpty()) return false;

        Component component = messagePipeline.build(muteContext.get());

        eventDispatcher.dispatch(new MessageSendEvent(ModuleName.ERROR, fPlayer, component));

        return true;
    }

}
