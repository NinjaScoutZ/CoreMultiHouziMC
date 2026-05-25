package com.houzicore.arcade.nautilus.game.arcade.game;

public class EssenceData 
{
	public double Gems;
	public int Amount;
	
	public EssenceData(double gems, boolean amount)
	{
		Gems = gems;
		
		if (amount)
			Amount = 1;
	}
	
	public void AddGems(double gems)
	{
		Gems += gems;
		
		if (Amount > 0)
			Amount++;
	}
}
