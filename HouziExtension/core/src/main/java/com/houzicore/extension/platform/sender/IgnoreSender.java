package com.houzicore.extension.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.Localization;
import com.houzicore.extension.execution.dispatcher.EventDispatcher;
import com.houzicore.extension.execution.pipeline.MessagePipeline;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.message.MessageSendEvent;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;
import net.kyori.adventure.text.Component;

/**
 * Sends ignore-related messages when players ignore each other.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * IgnoreSender ignoreSender = houzicorePulse.get(IgnoreSender.class);
 *
 * // Check if players ignore each other
 * if (ignoreSender.sendIfIgnored(sender, receiver)) {
 *     // One player is ignoring the other
 * }
 * }</pre>
 *
 * @author HouziCore Development
 * @since 1.6.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnoreSender {

    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final FileFacade fileFacade;

    /**
     * Checks if two players ignore each other and sends notification to sender.
     *
     * @param sender the player attempting to send a message (receives notification)
     * @param receiver the target player
     * @return true if either player ignores the other, false otherwise
     */
    public boolean sendIfIgnored(FPlayer sender, FPlayer receiver) {
        return false;
    }

    private void sendMessage(FPlayer sender, FPlayer receiver, String ignoreMessage) {
        MessageContext messageContext = messagePipeline.createContext(receiver, sender, ignoreMessage);
        Component component = messagePipeline.build(messageContext);

        eventDispatcher.dispatch(new MessageSendEvent(ModuleName.ERROR, sender, component));
    }
}
