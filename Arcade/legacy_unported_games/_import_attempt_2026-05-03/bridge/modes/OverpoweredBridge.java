package com.houzicore.arcade.nautilus.game.arcade.game.games.bridge.modes;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bridge.Bridge;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.AbsorptionFix;

public class OverpoweredBridge extends Bridge
{

	private final ItemStack[] _items =
			{
					new ItemStack(Material.DIAMOND, 64),
					new ItemStack(Material.DIAMOND, 64),
					new ItemStack(Material.WOOD, 128),
					new ItemStack(Material.GOLDEN_APPLE, 10),
					new ItemStack(Material.ENCHANTMENT_TABLE),
					new ItemStack(Material.BOOKSHELF, 32),
					new ItemStack(Material.EXP_BOTTLE, 64),
					new ItemStack(Material.EXP_BOTTLE, 64),
					new ItemStack(Material.COOKED_BEEF, 64),
					new ItemStack(Material.MUSHROOM_SOUP),
					new ItemStack(Material.MUSHROOM_SOUP),
					new ItemStack(Material.MUSHROOM_SOUP),
					new ItemStack(Material.MUSHROOM_SOUP)
			};

	public OverpoweredBridge(ArcadeManager manager)
	{
		super(manager, GameType.Bridge);

		new AbsorptionFix()
				.register(this);

		setBridgeTime(TimeUnit.MINUTES.toMillis(5));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerTeleportIn(PlayerPrepareTeleportEvent event)
	{
		Player player = event.GetPlayer();

		player.getInventory().addItem(_items);
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		event.SetDamageToLevel(false);
	}

	@Override
	public String GetMode()
	{
		return "OP Bridges";
	}

	@Override
	public boolean isAllowingGameStats()
	{
		return false;
	}
}

