package nautilus.game.arcade.game.games.baconbrawl.kits;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.UtilEnt;
import com.houzicore.shared.core.disguise.disguises.DisguiseSheep;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkBackstabKnockback;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkPigCloak;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

public class KitSheepPig extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkPigCloak(),
					new PerkBackstabKnockback()
			};

	public KitSheepPig(ArcadeManager manager)
	{
		super(manager, GameKit.BACON_BRAWL_SHEEP, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_AXE));

		//Disguise
		DisguiseSheep disguise = new DisguiseSheep(player);
		disguise.setName(C.cYellow + player.getName());
		disguise.setCustomNameVisible(false);
		disguise.setColor(DyeColor.PINK);
		Manager.GetDisguise().disguise(disguise);
	}
}
