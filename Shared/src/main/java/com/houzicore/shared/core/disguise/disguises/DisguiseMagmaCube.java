package com.houzicore.shared.core.disguise.disguises;

import org.bukkit.entity.Entity;

public class DisguiseMagmaCube extends DisguiseInsentient {
    private int _size = 1;

    public DisguiseMagmaCube(Entity entity) { super(entity); }
    public DisguiseMagmaCube() { super(null); }

    public int GetSize() { return _size; }
    public void SetSize(int size) { this._size = size; }
}
