package com.houzicore.extension.listener;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.annotation.Pulse;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.module.ModuleEnableEvent;
import com.houzicore.extension.module.ModuleSimple;
import com.houzicore.extension.module.message.bubble.BubbleModule;
import com.houzicore.extension.platform.provider.PacketProvider;
import com.houzicore.extension.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftBasePulseListener implements PulseListener {

    private final PacketProvider packetProvider;
    private final FLogger fLogger;

    @Pulse
    public Event onModuleEnableEvent(ModuleEnableEvent event) {
        ModuleSimple eventModule = event.module();
        if (eventModule instanceof BubbleModule
                && packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2)) {
            fLogger.warning("Bubble module is not supported on this version of Minecraft");
            return event.withCancelled(true);
        }



        return event;
    }

}
