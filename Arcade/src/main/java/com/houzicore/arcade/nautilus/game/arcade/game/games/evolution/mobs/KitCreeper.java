package com.houzicore.arcade.nautilus.game.arcade.game.games.evolution.mobs;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkExplode;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFood;

public class KitCreeper extends Kit
{
	public KitCreeper(ArcadeManager manager)
	{
		super(manager, "Creeper", KitAvailability.Hide, 

				new String[] 
						{
				""
						}, 

						new Perk[] 
								{
				new PerkFood(1),
				new PerkExplode("Detonate", 1, 6000)
								}, 
								EntityType.SLIME,
								null);

	}

	@Override
	public void GiveItems(Player player) 
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.TNT));

		UtilPlayer.message(player, C.Line);
		UtilPlayer.message(player, C.Bold + "You evolved into " + F.elem(C.cGreen + C.Bold + GetName()) + "!");	
		UtilPlayer.message(player, F.elem("Attack") + " to use " + F.elem("Detonate"));
		UtilPlayer.message(player, C.Line);

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 4f, 1f);

		//Disguise
		com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
			player.getUniqueId(),
			com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
			"CREEPER",
			true,
			false,
			false
		);
		Manager.GetDisguise().getService().apply(player, request);
	}
}
