package nautilus.game.arcade.game.games.milkcow.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.disguise.disguises.DisguiseCow;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.milkcow.kits.perk.PerkCharge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.milkcow.kits.perk.PerkCowBomb;
import com.houzicore.arcade.nautilus.game.arcade.game.games.milkcow.kits.perk.PerkSeismicCow;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDamageSet;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkKnockbackMultiplier;

public class KitCow extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDamageSet(4),
					new PerkKnockbackMultiplier(4),
					new PerkCharge(),
					new PerkCowBomb(),
					new PerkSeismicCow()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
							C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Cow Bomb",
							new String[]
									{
											ChatColor.RESET + "Say goodbye to one of your children",
											ChatColor.RESET + "and hurl them towards your opponents.",
											ChatColor.RESET + "Explodes on impact, dealing knockback",
									}),
					ItemStackFactory.Instance.CreateStack(Material.IRON_SPADE, (byte) 0, 1,
							C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Body Slam",
							new String[]
									{
											ChatColor.RESET + "Hurl your giant fat cow-body forwards.",
											ChatColor.RESET + "Deals damage and knockback to anyone it",
											ChatColor.RESET + "collides with.",
									}),
					ItemStackFactory.Instance.CreateStack(Material.LEATHER, (byte) 0, 1,
							C.cYellow + C.Bold + "Sprint" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Cow Charge",
							new String[]
									{
											ChatColor.RESET + "Charge with great power, flinging",
											ChatColor.RESET + "filthy farmers out of your way, making them drop the milk in their buckets!",
									})
			};

	public KitCow(ArcadeManager manager)
	{
		super(manager, GameKit.MILK_THE_COW_COW, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);

		//Disguise
		DisguiseCow disguise = new DisguiseCow(player);
		disguise.setName(C.cRed + player.getName());
		disguise.setCustomNameVisible(true);
		Manager.GetDisguise().disguise(disguise);
	}
}
