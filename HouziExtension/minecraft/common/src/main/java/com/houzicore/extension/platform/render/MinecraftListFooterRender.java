package com.houzicore.extension.platform.render;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.platform.sender.PacketSender;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftListFooterRender implements ListFooterRender {

    private final PacketSender packetSender;

    @Override
    public void render(FPlayer fPlayer, Component header, Component footer) {
        packetSender.send(fPlayer, new WrapperPlayServerPlayerListHeaderAndFooter(header, footer));
    }

}
