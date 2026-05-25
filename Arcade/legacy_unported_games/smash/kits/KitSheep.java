package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.UtilInv;
import com.houzicore.shared.core.disguise.disguises.DisguiseSheep;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.SuperSmash;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.sheep.SmashSheep;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkLazer;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkWoolBomb;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkWoolCloud;

public class KitSheep extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkLazer(),
	  new PerkWoolBomb(),
	  new PerkWoolCloud(),
	  new SmashSheep()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Static Laser",
		new String[]{
		  ChatColor.RESET + "Charge up static electricity in your",
		  ChatColor.RESET + "wooly coat, and then unleash it upon",
		  ChatColor.RESET + "enemies in a devastating laser beam!",

		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Wool Mine",
		new String[]{
		  ChatColor.RESET + "Shear yourself and use the wool as",
		  ChatColor.RESET + "an explosive device. You can Right-Click",
		  ChatColor.RESET + "a second time to solidify the bomb, and",
		  ChatColor.RESET + "a third time to detonate it on command.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SPADE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Wooly Rocket",
		new String[]{
		  ChatColor.RESET + "Become like a cloud and shoot yourself",
		  ChatColor.RESET + "directly upwards. You deal damage to anyone",
		  ChatColor.RESET + "you collide with on your ascent.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Using this recharges your Double Jump.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Homing Sheeples",
		new String[]{
		  ChatColor.RESET + "Release one Homing Sheeple towards every player.",
		  ChatColor.RESET + "They will slowly home in on their target and",
		  ChatColor.RESET + "explode to deal devastating damage.",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  null
	};

	public KitSheep(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_SHEEP, PERKS, DisguiseSheep.class);
	}
	
	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1], PLAYER_ITEMS[2]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[3]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
