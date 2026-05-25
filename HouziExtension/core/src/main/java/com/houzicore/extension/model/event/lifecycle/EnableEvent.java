package com.houzicore.extension.model.event.lifecycle;

import lombok.With;
import com.houzicore.extension.HouziExtension;
import com.houzicore.extension.model.event.Event;

@With
public record EnableEvent(
        boolean cancelled,
        HouziExtension houzicorePulse
) implements Event {

    public EnableEvent(HouziExtension houzicorePulse) {
        this(false, houzicorePulse);
    }

}
