package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.disguise.disguises.DisguiseZombie;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.zombie.PerkOvercharge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.zombie.PerkZombieBile;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.zombie.SmashZombie;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDeathsGrasp;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFletcher;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkKnockbackArrow;

public class KitZombie extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkFletcher(),
	  new PerkKnockbackArrow(),
	  new PerkOvercharge(),
	  new PerkZombieBile(),
	  new PerkDeathsGrasp(),
	  new SmashZombie()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bile Blaster",
		new String[]{
		  ChatColor.RESET + "Spew up your dinner from last night.",
		  ChatColor.RESET + "Deals damage and knockback to enemies.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.BOW, (byte) 0, 1,
		C.cYellow + C.Bold + "Left-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Deaths Grasp",
		new String[]{
		  ChatColor.RESET + "Leap forwards. If you collide with an ",
		  ChatColor.RESET + "opponent, you deal damage, throw them ",
		  ChatColor.RESET + "behind you and recharge the ability.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Arrows deal double damage to enemies",
		  ChatColor.RESET + "recently hit by Deaths Grasp.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.ARROW, (byte) 0, 1,
		C.cYellow + C.Bold + "Left-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Corrupted Arrow",
		new String[]{
		  ChatColor.RESET + "Charge your arrows to corrupt them,",
		  ChatColor.RESET + "adding up to an additional 6 damage.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Night of the Living Dead",
		new String[]{
		  ChatColor.RESET + "Cast the world into darkness as hundreds",
		  ChatColor.RESET + "of undead minions sprout up from the ground",
		  ChatColor.RESET + "to attack your enemies.",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  null,
	};


	public KitZombie(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_ZOMBIE, PERKS, DisguiseZombie.class);
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
	
	@EventHandler
	public void arrowDamage(CustomDamageEvent event)
	{
		Player player = event.GetDamagerPlayer(true);
		Projectile proj = event.GetProjectile();
		
		if (player == null || proj == null)
		{
			return;
		}
		
		if (!(proj instanceof Arrow))
		{
			return;
		}
		
		if (!HasKit(player))
		{
			return;
		}
		
		event.AddMod("Arrow Nerf", "Arrow Nerf", -1, false);
	}
}
