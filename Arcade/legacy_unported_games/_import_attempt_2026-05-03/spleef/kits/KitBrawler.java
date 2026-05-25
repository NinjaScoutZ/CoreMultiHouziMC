package com.houzicore.arcade.nautilus.game.arcade.game.games.spleef.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.nautilus.game.arcade.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkKnockback;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkLeap;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkSmasher;

public class KitBrawler extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkLeap("Leap", 1.2, 1.2, 6000),
					new PerkSmasher(),
					new PerkKnockback(0.6)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE),
			};

	public KitBrawler(ArcadeManager manager)
	{
		super(manager, GameKit.SPLEEF_BRAWLER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}

