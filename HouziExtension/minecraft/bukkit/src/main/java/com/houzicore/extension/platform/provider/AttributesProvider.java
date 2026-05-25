package com.houzicore.extension.platform.provider;

import org.bukkit.entity.Player;

public interface AttributesProvider {

    double getArmorValue(Player player);

    double getAttackDamage(Player player);

}
