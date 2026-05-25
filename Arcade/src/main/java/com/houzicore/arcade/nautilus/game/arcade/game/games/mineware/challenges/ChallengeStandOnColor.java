package com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.challenges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.Challenge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.MineWare;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

public class ChallengeStandOnColor extends Challenge
{
	private ArrayList<Integer> _colors = new ArrayList<Integer>();
	private int _currentColor;
	private boolean _isFalling;
	private long _lastSound;
	private long _stageExpires;
	private long _timeDelay = 3000;

	private static final Material[] COLORS = {
		Material.WHITE_TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.MAGENTA_TERRACOTTA,
		Material.LIGHT_BLUE_TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.LIME_TERRACOTTA,
		Material.PINK_TERRACOTTA, Material.GRAY_TERRACOTTA, Material.LIGHT_GRAY_TERRACOTTA,
		Material.CYAN_TERRACOTTA, Material.PURPLE_TERRACOTTA, Material.BLUE_TERRACOTTA,
		Material.BROWN_TERRACOTTA, Material.GREEN_TERRACOTTA, Material.RED_TERRACOTTA,
		Material.BLACK_TERRACOTTA
	};

	public ChallengeStandOnColor(MineWare host)
	{
		super(host, ChallengeType.LastStanding, "Stand on the correct color");

		for (int i = 0; i <= 15; i++)
		{
			_colors.add(i);
		}

		_colors.remove(Integer.valueOf(2));
		_colors.remove(Integer.valueOf(6));
		_colors.remove(Integer.valueOf(7));
		_colors.remove(Integer.valueOf(9));
		_colors.remove(Integer.valueOf(12));
	}

	@Override
	public void cleanupRoom()
	{
		for (Player player : Host.GetPlayers(true))
		{
			UtilInv.Clear(player);
		}
	}

	private void generateFloor()
	{
		ArrayList<Entry<Integer, Integer>> cords = new ArrayList<Entry<Integer, Integer>>();

		int i = UtilMath.r(_colors.size());

		for (int x = -4; x <= 4; x++)
		{
			for (int z = -4; z <= 4; z++)
			{
				cords.add(new HashMap.SimpleEntry(x * 2, z * 2));
			}
		}

		Collections.shuffle(cords);

		for (Entry<Integer, Integer> entry : cords)
		{
			byte color = (byte) (int) _colors.get(i++);

			if (i >= _colors.size())
			{
				i = 0;
			}
			for (int x = 0; x <= 1; x++)
			{
				for (int z = 0; z <= 1; z++)
				{
					Block b = getCenter().getBlock().getRelative(entry.getKey() + x, 0, entry.getValue() + z);

					b.setType(COLORS[color]);
					addBlock(b);
				}
			}
		}
	}

	@Override
	public void generateRoom()
	{
		setBorder(-20, 20, 0, 10, -20, 20);
		generateFloor();
	}

	@Override
	public ArrayList<Location> getSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (int x = -7; x <= 7; x++)
		{
			for (int z = -7; z <= 7; z++)
			{
				if (x % 2 == 0 && z % 2 == 0)
				{
					spawns.add(getCenter().clone().add(x + 0.5, 1.1, z + 0.5));
				}
			}
		}

		return spawns;
	}

	@EventHandler
	public void OnTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (!Host.IsLive())
		{
			return;
		}

		Player[] players = getChallengers().toArray(new Player[0]);

		if (_stageExpires < System.currentTimeMillis())
		{
			UtilTextBottom.displayProgress(0, players);

			if (_isFalling)
			{
				for (Player player : UtilServer.getPlayers())
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 2f, 0f);

				_timeDelay *= 0.8;
				_isFalling = false;
				_stageExpires = System.currentTimeMillis() + _timeDelay;
				_currentColor = _colors.get(UtilMath.r(_colors.size()));
				generateFloor();

				for (Player player : getChallengers())
				{
					for (int i = 0; i < 9; i++)
					{
						player.getInventory().setItem(i, new ItemStack(COLORS[_currentColor], 1));
					}
				}
			}
			else
			{
				_isFalling = true;
				_stageExpires = System.currentTimeMillis() + 4000;

				for (int x = -8; x <= 9; x++)
				{
					for (int z = -8; z <= 9; z++)
					{
						Block b = getCenter().getBlock().getRelative(x, 0, z);

						if (b.getType() != COLORS[_currentColor])
						{
							b.setType(Material.AIR);
						}
					}
				}
			}

			UtilTextBottom.displayProgress(1D - (double) (_stageExpires - System.currentTimeMillis()) / (double) _timeDelay, players);

			if (_lastSound < System.currentTimeMillis())
			{
				_lastSound = System.currentTimeMillis() + 1000;

				for (Player player : UtilServer.getPlayers())
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
			}
		}
		else
		{
			UtilTextBottom.displayProgress(0, players);
		}
	}

	@Override
	public void setupPlayers()
	{
		_stageExpires = System.currentTimeMillis() + _timeDelay;
		_currentColor = _colors.get(UtilMath.r(_colors.size()));

		for (Player player : Host.GetPlayers(true))
		{
			for (int i = 0; i < 9; i++)
			{
				player.getInventory().setItem(i, new ItemStack(COLORS[_currentColor], 1));
			}
		}
	}

}
