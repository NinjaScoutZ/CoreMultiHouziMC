package com.houzicore.arcade.nautilus.game.arcade.game.games.castlesiege.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

public class KitHumanKnight extends KitHuman
{
	public KitHumanKnight(ArcadeManager manager)
	{
		super(manager, "Castle Knight", KitAvailability.Gem, 

				// EN
				new String[] 
						{
				"Robust knight, also able to construct defenses."
						}, 
				// TH
				new String[] 
						{
				"[TH] Robust knight, also able to construct defenses."
						}, 
				new Perk[] 
								{
				new PerkMammoth(),
				new PerkConstructor("Constructor", 40, 2, Material.OAK_FENCE, "Castle Barricade", true)
								}, 

								EntityType.ZOMBIE, new ItemStack(Material.IRON_SWORD));

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
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.BOW));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.ARROW, 64));

		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.OAK_FENCE, (byte)0, 2, F.item("Castle Barricade")));
		
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		
		player.getInventory().setHelmet(ItemStackFactory.Instance.CreateStack(Material.IRON_HELMET));
		player.getInventory().setChestplate(ItemStackFactory.Instance.CreateStack(Material.IRON_CHESTPLATE));
		player.getInventory().setLeggings(ItemStackFactory.Instance.CreateStack(Material.IRON_LEGGINGS));
		player.getInventory().setBoots(ItemStackFactory.Instance.CreateStack(Material.IRON_BOOTS));
	}
	
	@Override
	public void SpawnCustom(LivingEntity ent) 
	{
		ent.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
		ent.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		ent.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		ent.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
	}
}
