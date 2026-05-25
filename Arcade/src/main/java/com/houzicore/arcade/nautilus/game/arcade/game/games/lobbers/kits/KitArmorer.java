package com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits.perks.PerkCraftman;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDummy;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitArmorer extends Kit
{

	public KitArmorer(ArcadeManager manager)
	{
		super(manager, "Armorer", KitAvailability.Gem, 2000, // EN
				new String[]
				{
				C.cGray + "He uses his expert armor-making",
				C.cGray + "skills to block excess damage!"
				}, 
				// TH
				new String[]
				{
				C.cGray + "[TH] He uses his expert armor-making",
				C.cGray + "[TH] skills to block excess damage!"
				}, 
				new Perk[]
						{
				new PerkDummy("Armorer",
						new String[]
								{
						C.cGray + "Recieve " + C.cYellow + "Full Gold Armor"
								}),
				new PerkCraftman()
						}, EntityType.ZOMBIE,
				new ItemBuilder(Material.GOLDEN_HELMET).build());
	}
	
	@Override
	public void ApplyKit(Player player)
	{
		UtilInv.Clear(player);
		
		for (Perk perk : GetPerks())
			perk.Apply(player);
		
		GiveItemsCall(player);
		
		player.getInventory().setHelmet(new ItemBuilder(Material.GOLDEN_HELMET).setUnbreakable(true).build());
		player.getInventory().setChestplate(new ItemBuilder(Material.GOLDEN_CHESTPLATE).setUnbreakable(true).build());
		player.getInventory().setLeggings(new ItemBuilder(Material.GOLDEN_LEGGINGS).setUnbreakable(true).build());
		player.getInventory().setBoots(new ItemBuilder(Material.GOLDEN_BOOTS).setUnbreakable(true).build());
		
		UtilInv.Update(player);
	}

	@Override
	public void SpawnCustom(LivingEntity entity)
	{
		entity.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
		entity.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
		entity.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
	}
	
	@Override
	public void GiveItems(Player player)
	{
		
	}

}
