package com.houzicore.arcade.nautilus.game.arcade.game.games.quiver.module;

import com.houzicore.shared.updater.UpdateType;
import com.houzicore.arcade.nautilus.game.arcade.game.games.quiver.QuiverTeamBase;

public abstract class QuiverTeamModule
{

	private QuiverTeamBase _base;
		
	public QuiverTeamModule(QuiverTeamBase base)
	{
		_base = base;
	}
	
	public abstract void setup();
	
	public abstract void update(UpdateType updateType);
	
	public abstract void finish();
	
	public void updateScoreboard()
	{
		
	}
	
	public QuiverTeamBase getBase()
	{
		return _base;
	}
		
}

