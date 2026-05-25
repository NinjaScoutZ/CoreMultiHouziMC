package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.FissureData;

public class PerkFissure extends Perk
{	
	private HashSet<FissureData> _active = new HashSet<FissureData>();

	public PerkFissure() 
	{
		super("Fissure", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Fissure"
				});
	}

	@EventHandler
	public void Leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;
		
		if (!UtilEnt.isGrounded(player))
		{
			UtilPlayer.message(player, F.main("Game", "You cannot use " + F.skill(GetName()) + " while airborne."));
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), 8000, true, true))
			return;

		FissureData data = new FissureData(this, player, player.getLocation().getDirection(), player.getLocation().add(0, -0.4, 0));
		_active.add(data);
		
		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}
	
	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		HashSet<FissureData> remove = new HashSet<FissureData>();
		
		for (FissureData data : _active)
			if (data.Update())
				remove.add(data);
		
		for (FissureData data : remove)
		{
			_active.remove(data);
			data.Clear();
		}	
	}
}
