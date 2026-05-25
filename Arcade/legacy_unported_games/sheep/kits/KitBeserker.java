package nautilus.game.arcade.game.games.sheep.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitBeserker extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump("Beserker Jump", 1.2, 1.2, true, 8000, true)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE),
					ItemStackFactory.Instance.CreateStack(Material.SADDLE, (byte) 0, 1, C.cYellow + C.Bold + "Hold This" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Grab/Hold Sheep")
			};

	public KitBeserker(ArcadeManager manager)
	{
		super(manager, GameKit.SHEEP_QUEST_BESERKER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
