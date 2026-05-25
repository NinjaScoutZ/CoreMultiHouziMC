package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.core.itemstack.ItemBuilder;

import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.BedwarsSpecialItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public class BedwarsIceBridge extends BedwarsSpecialItem
{

	public static final ItemStack ITEM_STACK = new ItemBuilder(Material.ICE)
			.setTitle(C.cYellow + C.Bold + "Ice Bridge")
			.addLore("", "Creates a huge bridge of ice", "Warning! Ice Bridges have a", C.cRed + "20 second" + C.cGray + " cooldown between uses ", "and only last for " + C.cRed + "7 Seconds" + C.cGray + "!", "Uses: " + C.cRed + "1")
			.build();
	private static final int MAX_DISTANCE = 30;
	private static final long BRIDGE_TIME = TimeUnit.SECONDS.toMillis(7);

	public BedwarsIceBridge(Bedwars game)
	{
		super(game, ITEM_STACK, "Ice Bridge", TimeUnit.SECONDS.toMillis(20));
	}

	@Override
	protected boolean onClick(PlayerInteractEvent event, BedwarsTeam bedTeam)
	{
		event.setCancelled(true);

		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (block == null)
		{
			block = player.getLocation().getBlock();
		}
		else
		{
			block = block.getRelative(BlockFace.UP);
		}

		if (isInvalidBlock(block))
		{
			return false;
		}

		float yaw = player.getLocation().getYaw();
		BlockFace direction = BlockFace.NORTH;
		double rot = (yaw - 90) % 360;
		if (rot < 0) rot += 360;
		if (0 <= rot && rot < 45.0) direction = BlockFace.WEST;
		else if (45.0 <= rot && rot < 135.0) direction = BlockFace.NORTH;
		else if (135.0 <= rot && rot < 225.0) direction = BlockFace.EAST;
		else if (225.0 <= rot && rot < 315.0) direction = BlockFace.SOUTH;
		else if (315.0 <= rot && rot < 360.0) direction = BlockFace.WEST;
		BlockRestore restore = _game.getArcadeManager().GetBlockRestore();
		Block finalBlock = block;
		final BlockFace finalDirection = direction;

		_game.getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			Block target = finalBlock;
			int i = 0;

			@Override
			public void run()
			{
				if (!_game.IsLive())
				{
					cancel();
					return;
				}

				target = target.getRelative(finalDirection);
				boolean blockChanged = false;

				for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST })
				{
					Block nearby = target.getRelative(face);

					if (!isInvalidBlock(nearby))
					{
						restore.Add(nearby, com.houzicore.shared.common.util.IdUtil.getTypeId(Material.ICE), (byte) 0, BRIDGE_TIME + UtilMath.r(501));
						blockChanged = true;
					}
				}

				if (!blockChanged || i++ > MAX_DISTANCE)
				{
					cancel();
				}
				else
				{
					target.getWorld().playSound(target.getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 1);
				}
			}
		}, 0, 5);

		return true;
	}
}
