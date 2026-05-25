package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.MeteorShowerData;

public class PerkMeteorShower extends SmashPerk
{
	private ArrayList<MeteorShowerData> _meteors = new ArrayList<MeteorShowerData>();
	
	public PerkMeteorShower() 
	{
		super("Meteor Shower", new String[] 
				{ 
				}, false);
	}

	@Override
	public void addSuperCustom(Player player)
	{ 
		_meteors.add(new MeteorShowerData(player, player.getTargetBlock(null, 128).getLocation()));
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		 if (event.getType() != UpdateType.TICK)
			 return;
		 
		 Iterator<MeteorShowerData> meteorIter = _meteors.iterator();
		 
		 while (meteorIter.hasNext())
		 {
			 MeteorShowerData data = meteorIter.next();
			 
			 if (data.update())
			 {
				 meteorIter.remove();
			 }
		 }
	}
}
