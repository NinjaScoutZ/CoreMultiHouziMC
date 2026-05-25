package com.houzicore.extension.processing.parser.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.houzicore.extension.model.util.Moderation;
import com.houzicore.extension.service.ModerationService;

@Singleton
public class MuteModerationParser extends ModerationParser {

    @Inject
    public MuteModerationParser(ModerationService moderationService) {
        super(Moderation.Type.MUTE, moderationService);
    }

}
