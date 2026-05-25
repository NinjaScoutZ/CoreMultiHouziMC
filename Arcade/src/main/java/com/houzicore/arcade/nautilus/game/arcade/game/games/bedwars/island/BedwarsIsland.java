package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.island;

import java.util.List;

import org.bukkit.Location;

public class BedwarsIsland
{

	private final List<Location> _blocks;

	private long _chestOpen;
	private boolean _crumbing;

	BedwarsIsland(List<Location> blocks)
	{
		_blocks = blocks;
	}

	public List<Location> getBlocks()
	{
		return _blocks;
	}

	public void setChestOpen()
	{
		_chestOpen = System.currentTimeMillis();
	}

	public long getChestOpen()
	{
		return _chestOpen;
	}

	public void setCrumbing(boolean crumbing)
	{
		_crumbing = crumbing;
	}

	public boolean isCrumbing()
	{
		return _crumbing;
	}
}
