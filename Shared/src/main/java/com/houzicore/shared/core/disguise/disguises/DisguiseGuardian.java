package com.houzicore.shared.core.disguise.disguises;

import org.bukkit.entity.Entity;

public class DisguiseGuardian extends DisguiseMonster {
    private int _targetId = 0;
    private boolean _elder = false;

    public DisguiseGuardian(Entity entity) { super(entity); }
    public DisguiseGuardian() { super(null); }

    public int getTarget() { return _targetId; }
    public void setTarget(int targetId) { this._targetId = targetId; }

    public boolean isElder() { return _elder; }
    public void setElder(boolean elder) { this._elder = elder; }
}
