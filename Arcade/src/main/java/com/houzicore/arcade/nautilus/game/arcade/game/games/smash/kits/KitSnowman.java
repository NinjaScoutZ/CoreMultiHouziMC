package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.disguise.disguises.DisguiseSnowman;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.snowman.PerkArcticAura;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.snowman.PerkIcePath;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.snowman.SmashSnowman;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkBlizzard;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDamageSnow;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitSnowman extends SmashKit
{

	private static final Perk[] PERKS = {
			new PerkSmashStats(),
			new PerkDoubleJump("Double Jump"),
			new PerkDamageSnow(),
			new PerkArcticAura(),
			new PerkBlizzard(),
			new PerkIcePath(),
			new SmashSnowman()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1,
		C.cYellow + C.Bold + "Hold Block" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Blizzard",
		new String[]{
		  ChatColor.RESET + "Release a windy torrent of snow, able",
		  ChatColor.RESET + "to blow opponents off the stage.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + C.cAqua + "Blizzard uses Energy (Experience Bar)",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Ice Path",
		new String[]{
		  ChatColor.RESET + "Create a temporary icy path in the",
		  ChatColor.RESET + "direction you are looking.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.SNOW_BLOCK, (byte) 0, 1,
		C.cYellow + C.Bold + "Passive" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Arctic Aura",
		new String[]{
		  ChatColor.RESET + "Creates a field of snow around you",
		  ChatColor.RESET + "granting +1 damage and 60% knockback",
		  ChatColor.RESET + "to opponents standing on it.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Your aura shrinks on low energy.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Snow Turret",
		new String[]{
		  ChatColor.RESET + "Spawn three snow turrets that continously",
		  ChatColor.RESET + "throw snowballs at the nearest enemy,",
		  ChatColor.RESET + "dealing damage and knockback.",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET),
	};

	public KitSnowman(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_SNOWMAN, PERKS, DisguiseSnowman.class);
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
