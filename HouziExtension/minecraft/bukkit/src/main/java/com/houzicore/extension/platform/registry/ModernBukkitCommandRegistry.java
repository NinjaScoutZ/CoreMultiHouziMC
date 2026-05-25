package com.houzicore.extension.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.platform.handler.CommandExceptionHandler;
import com.houzicore.extension.processing.mapper.FPlayerMapper;
import com.houzicore.extension.processing.resolver.ReflectionResolver;
import com.houzicore.extension.util.file.FileFacade;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;

@Singleton
public class ModernBukkitCommandRegistry extends LegacyBukkitCommandRegistry implements BrigadierCommandRegistry {

    @Inject
    public ModernBukkitCommandRegistry(FileFacade fileFacade,
                                       ReflectionResolver reflectionResolver,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       TaskScheduler taskScheduler,
                                       FPlayerMapper fPlayerMapper) {
        super(fileFacade, commandExceptionHandler, plugin, reflectionResolver, taskScheduler, fPlayerMapper);
    }

    @Override
    public void init() {
        super.init();

        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
            setupBrigadierManager(manager.brigadierManager());
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
    }
}
