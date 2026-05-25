package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.UtilInv;
import com.houzicore.shared.core.disguise.disguises.DisguiseSlime;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.slime.PerkSlimeRocket;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.slime.PerkSlimeSlam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.slime.SmashSlime;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitSlime extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkSlimeSlam(),
	  new PerkSlimeRocket(),  
	  new SmashSlime()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1,
		C.cYellow + C.Bold + "Hold/Release Block" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Slime Rocket",
		new String[]{
		  ChatColor.RESET + "Slowly transfer your slimey goodness into",
		  ChatColor.RESET + "a new slime. When you release block, the",
		  ChatColor.RESET + "new slime is propelled forward.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "The more you charge the ability, the stronger",
		  ChatColor.RESET + "the new slime is projected forwards.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + C.cAqua + "Slime Rocket uses Energy (Experience Bar)",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Slime Slam",
		new String[]{
		  ChatColor.RESET + "Throw your slimey body forwards. If you hit",
		  ChatColor.RESET + "another player before you land, you deal",
		  ChatColor.RESET + "large damage and knockback to them.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "However, you take 50% of the damage and",
		  ChatColor.RESET + "knockback in the opposite direction.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Giga Slime",
		new String[]{
		  ChatColor.RESET + "Grow into a gigantic slime that deals damage",
		  ChatColor.RESET + "and knockback to anyone that comes nearby.",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  null,
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET),
	};

	public KitSlime(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_SLIME, PERKS, DisguiseSlime.class);
	}
	
	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		DisguiseSlime disguise = (DisguiseSlime) Manager.GetDisguise().getActiveDisguise(player);
		
		disguise.SetSize(3);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[2]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}

}
