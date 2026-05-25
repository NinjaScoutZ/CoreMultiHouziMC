package com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.challenges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.disguise.disguises.DisguiseMagmaCube;
import com.houzicore.shared.core.disguise.disguises.DisguiseVillager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.Challenge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.MineWare;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class ChallengeLavaRun extends Challenge
{
	private long _delay;
	private long _minusDelay = 1000;
	private int _disappearingBlocks = 10;
	private Location _obsidian;

	public ChallengeLavaRun(MineWare host)
	{
		super(host, ChallengeType.LastStanding, "The lava is coming! Stand on the obsidian!");
	}

	@Override
	public ArrayList<Location> getSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (int x = -7; x <= 7; x++)
		{
			for (int z = -7; z <= 7; z++)
			{
				spawns.add(getCenter().clone().add(x + 0.5, 2, z + 0.5));
			}
		}

		return spawns;
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() != null)
		{
			return;
		}

  // /* event.AddMod(...) */;
	}

	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (_delay > System.currentTimeMillis())
			return;

		if (_obsidian == null)
		{
			_obsidian = getCenter().add(UtilMath.r(21) - 9.5, 1, UtilMath.r(21) - 9.5);

			for (int x = -10; x <= 10; x++)
			{
				for (int z = -10; z <= 10; z++)
				{
					Block b = getCenter().getBlock().getRelative(x, 1, z);

					b.setType(Material.GLASS);
					addBlock(b);
				}
			}

			_obsidian.getBlock().setType(Material.OBSIDIAN);

			for (Player player : UtilServer.getPlayers())
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 2f, 0f);

			_delay = System.currentTimeMillis() + _minusDelay;
			_minusDelay -= 100;

			_disappearingBlocks++;
		}
		else
		{
			ArrayList<Block> glassBlocks = new ArrayList<Block>();

			for (int x = -10; x <= 10; x++)
			{
				for (int z = -10; z <= 10; z++)
				{
					Block b = getCenter().getBlock().getRelative(x, 1, z);

					if (b.getType() == Material.GLASS)
					{
						glassBlocks.add(b);
					}
				}
			}

			if (glassBlocks.isEmpty())
			{
				_delay = System.currentTimeMillis() + 1500;
				_obsidian = null;
			}
			else
			{
				final HashMap<Block, Double> distance = new HashMap<Block, Double>();

				for (Block b : glassBlocks)
				{
					distance.put(b, b.getLocation().add(0.5, 0, 0.5).distance(_obsidian));
				}

				Collections.sort(glassBlocks, new Comparator<Block>()
				{

					@Override
					public int compare(Block o1, Block o2)
					{
						return distance.get(o2).compareTo(distance.get(o1));
					}
				});

				for (int i = 0; i < Math.min(_disappearingBlocks, glassBlocks.size()); i++)
				{
					Block b = glassBlocks.remove(0);
					b.setType(Material.AIR);
				}
			}
		}
		
		for (int x = -10; x <= 10; x++)
		{
			for (int z = -10; z <= 10; z++)
			{
				Block block = getCenter().getBlock().getRelative(x, 0, z);
				block.setType(Material.LAVA);
				addBlock(block);

				if (Math.abs(x) <= 10 && Math.abs(z) <= 10)
				{
					Block b = block.getRelative(BlockFace.UP);
					b.setType(Material.GLASS);
					addBlock(b);
				}
			}
		}

		_obsidian = getCenter().add(UtilMath.r(21) - 9.5, 1, UtilMath.r(21) - 9.5);

		_obsidian.getBlock().setType(Material.OBSIDIAN);
	}


	@Override
	public void generateRoom() {
		// TODO: implement
	}

	@Override
	public void setupPlayers() {
		// TODO: implement
	}

	@Override
	public void cleanupRoom() {
		// TODO: implement
	}
}
