package com.houzicore.extension.module.message.format.replacement.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.annotation.Pulse;
import com.houzicore.extension.listener.PulseListener;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.message.MessageFormattingEvent;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.module.message.format.replacement.ReplacementModule;
import com.houzicore.extension.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReplacementPulseListener implements PulseListener {

    private final ReplacementModule replacementModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.REPLACEMENT_MODULE)) return event;

        messageContext = replacementModule.format(messageContext);
        messageContext = replacementModule.addTags(messageContext);
        return event.withContext(messageContext);
    }

}
