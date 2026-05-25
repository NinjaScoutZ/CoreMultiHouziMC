package com.houzicore.extension.module.message;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.Message;
import com.houzicore.extension.config.Permission;
import com.houzicore.extension.module.ModuleSimple;
import com.houzicore.extension.module.message.bubble.BubbleModule;
import com.houzicore.extension.module.message.format.FormatModule;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageModule implements ModuleSimple {

    private final FileFacade fileFacade;

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return ModuleSimple.super.childrenBuilder().add(
                BubbleModule.class,
                FormatModule.class
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE;
    }

    @Override
    public Message config() {
        return fileFacade.message();
    }

    @Override
    public Permission.Message permission() {
        return fileFacade.permission().message();
    }

}
