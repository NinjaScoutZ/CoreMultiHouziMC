package com.houzicore.extension.model.event;

import com.houzicore.extension.config.setting.LocalizationSetting;

public interface VanishMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    boolean ignoreVanish();

}
