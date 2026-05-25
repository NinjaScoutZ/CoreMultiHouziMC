package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.disguise.disguises.DisguiseGuardian;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.guardian.PerkTargetLazer;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.guardian.PerkThorns;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.guardian.PerkWaterSplash;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.guardian.PerkWhirlpoolBlade;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.guardian.SmashGuardian;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitGuardian extends SmashKit
{

	private static final Perk[] PERKS = {
		new PerkSmashStats(),
		new PerkDoubleJump("Double Jump"),
		new PerkWhirlpoolBlade(),
		new PerkWaterSplash(),
		new PerkTargetLazer(),
		new PerkThorns(),
		new SmashGuardian()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
		ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1, 
				C.cYellow + C.Bold + "Right-Click" + (C.cWhite + C.Bold) + " - " + (C.cGreen + C.Bold) + "Whirlpool Axe",
				new String[] {
					ChatColor.RESET + "Fires a Prismarine Shard that deals damage to",
					ChatColor.RESET + "the first player it collides with.",
					ChatColor.RESET + "The player is then drawn towards you."
				}),
		ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1, 
				C.cYellow + C.Bold + "Right-Click" + (C.cWhite + C.Bold) + " - " + (C.cGreen + C.Bold) + "Water Splash",
				new String[] {
					ChatColor.RESET + "You bounce into the air and pull all nearby players",
					ChatColor.RESET + "towards you.",
					ChatColor.RESET + "Blocking with the sword while bouncing increases the height.",
					ChatColor.RESET + "Landing causes a water splash dealing damage and knockback",
					ChatColor.RESET + "to nearby players."
				}),
		ItemStackFactory.Instance.CreateStack(Material.IRON_PICKAXE, (byte) 0, 1, 
				C.cYellow + C.Bold + "Right-Click" + (C.cWhite + C.Bold) + " - " + (C.cGreen + C.Bold) + "Target Laser",
				new String[] {
					ChatColor.RESET + "You target the nearest player with your laser.",
					ChatColor.RESET + "That player takes increased damage and knockback from you.",
					ChatColor.RESET + "Your laser breaks if you get too far away or after some time."
				}),
		ItemStackFactory.Instance.CreateStack(Material.PRISMARINE_SHARD, (byte) 0, 1, 
				C.cYellow + C.Bold + "Passive" + (C.cWhite + C.Bold) + " - " + (C.cGreen + C.Bold) + "Thorns",
				new String[] {
					ChatColor.RESET + "Takes 66% less damage and knockback from projectiles",
					ChatColor.RESET + "when under 10 health.",
				}),
		ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1, 
				C.cYellow + C.Bold + "Smash Crystal" + (C.cWhite + C.Bold) + " - " + (C.cGreen + C.Bold) + "Rise of the Guardian",
				new String[] {
					ChatColor.RESET + "You call upon Gwen who begins to charge her laser.",
					ChatColor.RESET + "Any player near the laser is drawn to it and cannot",
					ChatColor.RESET + "escape from it.",
					ChatColor.RESET + "Once the laser has charged all players nearby get hit",
					ChatColor.RESET + "with HUGE damage and knockback!"
				}),
	};
	
	private static final ItemStack[] PLAYER_ARMOR = {
		ItemStackFactory.Instance.CreateStack(Material.DIAMOND_BOOTS),
		ItemStackFactory.Instance.CreateStack(Material.DIAMOND_LEGGINGS),
		null,
		null,
	};
	
	public KitGuardian(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_GUARDIAN, PERKS, DisguiseGuardian.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1], PLAYER_ITEMS[2]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[3], PLAYER_ITEMS[4]);	
		
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
		
}
