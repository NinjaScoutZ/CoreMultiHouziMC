package com.houzicore.extension.processing.parser.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.houzicore.extension.model.util.Moderation;
import com.houzicore.extension.service.ModerationService;

@Singleton
public class WarnModerationParser extends ModerationParser {

    @Inject
    public WarnModerationParser(ModerationService moderationService) {
        super(Moderation.Type.WARN, moderationService);
    }

}
