package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class ChestOpenStatTracker extends StatTracker<Game>
{
	public ChestOpenStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onChestOpen(PlayerInteractEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
			return;

		if (event.getClickedBlock() == null)
			return;

		if (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.TRAPPED_CHEST || event.getClickedBlock().getType() == Material.ENDER_CHEST)
		{
			addStat(event.getPlayer(), "ChestsOpened", 1, false, false);
		}
	}
}
