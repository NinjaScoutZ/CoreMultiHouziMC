package com.houzicore.extension.platform.render;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.util.BossBar;
import com.houzicore.extension.platform.sender.PacketSender;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftBossBarRender implements BossBarRender {

    private final PacketSender packetSender;
    private final TaskScheduler taskScheduler;

    @Override
    public void render(FPlayer fPlayer, Component component, BossBar bossBar) {
        UUID bossBarUUID = UUID.randomUUID();

        WrapperPlayServerBossBar addWrapper = new WrapperPlayServerBossBar(bossBarUUID, WrapperPlayServerBossBar.Action.ADD);

        addWrapper.setTitle(component);
        addWrapper.setHealth(bossBar.health());
        addWrapper.setOverlay(bossBar.overlay());
        addWrapper.setColor(bossBar.color());
        addWrapper.setFlags(bossBar.flags());

        packetSender.send(fPlayer, addWrapper);

        taskScheduler.runAsyncLater(() -> {
            WrapperPlayServerBossBar removeWrapper = new WrapperPlayServerBossBar(bossBarUUID, WrapperPlayServerBossBar.Action.REMOVE);
            packetSender.send(fPlayer, removeWrapper);

        }, bossBar.duration());
    }

}
