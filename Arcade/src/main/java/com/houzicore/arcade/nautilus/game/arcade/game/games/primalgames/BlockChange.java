package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames;

import org.bukkit.Location;

public class BlockChange 
{
	public Location Location;
	public int Id;
	public byte Data;
	
	public BlockChange(Location loc, int id, byte data)
	{
		Location = loc;
		Id = id;
		Data = data;
	}
}
