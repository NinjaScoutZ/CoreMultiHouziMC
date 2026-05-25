package nautilus.game.arcade.game.games.paintball.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import com.houzicore.shared.core.common.util.F;
import com.houzicore.shared.core.common.util.UtilInv;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.paintball.kits.perks.PerkPaintballRifle;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkSpeed;

public class KitRifle extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkPaintballRifle(),
					new PerkSpeed(0)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_BARDING, (byte) 0, 1, F.item("Paintball Rifle"))
			};

	private static final ItemStack[] PLAYER_ARMOR =
			{
					new ItemBuilder(Material.LEATHER_BOOTS).setUnbreakable(true).setColor(Color.WHITE).build(),
					new ItemBuilder(Material.LEATHER_LEGGINGS).setUnbreakable(true).setColor(Color.WHITE).build(),
					new ItemBuilder(Material.LEATHER_CHESTPLATE).setUnbreakable(true).setColor(Color.WHITE).build(),
					new ItemBuilder(Material.LEATHER_HELMET).setUnbreakable(true).setColor(Color.WHITE).build(),
			};

	public KitRifle(ArcadeManager manager)
	{
		super(manager, GameKit.PAINTBALL_RIFLE, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		UtilInv.insert(player, PLAYER_ITEMS[0]);

		ItemStack potion = new ItemStack(Material.POTION, 3, (short) 16429); // 16422
		PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
		potionMeta.setDisplayName(F.item("Water Bomb"));
		potion.setItemMeta(potionMeta);
		UtilInv.insert(player, potion);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
