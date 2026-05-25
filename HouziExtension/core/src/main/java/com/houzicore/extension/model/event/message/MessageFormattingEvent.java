package com.houzicore.extension.model.event.message;

import lombok.With;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.message.context.MessageContext;

@With
public record MessageFormattingEvent(
        boolean cancelled,
        MessageContext context
) implements Event {

    public MessageFormattingEvent(MessageContext context) {
        this(false, context);
    }

    public MessageFormattingEvent withContext(MessageContext context) {
        return this.context == context ? this : new MessageFormattingEvent(this.cancelled, context);
    }

}
