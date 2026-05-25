package com.houzicore.arcade.nautilus.game.arcade.stats;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.shared.common.util.UtilGear;

public class FireworkTauntStatTracker extends StatTracker<Game>
{
	public FireworkTauntStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		Player player = event.getPlayer();
		GameTeam team = getGame().GetTeam(player);
		
		if (team == null || !team.GetName().equals("Hiders"))
			return;

		if (UtilGear.isMat(player.getInventory().getItemInMainHand(), Material.FIREWORK_ROCKET))
		{
			addStat(player, "Taunting", 1, false, false);
		}
	}
}
