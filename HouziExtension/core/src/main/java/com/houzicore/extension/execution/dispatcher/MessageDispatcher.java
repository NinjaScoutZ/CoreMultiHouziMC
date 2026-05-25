package com.houzicore.extension.execution.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.setting.LocalizationSetting;
import com.houzicore.extension.execution.pipeline.MessagePipeline;
import com.houzicore.extension.execution.scheduler.SchedulerRunnable;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.EventMetadata;
import com.houzicore.extension.model.event.message.MessagePrepareEvent;
import com.houzicore.extension.model.event.message.MessageSendEvent;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.model.util.Destination;
import com.houzicore.extension.module.ModuleLocalization;

import com.houzicore.extension.platform.filter.RangeFilter;
import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.util.constant.MessageFlag;
import com.houzicore.extension.util.constant.ModuleName;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageDispatcher {

    private final FPlayerService fPlayerService;
    private final RangeFilter rangeFilter;
    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;

    public <L extends LocalizationSetting> List<FPlayer> createReceivers(ModuleLocalization<L> module,
                                                                         EventMetadata<L> eventMetadata) {
        return createReceivers(module.name(), module, eventMetadata);
    }

    public <L extends LocalizationSetting> List<FPlayer> createReceivers(ModuleName moduleName,
                                                                         ModuleLocalization<L> module,
                                                                         EventMetadata<L> eventMetadata) {
        String rawFormat = eventMetadata.resolveFormat(FPlayer.UNKNOWN, module.localization());

        MessagePrepareEvent messagePrepareEvent = eventDispatcher.dispatch(new MessagePrepareEvent(moduleName, rawFormat, eventMetadata));

        // if canceled, it means that message was sent to Proxy
        if (messagePrepareEvent.cancelled()) return Collections.emptyList();

        return fPlayerService.getFPlayersWithConsole().stream()
                .filter(eventMetadata.filter())
                .filter(rangeFilter.createFilter(eventMetadata.filterPlayer(), eventMetadata.range()))
                .filter(fReceiver -> fReceiver.isSetting(moduleName))
                .toList();
    }

    public <L extends LocalizationSetting> void dispatch(ModuleLocalization<L> module,
                                                         EventMetadata<L> eventMetadata) {
        dispatch(module.name(), module, eventMetadata);
    }

    public <L extends LocalizationSetting> void dispatch(ModuleName moduleName,
                                                         ModuleLocalization<L> module,
                                                         EventMetadata<L> eventMetadata) {
        List<FPlayer> receivers = createReceivers(moduleName, module, eventMetadata);
        dispatch(moduleName, receivers, module, eventMetadata);
    }

    public <L extends LocalizationSetting> void dispatch(List<FPlayer> receivers,
                                                         ModuleLocalization<L> module,
                                                         EventMetadata<L> eventMetadata) {
        dispatch(module.name(), receivers, module, eventMetadata);
    }

    public <L extends LocalizationSetting> void dispatch(ModuleName moduleName,
                                                         List<FPlayer> receivers,
                                                         ModuleLocalization<L> module,
                                                         EventMetadata<L> eventMetadata) {
        if (receivers.isEmpty()) return;

        SchedulerRunnable sendMessageRunnable = () -> receivers.forEach(receiver -> {
            // example
            // format: HouziCore Development > <message>
            // message: hello world!
            // final formatted message: HouziCore Development > hello world!
            Component messageComponent = buildMessageComponent(receiver, eventMetadata);
            Component formatComponent = buildFormatComponent(receiver, eventMetadata, module, messageComponent);

            // destination subtext
            Component subComponent = Component.empty();
            Destination destination = eventMetadata.destination();
            if (StringUtils.isNotEmpty(destination.subtext())) {
                subComponent = buildSubcomponent(receiver, eventMetadata, messageComponent);
            }

            eventDispatcher.dispatch(new MessageSendEvent(
                    moduleName,
                    receiver,
                    formatComponent,
                    subComponent,
                    eventMetadata
            ));
        });

        FPlayer regionPlayer = eventMetadata.sender() instanceof FPlayer fPlayer
                ? fPlayer
                : fPlayerService.getRandomFPlayer();

        taskScheduler.runRegion(regionPlayer, sendMessageRunnable);
    }

    public <L extends LocalizationSetting> void dispatchError(ModuleLocalization<L> module, EventMetadata<L> eventMetadata) {
        dispatch(ModuleName.ERROR, module, eventMetadata);
    }

    private <L extends LocalizationSetting> Component buildSubcomponent(FPlayer receiver,
                                                                        EventMetadata<L> eventMetadata,
                                                                        Component message) {
        Destination destination = eventMetadata.destination();
        if (destination.subtext().isEmpty()) return Component.empty();

        MessageContext context = messagePipeline.createContext(eventMetadata.sender(), receiver, destination.subtext())
                .withFlags(eventMetadata.flags())
                .addTagResolver(messagePipeline.messageTag(message));

        return messagePipeline.build(context);
    }

    private <L extends LocalizationSetting> Component buildMessageComponent(FPlayer receiver,
                                                                            EventMetadata<L> eventMetadata) {
        String message = eventMetadata.message();
        if (StringUtils.isEmpty(message)) return Component.empty();

        MessageContext context = messagePipeline.createContext(eventMetadata.sender(), receiver, message)
                .withFlags(eventMetadata.flags())
                .addFlag(MessageFlag.PLAYER_MESSAGE, true);

        return messagePipeline.build(context);
    }

    private <L extends LocalizationSetting> Component buildFormatComponent(FPlayer receiver,
                                                                           EventMetadata<L> eventMetadata,
                                                                           ModuleLocalization<L> module,
                                                                           Component message) {
        String formatContent = eventMetadata.resolveFormat(receiver, module.localization(receiver));
        if (StringUtils.isEmpty(formatContent)) return Component.empty();

        FEntity sender = eventMetadata.sender();

        MessageContext messageContext = messagePipeline.createContext(eventMetadata.uuid(), sender, receiver, formatContent)
                .withFlags(eventMetadata.flags())
                .addTagResolvers(eventMetadata.resolveTags(receiver))
                .addTagResolver(messagePipeline.messageTag(message));

        if (!receiver.isUnknown()) {
            messageContext = messageContext
                    .withUserMessage(eventMetadata.message());
        }

        return messagePipeline.build(messageContext);
    }

}
