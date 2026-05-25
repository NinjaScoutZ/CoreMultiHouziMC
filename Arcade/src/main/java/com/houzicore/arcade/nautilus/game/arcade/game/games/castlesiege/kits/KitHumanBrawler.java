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
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkCleave;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkKnockbackGive;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkSeismicSlamCS;

public class KitHumanBrawler extends KitHuman
{
	public KitHumanBrawler(ArcadeManager manager)
	{
		super(manager, "Castle Brawler", KitAvailability.Gem, 5000,

				// EN
				new String[] 
						{
				"Extremely tanky, can smash the undead around."
						}, 
				// TH
				new String[] 
						{
				"[TH] Extremely tanky, can smash the undead around."
						}, 
				new Perk[] 
								{
				new PerkSeismicSlamCS(),
				new PerkCleave(0.75, true),
								}, 

								EntityType.ZOMBIE, new ItemStack(Material.IRON_AXE));
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
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_AXE));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.BOW));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.ARROW, 16));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		
		player.getInventory().setHelmet(ItemStackFactory.Instance.CreateStack(Material.DIAMOND_HELMET));
		player.getInventory().setChestplate(ItemStackFactory.Instance.CreateStack(Material.DIAMOND_CHESTPLATE));
		player.getInventory().setLeggings(ItemStackFactory.Instance.CreateStack(Material.DIAMOND_LEGGINGS));
		player.getInventory().setBoots(ItemStackFactory.Instance.CreateStack(Material.DIAMOND_BOOTS));
	}
	
	@Override
	public void SpawnCustom(LivingEntity ent) 
	{
		ent.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
		ent.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		ent.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		ent.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
	}
}
