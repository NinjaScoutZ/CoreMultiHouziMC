package nautilus.game.arcade.game.games.bridge.kits;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.game.kit.GameKit;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDestructor;

public class KitDestructor extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDestructor(40, 2, 400, 3, false)
			};

	public KitDestructor(ArcadeManager manager)
	{
		super(manager, GameKit.BRIDGES_DESTRUCTOR, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}

	public void SetEnabled(boolean var)
	{
		for (Perk perk : this.GetPerks())
		{
			if (perk instanceof PerkDestructor)
			{
				((PerkDestructor) perk).setEnabled(var);
			}
		}
	}
}
