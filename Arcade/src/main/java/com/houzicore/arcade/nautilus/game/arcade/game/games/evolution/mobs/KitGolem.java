package com.houzicore.arcade.nautilus.game.arcade.game.games.evolution.mobs;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.disguise.disguises.DisguiseIronGolem;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkSlam;

public class KitGolem extends Kit
{
	public KitGolem(ArcadeManager manager)
	{
		super(manager, "Iron Golem", KitAvailability.Hide, 

				new String[] 
						{
				""
						}, 

						new Perk[] 
								{
						new PerkSlam("Ground Pound", 1, 1.2, 10000),
								}, 
								EntityType.SLIME,
								null);

	}

	@Override
	public void GiveItems(Player player) 
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.DIAMOND_AXE));

		UtilPlayer.message(player, C.Line);
		UtilPlayer.message(player, C.Bold + "You evolved into " + F.elem(C.cGreen + C.Bold + GetName()) + "!");	
		UtilPlayer.message(player, F.elem("Right-Click with Axe") + " to use " + F.elem("Ground Pound"));
		UtilPlayer.message(player, C.Line);
		
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 4f, 1f);
		
		//Disguise
		com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
			player.getUniqueId(),
			com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
			"IRON_GOLEM",
			true,
			true,
			true
		);
		Manager.GetDisguise().getService().apply(player, request);
	}
}
