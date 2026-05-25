package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.core.itemstack.ItemBuilder;

import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.BedwarsSpecialItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public class BedwarsDeployPlatform extends BedwarsSpecialItem
{

	public static final ItemStack ITEM_STACK = new ItemBuilder(Material.WHITE_DYE)
			.setTitle(C.cYellow + C.Bold + "Deploy Platform")
			.addLore("", "Creates a platform of wool next to", "any block you click!", "Uses: " + C.cRed + "1")
			.build();
	private static final int PLATFORM_DELTA = 1;

	public BedwarsDeployPlatform(Bedwars game)
	{
		super(game, ITEM_STACK);
	}

	private org.bukkit.DyeColor getDyeColor(org.bukkit.ChatColor chatColor)
	{
		if (chatColor == org.bukkit.ChatColor.WHITE) return org.bukkit.DyeColor.WHITE;
		if (chatColor == org.bukkit.ChatColor.GOLD) return org.bukkit.DyeColor.ORANGE;
		if (chatColor == org.bukkit.ChatColor.LIGHT_PURPLE) return org.bukkit.DyeColor.PINK;
		if (chatColor == org.bukkit.ChatColor.AQUA) return org.bukkit.DyeColor.LIGHT_BLUE;
		if (chatColor == org.bukkit.ChatColor.YELLOW) return org.bukkit.DyeColor.YELLOW;
		if (chatColor == org.bukkit.ChatColor.GREEN) return org.bukkit.DyeColor.LIME;
		if (chatColor == org.bukkit.ChatColor.DARK_GRAY) return org.bukkit.DyeColor.GRAY;
		if (chatColor == org.bukkit.ChatColor.GRAY) return org.bukkit.DyeColor.LIGHT_GRAY;
		if (chatColor == org.bukkit.ChatColor.DARK_AQUA) return org.bukkit.DyeColor.CYAN;
		if (chatColor == org.bukkit.ChatColor.DARK_PURPLE) return org.bukkit.DyeColor.PURPLE;
		if (chatColor == org.bukkit.ChatColor.BLUE || chatColor == org.bukkit.ChatColor.DARK_BLUE) return org.bukkit.DyeColor.BLUE;
		if (chatColor == org.bukkit.ChatColor.DARK_GREEN) return org.bukkit.DyeColor.GREEN;
		if (chatColor == org.bukkit.ChatColor.RED || chatColor == org.bukkit.ChatColor.DARK_RED) return org.bukkit.DyeColor.RED;
		return org.bukkit.DyeColor.WHITE;
	}

	@Override
	protected boolean onClick(PlayerInteractEvent event, BedwarsTeam bedTeam)
	{
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (block == null)
		{
			return false;
		}

		BlockFace[] horizontals = { BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST };
		BlockFace face = horizontals[Math.round(player.getLocation().getYaw() / 90F) & 0x3].getOppositeFace();
		GameTeam team = bedTeam.getGameTeam();
		org.bukkit.DyeColor dyeColor = getDyeColor(team.GetColor());
		Material woolMaterial = Material.getMaterial(dyeColor.name() + "_WOOL");
		if (woolMaterial == null)
		{
			woolMaterial = Material.WHITE_WOOL;
		}
		boolean blockChanged = false;

		block = block.getRelative(face, 2);

		for (int x = -PLATFORM_DELTA; x <= PLATFORM_DELTA; x++)
		{
			for (int z = -PLATFORM_DELTA; z <= PLATFORM_DELTA; z++)
			{
				Block nearby = block.getRelative(x, 0, z);
				Location nearbyLocation = nearby.getLocation();

				if (isInvalidBlock(nearby))
				{
					continue;
				}

				_game.getBedwarsPlayerModule().getPlacedBlocks().add(nearby);
				MapUtil.QuickChangeBlockAt(nearbyLocation, woolMaterial);
				blockChanged = true;
			}
		}

		return blockChanged;
	}
}
