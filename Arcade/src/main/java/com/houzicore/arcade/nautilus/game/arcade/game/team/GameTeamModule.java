package com.houzicore.arcade.nautilus.game.arcade.game.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.friend.FriendManager;
import com.houzicore.shared.core.friend.data.FriendStatus;
import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.core.party.PartyManager;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.GameModule;

public class GameTeamModule extends GameModule<Game>
{

	private final Map<Player, GameTeam> _preferences;
	private List<GameTeam> _visibleTeams;

	private int _target;
	private boolean _prioritisePreferences;

	public GameTeamModule(Game game)
	{
		super(game);
		_preferences = new HashMap<>();
	}

	@Override
	public void unregister()
	{
		super.unregister();
		_preferences.clear();
	}

	public GameTeamModule setPlayersPerTeam(int target)
	{
		_target = target;
		return null;
	}

	public GameTeamModule setPrioritisePreferences(boolean prioritise)
	{
		_prioritisePreferences = prioritise;
		return null;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void assignTeams(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		Game game = getGame();
		FriendManager friendManager = com.houzicore.shared.core.plugin.PluginRegistry.require(FriendManager.class);
		boolean autoBalance = game.Manager.IsTeamBalance();

		List<Player> players = game.Manager.getValidPlayersForGameStart();
		_visibleTeams = game.GetTeamList().stream()
				.filter(GameTeam::GetVisible)
				.collect(Collectors.toList());

		// Only one team. Add all players to that, no need to balance.
		if (_visibleTeams.size() == 1)
		{
			GameTeam team = _visibleTeams.get(0);

			for (Player player : players)
			{
				game.SetPlayerTeam(player, team, true);
			}

			return;
		}

		int target;
		int total = players.size();

		if (_target > 0)
		{
			target = _target;
		}
		else
		{
			target = (int) Math.ceil((double) total / _visibleTeams.size());
		}

		// Step 1 - Team Preference (If prioritised)
		// This would be set in games where /team is used.
		if (_prioritisePreferences)
		{
			assignPreferences(target, total);
		}

		// Step 2 - Parties
		// Only worry about parties if autoBalance is on, otherwise we don't care
		if (autoBalance)
		{
			for (Party party : game.Manager.getPartyManager()._partyLeaderMap.values())
			{
				List<Player> partyMembers = new ArrayList<>(party.GetPlayersOnline());
				// Remove party members that are already on a team
				partyMembers.removeIf(other -> game.GetTeam(other) != null || !players.contains(other));

				// Keep going until we just have 1 party member left
				while (partyMembers.size() > 1)
				{
					int partySize = Math.min(partyMembers.size(), target);
					GameTeam team = getTeamToJoin(partySize, target, total);

					// Fill the team up as far as it can
					for (int i = 0; i < partySize; i++)
					{
						game.SetPlayerTeam(partyMembers.remove(0), team, true);
					}
				}
			}
		}
		// No balancing no need to worry about unbalanced teams
		else if (_target == 0)
		{
			target = Integer.MAX_VALUE;
		}

		// Step 2 - Team Preferences (Again)
		assignPreferences(target, total);

		// Now we worry around everyone else
		for (Player player : players)
		{
			GameTeam playerTeam = game.GetTeam(player);

			// Already in a team
			if (playerTeam != null)
			{
				continue;
			}

			// Step 3 - Friends
			List<Player> friends = friendManager.Get(player).getFriends().stream()
					.filter(friendStatus -> friendStatus.Online)
					.map(friendStatus -> UtilPlayer.searchExact(friendStatus.Uuid))
					.filter(players::contains)
					.collect(Collectors.toList());

			if (!friends.isEmpty())
			{
				friends = new ArrayList<>(friends);
				// Remove friends that are already on a team
				friends.removeIf(other -> game.GetTeam(other) != null);
				// We need to add the player here so that it handles the max team size correctly.
				// Don't think about it too much, you just need this otherwise you get 3 players in
				// duo games.
				friends.add(0, player);

				// Friends operate similarly to parties however we don't need to worry about getting everyone
				// on a team with at least one friend since these are friends of the current iterating player.
				if (friends.size() > 1)
				{
					int friendSize = Math.min(friends.size(), target);
					GameTeam team = getTeamToJoin(friendSize, target, total);

					// Fill the team up as far as it can
					for (int i = 0; i < friendSize; i++)
					{
						game.SetPlayerTeam(friends.get(i), team, true);
					}

					continue;
				}
			}

			// Step 4 - Emptiest
			GameTeam team = getTeamToJoin(1, target, total);

			if (team == null)
			{
				continue;
			}

			game.SetPlayerTeam(player, team, true);
		}
	}

	private void assignPreferences(int target, int total)
	{
		_preferences.forEach((player, preference) ->
		{
			if (getGame().getTeamSelector().canJoinTeam(preference, 1, target, total))
			{
				getGame().SetPlayerTeam(player, preference, true);
			}
		});

		_preferences.clear();
	}

	public void addPlayerQueue(Player player, GameTeam team)
	{
		ArcadeManager manager = getGame().Manager;

		// No team balancing, we don't care. Just put them on the team.
		if (!manager.IsTeamBalance())
		{
			getGame().SetPlayerTeam(player, team, true);
			return;
		}

		GameTeam previous = _preferences.put(player, team);

		if (team.equals(previous))
		{
			player.sendMessage(F.main("Game", "You are already queued for " + F.name(team.GetColor() + team.GetDisplayName()) + "."));
		}
		else
		{
			player.sendMessage(F.main("Game", "You are now queued for " + F.name(team.GetColor() + team.GetDisplayName()) + "."));
		}
	}

	private GameTeam getTeamToJoin(int desiredAmount, int target, int total)
	{
		return getGame().getTeamSelector().getTeamToJoin(_visibleTeams, desiredAmount, target, total);
	}

	public void clearQueue(GameTeam team)
	{
		if (team == null)
		{
			return;
		}

		_preferences.values().removeIf(team::equals);
	}

	public int getPlayersQueued(GameTeam team)
	{
		return (int) _preferences.values().stream()
				.filter(team::equals)
				.count();
	}

	public Map<Player, GameTeam> getPreferences()
	{
		return _preferences;
	}

	public int getPlayersPerTeam()
	{
		return _target;
	}
}
