package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilTime.TimeUnit;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

public class SoloSkywars extends Skywars
{

	private GameTeam _players;
	
	public SoloSkywars(ArcadeManager manager)
	{
		super(manager, GameType.Skywars, 
				 new String[]
							{
					C.cGray + "The void beckons. Don't fall.",
					"",
					C.cGray + "Loot " + C.cYellow + "Island Chests" + C.cGray + " for weapons and armor,",
					C.cGray + "bridge across the abyss, and " + C.cRed + "eliminate" + C.cGray + " your foes.",
					"",
					C.cGreen + "Conquer the skies."
							});
		
		this.DamageTeamSelf = true;
		
	}
	
	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
			return;

		if (GetTeamList().isEmpty())
			return;

		GameTeam playersTeam = GetTeamList().get(0);
		_players = playersTeam;
		playersTeam.SetColor(ChatColor.YELLOW);
		playersTeam.SetName("Players");
		playersTeam.setDisplayName(C.cYellow + C.Bold + "Players");

		ArrayList<org.bukkit.Location> validSpawns = new ArrayList<>();
		for (GameTeam team : GetTeamList())
		{
			for (org.bukkit.Location spawn : team.GetSpawns())
			{
				if (WorldData.GetDataLocs("YELLOW").contains(spawn) ||
					WorldData.GetDataLocs("BROWN").contains(spawn) ||
					WorldData.GetDataLocs("WHITE").contains(spawn) ||
					WorldData.GetDataLocs("LIME").contains(spawn))
				{
					continue;
				}
				validSpawns.add(spawn);
			}
		}

		playersTeam.GetSpawns().clear();
		playersTeam.GetSpawns().addAll(validSpawns);

		while (GetTeamList().size() > 1)
		{
			GetTeamList().remove(1);
		}
	}
	
	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (GetTeamList().isEmpty())
			return;

		Scoreboard.Reset();

		GameTeam team = GetTeamList().get(0);

		if (IsLive())
		{
			Scoreboard.WriteBlank();
			Scoreboard.Write("§c§l" + UtilText.toSmallCaps("Game Info"));
			Scoreboard.Write("Players left: §a" + team.GetPlayers(true).size());
			
			// Always show Kills section — from 0 immediately (UX standard)
			Scoreboard.WriteBlank();
			Scoreboard.Write("§6§l" + UtilText.toSmallCaps("Kills"));
			
			Player topKiller = null;
			int maxKills = 0;
			
			for (Player p : GetPlayers(false))
			{
				int kills = 0;
				if (GetStats().containsKey(p) && GetStats().get(p).containsKey("Kills"))
				{
					kills = GetStats().get(p).get("Kills");
				}
				
				if (kills > maxKills)
				{
					maxKills = kills;
					topKiller = p;
				}
				
				// Show every alive player's kills — including 0
				if (IsAlive(p))
				{
					Scoreboard.Write("§f" + p.getName() + " §7(§a" + kills + "§7)");
				}
			}
			
			Scoreboard.WriteBlank();
			Scoreboard.Write("§e§l" + UtilText.toSmallCaps("Time"));
			Scoreboard.Write("§f" + UtilTime.convertString(System.currentTimeMillis() - GetStateTime(), 0, TimeUnit.FIT));
		}
		else
		{
			// Recruit / Prepare phase
			Scoreboard.WriteBlank();
			Scoreboard.Write("§e§l" + UtilText.toSmallCaps("Status"));
			Scoreboard.Write("Players: §a" + team.GetPlayers(true).size() + "§7/§a" + Manager.GetPlayerFull());
		}

		Scoreboard.Draw();
	}
	
	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		if (GetPlayers(true).size() <= 1)
		{	
			ArrayList<Player> places = GetTeamList().get(0).GetPlacements(true);
			
			//Announce
			AnnounceEnd(places);

			//Gems
			if (places.size() >= 1)
				AddGems(places.get(0), 20, "1st Place", false, false);

			if (places.size() >= 2)
				AddGems(places.get(1), 15, "2nd Place", false, false);

			if (places.size() >= 3)
				AddGems(places.get(2), 10, "3rd Place", false, false);

			for (Player player : GetPlayers(false))
				if (player.isOnline())
					AddGems(player, 10, "Participation", false, false);

			//End
			SetState(GameState.End);
		}
	}

	@Override
	public List<Player> getWinners()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			if (GetTeamList().isEmpty())
				return Arrays.asList();

			List<Player> places = GetTeamList().get(0).GetPlacements(true);

			if (places.isEmpty() || !places.get(0).isOnline())
				return Arrays.asList();
			else
				return Arrays.asList(places.get(0));
		}
		else
			return null;
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> winners = getWinners();

		if (winners == null)
			return null;

		if (GetTeamList().isEmpty())
			return new java.util.ArrayList<Player>();

		List<Player> losers = GetTeamList().get(0).GetPlayers(false);

		losers.removeAll(winners);

		return losers;
	}

	@Override
	public String GetMode()
	{
		return "Solo Mode";
	}
}
