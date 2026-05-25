package com.houzicore.arcade.nautilus.game.arcade.scoreboard;

import java.util.ArrayList;

public class ScoreboardElementText extends ScoreboardElement
{
	private String _line;
	
	public ScoreboardElementText(String line)
	{
		_line = line;
	}
	
	@Override
	public ArrayList<ScoreboardLine> GetLines()
	{
		ArrayList<ScoreboardLine> orderedScores = new ArrayList<ScoreboardLine>();
		
		orderedScores.add(ScoreboardLine.legacy(_line));
		
		return orderedScores;
	}
	
}
