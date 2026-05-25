package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.achievement.Achievement;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

public class KitDestructor extends Kit
{
	public KitDestructor(ArcadeManager manager)
	{
		super(manager, "Destructor", KitAvailability.Achievement, 0, new String[0], new String[0], new Perk[0], EntityType.ZOMBIE, null);

		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData fallback = new com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData();
		fallback.name = "Destructor";
		fallback.availability = KitAvailability.Achievement;
		fallback.cost = 0;
		fallback.entityType = EntityType.ZOMBIE;
		fallback.displayItem = Material.ENDER_PEARL;
		fallback.descEn = new String[] {
			"§eEnder Pearls §7make the world crumble!",
			"",
			"§7Your pearls also §cwither §7your enemies."
		};
		fallback.descTh = new String[] {
			"§eมุกเอนเดอร์ §7ของคุณจะทำให้แผนที่ถล่มทลาย!",
			"",
			"§7มุกของคุณจะทำให้เป้าหมายติดสถานะ §cWither"
		};

		com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData data = com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.loadKitData(manager.getPlugin(), "skywars-kits.yml", "destructor", fallback);

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
		this._kitPerks = new Perk[]{new PerkDestructor(40, 2, 2500, true)};
		for (Perk perk : this._kitPerks) perk.SetHost(this);

		this.setAchievementRequirements(new Achievement[] 
				{
				Achievement.SKYWARS_BOMBER,
				Achievement.SKYWARS_NOARMOR,
				Achievement.SKYWARS_NOCHEST,
				Achievement.SKYWARS_PLAYER_KILLS,
				Achievement.SKYWARS_TNT,
				Achievement.SKYWARS_WINS,
				Achievement.SKYWARS_ZOMBIE_KILLS
				});
	}

	@Override
	public void GiveItems(Player player) 
	{
		player.getInventory().addItem(new ItemStack(Material.WOODEN_AXE));
		player.getInventory().addItem(new ItemStack(Material.WOODEN_PICKAXE));
	}
}
