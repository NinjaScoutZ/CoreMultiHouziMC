package nautilus.game.arcade.game.games.deathtag.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFletcher;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkKnockbackArrow;

public class KitRunnerArcher extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkKnockbackArrow(3),
					new PerkFletcher(2, 2, true),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.BOW)
			};

	public KitRunnerArcher(ArcadeManager manager)
	{
		super(manager, GameKit.DEATH_TAG_ARCHER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
