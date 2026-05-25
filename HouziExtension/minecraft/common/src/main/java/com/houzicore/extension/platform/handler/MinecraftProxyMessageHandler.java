package com.houzicore.extension.platform.handler;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.houzicore.extension.data.repository.CooldownRepository;
import com.houzicore.extension.execution.dispatcher.MessageDispatcher;
import com.houzicore.extension.execution.pipeline.MessagePipeline;
import com.houzicore.extension.execution.scheduler.TaskScheduler;

import com.houzicore.extension.platform.controller.ModuleController;
import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.service.ModerationService;
import com.houzicore.extension.util.file.FileFacade;
import com.houzicore.extension.util.logging.FLogger;

import java.io.IOException;
import java.util.UUID;

@Singleton
public class MinecraftProxyMessageHandler extends ProxyMessageHandler {

    private final Injector injector;

    @Inject
    public MinecraftProxyMessageHandler(Injector injector,
                                        FileFacade fileFacade,
                                        FPlayerService fPlayerService,
                                        FLogger fLogger,
                                        ModerationService moderationService,
                                        Gson gson,
                                        TaskScheduler taskScheduler,
                                        CooldownRepository cooldownRepository,
                                        MessagePipeline messagePipeline,
                                        MessageDispatcher messageDispatcher,
                                        ModuleController moduleController) {
        super(injector, fileFacade, fPlayerService, fLogger, moderationService, gson, taskScheduler, cooldownRepository, messagePipeline, messageDispatcher, moduleController);

        this.injector = injector;
    }

    @Override
    public void handleSystemOnline(UUID uuid) throws IOException {
        super.handleSystemOnline(uuid);

    }

    @Override
    public void handleSystemOffline(UUID uuid) throws IOException {
        super.handleSystemOffline(uuid);

    }

}
