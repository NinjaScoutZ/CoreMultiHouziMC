package com.houzicore.extension.util.checker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.module.integration.IntegrationModule;

import com.houzicore.extension.service.ModerationService;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MuteChecker {

    private final ModerationService moderationService;
    private final Provider<IntegrationModule> integrationModuleProvider;


    public Status check(FPlayer fPlayer) {
        if (!moderationService.getValidMutes(fPlayer).isEmpty()) {
            return Status.LOCAL;
        }



        if (integrationModuleProvider.get().isMuted(fPlayer)) {
            return Status.EXTERNAL;
        }

        return Status.NONE;
    }

    public enum Status {
        LOCAL,
        EXTERNAL,
        NEWBIE,
        NONE
    }

}
