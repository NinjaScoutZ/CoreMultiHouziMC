package com.houzicore.extension.platform.provider;

import org.bukkit.entity.Player;

import java.util.List;

public interface PassengersProvider {

    List<Integer> getPassengers(Player player);

}
