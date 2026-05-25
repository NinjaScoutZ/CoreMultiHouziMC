package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits.perk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.houzicore.shared.common.util.UtilPlayer;

import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkLifeSteal extends Perk
{

	private final double _increase;

	public PerkLifeSteal(double increase)
	{
		super("Lifesteal", new String[] { "Heals you on kills" });

		_increase = increase;
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		Player killer = player.getKiller();

		if (killer != null && Kit.HasKit(killer))
		{
			Bedwars game = (Bedwars) Manager.GetGame();
			double increase = Math.max(0, _increase - game.getDeathsInLastMinute(player) * 2);

			UtilPlayer.health(killer, increase);
		}
	}
}
