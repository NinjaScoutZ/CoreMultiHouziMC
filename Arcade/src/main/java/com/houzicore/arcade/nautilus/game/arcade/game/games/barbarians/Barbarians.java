package com.houzicore.arcade.nautilus.game.arcade.game.games.barbarians;

import com.houzicore.arcade.nautilus.game.arcade.game.modules.CompassModule;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.SoloGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.barbarians.kits.*;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class Barbarians extends SoloGame
{
	public Barbarians(ArcadeManager manager) 
	{
		super(manager, GameType.Barbarians,

				new Kit[]
						{
				new KitBrute(manager),
				new KitArcher(manager),
				new KitBomber(manager),
						},

						// EN
				new String[]
								{
				"Free for all fight to the death!",
				"Wooden blocks are breakable.",
				"Attack people to restore hunger!",
				"Last player alive wins!"
								}, 
				// TH
				new String[]
								{
				"[TH] Free for all fight to the death!",
				"[TH] Wooden blocks are breakable.",
				"[TH] Attack people to restore hunger!",
				"[TH] Last player alive wins!"
								});
	
		this.DamageTeamSelf = true;
		registerModule(new CompassModule(this));this.BlockBreakAllow.add(5);
		this.BlockBreakAllow.add(17);
		this.BlockBreakAllow.add(18);
		this.BlockBreakAllow.add(20);
		this.BlockBreakAllow.add(30);
		this.BlockBreakAllow.add(47);
		this.BlockBreakAllow.add(53);
		this.BlockBreakAllow.add(54);
		this.BlockBreakAllow.add(58);
		this.BlockBreakAllow.add(64);
		this.BlockBreakAllow.add(83);
		this.BlockBreakAllow.add(85);
		this.BlockBreakAllow.add(96);
		this.BlockBreakAllow.add(125);
		this.BlockBreakAllow.add(126);
		this.BlockBreakAllow.add(134);
		this.BlockBreakAllow.add(135);
		this.BlockBreakAllow.add(136);
	}
	
	@EventHandler
	public void BlockDamage(BlockDamageEvent event)
	{
		event.setInstaBreak(true);
	}
	
	@EventHandler
	public void ItemSpawn(ItemSpawnEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void Hunger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;
		
		if (!IsLive())
			return;
		
		for (Player player : GetPlayers(true))
		{
			if (player.getFoodLevel() <= 0)
			{
				// Manager.GetDamage()...;
			}
			
			UtilPlayer.hunger(player, -2);
		}
	}
	
	@EventHandler
	public void HungerRestore(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager != null)
			UtilPlayer.hunger(damager, 2);
	}
}
