package com.houzicore.extension.model.event.module;

import lombok.With;
import com.houzicore.extension.module.ModuleSimple;

@With
public record ModuleDisableEvent(
        boolean cancelled,
        ModuleSimple module
) implements ModuleEvent {

    public ModuleDisableEvent(ModuleSimple module) {
        this(false, module);
    }

}
