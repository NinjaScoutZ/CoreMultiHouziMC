package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDigger;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkLootMultiplier;

public class KitProspector extends Kit
{

	public KitProspector(ArcadeManager manager)
	{
		super(manager, "Prospector", KitAvailability.Free, 0, new String[0], new String[0], new Perk[0], EntityType.ZOMBIE, null);

		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData fallback = new com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData();
		fallback.name = "Prospector";
		fallback.availability = KitAvailability.Free;
		fallback.cost = 0;
		fallback.entityType = EntityType.ZOMBIE;
		fallback.displayItem = Material.IRON_PICKAXE;
		fallback.descEn = new String[] {
			"§7Mine blocks at §eextreme speeds§7!",
			"§7Find §eextra items §7in chests."
		};
		fallback.descTh = new String[] {
			"§7ขุดบล็อกด้วยความเร็วระดับ §eบ้าคลั่ง§7!",
			"§7ค้นพบ §eไอเทมพิเศษ §7จากหีบสมบัติ"
		};

		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData data = com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.loadKitData(manager.getPlugin(), "skywars-kits.yml", "prospector", fallback);

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
		this._kitPerks = new Perk[]{new PerkDigger(), new PerkLootMultiplier()};
		for (Perk perk : this._kitPerks) perk.SetHost(this);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(new ItemStack(Material.STONE_AXE));
		player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
	}

}
