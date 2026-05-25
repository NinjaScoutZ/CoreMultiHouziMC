package com.houzicore.shared.api.disguise;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.LivingEntity;

public interface DisguiseService {

    Optional<DisguiseSession> getActiveSession(UUID subjectId);

    DisguiseSession apply(LivingEntity target, DisguiseRequest request);

    void clear(LivingEntity target);

    boolean supports(DisguiseRequest request);
}
