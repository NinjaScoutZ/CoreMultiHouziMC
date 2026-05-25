package com.houzicore.arcade.nautilus.game.arcade.game.games.barbarians.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

public class KitBomber extends Kit
{
	public KitBomber(ArcadeManager manager)
	{
		super(manager, "Bomber", KitAvailability.Gem, 5000,

				// EN
				new String[] 
						{
				"Crazy bomb throwing barbarian. BOOM!"
						}, 
				// TH
				new String[] 
						{
				"[TH] Crazy bomb throwing barbarian. BOOM!"
						}, 
				new Perk[] 
								{
				new PerkBomber(8, 2, -1),
				new PerkLeap("Leap", 1, 1, 8000)
								}, 
								EntityType.ZOMBIE,
								new ItemStack(Material.TNT));

	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_AXE));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.TNT, (byte)0, 1, F.item("Throwing TNT")));
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
