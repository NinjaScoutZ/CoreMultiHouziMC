package com.houzicore.extension.model.event.module;

import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.module.ModuleSimple;

public interface ModuleEvent extends Event {

    ModuleSimple module();

    ModuleEvent withModule(ModuleSimple module);

}
