package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFletcher;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkKnockbackArrow;

public class KitArcher extends Kit
{
	public KitArcher(ArcadeManager manager)
	{
		super(manager, "Archer", KitAvailability.Gem, 5000, new String[0], new String[0], new Perk[0], EntityType.ZOMBIE, null);
		
		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData fallback = new com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData();
		fallback.name = "Archer";
		fallback.availability = KitAvailability.Gem;
		fallback.cost = 5000;
		fallback.entityType = EntityType.ZOMBIE;
		fallback.displayItem = Material.BOW;
		fallback.descEn = new String[] {
			"§7A skilled marksman with a §ebow§7.",
			"",
			"§7Arrows deal §eheavy knockback§7."
		};
		fallback.descTh = new String[] {
			"§7นักแม่นธนูผู้เชี่ยวชาญการใช้ §eธนู§7",
			"",
			"§7ลูกธนูจะทำให้เป้าหมาย §eกระเด็นถอยหลังอย่างรุนแรง"
		};
		
		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData data = com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.loadKitData(manager.getPlugin(), "skywars-kits.yml", "archer", fallback);
		
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
		this._kitPerks = new Perk[]{new PerkFletcher(2, 2, false), new PerkKnockbackArrow(1.5)};
		for (Perk perk : this._kitPerks) perk.SetHost(this);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(new ItemStack(Material.WOODEN_AXE));
		player.getInventory().addItem(new ItemStack(Material.WOODEN_PICKAXE));
		player.getInventory().addItem(new ItemStack(Material.BOW));
		player.getInventory().addItem(new ItemStack(Material.ARROW, 4));
	}
}
