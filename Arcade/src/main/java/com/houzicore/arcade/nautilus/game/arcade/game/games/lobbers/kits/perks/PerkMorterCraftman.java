package com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits.perks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkMorterCraftman extends Perk
{
	private Recharge _recharge;

	public PerkMorterCraftman()
	{
		super("Morter Craftman", new String[]
				{
				"You will recieve 1 mortar every " + C.cYellow + "10 Seconds.",
				"Maximum of 1."
				});
		
		_recharge = Recharge.Instance;
	}
	
	@EventHandler
	public void give(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;
			
			if (!_recharge.use(player, "Mortar Give", 10000, false, false))
				continue;
			
			//Has 1
			if (UtilInv.contains(player, "", Material.FIRE_CHARGE, (byte) 0, 1))
				continue;
			
			UtilInv.insert(player, new ItemBuilder(Material.FIRE_CHARGE).setTitle(F.item("Mortar")).build());
		}
	}
}
