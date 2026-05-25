package com.houzicore.shared.core.gadget.gadgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.houzicore.shared.api.disguise.DisguiseArchetype;
import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.gadget.event.GadgetBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class BlockForm {
	private final MorphBlock _host;
	private final Player _player;

	private final Material _mat;
	private Block _block;
	private Location _loc;

	public BlockForm(MorphBlock host, Player player, Material mat) {
		_host = host;
		_player = player;

		_mat = mat;
		_loc = player.getLocation();

		Apply();
	}

	public void Apply() {
		// Player > Falling Block Natively
        DisguiseRequest request = new DisguiseRequest(
				_player.getUniqueId(),
				DisguiseArchetype.DISPLAY_ONLY,
				"FALLING_BLOCK",
				true,
				false,
				false,
				null,
				false,
				Map.of("blockMaterial", _mat.name()));
        _host.Manager.getDisguiseManager().getService().apply(_player, request);

		// Inform
		final String blockName = F.elem(ItemStackFactory.Instance.GetName(_mat, (byte) 0, false));
		if (!blockName.contains("Block")) {
			UtilPlayer.message(_player, F.main("Morph", "You are now a "
					+ F.elem(ItemStackFactory.Instance.GetName(_mat, (byte) 0, false) + " Block") + "!"));
		} else {
			UtilPlayer.message(_player, F.main("Morph",
					"You are now a " + F.elem(ItemStackFactory.Instance.GetName(_mat, (byte) 0, false)) + "!"));
		}

		// Sound
		_player.playSound(_player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 2f, 2f);
	}

	public void FallingBlockCheck() {
		// Unused: Removed to support native LibsDisguise logic without passenger
	}

	public Block GetBlock() {
		return _block;
	}

	public void Remove() {
		SolidifyRemove();
		_host.Manager.getDisguiseManager().getService().clear(_player);
	}

	public void SolidifyRemove() {
		if (_block != null) {
			for (Player other : UtilServer.getPlayers()) {
				other.sendBlockChange(_block.getLocation(), _block.getBlockData());
			}
			_block = null;
		}

		_player.setExp(0f);

		// Host.Manager.GetCondition().EndCondition(Player, null, "Disguised as Block");

		// Inform
		_player.playSound(_player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);

		FallingBlockCheck();
	}

	public void SolidifyUpdate() {
		if (!_player.isSprinting()) {
			// NMS removed: ((org.bukkit.entity.Entity) _player).getHandle().getDataWatcher().watch(0, Byte.valueOf((byte) 32));
		}

		// Not a Block
		if (_block == null) {
			// Moved
			if (!_loc.getBlock().equals(_player.getLocation().getBlock())) {
				_player.setExp(0);
				_loc = _player.getLocation();
			}
			// Unmoved
			else {
				final double hideBoost = 0.025;

				_player.setExp((float) Math.min(0.999f, _player.getExp() + hideBoost));

				// Set Block
				if (_player.getExp() >= 0.999f) {
					final Block block = _player.getLocation().getBlock();

					final List<Block> blockList = new ArrayList<>();
					blockList.add(block);

					final GadgetBlockEvent event = new GadgetBlockEvent(_host, blockList);

					Bukkit.getServer().getPluginManager().callEvent(event);

					// Not Able
					if (block.getType() != Material.AIR || !UtilBlock.solid(block.getRelative(BlockFace.DOWN))
							|| event.getBlocks().isEmpty() || event.isCancelled()) {
						boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(_player);
						UtilPlayer.message(_player, F.main("Morph", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e44\u0e21\u0e48\u0e2a\u0e32\u0e21\u0e32\u0e23\u0e16\u0e41\u0e1bล\u0e07\u0e23\u0e48\u0e32\u0e07\u0e40\u0e1b\u0e47\u0e19\u0e1a\u0e25\u0e47\u0e2d\u0e01\u0e15\u0e23\u0e07\u0e19\u0e35\u0e49\u0e44\u0e14\u0e49" : "§7You cannot morph into a block here."));
						_player.setExp(0f);
						return;
					}

					// Set Block
					_block = block;

					// Snap Location precisely to block center
					Location center = block.getLocation().add(0.5, 0, 0.5);
					center.setYaw(_player.getLocation().getYaw());
					center.setPitch(_player.getLocation().getPitch());
					_player.teleport(center);
					_loc = _player.getLocation(); // update local reference so we don't revert!

					// Effect
					_player.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, _player.getLocation().add(0, 0.5, 0), 20, 0.3, 0.3, 0.3, 0.0, _mat.createBlockData());
					// block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, _mat);

					// Display
					SolidifyVisual();

					// Invisible
					// Host.Manager.GetCondition().Factory().Cloak("Disguised as Block", Player,
					// Player, 60000, false, false);

					// Sound
					_player.playSound(_player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
				}
			}
		}
		// Is a Block
		else {
			// Moved
			if (!_loc.getBlock().equals(_player.getLocation().getBlock())) {
				SolidifyRemove();
			}
			// Send Packets
			else {
				// SolidifyVisual(); - Removed to prevent movement spam and block flickering
			}
		}
	}

	public void SolidifyVisual() {
		if (_block == null)
			return;

		for (final Player other : UtilServer.getPlayers()) {
			if (other.equals(_player)) continue;
			other.sendBlockChange(_player.getLocation(), _mat.createBlockData());
		}

		// Self
		_player.sendBlockChange(_player.getLocation(), Material.MOVING_PISTON.createBlockData());
	}
}
