package nautilus.game.arcade.game.games.uhc;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.game.kit.GameKit;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class KitUHC extends Kit
{

	KitUHC(ArcadeManager manager)
	{
		super(manager, GameKit.UHC_PLAYER);
	}

	@Override
	public void GiveItems(Player player) 
	{
	}
}
