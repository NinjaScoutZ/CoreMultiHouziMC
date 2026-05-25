package com.houzicore.arcade.nautilus.game.arcade.game.games.castlesiege.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

public class KitUndeadZombie extends KitUndead
{
	public KitUndeadZombie(ArcadeManager manager)
	{
		super(manager, "Undead Zombie", KitAvailability.Gem, 5000,

				// EN
				new String[] 
						{
				"Regenerates rapidly"
						}, 
				// TH
				new String[] 
						{
				"[TH] Regenerates rapidly"
						}, 
				new Perk[] 
								{
				new PerkRegeneration(2)
								}, 
								EntityType.ZOMBIE,
								new ItemStack(Material.STONE_AXE));

	}

	@Override 
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.STONE_AXE));
		
		com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
			player.getUniqueId(),
			com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
			"ZOMBIE",
			true,
			false,
			false
		);
		
		if (Manager.GetGame().GetTeam(player) != null)
		{
		}
		
		Manager.GetDisguise().getService().apply(player, request);
	}

	@EventHandler
	public void PickupArrow(PlayerPickupItemEvent event)
	{
		if (!HasKit(event.getPlayer()))
			return;
		
		if (event.getItem().getItemStack().getType() == Material.ARROW)
			event.setCancelled(true);
	}
}
