package nautilus.game.arcade.game.games.halloween.waves;

import com.houzicore.shared.core.common.util.UtilTime;
import com.houzicore.arcade.nautilus.game.arcade.game.games.halloween.Halloween;
import com.houzicore.arcade.nautilus.game.arcade.game.games.halloween.HalloweenAudio;
import com.houzicore.arcade.nautilus.game.arcade.game.games.halloween.creatures.MobSkeletonArcher;
import com.houzicore.arcade.nautilus.game.arcade.game.games.halloween.creatures.MobSkeletonWarrior;

public class Wave1 extends WaveBase
{
	public Wave1(Halloween host) 
	{
		super(host, "Skeletons? Farmers? FARMER SKELETONS!!!", 60000, host.GetSpawnSet(1), HalloweenAudio.WAVE_1);
	}

	@Override
	public void Spawn(int tick) 
	{
		if (UtilTime.elapsed(_start, 30000))
			return;
		
		if (Host.GetCreatures().size() > Host.GetMaxMobs())
			return;
		
		if (tick % 10 == 0)
			Host.AddCreature(new MobSkeletonWarrior(Host, GetSpawn()));

		if (tick % 20 == 0)
			Host.AddCreature(new MobSkeletonArcher(Host, GetSpawn()));
	}
}
