package com.houzicore.extension;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.google.inject.Singleton;
import com.houzicore.extension.execution.scheduler.BukkitTaskScheduler;
import com.houzicore.extension.execution.scheduler.TaskScheduler;

import com.houzicore.extension.module.integration.BukkitIntegrationModule;
import com.houzicore.extension.module.integration.MinecraftIntegrationModule;


import com.houzicore.extension.platform.adapter.BukkitPlayerAdapter;
import com.houzicore.extension.platform.adapter.BukkitServerAdapter;
import com.houzicore.extension.platform.adapter.PlatformPlayerAdapter;
import com.houzicore.extension.platform.adapter.PlatformServerAdapter;
import com.houzicore.extension.platform.provider.*;
import com.houzicore.extension.platform.registry.*;
import com.houzicore.extension.platform.sender.BukkitMessageSender;
import com.houzicore.extension.platform.sender.MinecraftMessageSender;
import com.houzicore.extension.processing.resolver.LibraryResolver;
import com.houzicore.extension.processing.resolver.ReflectionResolver;
import com.houzicore.extension.util.checker.BukkitPermissionChecker;
import com.houzicore.extension.util.checker.PermissionChecker;
import com.houzicore.extension.util.logging.FLogger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitInjector extends MinecraftPlatformInjector {

    private final BukkitHouziExtension instance;
    private final Plugin plugin;

    public BukkitInjector(BukkitHouziExtension instance,
                          Plugin plugin,
                          LibraryResolver libraryResolver,
                          FLogger fLogger) {
        super(plugin.getDataFolder().toPath(), libraryResolver, fLogger);

        this.instance = instance;
        this.plugin = plugin;
    }

    @Override
    public void setupPlatform(ReflectionResolver reflectionResolver) {
        super.setupPlatform(reflectionResolver);

        bind(HouziExtension.class).toInstance(instance);
        bind(BukkitHouziExtension.class).toInstance(instance);
        bind(Plugin.class).toInstance(plugin);

        // Adapters
        bind(PlatformPlayerAdapter.class).to(BukkitPlayerAdapter.class);
        bind(PlatformServerAdapter.class).to(BukkitServerAdapter.class);

        // Providers
        if (reflectionResolver.hasClass("org.bukkit.attribute.Attribute")) {
            bind(AttributesProvider.class).to(ModernAttributesProvider.class);
        } else {
            bind(AttributesProvider.class).to(LegacyAttributesProvider.class);
        }

        if (reflectionResolver.hasMethod(Player.class, "getPassengers")) {
            bind(PassengersProvider.class).to(ModernPassengersProvider.class);
        } else {
            bind(PassengersProvider.class).to(LegacyPassengersProvider.class);
        }

        // Registries
        bind(PermissionRegistry.class).to(BukkitPermissionRegistry.class);
        bind(MinecraftListenerRegistry.class).to(BukkitListenerRegistry.class);
        bind(ProxyRegistry.class).to(BukkitProxyRegistry.class);

        if (reflectionResolver.hasClass("com.mojang.brigadier.arguments.ArgumentType")) {
            bind(CommandRegistry.class).to(ModernBukkitCommandRegistry.class);
        } else {
            bind(CommandRegistry.class).to(LegacyBukkitCommandRegistry.class);
        }

        // Checkers and utilities
        bind(PermissionChecker.class).to(BukkitPermissionChecker.class);
        bind(TaskScheduler.class).to(BukkitTaskScheduler.class);

        // Modules
        bind(MinecraftIntegrationModule.class).to(BukkitIntegrationModule.class);



        if (reflectionResolver.isPaper() && reflectionResolver.hasClass("com.", "github.retrooper.packetevents.util.adventure.AdventureSerializer")) {
            bind(MinecraftMessageSender.class).to(BukkitMessageSender.class);
        }



        // Scheduler
        bind(com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler.class)
                .toInstance(UniversalScheduler.getScheduler(plugin));
    }
}
