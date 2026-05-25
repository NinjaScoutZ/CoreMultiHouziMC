package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkRewind extends Perk
{
	private HashMap<Player, LinkedList<Location>> _locMap = new HashMap<Player, LinkedList<Location>>();
	
	public PerkRewind() 
	{
		super("Rewind", new String[] 
				{ 
				C.cYellow + "Right-Click" + C.cGray + " with Star to " + C.cGreen + "Rewind",
				});
	}
		
	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.NETHER_STAR))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 20000, true, true))
			return;
			
		event.setCancelled(true);

		LinkedList<Location> locs = _locMap.remove(player);
		if (locs == null)
			return;
		
		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2f, 2f);
		
		Location current = player.getLocation();
		Location target = locs.getLast();
		
		player.teleport(target);
		
		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
		
		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2f, 2f);
		
		while (UtilMath.offset(current, target) > 0.5)
		{
			UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, current, 0, 1f, 0, 0, 1,
					ViewDist.LONGER, UtilServer.getPlayers());
			current = current.add(UtilAlg.getTrajectory(current, target).multiply(0.1));
		}
	}

	@EventHandler
	public void StoreLocation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;

		for (Player cur : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(cur))
				continue;
			
			if (!_locMap.containsKey(cur))
				_locMap.put(cur, new LinkedList<Location>());
			
			_locMap.get(cur).addFirst(cur.getLocation());
			
			if (_locMap.get(cur).size() > 160)
				_locMap.get(cur).removeLast();
		}
	}
}
