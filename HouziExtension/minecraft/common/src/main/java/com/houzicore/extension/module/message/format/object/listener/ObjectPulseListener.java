package com.houzicore.extension.module.message.format.object.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.annotation.Pulse;
import com.houzicore.extension.listener.PulseListener;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.message.MessageFormattingEvent;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.module.message.format.object.MinecraftObjectModule;
import com.houzicore.extension.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ObjectPulseListener implements PulseListener {

    private final MinecraftObjectModule objectModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (messageContext.isFlag(MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING)) {
            messageContext = objectModule.addPlayerHeadTag(messageContext);
        }

        if (messageContext.isFlag(MessageFlag.OBJECT_SPRITE_PROCESSING)) {
            messageContext = objectModule.addSpriteTag(messageContext);
        }

        if (messageContext.isFlag(MessageFlag.OBJECT_TEXTURE_PROCESSING)) {
            messageContext = objectModule.addTextureTag(messageContext);
        }

        return event.withContext(messageContext);
    }

}
