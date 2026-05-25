package com.houzicore.extension.module.message.format.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.annotation.Pulse;
import com.houzicore.extension.listener.PulseListener;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.message.MessageFormattingEvent;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.module.message.format.convertor.LegacyColorConvertor;
import com.houzicore.extension.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LegacyColorPulseListener implements PulseListener {

    private final LegacyColorConvertor legacyColorConvertor;

    @Pulse(priority = Event.Priority.HIGHEST)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.LEGACY_COLOR_CONVERSION)) return event;

        return event.withContext(legacyColorConvertor.convert(messageContext));
    }
}
