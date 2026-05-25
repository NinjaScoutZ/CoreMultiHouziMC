package com.houzicore.arcade.nautilus.game.arcade.scoreboard;

import java.util.ArrayList;
import java.util.HashMap;

public class ScoreboardElementScores extends ScoreboardElement
{
	private String _key;
	
	private HashMap<String, Integer> _scores;
	
	private boolean _higherIsBetter;
	
	public ScoreboardElementScores(String key, String line, int value, boolean higherIsBetter)
	{
		_scores = new HashMap<String, Integer>();
		
		_key = key;
		
		AddScore(line, value);
		
		_higherIsBetter = higherIsBetter;
	}
	
	@Override
	public ArrayList<ScoreboardLine> GetLines()
	{
		ArrayList<String> orderedKeys = new ArrayList<String>();
		
		//Order Scores
		while (orderedKeys.size() < _scores.size())
		{
			String bestKey = null;
			int bestScore = 0;
			
			for (String key : _scores.keySet())
			{
				if (orderedKeys.contains(key))
					continue;
				
				if (bestKey == null || 
						(_higherIsBetter && _scores.get(key) >= bestScore) ||
						(!_higherIsBetter && _scores.get(key) <= bestScore))
				{
					bestKey = key;
					bestScore = _scores.get(key);
				}
			}
			
			orderedKeys.add(bestKey);
		}

		ArrayList<ScoreboardLine> orderedScores = new ArrayList<ScoreboardLine>();
		for (String key : orderedKeys)
		{
			orderedScores.add(ScoreboardLine.legacy(key));
		}
		
		return orderedScores;
	}

	public boolean IsKey(String key)
	{
		return _key.equals(key);
	}

	public void AddScore(String line, int value)
	{
		_scores.put(line, value);
	}
}
