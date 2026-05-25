package com.houzicore.arcade.nautilus.game.arcade.game.games.castlesiege.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFletcher;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkLeap;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkPowershot;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkRegeneration;

public class KitHumanAssassin extends KitHuman
{
	public KitHumanAssassin(ArcadeManager manager)
	{
		super(manager, "Castle Assassin", KitAvailability.Gem, 5000,
				// EN
				new String[] 
				{
					"Able to kill with a single shot!"
				}, 
				// TH
				new String[] 
				{
					"[TH] Able to kill with a single shot!"
				}, 
				new Perk[] 
				{
					new PerkFletcher(2, 4, false),
					new PerkLeap("Leap", 1.2, 1, 8000),
					new PerkPowershot(5, 400),
					new PerkRegeneration(0),
				}, 
				EntityType.ZOMBIE,	
				new ItemStack(Material.BOW));

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
			
			// // Manager.GetCondition().Factory().FireItemImmunity(GetName(), player, player, 1.9, false); // TODO // TODO: condition factory not migrated
		} 
	}
	
	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.DIAMOND_AXE));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.BOW));
		
		player.getInventory().setHelmet(ItemStackFactory.Instance.CreateStack(Material.LEATHER_HELMET));
		player.getInventory().setChestplate(ItemStackFactory.Instance.CreateStack(Material.LEATHER_CHESTPLATE));
		player.getInventory().setLeggings(ItemStackFactory.Instance.CreateStack(Material.LEATHER_LEGGINGS));
		player.getInventory().setBoots(ItemStackFactory.Instance.CreateStack(Material.LEATHER_BOOTS));
	}
	
	@Override
	public void SpawnCustom(LivingEntity ent) 
	{
		ent.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
		ent.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
		ent.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
		ent.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
	}
}
