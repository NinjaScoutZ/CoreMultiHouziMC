package nautilus.game.arcade.game.games.halloween.waves;

import com.houzicore.shared.core.common.util.UtilTime;
import com.houzicore.arcade.nautilus.game.arcade.game.games.halloween.Halloween;
import com.houzicore.arcade.nautilus.game.arcade.game.games.halloween.HalloweenAudio;
import com.houzicore.arcade.nautilus.game.arcade.game.games.halloween.creatures.MobSpiderLeaper;
import com.houzicore.arcade.nautilus.game.arcade.game.games.halloween.creatures.MobSpiderSmasher;

public class Wave3 extends WaveBase
{
	public Wave3(Halloween host) 
	{
		super(host, "Spiders, Spiders and even more Spiders!", 70000, host.GetSpawnSet(2), HalloweenAudio.WAVE_3);
	}

	@Override
	public void Spawn(int tick) 
	{
		if (Host.GetCreatures().size() > Host.GetMaxMobs())
			return;
		
		if (tick > 200 && tick % 10 == 0 && !UtilTime.elapsed(_start, 35000))
			Host.AddCreature(new MobSpiderSmasher(Host, GetSpawn()));
		
		if (tick % 8 == 0 && !UtilTime.elapsed(_start, 25000))
			Host.AddCreature(new MobSpiderLeaper(Host, GetSpawn()));
	}
}
