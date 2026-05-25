package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkAutoBridge;

public class KitBuilder extends Kit
{
	public KitBuilder(ArcadeManager manager)
	{
		super(manager, "Builder", KitAvailability.Gem, 5000, new String[0], new String[0], new Perk[0], EntityType.ZOMBIE, null);

		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData fallback = new com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData();
		fallback.name = "Builder";
		fallback.availability = KitAvailability.Gem;
		fallback.cost = 5000;
		fallback.entityType = EntityType.ZOMBIE;
		fallback.displayItem = Material.BRICKS;
		fallback.descEn = new String[] {
			"§7A master of §econstruction§7.",
			"",
			"§7Sneak and look down to build",
			"§eautomatic bridges§7!"
		};
		fallback.descTh = new String[] {
			"§7ปรมาจารย์ด้านการ §eก่อสร้าง",
			"",
			"§7ก้มหน้าและย่อตัวลงเพื่อต่อ",
			"§eสะพานอัตโนมัติ§7!"
		};

		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData data = com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.loadKitData(manager.getPlugin(), "skywars-kits.yml", "builder", fallback);

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
		this._kitPerks = new Perk[]{new PerkAutoBridge()};
		for (Perk perk : this._kitPerks) perk.SetHost(this);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(new ItemStack(Material.WOODEN_AXE));
		player.getInventory().addItem(new ItemStack(Material.WOODEN_PICKAXE));
		player.getInventory().addItem(new ItemStack(Material.BRICKS, 64));
		player.getInventory().addItem(new ItemStack(Material.BRICKS, 64));
	}
}
