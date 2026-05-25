package com.houzicore.extension.platform.registry;

import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.houzicore.extension.listener.BasePacketListener;
import com.houzicore.extension.listener.DialogPacketListener;
import com.houzicore.extension.listener.InventoryPacketListener;
import com.houzicore.extension.listener.MinecraftBasePulseListener;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.platform.provider.PacketProvider;
import com.houzicore.extension.util.logging.FLogger;

import java.util.List;

@Singleton
public class MinecraftListenerRegistry extends ListenerRegistry {

    private final List<PacketListenerCommon> packetListeners = new ObjectArrayList<>();

    private final Injector injector;
    private final PacketProvider packetProvider;

    @Inject
    public MinecraftListenerRegistry(FLogger fLogger,
                                     Injector injector,
                                     PacketProvider packetProvider) {
        super(fLogger, injector);

        this.injector = injector;
        this.packetProvider = packetProvider;
    }

    @Override
    public void registerDefaultListeners() {
        super.registerDefaultListeners();

        register(MinecraftBasePulseListener.class);
        register(BasePacketListener.class);
        register(InventoryPacketListener.class);

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_6)) {
            register(DialogPacketListener.class);
        }
    }

    @Override
    public void register(Class<?> clazzListener, Event.Priority eventPriority) {
        if (PacketListener.class.isAssignableFrom(clazzListener)) {
            PacketListener packetListener = (PacketListener) injector.getInstance(clazzListener);
            register(packetListener, PacketListenerPriority.valueOf(eventPriority.name()));
        } else {
            super.register(clazzListener, eventPriority);
        }
    }

    public void register(PacketListener packetListener, PacketListenerPriority priority) {
        PacketListenerCommon packetListenerCommon = packetProvider.getApi().getEventManager().registerListener(packetListener, priority);
        packetListeners.add(packetListenerCommon);
    }

    @Override
    public void unregisterAll() {
        EventManager eventManager = packetProvider.getApi().getEventManager();
        packetListeners.forEach(eventManager::unregisterListeners);
        packetListeners.clear();

        super.unregisterAll();
    }

}
