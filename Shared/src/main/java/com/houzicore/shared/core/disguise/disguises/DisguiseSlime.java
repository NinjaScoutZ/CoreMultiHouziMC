package com.houzicore.shared.core.disguise.disguises;

import org.bukkit.entity.Entity;

public class DisguiseSlime extends DisguiseInsentient {
    private int _size = 1;

    public DisguiseSlime(Entity entity) { super(entity); }
    public DisguiseSlime() { super(null); }

    public int GetSize() { return _size; }
    public void SetSize(int size) { this._size = size; }
}
