package nautilus.game.arcade.game.games.bridge.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bridge.Bridge;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDigger;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkOreFinder;

public class KitMiner extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkOreFinder(),
					new PerkDigger(),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.STONE_PICKAXE)
			};

	public KitMiner(ArcadeManager manager)
	{
		super(manager, GameKit.BRIDGES_MINER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		if (!(Manager.GetGame() instanceof Bridge))
		{
			return;
		}

		Bridge bridge = (Bridge) Manager.GetGame();

		if (!bridge.hasUsedRevive(player))
		{
			player.getInventory().addItem(PLAYER_ITEMS);
		}
	}
}
