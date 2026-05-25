package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.houzicore.shared.api.disguise.DisguiseArchetype;
import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkWitherForm extends SmashPerk
{
	public PerkWitherForm() 
	{
		super("Wither Form", new String[] 
				{ 
				}, false);
	}

	@Override
	public void addSuperCustom(Player player)
	{
		applyMobDisguise(player, "WITHER", false);
	}

	@Override
	public void removeSuperCustom(Player player)
	{
		applyMobDisguise(player, "WITHER_SKELETON", true);
		
		player.setFlying(false);
	}

	private void applyMobDisguise(Player player, String variantKey, boolean hideArmor)
	{
		Manager.GetDisguise().getService().clear(player);
		Manager.GetDisguise().getService().apply(player, new DisguiseRequest(
				player.getUniqueId(),
				DisguiseArchetype.MOB,
				variantKey,
				true,
				hideArmor,
				false,
				getTeamName(player),
				true));
	}

	private String getTeamName(Player player)
	{
		if (Manager.GetGame().GetTeam(player) != null)
			return Manager.GetGame().GetTeam(player).GetColor() + player.getName();

		return player.getName();
	}

	@EventHandler
	public void witherBump(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		for (Player player : ((SmashKit)Kit).getSuperActive())
		{
			ArrayList<Location> collisions = new ArrayList<Location>();	

			//Bump
			for (Block block : UtilBlock.getInRadius(player.getLocation().add(0, 0.5, 0), 1.5d).keySet())
			{
				if (!UtilBlock.airFoliage(block))
				{
					collisions.add(block.getLocation().add(0.5, 0.5, 0.5));
				}
			}

			Vector vec = UtilAlg.getAverageBump(player.getLocation(), collisions);

			if (vec == null)
				continue;

			UtilAction.velocity(player, vec, 0.6, false, 0, 0.4, 10, true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void witherMeleeCancel(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (!(event.getDamager() instanceof Player)) return;
		Player player = ((Player) event.getDamager());
		if (player == null)
			return;

		if (!isSuperActive(player))
			return;

		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void witherFlight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : ((SmashKit)Kit).getSuperActive())
		{
			if (player.isFlying())
				continue;

			player.setAllowFlight(true);
			player.setFlying(true);
		}
	}
}
