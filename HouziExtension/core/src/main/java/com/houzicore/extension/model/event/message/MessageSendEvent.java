package com.houzicore.extension.model.event.message;

import lombok.With;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.EventMetadata;
import com.houzicore.extension.util.constant.ModuleName;
import net.kyori.adventure.text.Component;

@With
public record MessageSendEvent(
        boolean cancelled,
        ModuleName moduleName,
        FEntity sender,
        FPlayer receiver,
        Component message,
        Component submessage,
        EventMetadata<?> eventMetadata
) implements Event {

    public MessageSendEvent(ModuleName moduleName,
                            FPlayer receiver,
                            Component message,
                            Component submessage,
                            EventMetadata<?> eventMetadata) {
        this(false, moduleName, eventMetadata.sender(), receiver, message, submessage, eventMetadata);
    }

    public MessageSendEvent(ModuleName moduleName,
                            FPlayer sender,
                            Component message) {
        this(moduleName, sender, message, Component.empty(), EventMetadata.builder().sender(sender).format("").build());
    }

}
