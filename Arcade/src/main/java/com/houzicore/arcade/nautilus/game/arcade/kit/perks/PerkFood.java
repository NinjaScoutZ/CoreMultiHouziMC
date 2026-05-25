package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkFood extends Perk
{
	private int _amount;
	
	public PerkFood(int amount) 
	{
		super("Strength", new String[] 
				{ 
				C.cGray + "Your Hunger is permanently " + amount + "",
				});
		
		_amount = amount;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (Manager.GetGame() == null)
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (Kit.HasKit(player))
			{
				player.setFoodLevel(_amount);
			}
		}
	}
}
