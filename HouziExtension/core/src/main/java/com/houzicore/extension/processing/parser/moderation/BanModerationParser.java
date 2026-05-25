package com.houzicore.extension.processing.parser.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.houzicore.extension.model.util.Moderation;
import com.houzicore.extension.service.ModerationService;

@Singleton
public class BanModerationParser extends ModerationParser {

    @Inject
    public BanModerationParser(ModerationService moderationService) {
        super(Moderation.Type.BAN, moderationService);
    }

}
