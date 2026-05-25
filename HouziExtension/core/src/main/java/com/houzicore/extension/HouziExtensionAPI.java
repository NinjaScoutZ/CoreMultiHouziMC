package com.houzicore.extension;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.SneakyThrows;
import com.houzicore.extension.data.database.Database;
import com.houzicore.extension.exception.ReloadException;
import com.houzicore.extension.execution.dispatcher.EventDispatcher;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.lifecycle.DisableEvent;
import com.houzicore.extension.model.event.lifecycle.EnableEvent;
import com.houzicore.extension.model.event.lifecycle.ReloadEvent;
import com.houzicore.extension.model.event.player.PlayerLoadEvent;
import com.houzicore.extension.platform.controller.ModuleController;
import com.houzicore.extension.platform.registry.*;
import com.houzicore.extension.platform.render.TextScreenRender;
import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.service.MetricsService;
import com.houzicore.extension.service.ModerationService;
import com.houzicore.extension.service.TranslationService;
import com.houzicore.extension.util.file.FileFacade;
import com.houzicore.extension.util.logging.FLogger;
import com.houzicore.extension.util.logging.filter.LogFilter;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * API entry point for HouziExtension plugin integration.
 * Provides static access to the main {@link HouziExtension} instance and lifecycle management.
 *
 * @see HouziExtension
 * @since 0.1.0
 */
@Singleton
public class HouziExtensionAPI {

    /**
     * The main instance of the HouziExtension.
     * Provides access to dependency injection and functionality.
     *
     * @see HouziExtension
     */
    @Getter private static HouziExtension instance;

    /**
     * Constructs the API wrapper with dependency injection.
     * This constructor is called internally by Google Guice.
     *
     * @param instance the main HouziExtension implementation instance
     */
    @Inject
    public HouziExtensionAPI(HouziExtension instance) {
        HouziExtensionAPI.instance = instance;
    }

    /**
     * Initializes and enables the HouziExtension .
     * Called automatically by the platform on enable.
     *
     * @throws IllegalStateException if called when is already enabled
     */
    @SneakyThrows
    public void onEnable() {
        if (!instance.isReady()) return;

        // get configs
        FileFacade fileFacade = instance.get(FileFacade.class);

        // get fLogger
        FLogger fLogger = instance.get(FLogger.class);

        // log plugin information
        fLogger.logDescription();

        // load platform localizations
        instance.get(TranslationService.class).reload();

        // init command registry
        instance.get(CommandRegistry.class).init();

        // register default listeners
        instance.get(ListenerRegistry.class).registerDefaultListeners();

        // setup filter
        instance.get(LogFilter.class).setFilters(fileFacade.config().logger().filter());

        // test database connection
        instance.get(Database.class).connect();

        // initialize packetevents
        instance.initPacketAdapter();

        // reload modules and their children
        instance.get(ModuleController.class).reload();

        // reload fplayer service
        instance.get(FPlayerService.class).reload();

        // enable proxy registry
        instance.get(ProxyRegistry.class).onEnable();

        // reload metrics service if enabled
        if (fileFacade.config().metrics().enable()) {
            instance.get(MetricsService.class).reload();
        }

        // call enable event
        instance.get(EventDispatcher.class).dispatch(new EnableEvent(instance));

        // log plugin enabled
        fLogger.logEnabled();
    }

