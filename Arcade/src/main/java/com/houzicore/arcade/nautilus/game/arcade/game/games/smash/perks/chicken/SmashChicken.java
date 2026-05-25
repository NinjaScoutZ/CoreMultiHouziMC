package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.chicken;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashChicken extends SmashUltimate
{

	public SmashChicken()
	{
		super("Aerial Gunner", new String[] {}, Sound.ENTITY_CHICKEN_AMBIENT, 0);
	}
	
	/**
	 * See {@link PerkEggGun} for smash code.
	 */
	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		player.getInventory().remove(Material.IRON_SWORD);
		player.getInventory().remove(Material.IRON_AXE);
	}
}
