package com.houzicore.extension.module.integration;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.houzicore.extension.config.Integration;
import com.houzicore.extension.config.Permission;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.util.ExternalModeration;
import com.houzicore.extension.module.ModuleSimple;

import com.houzicore.extension.platform.adapter.PlatformServerAdapter;
import com.houzicore.extension.platform.controller.ModuleController;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.function.UnaryOperator;

public abstract class IntegrationModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final PlatformServerAdapter platformServerAdapter;
    private final ModuleController moduleController;
    private final Injector injector;

    protected IntegrationModule(FileFacade fileFacade,
                                PlatformServerAdapter platformServerAdapter,
                                ModuleController moduleController,
                                Injector injector) {
        this.fileFacade = fileFacade;
        this.platformServerAdapter = platformServerAdapter;
        this.moduleController = moduleController;
        this.injector = injector;
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return ModuleSimple.super.childrenBuilder();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION;
    }

    @Override
    public Integration config() {
        return fileFacade.integration();
    }

    @Override
    public Permission.Integration permission() {
        return fileFacade.permission().integration();
    }

    public abstract String checkMention(FEntity fPlayer, String message);

    public abstract boolean isVanished(FEntity sender);

    public abstract boolean hasSeeVanishPermission(FEntity sender);

    public abstract boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message);

    public abstract boolean isMuted(FPlayer fPlayer);

    public abstract boolean isBedrockPlayer(FEntity fPlayer);

    public abstract ExternalModeration getMute(FPlayer fPlayer);

    public abstract String getTritonLocale(FPlayer fPlayer);

    public boolean containsEnabledChild(Class<? extends ModuleSimple> clazz) {
        return false;
    }

    public <T> T getInstance(Class<T> clazz) {
        return null; // Deleted
    }

    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        return false;
    }

    public String getPrefix(FPlayer fPlayer) {
        return null;
    }

    public String getSuffix(FPlayer fPlayer) {
        return null;
    }

    public Set<String> getGroups() {
        return Collections.emptySet();
    }

    public int getGroupWeight(FPlayer fPlayer) {
        return 0;
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> discordString) {
        // Deleted
    }

    public boolean hasMessenger() {
        return false;
    }

    public boolean canSeeVanished(FEntity fTarget, FEntity fViewer) {
        if (fTarget.equals(fViewer)) return true;

        boolean isVanished = isVanished(fTarget);
        return !isVanished || hasSeeVanishPermission(fViewer);
    }

    public String deeplTranslate(FPlayer sender, String source, String target, String text) {
        return text;
    }

    public String yandexTranslate(FPlayer sender, String source, String target, String text) {
        return text;
    }
}
