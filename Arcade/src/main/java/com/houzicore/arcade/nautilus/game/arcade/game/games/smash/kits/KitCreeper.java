package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.disguise.disguises.DisguiseCreeper;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.creeper.PerkCreeperElectricity;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.creeper.PerkCreeperExplode;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.creeper.PerkCreeperSulphurBomb;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.creeper.SmashCreeper;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitCreeper extends SmashKit
{
	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkCreeperElectricity(),
	  new PerkCreeperSulphurBomb(),
	  new PerkCreeperExplode(),
	  new SmashCreeper()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Sulphur Bomb",
		new String[]{
		  ChatColor.RESET + "Throw a small bomb of sulphur.",
		  ChatColor.RESET + "Explodes on contact with players,",
		  ChatColor.RESET + "dealing some damage and knockback.",

		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SHOVEL, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Explode",
		new String[]{
		  ChatColor.RESET + "You freeze in location and charge up",
		  ChatColor.RESET + "for 1.5 seconds. Then you explode!",
		  ChatColor.RESET + "You are sent flying in the direction",
		  ChatColor.RESET + "you are looking, while opponents take",
		  ChatColor.RESET + "large damage and knockback.",

		}),
	  ItemStackFactory.Instance.CreateStack(Material.COAL, (byte) 0, 1,
		C.cYellow + C.Bold + "Passive" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Lightning Shield",
		new String[]{
		  ChatColor.RESET + "When attacked by a non-melee attack,",
		  ChatColor.RESET + "you gain Lightning Shield for 2 seconds.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Lightning Shield blocks 1 melee attack,",
		  ChatColor.RESET + "striking lightning on the attacker.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Atomic Blast",
		new String[]
		  {
			ChatColor.RESET + "After a short duration, you will explode",
			ChatColor.RESET + "a gigantic explosion which destroys the",
			ChatColor.RESET + "map and everyone nearby. You are sent flying",
			ChatColor.RESET + "in the direction you are looking."
		  })
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.LEATHER_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.LEATHER_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.LEATHER_HELMET),
	};

	public KitCreeper(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_CREEPER, PERKS, DisguiseCreeper.class);
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
