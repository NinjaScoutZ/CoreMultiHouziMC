package com.houzicore.shared.core.disguise.disguises;

import org.bukkit.entity.Entity;

public abstract class DisguiseBase {
    private Entity _entity;
    private String _name;
    private boolean _customNameVisible;

    public DisguiseBase(Entity entity) { _entity = entity; }
    public DisguiseBase() { _entity = null; }
    public Entity getEntity() { return _entity; }

    public void setName(String name) { _name = name; }
    public String getName() { return _name; }
    public void setCustomNameVisible(boolean visible) { _customNameVisible = visible; }
    public boolean isCustomNameVisible() { return _customNameVisible; }
}