    /**
     * Shuts down the HouziExtension .
     * Called automatically by the platform on disable.
     */
    public void onDisable() {
        instance.terminateFailedPacketAdapter();

        if (!instance.isReady()) return;

        FLogger fLogger = instance.get(FLogger.class);

        // log plugin disabling
        fLogger.logDisabling();

        // call disable event
        instance.get(EventDispatcher.class).dispatch(new DisableEvent(instance));

        // disable task scheduler
        instance.get(TaskScheduler.class).shutdown();

        // close all open inventories
        instance.closeUIs();

        // get fplayer service
        FPlayerService fPlayerService = instance.get(FPlayerService.class);

        // update and clear all fplayers
        fPlayerService.getOnlineFPlayers().forEach(fPlayerService::clearAndSave);
        fPlayerService.clear();

        // disable all modules
        instance.get(ModuleController.class).terminate();

        // unregister all listeners
        instance.get(ListenerRegistry.class).unregisterAll();

        // terminate packetevents
        instance.terminatePacketAdapter();

        // disable proxy registry
        instance.get(ProxyRegistry.class).onDisable();

        // disconnect from database
        instance.get(Database.class).disconnect();

        // log plugin disabled
        fLogger.logDisabled();
    }

    /**
     * Reloads the plugin configuration and modules at runtime.
     *
     * @throws ReloadException if any error occurs during reload process
     */
    public void reload() throws ReloadException {
        if (!instance.isReady()) return;

        ReloadException reloadException = null;

        FLogger fLogger = instance.get(FLogger.class);

        // log plugin reloading
        fLogger.logReloading();

        // close all UIs
        instance.closeUIs();

        // reload ListenerRegistry and save reloadListeners to call them later
        ListenerRegistry listenerRegistry = instance.get(ListenerRegistry.class);

        Map<Event.Priority, List<UnaryOperator<Event>>> reloadListeners = listenerRegistry.getPulseListeners(ReloadEvent.class);

        listenerRegistry.reload();

        // reload task scheduler
        instance.get(TaskScheduler.class).reload();

        // get file resolver for configuration
        FileFacade fileFacade = instance.get(FileFacade.class);

        // invalidate cache
        instance.get(CacheRegistry.class).invalidate();

        // get database
        Database database = instance.get(Database.class);

        // save old database type
        Database.Type oldDatabaseType = database.config().type();

        try {
            // reload configuration files
            fileFacade.reload();
        } catch (Exception e) {
            reloadException = new ReloadException(e);
        }

        // load minecraft localizations
        instance.get(TranslationService.class).reload();

        // reload registries
        instance.get(CommandRegistry.class).reload();
        instance.get(PermissionRegistry.class).reload();
        instance.get(ProxyRegistry.class).reload();

        // reload logger filters
        instance.get(LogFilter.class).setFilters(fileFacade.config().logger().filter());

        // terminate database
        database.disconnect();

        // test new database connection
        try {
            database.connect();
        } catch (Exception e) {
            if (reloadException == null) {
                reloadException = new ReloadException(e);
            }

            // try to connect to old database
            if (database.config().type() != oldDatabaseType) {
                fileFacade.updateFilePack(filePack ->
                        filePack.withConfig(
                                filePack.config().withDatabase(
                                        filePack.config().database().withType(oldDatabaseType)
                                )
                        )
                );

                try {
                    database.connect();
                } catch (Exception ignored) {
                    throw reloadException;
                }
            }
        }

        // get fplayer service
        FPlayerService fPlayerService = instance.get(FPlayerService.class);

        // reload fplayer service
        fPlayerService.reload();

        // reload moderation service
        instance.get(ModerationService.class).reload();

        // reload modules and their children
        instance.get(ModuleController.class).reload();

        // clear text screens
        instance.get(TextScreenRender.class).clear();

        // process player load event for all platform fplayers
        EventDispatcher eventDispatcher = instance.get(EventDispatcher.class);
        fPlayerService.getPlatformFPlayers().forEach(fPlayer ->
                eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer, true))
        );

        // reload metrics service if enabled
        if (fileFacade.config().metrics().enable()) {
            instance.get(MetricsService.class).reload();
        }

        // call reload event
        eventDispatcher.dispatch(reloadListeners, new ReloadEvent(instance, reloadException));

        // log plugin reloaded
        fLogger.logReloaded();

        // throw reload exception if occurred
        if (reloadException != null) {
            throw reloadException;
        }
    }

}
