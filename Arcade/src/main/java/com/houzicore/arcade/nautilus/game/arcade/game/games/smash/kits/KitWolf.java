package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.disguise.disguises.DisguiseWolf;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.wolf.PerkWolf;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.wolf.SmashWolf;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitWolf extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Wolf Jump"),
	  new PerkWolf(),
	  new SmashWolf()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Cub Tackle",
		new String[]{
		  ChatColor.RESET + "Launch a wolf cub at an opponent.",
		  ChatColor.RESET + "If it hits, the cub latches onto the",
		  ChatColor.RESET + "opponent, preventing them from moving",
		  ChatColor.RESET + "for up to 5 seconds.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SHOVEL, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Wolf Strike",
		new String[]{
		  ChatColor.RESET + "Leap forward with great power.",
		  ChatColor.RESET + "If you collide with an enemy, you deal",
		  ChatColor.RESET + "damage to them. If they are being tackled",
		  ChatColor.RESET + "by a cub, it deals 300% Knockback.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.BONE, (byte) 0, 1,
		C.cYellow + C.Bold + "Passive" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Ravage",
		new String[]{
		  ChatColor.RESET + "When you attack someone, you receive",
		  ChatColor.RESET + "+1 Damage for 3 seconds. Bonus damage",
		  ChatColor.RESET + "stacks from multiple hits.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Frenzy",
		new String[]{
		  ChatColor.RESET + "Gain incredible speed, regeneration",
		  ChatColor.RESET + "and damage. All your abilities recharge",
		  ChatColor.RESET + "extremely rapidly.",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  null,
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  null
	};


	public KitWolf(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_WOLF, PERKS, DisguiseWolf.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
