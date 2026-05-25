package com.houzicore.arcade.nautilus.game.arcade.stats;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

//import com.houzicore.shared.damage.*;
import com.houzicore.arcade.nautilus.game.arcade.game.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class DamageTakenStatTracker extends StatTracker<Game>
{
	public DamageTakenStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCustomDamage(EntityDamageByEntityEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = (Player) event.getEntity();
		
		addStat(damagee, "Damage Taken", (int) Math.round(event.getDamage()), false, false);
		if (event.getDamager() instanceof Player)
			addStat(damagee, "Damage Taken PvP ", (int) Math.round(event.getDamage()), false, false);
		
//		if (getGame().GetKit(damagee) != null)
//		{
//			addStat(damagee, getGame().GetKit(damagee).getName() + " Damage Taken", (int) Math.round(event.getDamage()), false, false);
//			
//			if (((Player) event.getDamager()) != null)
//				addStat(damagee, getGame().GetKit(damagee).getName() + " Damage Taken PvP ", (int) Math.round(event.getDamage()), false, false);
//		}
	}
}
