package com.houzicore.extension.module.integration;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.util.ExternalModeration;
import com.houzicore.extension.module.ModuleSimple;
import com.houzicore.extension.module.integration.placeholderapi.PlaceholderAPIModule;
import com.houzicore.extension.platform.adapter.PlatformServerAdapter;
import com.houzicore.extension.platform.controller.ModuleController;
import com.houzicore.extension.processing.resolver.ReflectionResolver;
import com.houzicore.extension.util.file.FileFacade;
import com.houzicore.extension.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permissible;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Set;

@Singleton
public class BukkitIntegrationModule extends MinecraftIntegrationModule {

    private final PlatformServerAdapter platformServerAdapter;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final FLogger fLogger;

    @Inject
    public BukkitIntegrationModule(FileFacade fileFacade,
                                   FLogger fLogger,
                                   PlatformServerAdapter platformServerAdapter,
                                   ReflectionResolver reflectionResolver,
                                   ModuleController moduleController,
                                   Injector injector) {
        super(fileFacade, fLogger, platformServerAdapter, reflectionResolver, moduleController, injector);
        
        this.platformServerAdapter = platformServerAdapter;
        this.reflectionResolver = reflectionResolver;
        this.moduleController = moduleController;
        this.fLogger = fLogger;
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> builder = super.childrenBuilder();

        if (platformServerAdapter.hasProject("PlaceholderAPI")) {
            builder.add(PlaceholderAPIModule.class);
        }

        return builder;
    }

    @Override
    public String checkMention(FEntity fSender, String message) {
        return message;
    }

    @Override
    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        return super.hasFPlayerPermission(fPlayer, permission);
    }

    @Override
    public String getPrefix(FPlayer fPlayer) {
        String prefix = super.getPrefix(fPlayer);
        if (prefix != null) return prefix;

        try {
            Class<?> chatClass = Class.forName("com.houzicore.shared.core.chat.Chat");
            Player player = Bukkit.getPlayer(fPlayer.uuid());
            if (player != null) {
                java.lang.reflect.Method getPrefixMethod = chatClass.getMethod("getExtChatPrefix", Player.class);
                Object result = getPrefixMethod.invoke(null, player);
                if (result != null && !result.toString().isEmpty()) {
                    return result.toString();
                }
            }
        } catch (Exception ignored) {
            // HouziCore not found or method unavailable
        }

        return null;
    }

    @Override
    public String getSuffix(FPlayer fPlayer) {
        String suffix = super.getSuffix(fPlayer);
        if (suffix != null) return suffix;

        return null;
    }

    @Override
    public Set<String> getGroups() {
        Set<String> groups = super.getGroups();
        if (!groups.isEmpty()) return groups;

        return Collections.emptySet();
    }

    @Override
    public boolean isVanished(FEntity sender) {
        Player player = Bukkit.getPlayer(sender.uuid());
        if (player == null) return false;

        return player.getMetadata("vanished")
                .stream()
                .anyMatch(MetadataValue::asBoolean);
    }

    @Override
    public boolean hasSeeVanishPermission(FEntity sender) {
        Player player = Bukkit.getPlayer(sender.uuid());
        if (player == null) return false;

        return player.hasPermission("sv.see") || player.hasPermission("cmi.seevanished");
    }

    @Override
    public boolean isMuted(FPlayer fPlayer) {
        return false;
    }

    @Override
    public ExternalModeration getMute(FPlayer fPlayer) {
        return null;
    }

    @Override
    public String getTritonLocale(FPlayer fPlayer) {
        return null;
    }

    @Override
    public boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message) {
        return false;
    }
}
