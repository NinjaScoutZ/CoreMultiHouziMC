package nautilus.game.arcade.game.games.zombiesurvival.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.disguise.disguises.DisguiseSkeleton;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkIronSkin;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkLeap;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkRegeneration;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkStrength;

public class KitUndeadAlpha extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkLeap("Leap", 1, 1, 8000),
					new PerkStrength(2),
					new PerkIronSkin(2),
					new PerkRegeneration(1)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.STONE_AXE),
			};

	public KitUndeadAlpha(ArcadeManager manager)
	{
		super(manager, GameKit.ZOMBIE_SURVIVAL_ALPHA, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);

		DisguiseSkeleton disguise = new DisguiseSkeleton(player);
		disguise.SetSkeletonType(SkeletonType.WITHER);
		disguise.setName(Manager.GetGame().GetTeam(player).GetColor() + player.getName());
		disguise.setCustomNameVisible(true);
		Manager.GetDisguise().disguise(disguise);
	}
}
