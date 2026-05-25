package nautilus.game.arcade.game.games.baconbrawl.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.disguise.disguises.DisguisePig;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.baconbrawl.kits.perks.PerkCrispyBacon;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class KitChrisPBacon extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkCrispyBacon()
			};

	public KitChrisPBacon(ArcadeManager manager)
	{
		super(manager, GameKit.BACON_BRAWL_CHRIS_P_BACON, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_AXE));

		DisguisePig disguise = new DisguisePig(player);
		disguise.setName(C.cYellow + player.getName());
		disguise.setCustomNameVisible(false);
		Manager.GetDisguise().disguise(disguise);
	}
}
