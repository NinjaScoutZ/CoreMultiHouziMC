package com.houzicore.arcade.nautilus.game.arcade.game.games.castlesiege.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkKnockbackGive;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkStrength;

public class KitHumanPeasant extends KitHuman
{
	public KitHumanPeasant(ArcadeManager manager)
	{
		super(manager, "Castle Wolf", KitAvailability.Hide, 

				// EN
				new String[] 
						{
				"OINK! OINK!"
						}, 
				// TH
				new String[] 
						{
				"[TH] OINK! OINK!"
						}, 
				new Perk[] 
								{
				new PerkStrength(1),
				new PerkKnockbackGive(2)
								}, 

								EntityType.ZOMBIE, new ItemStack(Material.IRON_HOE));

	}
	
	@EventHandler
	public void FireItemResist(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (Manager.GetGame() == null)
			return;
			
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!HasKit(player))
				continue;
			
			// Manager.GetCondition().Factory().FireItemImmunity(GetName(), player, player, 1.9, false); // TODO
		}
	}
	
	@Override 
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.BONE, (byte)0, 0, "Wolf Bite"));
		
		player.setHealth(4);
		
		com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
			player.getUniqueId(),
			com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
			"WOLF",
			true,
			false,
			false
		);
		Manager.GetDisguise().getService().apply(player, request);
	}
}
