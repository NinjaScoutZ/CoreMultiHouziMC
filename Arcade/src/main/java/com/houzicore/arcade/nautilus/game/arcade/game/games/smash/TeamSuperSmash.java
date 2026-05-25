package com.houzicore.arcade.nautilus.game.arcade.game.games.smash;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.houzicore.shared.common.Pair;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.team.NamedTeamsModule;
import com.houzicore.arcade.nautilus.game.arcade.game.team.TeamRequestsModule;
import com.houzicore.arcade.nautilus.game.arcade.game.team.selectors.FillToSelector;
import com.houzicore.arcade.nautilus.game.arcade.stats.FreeKitWinStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.KillFastStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.OneVThreeStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.RecoveryMasterStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.WinWithoutDyingStatTracker;

public class TeamSuperSmash extends SuperSmash
{

	public TeamSuperSmash(ArcadeManager manager)
	{
		super(manager, GameType.SmashTeams, new String[] { "Each player has 3 respawns", "Attack to restore hunger!", "Last team alive wins!" });

		SpawnNearAllies = true;
		DamageTeamSelf = false;

		_teamSelector = new FillToSelector(this, 2);

		new NamedTeamsModule(this)
				.register();

		new TeamRequestsModule(this)
				.register();

		registerStatTrackers(
				new WinWithoutDyingStatTracker(this, "MLGPro"),
				new FreeKitWinStatTracker(this),
				new OneVThreeStatTracker(this),
				new KillFastStatTracker(this, 3, 10, "TripleKill"),
				new RecoveryMasterStatTracker(this)
		);

		/*
		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageTaken,
				DamageDealt,
				BlankLine,
				new ChatStatData("kit", "Kit", true)
		);

		new TeamArmorModule()
				.giveHotbarItem()
				.register(this);
		*/
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		//Wipe Last
		Scoreboard.Reset();
 
		List<GameTeam> aliveTeams = new ArrayList<>();
		
		for (GameTeam team : GetTeamList())
		{
			if (team.GetPlayers(true).isEmpty())
			{
				 continue;
			}
			
			aliveTeams.add(team);
		}
		
		if (aliveTeams.size() > 7)
		{
			Scoreboard.WriteBlank();
			Scoreboard.Write(C.cYellow + C.Bold + "Teams Alive");
			Scoreboard.Write(aliveTeams.size() + " ");
		}
		else
		{
			Scoreboard.WriteBlank();
 
			for (GameTeam team : GetTeamList())
			{
				writeGroup(team.GetPlayers(false), player ->
				{
					int lives = getLives(player);
					
					return Pair.create(team.GetColor() + (IsAlive(player) ? "" : C.Strike) + player.getName(), lives);
				});
			}
		}
 
		Scoreboard.Draw();
	}
 
	private void writeGroup(List<Player> players, Function<Player, Pair<String, Integer>> lineMapper)
	{
		for (Player player : players)
		{
			Pair<String, Integer> line = lineMapper.apply(player);
			Scoreboard.Write(line.getLeft());
		}
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (!(event.GetDamagerEntity(true) instanceof Player || event.GetDamageeEntity() instanceof Player))
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(true);
		Player damagee = event.GetDamageePlayer();
		
		if (GetTeam(damager) == null || GetTeam(damagee) == null)
		{
			return;
		}
		
		if (GetTeam(damager).equals(GetTeam(damagee)))
		{
			if (event.GetCause() == DamageCause.FIRE)
			{
				damagee.setFireTicks(0);
			}
			
			event.SetCancelled("Team Damage");
		}
	}
	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		ArrayList<GameTeam> teamsAlive = new ArrayList<GameTeam>();

		for (GameTeam team : GetTeamList())
			if (team.GetPlayers(true).size() > 0)
				teamsAlive.add(team);

		if (teamsAlive.size() <= 1)
		{
			// Announce
			if (teamsAlive.size() > 0)
				AnnounceEnd(teamsAlive.get(0));

			for (GameTeam team : GetTeamList())
			{
				if (WinnerTeam != null && team.equals(WinnerTeam))
				{
					for (Player player : team.GetPlayers(false))
						AddGems(player, 10, "Winning Team", false, false);
				}

				for (Player player : team.GetPlayers(false))
					if (player.isOnline())
						AddGems(player, 10, "Participation", false, false);
			}

			// End
			SetState(GameState.End);
		}
	}

	@Override
	public List<Player> getWinners()
	{
		if (WinnerTeam == null)
			return null;

		return WinnerTeam.GetPlayers(false);
	}

	@Override
	public List<Player> getLosers()
	{
		if (WinnerTeam == null)
			return null;

		List<Player> players = new ArrayList<>();

		for (GameTeam team : GetTeamList())
		{
			if (team != WinnerTeam)
				players.addAll(team.GetPlayers(false));
		}

		return players;
	}

	@Override
	public String GetMode()
	{
		return "Team Mode";
	}
	
	/**
	 * @param manager The arcade manager
	 * @param player The player to check
	 * @param includeSelf If true, then the list will contain the given player in addition to team mates. If false then it will
	 * not include the player. If the given player is a spectator then this will always return empty.
	 * @return Returns a list of players who the given player should not be able to damage as they are
	 * the player's team mates. If the given player is a spectator the list will return empty. If the game is Solo Super Smash Mobs
	 * or any other game than Team Super Smash Mob then the list will return only the player or empty depending on <code>includeSelf</code>
	 */
	public static List<Player> getTeam(ArcadeManager manager, Player player, boolean includeSelf)
	{
		List<Player> list = new ArrayList<>();
		if(!manager.IsAlive(player))
		{
			return list;
		}
		else if(manager.GetGame() instanceof TeamSuperSmash)
		{
			list.addAll(manager.GetGame().GetTeam(player).GetPlayers(true));
			if(!includeSelf) list.remove(player);
		}
		else
		{
			if(includeSelf)
			{
				list.add(player);
			}
		}
		return list;
	}
}
