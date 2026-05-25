package com.houzicore.extension.model.event;

import lombok.Builder;
import com.houzicore.extension.config.setting.LocalizationSetting;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.util.Moderation;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Builder
public record UnModerationMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull FPlayer moderator,
        @NonNull List<Moderation> moderations
) implements EventMetadata<L> {
}
