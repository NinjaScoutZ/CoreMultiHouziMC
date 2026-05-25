package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkIronShell extends Perk
{
	public PerkIronShell() 
	{
		super("Iron Shell", new String[] 
				{ 
				C.cYellow + "Right-Click" + C.cGray + " with Spade to use " + C.cGreen + "Iron Shell"
				});
	}

	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_SHOVEL"))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 60000, true, true))
			return;

		//Sphere
		Block block = player.getLocation().getBlock();
		HashMap<Block, Double> blocks = UtilBlock.getInRadius(block.getLocation().add(0.5, 0.5, 0.5), 6d);
		for (Block cur : blocks.keySet()) 
		{
			if (UtilMath.offset(block.getLocation(), cur.getLocation()) <= 5)
				continue;
			
			//Doors
			if (cur.getY() < block.getLocation().getY() + 2)
				if (cur.getX() == block.getX() || cur.getZ() == block.getZ())
					continue;
			
			//None Below
			if (cur.getY() < block.getLocation().getY())
				continue;
			
			Manager.GetBlockRestore().Add(cur, 42, (byte)0, 12000);
		}
	

		//Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}
}
