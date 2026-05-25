package com.houzicore.extension.model.file;

import lombok.With;
import com.houzicore.extension.config.*;

import java.util.Map;

@With
public record FilePack(
        Command command,
        Config config,
        Integration integration,
        Message message,
        Permission permission,
        Map<String, Localization> localizations
) {
}
