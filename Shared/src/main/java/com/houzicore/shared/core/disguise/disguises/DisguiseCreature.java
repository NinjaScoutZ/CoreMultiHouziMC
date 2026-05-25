package com.houzicore.shared.core.disguise.disguises;

import org.bukkit.entity.Entity;

public abstract class DisguiseCreature extends DisguiseInsentient {
    public DisguiseCreature(Entity entity) { super(entity); }
    public DisguiseCreature() { super(null); }
}
