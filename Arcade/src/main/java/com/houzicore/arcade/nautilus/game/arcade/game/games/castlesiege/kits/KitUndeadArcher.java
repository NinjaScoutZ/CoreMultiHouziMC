package com.houzicore.arcade.nautilus.game.arcade.game.games.castlesiege.kits;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkIronSkin;

public class KitUndeadArcher extends KitUndead
{
	public KitUndeadArcher(ArcadeManager manager)
	{
		super(manager, "Undead Archer", KitAvailability.Gem, 

				// EN
				new String[] 
						{
				"Makes use of arrows scavenged from human archers."
						}, 
				// TH
				new String[] 
						{
				"[TH] Makes use of arrows scavenged from human archers."
						}, 
				new Perk[] 
								{
				new PerkIronSkin(1)
								}, 
								EntityType.SKELETON,
								new ItemStack(Material.BOW));

	}
	
	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.STONE_AXE));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.BOW));
		
		com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
			player.getUniqueId(),
			com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
			"SKELETON",
			true,
			false,
			false
		);
		Manager.GetDisguise().getService().apply(player, request);
	}
	
	@EventHandler
	public void ArrowPickup(PlayerPickupItemEvent event)
	{
		if (event.getItem().getItemStack().getType() != Material.ARROW)
			return;
		
		if (!HasKit(event.getPlayer()))
			return;
		
		if (UtilInv.contains(event.getPlayer(), Material.ARROW, (byte)0, 4))
			return;
		
		event.getItem().remove();
		
		event.getPlayer().getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.ARROW));
		
		event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
	}
}
