package com.houzicore.extension.model.event.message;

import lombok.With;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.EventMetadata;
import com.houzicore.extension.util.constant.ModuleName;

@With
public record MessagePrepareEvent(
        boolean cancelled,
        ModuleName moduleName,
        String rawFormat,
        EventMetadata<?> eventMetadata
) implements Event {

    public MessagePrepareEvent(ModuleName moduleName, String rawFormat, EventMetadata<?> eventMetadata) {
        this(false, moduleName, rawFormat, eventMetadata);
    }

}
