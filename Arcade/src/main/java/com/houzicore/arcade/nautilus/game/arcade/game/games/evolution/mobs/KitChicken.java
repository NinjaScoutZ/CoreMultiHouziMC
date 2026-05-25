package com.houzicore.arcade.nautilus.game.arcade.game.games.evolution.mobs;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkConstructor;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFallDamage;

public class KitChicken extends Kit
{
	public KitChicken(ArcadeManager manager)
	{
		super(manager, "Chicken", KitAvailability.Hide, 

				new String[] 
						{
				""
						}, 

						new Perk[] 
								{
				new PerkConstructor("Egg Pouch", 0.8, 8, Material.EGG, "Egg", false),
				new PerkFallDamage(-2),
								}, 
								EntityType.SLIME,
								null);

	}

	@Override
	public void GiveItems(Player player) 
	{
		UtilPlayer.message(player, C.Line);
		UtilPlayer.message(player, C.Bold + "You evolved into " + F.elem(C.cGreen + C.Bold + GetName()) + "!");	
		UtilPlayer.message(player, F.elem("Right-Click with Eggs") + " to use " + F.elem("Throw Egg"));
		UtilPlayer.message(player, C.Line);

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 4f, 1f);

		//Disguise
		com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
			player.getUniqueId(),
			com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
			"CHICKEN",
			true,
			false,
			false
		);
		Manager.GetDisguise().getService().apply(player, request);
	}

	@EventHandler
	public void EggHit(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getDamager() == null)
			return;

		if (!(event.getDamager() instanceof Egg))
			return;

  // /* event.AddMod(...) */;
	}
}
