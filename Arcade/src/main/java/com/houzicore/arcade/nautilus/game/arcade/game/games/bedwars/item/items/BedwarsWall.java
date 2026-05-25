package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.core.itemstack.ItemBuilder;

import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.BedwarsSpecialItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public class BedwarsWall extends BedwarsSpecialItem
{

	public static final ItemStack ITEM_STACK = new ItemBuilder(Material.WHITE_WOOL)
			.setTitle(C.cYellow + C.Bold + "Wool Wall")
			.addLore("", "Creates a wall of wool above", "any block you click!", "Uses: " + C.cRed + "1")
			.build();
	private static final int PLATFORM_DELTA = 1;
	private static final int WALL_WARMUP_TICKS = 40;

	public BedwarsWall(Bedwars game)
	{
		super(game, ITEM_STACK, "Wool Wall", 500);
	}

	@Override
	protected boolean onClick(PlayerInteractEvent event, BedwarsTeam bedTeam)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return false;
		}

		event.setCancelled(true);

		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		BlockFace face = player.getFacing().getOppositeFace();
		GameTeam team = bedTeam.getGameTeam();
		Material woolMaterial = getWoolMaterial(team.GetColor());
		boolean blockChanged = false;

		List<Block> changed = new ArrayList<>();
		boolean xAxis = face == BlockFace.NORTH || face == BlockFace.SOUTH;
		block = block.getRelative(BlockFace.UP, 2);

		for (int x = -PLATFORM_DELTA; x <= PLATFORM_DELTA; x++)
		{
			for (int y = -PLATFORM_DELTA; y <= PLATFORM_DELTA; y++)
			{
				Block nearby = block.getRelative(xAxis ? x : 0, y, xAxis ? 0 : x);

				if (isInvalidBlock(nearby))
				{
					continue;
				}

				_game.getBedwarsPlayerModule().getPlacedBlocks().add(nearby);
				changed.add(nearby);
				blockChanged = true;
			}
		}

		Color color = team.GetColorBase();

		_game.getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			int ticks = 0;

			@Override
			public void run()
			{
				if (++ticks == WALL_WARMUP_TICKS)
				{
					cancel();

					for (Block wall : changed)
					{
						MapUtil.QuickChangeBlockAt(wall.getLocation(), woolMaterial);

						if (Math.random() > 0.5)
						{
							wall.getWorld().playEffect(wall.getLocation(), Effect.STEP_SOUND, woolMaterial);
						}
					}
				}
				else
				{
					int index = 0;
					double maxY = ((double) ticks / WALL_WARMUP_TICKS) * 3;

					for (Block wall : changed)
					{
						if (index++ % 3 == 0)
						{
							for (double y = 0; y < maxY; y += 0.2)
							{
								wall.getWorld().spawnParticle(
										Particle.DUST,
										wall.getLocation().add(xAxis ? Math.random() : 0.5, y, xAxis ? 0.5 : Math.random()),
										1,
										0, 0, 0,
										0,
										new DustOptions(color, 1.0f)
								);
							}
						}
					}
				}
			}
		}, 0, 1);

		return blockChanged;
	}

	private Material getWoolMaterial(org.bukkit.ChatColor chatColor)
	{
		try
		{
			String colorName = chatColor.name();
			if (chatColor == org.bukkit.ChatColor.LIGHT_PURPLE) colorName = "PINK";
			else if (chatColor == org.bukkit.ChatColor.DARK_GREEN) colorName = "GREEN";
			else if (chatColor == org.bukkit.ChatColor.DARK_BLUE || chatColor == org.bukkit.ChatColor.BLUE) colorName = "BLUE";
			else if (chatColor == org.bukkit.ChatColor.GOLD) colorName = "ORANGE";
			else if (chatColor == org.bukkit.ChatColor.GREEN) colorName = "LIME";
			else if (chatColor == org.bukkit.ChatColor.DARK_GRAY) colorName = "GRAY";
			else if (chatColor == org.bukkit.ChatColor.GRAY) colorName = "LIGHT_GRAY";
			else if (chatColor == org.bukkit.ChatColor.DARK_AQUA) colorName = "CYAN";
			else if (chatColor == org.bukkit.ChatColor.DARK_PURPLE) colorName = "PURPLE";
			return Material.valueOf(colorName + "_WOOL");
		}
		catch (Exception e)
		{
			return Material.WHITE_WOOL;
		}
	}
}
