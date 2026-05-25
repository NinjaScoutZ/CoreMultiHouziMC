package com.houzicore.shared.core.disguise.disguises;

import org.bukkit.entity.Entity;

public class DisguiseHorse extends DisguiseAnimal {
    private boolean _kick = false;

    public DisguiseHorse(Entity entity) { super(entity); }
    public DisguiseHorse() { super(null); }

    public void kick() { _kick = true; }
    public void stopKick() { _kick = false; }
    public boolean isKicking() { return _kick; }
}
