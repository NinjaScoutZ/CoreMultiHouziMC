package com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits;

import com.houzicore.shared.core.achievement.Achievement;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits.perks.PerkCraftman;
import com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits.perks.PerkWaller;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class KitWaller extends Kit
{
	public KitWaller(ArcadeManager manager)
	{
		super(manager, "Waller", KitAvailability.Achievement, 0, // EN
				new String[]
				{
				"When the times get tough,",
				"build yourself a wall!"
				}, 
				// TH
				new String[]
				{
				"[TH] When the times get tough,",
				"[TH] build yourself a wall!"
				}, 
				new Perk[]
						{
				new PerkWaller(),
				new PerkCraftman()
						}, EntityType.ZOMBIE,
				new ItemBuilder(Material.STONE_BRICKS).setUnbreakable(true).build());
		
		this.setAchievementRequirements(new Achievement[]
				{
				Achievement.BOMB_LOBBERS_WINS,
				Achievement.BOMB_LOBBERS_ULTIMATE_KILLER,
				Achievement.BOMB_LOBBERS_SNIPER,
				Achievement.BOMB_LOBBERS_PROFESSIONAL_LOBBER,
				Achievement.BOMB_LOBBERS_EXPLOSION_PROOF,
				Achievement.BOMB_LOBBERS_BLAST_PROOF
				});
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(1, new ItemBuilder(Material.STONE_SHOVEL).setAmount(3).setTitle(F.item("Wall Builder")).build());
	}

}

