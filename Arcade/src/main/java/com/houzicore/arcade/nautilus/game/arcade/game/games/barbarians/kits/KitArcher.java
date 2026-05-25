package com.houzicore.arcade.nautilus.game.arcade.game.games.barbarians.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFletcher;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkRopedArrow;

public class KitArcher extends Kit 
{
	public KitArcher(ArcadeManager manager)
	{
		super(manager, "Barbarian Archer", KitAvailability.Gem, 
				// EN
				new String[] 
				{
					"Uses some kind of less barbaric ranged weapon..."
				}, 
				// TH
				new String[] 
				{
					"[TH] Uses some kind of less barbaric ranged weapon..."
				}, 
				new Perk[] 
				{
					new PerkRopedArrow("Roped Arrow", 1, 6000),
					new PerkFletcher(2, 2, true),
				}, 
				EntityType.PLAYER,	
				new ItemStack(Material.BOW));

	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_AXE));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.BOW));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		
		player.getInventory().setHelmet(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET));
		player.getInventory().setChestplate(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE));
		player.getInventory().setLeggings(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS));
		player.getInventory().setBoots(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS));
	}
	
	@Override
	public void SpawnCustom(LivingEntity ent) 
	{
		ent.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
		ent.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
		ent.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
		ent.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
	}
}
