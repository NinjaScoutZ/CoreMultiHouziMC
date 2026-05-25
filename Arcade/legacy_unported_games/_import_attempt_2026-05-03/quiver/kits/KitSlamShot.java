package com.houzicore.arcade.nautilus.game.arcade.game.games.quiver.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.arcade.nautilus.game.arcade.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkSeismicSlamOITQ;

public class KitSlamShot extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkSeismicSlamOITQ()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.DIAMOND_SPADE),
					ItemStackFactory.Instance.CreateStack(Material.BOW)
			};

	public KitSlamShot(ArcadeManager manager)
	{
		super(manager, GameKit.OITQ_SLAM_SHOOTER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);

		if (Manager.GetGame().GetState() == GameState.Live)
		{
			player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(262, (byte) 0, 1, F.item("Super Arrow")));

			final Player fPlayer = player;

			UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					UtilInv.Update(fPlayer);
				}
			}, 10);
		}
	}
}

