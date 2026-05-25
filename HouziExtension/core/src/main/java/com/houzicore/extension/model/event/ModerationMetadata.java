package com.houzicore.extension.model.event;

import lombok.Builder;
import com.houzicore.extension.config.setting.LocalizationSetting;
import com.houzicore.extension.model.util.Moderation;
import org.jspecify.annotations.NonNull;

@Builder
public record ModerationMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull Moderation moderation
) implements EventMetadata<L> {
}
