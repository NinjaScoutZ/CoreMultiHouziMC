package com.houzicore.extension.module.message.format.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.annotation.Pulse;
import com.houzicore.extension.listener.PulseListener;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.message.MessageFormattingEvent;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.module.message.format.FormatModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FormatPulseListener implements PulseListener {

    private final FormatModule formatModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = formatModule.addTags(event.context());

        return event.withContext(messageContext);
    }
}
