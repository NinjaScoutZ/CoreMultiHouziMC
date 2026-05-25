package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class PerkAssassin extends Perk
{
	public PerkAssassin() 
	{
		super("Assassin", new String[] 
				{ 
				C.cGray + "When you kill an enemy, you gain",
				C.cGray + "Speed II and Invisibility for 4 seconds."
				});
	}
		
	@EventHandler
	public void onKill(PlayerDeathEvent event)
	{
		Player victim = event.getEntity();
		if (victim.getKiller() != null)
		{
			Player killer = victim.getKiller();
			if (!Kit.HasKit(killer))
				return;

			killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1, false, false, false));
			killer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 80, 0, false, false, false));
		}
	}
}
