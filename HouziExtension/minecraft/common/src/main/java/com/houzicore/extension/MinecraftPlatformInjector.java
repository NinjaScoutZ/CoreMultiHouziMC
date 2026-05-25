package com.houzicore.extension;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.gson.Gson;
import com.google.inject.name.Names;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import com.houzicore.extension.data.database.Database;
import com.houzicore.extension.data.database.MinecraftDatabase;

import com.houzicore.extension.module.integration.IntegrationModule;
import com.houzicore.extension.module.integration.MinecraftIntegrationModule;

import com.houzicore.extension.module.message.bubble.BubbleModule;
import com.houzicore.extension.module.message.bubble.MinecraftBubbleModule;
import com.houzicore.extension.module.message.bubble.render.BubbleRender;
import com.houzicore.extension.module.message.bubble.render.MinecraftBubbleRender;

import com.houzicore.extension.module.message.format.object.MinecraftObjectModule;
import com.houzicore.extension.module.message.format.object.ObjectModule;


import com.houzicore.extension.platform.handler.MinecraftProxyMessageHandler;
import com.houzicore.extension.platform.handler.ProxyMessageHandler;
import com.houzicore.extension.platform.registry.ListenerRegistry;
import com.houzicore.extension.platform.registry.MinecraftListenerRegistry;
import com.houzicore.extension.platform.render.*;
import com.houzicore.extension.platform.sender.MessageSender;
import com.houzicore.extension.platform.sender.MinecraftMessageSender;
import com.houzicore.extension.platform.sender.MinecraftSoundPlayer;
import com.houzicore.extension.platform.sender.SoundPlayer;
import com.houzicore.extension.processing.resolver.LibraryResolver;
import com.houzicore.extension.processing.resolver.ReflectionResolver;
import com.houzicore.extension.service.MinecraftSkinService;
import com.houzicore.extension.service.MinecraftTranslationService;
import com.houzicore.extension.service.SkinService;
import com.houzicore.extension.service.TranslationService;
import com.houzicore.extension.util.logging.FLogger;

import java.nio.file.Path;

public abstract class MinecraftPlatformInjector extends PlatformInjector {

    protected MinecraftPlatformInjector(Path projectPath,
                                        LibraryResolver libraryResolver,
                                        FLogger fLogger) {
        super(projectPath, libraryResolver, fLogger);
    }

    @Override
    public void setupPlatform(ReflectionResolver reflectionResolver) {
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());

        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_14")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_16")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_19_4")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_4));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_21_6")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_6));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_21_9")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9));

        // database
        bind(Database.class).to(MinecraftDatabase.class);



        // integrations
        bind(IntegrationModule.class).to(MinecraftIntegrationModule.class);

        // messages

        bind(BubbleModule.class).to(MinecraftBubbleModule.class);

        bind(ObjectModule.class).to(MinecraftObjectModule.class);


        // registers
        bind(ListenerRegistry.class).to(MinecraftListenerRegistry.class);

        // renders
        bind(ActionBarRender.class).to(MinecraftActionBarRender.class);
        bind(BossBarRender.class).to(MinecraftBossBarRender.class);
        bind(BrandRender.class).to(MinecraftBrandRender.class);
        bind(ListFooterRender.class).to(MinecraftListFooterRender.class);
        bind(TextScreenRender.class).to(MinecraftTextScreenRender.class);
        bind(TitleRender.class).to(MinecraftTitleRender.class);
        bind(ToastRender.class).to(MinecraftToastRender.class);
        bind(BubbleRender.class).to(MinecraftBubbleRender.class);

        // senders
        bind(MessageSender.class).to(MinecraftMessageSender.class);
        bind(SoundPlayer.class).to(MinecraftSoundPlayer.class);

        // others
        bind(SkinService.class).to(MinecraftSkinService.class);
        bind(TranslationService.class).to(MinecraftTranslationService.class);
        bind(ProxyMessageHandler.class).to(MinecraftProxyMessageHandler.class);
    }

}
