package com.houzicore.arcade.nautilus.game.arcade.stats;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

//import com.houzicore.shared.damage.*;
import com.houzicore.arcade.nautilus.game.arcade.game.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class DamageDealtStatTracker extends StatTracker<Game>
{
	public DamageDealtStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCustomDamage(EntityDamageByEntityEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (!(event.getDamager() instanceof Player)) return;
		Player damager = (Player) event.getDamager();

		addStat(damager, "Damage Dealt", (int) Math.round(event.getDamage()), false, false);
		
//		if (getGame().GetKit(damager) != null)
//			addStat(damager, getGame().GetKit(damager).getName() + " Damage Dealt", (int) Math.round(event.getDamage()), false, false);
	}
}
