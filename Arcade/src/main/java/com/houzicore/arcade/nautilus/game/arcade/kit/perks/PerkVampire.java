package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkVampire extends Perk
{
	private int _recover;
	
	public PerkVampire(int recover) 
	{
		super("Vampire", new String[] 
				{ 
				C.cGray + "You heal " + recover + "HP when you kill someone",
				});
		
		_recover = recover;
	}
		
	@EventHandler
	public void PlayerKillAward(PlayerDeathEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!(event.getEntity() instanceof Player))
			return;

		if (event.getEntity().getKiller() == null)
			return;

		Player killer = UtilPlayer.searchExact(event.getEntity().getKiller().getName());
		if (killer == null)
			return;

		UtilPlayer.health(killer, _recover);
	}
}
