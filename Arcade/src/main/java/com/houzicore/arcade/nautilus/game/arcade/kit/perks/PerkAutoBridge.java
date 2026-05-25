package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilPlayer;

public class PerkAutoBridge extends Perk
{
	private NautHashMap<Player, Long> _cooldowns = new NautHashMap<Player, Long>();

	public PerkAutoBridge()
	{
		super("Auto Bridge", new String[]
		{
			"Sneak while looking down to build a bridge",
			"Consumes blocks from your inventory.",
			"Maximum length: 16 blocks.",
			"Cooldown: 2 Seconds"
		});
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event)
	{
		if (!event.isSneaking())
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Manager.GetGame().IsLive())
			return;

		if (!Manager.GetGame().IsAlive(player))
			return;

		// Require pitch > 60 degrees (looking down)
		if (player.getLocation().getPitch() < 60)
			return;

		if (_cooldowns.containsKey(player) && !UtilTime.elapsed(_cooldowns.get(player), 2000))
		{
			// UtilPlayer.message(player, F.main("Kit", "Auto Bridge is on cooldown."));
			return;
		}

		// Find blocks in inventory
		ItemStack blocksToUse = null;
		for (ItemStack item : player.getInventory().getContents())
		{
			if (item != null && item.getType().isBlock() && item.getType().isSolid() && item.getType() != Material.TNT)
			{
				blocksToUse = item;
				break;
			}
		}

		if (blocksToUse == null)
		{
			com.houzicore.shared.common.util.UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("Kit", "You don't have any blocks to bridge with."));
			return;
		}

		Block startBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

		org.bukkit.util.Vector dir = player.getLocation().getDirection().setY(0).normalize();

		int blocksPlaced = 0;
		int maxBlocks = Math.min(16, blocksToUse.getAmount());
		Material type = blocksToUse.getType();

		for (int i = 1; i <= maxBlocks; i++)
		{
			Block target = startBlock.getLocation().add(dir.clone().multiply(i)).getBlock();

			if (target.getType() != Material.AIR)
				break; // Stop if hit something

			target.setType(type);
			target.getWorld().playSound(target.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
			blocksPlaced++;
		}

		if (blocksPlaced > 0)
		{
			blocksToUse.setAmount(blocksToUse.getAmount() - blocksPlaced);
			_cooldowns.put(player, System.currentTimeMillis());
		}
	}
}
