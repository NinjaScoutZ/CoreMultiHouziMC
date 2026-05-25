package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.stats.StatTracker;

public class FloorIsLavaTracker extends StatTracker<Bedwars>
{

	private static final long GRACE_TIME = TimeUnit.SECONDS.toMillis(30);

	private final Set<Player> _successful;

	public FloorIsLavaTracker(Bedwars game)
	{
		super(game);

		_successful = new HashSet<>();
	}

	@EventHandler
	public void gameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
		{
			_successful.addAll(getGame().GetPlayers(true));
		}
		else if (event.GetState() == GameState.End)
		{
			List<Player> winners = getGame().getWinners();

			_successful.removeIf(player -> !winners.contains(player));
			_successful.forEach(player -> addStat(player, "FloorIsLava", 1, true, false));
		}
	}

	@EventHandler
	public void updateFloor(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || !getGame().IsLive() || !UtilTime.elapsed(getGame().GetStateTime(), GRACE_TIME))
		{
			return;
		}

		_successful.removeIf(player ->
		{
			if (!player.isOnline())
			{
				return true;
			}

			Location location = player.getLocation();
			Block block = location.getBlock();
			Set<Block> blocks = getGame().getBedwarsPlayerModule().getPlacedBlocks();
			boolean surrounding = false;

			for (Block nearby : UtilBlock.getSurrounding(block, true))
			{
				if (blocks.contains(nearby))
				{
					surrounding = true;
					break;
				}
			}

			return !blocks.contains(block) && !surrounding && UtilEnt.onBlock(player) && !getGame().getBedwarsShopModule().isNearShop(location);
		});
	}
}
