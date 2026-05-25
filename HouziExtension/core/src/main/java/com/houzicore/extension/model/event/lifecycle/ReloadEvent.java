package com.houzicore.extension.model.event.lifecycle;

import lombok.With;
import com.houzicore.extension.HouziExtension;
import com.houzicore.extension.exception.ReloadException;
import com.houzicore.extension.model.event.Event;

@With
public record ReloadEvent(
        boolean cancelled,
        HouziExtension houzicorePulse,
        ReloadException reloadException
) implements Event {

    public ReloadEvent(HouziExtension houzicorePulse, ReloadException reloadException) {
        this(false, houzicorePulse, reloadException);
    }

    public boolean isSuccessful() {
        return reloadException == null;
    }

}
