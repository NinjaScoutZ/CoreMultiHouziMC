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

public class KitUndeadGhoul extends KitUndead
{
	public KitUndeadGhoul(ArcadeManager manager)
	{
		super(manager, "Undead Ghoul", KitAvailability.Free, 

				// EN
				new String[] 
						{
				"Weak, but able to jump around with ease."
						}, 
				// TH
				new String[] 
						{
				"[TH] Weak, but able to jump around with ease."
						}, 
				new Perk[] 
								{
				new PerkLeap("Ghoul Leap", 1.2, 0.8, 8000),
				new PerkSpeed(0)
								}, 
								EntityType.ZOMBIFIED_PIGLIN,
								new ItemStack(Material.STONE_AXE));
	}
	
	@Override 
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.STONE_AXE));
		
		com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
			player.getUniqueId(),
			com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
			"PIG_ZOMBIE",
			true,
			false,
			false
		);
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
