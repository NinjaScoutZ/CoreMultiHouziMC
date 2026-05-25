package com.houzicore.extension.model.event;

import com.houzicore.extension.config.setting.LocalizationSetting;
import com.houzicore.extension.config.setting.PermissionSetting;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.util.Destination;
import com.houzicore.extension.model.util.Range;
import com.houzicore.extension.model.util.Sound;
import com.houzicore.extension.util.ProxyDataConsumer;
import com.houzicore.extension.util.SafeDataOutputStream;
import com.houzicore.extension.util.constant.MessageFlag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public record BaseEventMetadata<L extends LocalizationSetting>(
        @NonNull UUID uuid,
        @NonNull FEntity sender,
        @Nullable FPlayer filterPlayer,
        @NonNull Predicate<FPlayer> filter,
        @NonNull Map<MessageFlag, Boolean> flags,
        @NonNull BiFunction<FPlayer, L, String> format,
        @NonNull Destination destination,
        @NonNull Range range,
        @Nullable Pair<Sound, PermissionSetting> sound,
        @Nullable String message,
        @Nullable Function<FPlayer, TagResolver[]> tagResolvers,
        @Nullable ProxyDataConsumer<SafeDataOutputStream> proxy,
        @Nullable UnaryOperator<String> integration
) implements EventMetadata<L> {

    @Override
    public BaseEventMetadata<L> base() {
        return this;
    }

    @Override
    public @Nullable TagResolver[] resolveTags(FPlayer fPlayer) {
        return this.tagResolvers == null ? null : tagResolvers.apply(fPlayer);
    }

    @Override
    public @NonNull String resolveFormat(FPlayer fPlayer, L localization) {
        return StringUtils.defaultString(format.apply(fPlayer, localization));
    }

}
