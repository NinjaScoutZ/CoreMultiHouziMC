package nautilus.game.arcade.game.games.uhc.stat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.core.updater.UpdateType;
import com.houzicore.shared.core.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.games.uhc.UHC;
import com.houzicore.arcade.nautilus.game.arcade.stats.StatTracker;

public class HalfHeartHealStat extends StatTracker<UHC>
{

	private Set<UUID> _players = new HashSet<>();

	public HalfHeartHealStat(UHC game)
	{
		super(game);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : getGame().GetPlayers(true))
		{
			if (player.getHealth() < 2 && !_players.contains(player.getUniqueId()))
			{
				_players.add(player.getUniqueId());
			}
			else if (player.getHealth() >= 20 && _players.contains(player.getUniqueId()))
			{
				getGame().addUHCAchievement(player, "Die");
				_players.remove(player.getUniqueId());
			}
		}
	}

}
