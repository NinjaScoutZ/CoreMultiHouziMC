package com.houzicore.extension.platform.sender;

import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.module.integration.IntegrationModule;
import com.houzicore.extension.platform.provider.PacketProvider;
import com.houzicore.extension.util.PaperItemStackUtil;
import com.houzicore.extension.util.file.FileFacade;
import com.houzicore.extension.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

@Singleton
public class BukkitMessageSender extends MinecraftMessageSender {

    private final FileFacade fileFacade;
    private final PaperItemStackUtil paperItemStackUtil;

    @Inject
    public BukkitMessageSender(PacketSender packetSender,
                               PacketProvider packetProvider,
                               IntegrationModule integrationModule,
                               FileFacade fileFacade,
                               PaperItemStackUtil paperItemStackUtil,
                               FLogger fLogger) {
        super(packetSender, packetProvider, integrationModule, fLogger);

        this.fileFacade = fileFacade;
        this.paperItemStackUtil = paperItemStackUtil;
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component, boolean silent) {
        // use default sendMessage
        if (!fileFacade.config().module().usePaperMessageSender()) {
            super.sendMessage(fPlayer, component, silent);
            return;
        }

        // replace item mark
        if (fPlayer.isConsole() || silent) {
            super.sendMessage(fPlayer, replaceItemMarkToEmpty(component), silent);
            return;
        }

        paperItemStackUtil.sendMessage(fPlayer, AdventureSerializer.serializer().gson().serialize(component));
    }

    private Component replaceItemMarkToEmpty(Component component) {
        return component.replaceText(TextReplacementConfig.builder()
                .match(PaperItemStackUtil.FLECTONEPULSE_ITEM_MARK_PATTERN)
                .replacement((matchResult, builder) -> Component.text(""))
                .build()
        );
    }
}
