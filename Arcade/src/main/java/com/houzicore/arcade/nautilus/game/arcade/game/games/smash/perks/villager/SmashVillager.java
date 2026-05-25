package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.villager;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;

import com.houzicore.shared.recharge.Recharge;

import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.kits.KitVillager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.kits.KitVillager.VillagerType;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashVillager extends SmashUltimate
{

	public SmashVillager()
	{
		super("Perfection", new String[0], Sound.ENTITY_VILLAGER_YES, 0);
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		for (VillagerType type : VillagerType.values())
		{
			Recharge.Instance.useForce(player, type.getName(), getLength());
		}

		((KitVillager) Kit).updateDisguise(player, Profession.CLERIC);
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);

		player.setWalkSpeed(0.2F);
		((KitVillager) Kit).updateDisguise(player, Profession.FARMER);
	}
}
