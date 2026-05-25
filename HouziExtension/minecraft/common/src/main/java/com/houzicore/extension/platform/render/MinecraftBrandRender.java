package com.houzicore.extension.platform.render;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.platform.sender.PacketSender;
import com.houzicore.extension.processing.serializer.PacketSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftBrandRender implements BrandRender {

    private static final String RESET_STYLE = "§r";

    private final PacketSender packetSender;
    private final PacketSerializer packetSerializer;

    @Override
    public void render(FPlayer fPlayer, Component component) {
        String message = LegacyComponentSerializer.legacySection().serialize(component) + RESET_STYLE;

        packetSender.send(fPlayer, new WrapperPlayServerPluginMessage(PacketSerializer.MINECRAFT_BRAND, packetSerializer.serialize(message)));
    }

}
