package com.houzicore.extension.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.Localization;
import com.houzicore.extension.execution.dispatcher.EventDispatcher;
import com.houzicore.extension.execution.pipeline.MessagePipeline;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.message.MessageSendEvent;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;
import net.kyori.adventure.text.Component;

/**
 * Sends disable messages when chat features are disabled for players.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * DisableSender disableSender = houzicorePulse.get(DisableSender.class);
 *
 * // Check if private messaging is disabled for receiver
 * if (disableSender.sendIfDisabled(sender, receiver, MessageType.COMMAND_ME)) {
 *     // Private messaging is disabled for receiver
 * }
 * }</pre>
 *
 * @author HouziCore Development
 * @since 1.6.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DisableSender {

    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final FileFacade fileFacade;

    /**
     * Checks if a message type is disabled for a receiver and sends appropriate message.
     *
     * @param entity the entity sending the message
     * @param receiver the entity receiving the message
     * @param moduleName the type of message being sent
     * @return true if message type is disabled for receiver, false otherwise
     */
    public boolean sendIfDisabled(FEntity entity, FEntity receiver, ModuleName moduleName) {
        if (!(receiver instanceof FPlayer fReceiver)) return false;
        if (fReceiver.isUnknown()) return false;
        if (fReceiver.isSetting(moduleName)) return false;

        // skip message for entities
        if (!(entity instanceof FPlayer fPlayer)) return true;

        Localization.Command.Chatsetting localization = fileFacade.localization(fReceiver).command().chatsetting();

        String disableMessage = fPlayer.equals(fReceiver)
                ? localization.disabledSelf()
                : localization.disabledOther();

        MessageContext messageContext = messagePipeline.createContext(receiver, fPlayer, disableMessage);
        Component component = messagePipeline.build(messageContext);

        eventDispatcher.dispatch(new MessageSendEvent(ModuleName.ERROR, fPlayer, component));

        return true;
    }

}
