package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkChampion extends Perk
{
	private HashMap<Player, Integer> _bonus = new HashMap<Player, Integer>();
	
	public PerkChampion() 
	{
		super("Champion", new String[] 
				{
				C.cGray + "You get stronger with each kill",
				});
	}

	@EventHandler
	public void kill(PlayerDeathEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	
			return;

		if (!(event.getEntity() instanceof Player))
			return;

		Player killed = (Player)event.getEntity();

		if (event.getEntity().getKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.getEntity().getKiller().getName());

			if (killer != null && !killer.equals(killed))
			{
				int past = 0;
				if (_bonus.containsKey(killer))
					past = _bonus.get(killer);
				
				_bonus.put(killer, past + 1);
				
				UtilPlayer.message(killer, F.main("Game", "Bonus Damage: " + F.elem((past + 1)+"")));
			}
		}
	}
	
	@EventHandler
	public void damageBonus(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.PROJECTILE)
			return;

		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)	
			return;

		if (!_bonus.containsKey(damager))
			return;
		
		int bonus = _bonus.get(damager);
		
  // /* event.AddMod(...) */, GetName(), bonus, false);
	}
}
