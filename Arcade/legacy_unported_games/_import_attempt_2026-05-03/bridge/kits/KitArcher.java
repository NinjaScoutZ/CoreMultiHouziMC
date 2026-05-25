package com.houzicore.arcade.nautilus.game.arcade.game.games.bridge.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.nautilus.game.arcade.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bridge.Bridge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.skeleton.PerkBarrage;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFletcher;

public class KitArcher extends Kit
{


	private static final Perk[] PERKS =
			{
					new PerkFletcher(20, 4, true),
					new PerkBarrage(5, 250, true, false),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.BOW)
			};

	public KitArcher(ArcadeManager manager)
	{
		super(manager, GameKit.BRIDGES_ARCHER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		if (!(Manager.GetGame() instanceof Bridge))
		{
			return;
		}

		Bridge bridge = (Bridge) Manager.GetGame();

		if (!bridge.hasUsedRevive(player))
		{
			player.getInventory().addItem(PLAYER_ITEMS);
		}
	}
}

