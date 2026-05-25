package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkChemist;

public class KitChemist extends Kit
{
	public KitChemist(ArcadeManager manager)
	{
		super(manager, "Chemist", KitAvailability.Gem, 5000, new String[0], new String[0], new Perk[0], EntityType.ZOMBIE, null);

		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData fallback = new com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData();
		fallback.name = "Chemist";
		fallback.availability = KitAvailability.Gem;
		fallback.cost = 5000;
		fallback.entityType = EntityType.ZOMBIE;
		fallback.displayItem = Material.SPLASH_POTION;
		fallback.descEn = new String[] {
			"§7Throw §ePotions §7to annoy your enemies!",
			"",
			"§7Gain potions when you §ekill enemies§7."
		};
		fallback.descTh = new String[] {
			"§7ปา §eน้ำยาเคมี §7เพื่อป่วนประสาทศัตรู!",
			"",
			"§7ได้รับน้ำยาเพิ่มเมื่อ §eสังหารศัตรู"
		};

		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData data = com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.loadKitData(manager.getPlugin(), "skywars-kits.yml", "chemist", fallback);

		this._kitName = data.name;
		this._kitAvailability = data.availability;
		this._cost = data.cost;
		this._entityType = data.entityType;
		if (data.displayItem != null) {
			this._displayItem = data.displayItem;
			this._itemInHand = new ItemStack(data.displayItem);
		}
		this._kitDesc = data.descEn;
		this._kitDescTh = data.descTh;
		this._kitPerks = new Perk[]{new PerkChemist()};
		for (Perk perk : this._kitPerks) perk.SetHost(this);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(new ItemStack(Material.WOODEN_AXE));
		player.getInventory().addItem(new ItemStack(Material.WOODEN_PICKAXE));
		
		// Poison Potion
		ItemStack poison = new ItemStack(Material.SPLASH_POTION);
		PotionMeta pMeta = (PotionMeta) poison.getItemMeta();
		pMeta.setBasePotionType(PotionType.POISON);
		poison.setItemMeta(pMeta);
		player.getInventory().addItem(poison);
		
		// Slowness Potion
		ItemStack slow = new ItemStack(Material.SPLASH_POTION);
		PotionMeta sMeta = (PotionMeta) slow.getItemMeta();
		sMeta.setBasePotionType(PotionType.SLOWNESS);
		slow.setItemMeta(sMeta);
		player.getInventory().addItem(slow);
	}

}
