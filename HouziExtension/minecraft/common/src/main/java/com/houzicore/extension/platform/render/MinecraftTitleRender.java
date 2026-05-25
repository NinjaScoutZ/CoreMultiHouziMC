package com.houzicore.extension.platform.render;

import com.github.retrooper.packetevents.protocol.player.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.util.Times;
import com.houzicore.extension.platform.provider.PacketProvider;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftTitleRender implements TitleRender {

    private final PacketProvider packetProvider;

    @Override
    public void render(FPlayer fPlayer, Component title, Component subTitle, Times times) {
        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        user.sendTitle(title, subTitle, times.fadeInTicks(), times.stayTicks(), times.fadeOutTicks());
    }

}
