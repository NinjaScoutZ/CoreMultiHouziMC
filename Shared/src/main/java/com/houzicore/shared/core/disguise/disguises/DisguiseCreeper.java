package com.houzicore.shared.core.disguise.disguises;

import org.bukkit.entity.Entity;

public class DisguiseCreeper extends DisguiseMonster {
    private boolean _powered;
    public DisguiseCreeper(Entity entity) { super(entity); }
    public DisguiseCreeper() { super(null); }
    public void SetPowered(boolean powered) { _powered = powered; }
    public boolean IsPowered() { return _powered; }
    public void a(int value) {}
    public int bV() { return 0; }
}
