package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkChicken;

public class KitEggman extends Kit
{

	static PerkChicken p;
	
	public KitEggman(ArcadeManager manager)
	{ 
		super(manager, "Eggman", KitAvailability.Free, 0, new String[0], new String[0], new Perk[0], EntityType.CHICKEN, null);

		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData fallback = new com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData();
		fallback.name = "Eggman";
		fallback.availability = KitAvailability.Free;
		fallback.cost = 0;
		fallback.entityType = EntityType.CHICKEN;
		fallback.displayItem = Material.EGG;
		fallback.descEn = new String[] {
			"§7Throw §eEggs §7to knock enemies into the void!",
			"",
			"§7Your §echicken §7will warn you of enemies."
		};
		fallback.descTh = new String[] {
			"§7ปา §eไข่ §7เพื่อผลักศัตรูให้ตกลงไปในห้วงอากาศ!",
			"",
			"§eไก่ §7ของคุณจะคอยส่งเสียงเตือนภัยเมื่อมีศัตรู"
		};

		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData data = com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.loadKitData(manager.getPlugin(), "skywars-kits.yml", "eggman", fallback);

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
		this._kitPerks = new Perk[]{p = new PerkChicken(manager)};
		for (Perk perk : this._kitPerks) perk.SetHost(this);
	}

	@Override
	public void GiveItems(Player player)
	{
		p.spawnChicken(player, player.getLocation());
	
		player.getInventory().addItem(new ItemStack(Material.EGG, 16));
		player.getInventory().addItem(new ItemStack(Material.WOODEN_AXE));
		player.getInventory().addItem(new ItemStack(Material.WOODEN_PICKAXE));
	}
}
