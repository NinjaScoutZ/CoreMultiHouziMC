package com.houzicore.extension.module.message.bubble;

import com.houzicore.extension.config.Message;
import com.houzicore.extension.config.Permission;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.module.ModuleSimple;
import com.houzicore.extension.module.message.bubble.listener.BubblePulseListener;
import com.houzicore.extension.module.message.bubble.service.BubbleService;
import com.houzicore.extension.platform.controller.ModuleController;
import com.houzicore.extension.platform.registry.ListenerRegistry;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.List;

public abstract class BubbleModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final BubbleService bubbleService;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;

    protected BubbleModule(FileFacade fileFacade,
                           TaskScheduler taskScheduler,
                           BubbleService bubbleService,
                           ListenerRegistry listenerRegistry,
                           ModuleController moduleController) {
        this.fileFacade = fileFacade;
        this.taskScheduler = taskScheduler;
        this.bubbleService = bubbleService;
        this.listenerRegistry = listenerRegistry;
        this.moduleController = moduleController;
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_BUBBLE;
    }

    @Override
    public Message.Bubble config() {
        return fileFacade.message().bubble();
    }

    @Override
    public Permission.Message.Bubble permission() {
        return fileFacade.permission().message().bubble();
    }

    @Override
    public void onEnable() {
        bubbleService.startTicker();

        listenerRegistry.register(BubblePulseListener.class);
    }

    @Override
    public void onDisable() {
        bubbleService.clear();
    }

    public void add(@NonNull FPlayer fPlayer, @NonNull String inputString, List<FPlayer> receivers) {
        taskScheduler.runRegion(fPlayer, () -> {
            if (moduleController.isDisabledFor(this, fPlayer)) return;

            bubbleService.addMessage(fPlayer, inputString, receivers);
        });
    }

    public enum Billboard {

        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER

    }
}
