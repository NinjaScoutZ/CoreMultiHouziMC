package com.houzicore.extension.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.module.message.bubble.listener.BubblePacketListener;
import com.houzicore.extension.module.message.bubble.service.BubbleService;
import com.houzicore.extension.platform.controller.ModuleController;
import com.houzicore.extension.platform.registry.ListenerRegistry;
import com.houzicore.extension.util.file.FileFacade;

@Singleton
public class MinecraftBubbleModule extends BubbleModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftBubbleModule(FileFacade fileFacade,
                                 TaskScheduler taskScheduler,
                                 BubbleService bubbleService,
                                 ListenerRegistry listenerRegistry,
                                 ModuleController moduleController) {
        super(fileFacade, taskScheduler, bubbleService, listenerRegistry, moduleController);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(BubblePacketListener.class);
    }

}
