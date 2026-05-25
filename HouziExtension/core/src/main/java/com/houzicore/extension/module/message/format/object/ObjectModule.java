package com.houzicore.extension.module.message.format.object;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.Localization;
import com.houzicore.extension.config.Message;
import com.houzicore.extension.config.Permission;
import com.houzicore.extension.config.setting.PermissionSetting;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.module.ModuleLocalization;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ObjectModule implements ModuleLocalization<Localization.Message.Format.Object> {

    private final FileFacade fileFacade;

    @Override
    public Localization.Message.Format.Object localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().object();
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder()
                .add(permission().playerHeadTag(), permission().spriteTag(), permission().textureTag());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_OBJECT;
    }

    @Override
    public Message.Format.Object config() {
        return fileFacade.message().format().object();
    }

    @Override
    public Permission.Message.Format.Object permission() {
        return fileFacade.permission().message().format().object();
    }

}
