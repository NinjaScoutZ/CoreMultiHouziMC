package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.UtilInv;
import com.houzicore.shared.core.disguise.disguises.DisguiseIronGolem;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.golem.PerkFissure;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.golem.SmashGolem;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkIronHook;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.golem.PerkSeismicSlam;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkSlow;

public class KitGolem extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkSlow(0),
	  new PerkFissure(),
	  new PerkIronHook(),
	  new PerkSeismicSlam(),
	  new SmashGolem()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Fissure",
		new String[]{
		  ChatColor.RESET + "Smash the ground, creating a fissure",
		  ChatColor.RESET + "which slowly rises in a line, dealing",
		  ChatColor.RESET + "damage and knockback to anyone it hits!",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_PICKAXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Iron Hook",
		new String[]{
		  ChatColor.RESET + "Throw a metal hook at opponents.",
		  ChatColor.RESET + "If it hits, it deals damage and pulls",
		  ChatColor.RESET + "them towards you with great force.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SPADE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Seismic Slam",
		new String[]{
		  ChatColor.RESET + "Take a mighty leap into the air, then",
		  ChatColor.RESET + "slam back into the ground with huge force.",
		  ChatColor.RESET + "Nearby opponents take damage and knockback.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Earthquake",
		new String[]{
		  ChatColor.RESET + "Begin an earthquake that will give damage",
		  ChatColor.RESET + "and knockback to any player who is touching",
		  ChatColor.RESET + "the ground, anywhere on the map!",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.DIAMOND_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_HELMET),
	};

	public KitGolem(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_GOLEM, PERKS, DisguiseIronGolem.class);
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
