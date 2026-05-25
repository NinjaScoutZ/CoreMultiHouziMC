package com.houzicore.lobby.hub.modules.jumppad;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.command.CommandBase;

public class DoubleJumpSetupCommand extends CommandBase<JumpPadManager> {

	public DoubleJumpSetupCommand(JumpPadManager plugin) {
		super(plugin, Rank.ADMIN, "doublejumpsetup");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		Location loc = caller.getLocation();
		int count = 0;
		int radius = 100;
		
		caller.sendMessage(F.main("JumpPad", "Scanning " + radius + " block radius for Emerald Blocks..."));
		
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					Block b = loc.getBlock().getRelative(x, y, z);
					if (b.getType() == Material.EMERALD_BLOCK) {
						b.setType(Material.SLIME_BLOCK);
						count++;
					}
				}
			}
		}
		
		caller.playSound(caller.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
		UtilPlayer.message(caller, F.main("JumpPad", "§aSuccessfully converted §e" + count + " §aEmerald Blocks to Slime Block Jump Pads!"));
	}
}
