package com.houzicore.extension.model.event.module;

import lombok.With;
import com.houzicore.extension.module.ModuleSimple;

@With
public record ModuleEnableEvent(
        boolean cancelled,
        ModuleSimple module
) implements ModuleEvent {

    public ModuleEnableEvent(ModuleSimple module) {
        this(false, module);
    }

}
