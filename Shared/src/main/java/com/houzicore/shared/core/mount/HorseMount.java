package com.houzicore.shared.core.mount;

import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;

public abstract class HorseMount extends Mount<AbstractHorse> {
    public HorseMount(MountManager manager, String name, String[] desc, Material mat, byte data, int cost, org.bukkit.entity.Horse.Color color, org.bukkit.entity.Horse.Style style, Object variant, double scale, Object unknown) {
        super(manager, name, mat, data, desc, cost);
    }

    @Override
    public void Disable(org.bukkit.entity.Player player) {
        AbstractHorse horse = _active.remove(player);
        if (horse != null) {
            horse.eject();
            horse.remove();
            Manager.removeActive(player);
        }
    }
}
