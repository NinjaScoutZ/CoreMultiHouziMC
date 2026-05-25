package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilTime.TimeUnit;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.lang.PrimalGamesLang;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class SoloPrimalGames extends PrimalGames
{
	private GameTeam _players;

	private boolean _surviveAnnounced = false;
	private boolean _airdropAnnounced = false;
	private boolean _dmAnnounced = false;

	public SoloPrimalGames(ArcadeManager manager)
	{
		super(manager, GameType.SurvivalPrimalGame,
				new String[]
						{
				C.cGray + "A collapsing world where " + C.cYellow + "loot tempo" + C.cGray + " decides every fight.",
				"",
				C.cGray + "Open " + C.cYellow + "Supply Chests" + C.cGray + ", mine key upgrades,",
				C.cGray + "and spike your gear with powerful " + C.cAqua + "Runes" + C.cGray + ".",
				C.cGray + "Survive the " + C.cPurple + "Disasters" + C.cGray + " and outlast the final collapse.",
				"",
				C.cGreen + "One survivor. No reset button."
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

		_players = GetTeamList().get(0);
		_players.SetColor(ChatColor.GOLD);
		_players.SetName("Survivors");
		_players.setDisplayName(C.cGold + C.Bold + "Survivors");
	}

	@EventHandler
	public void AnnounceSurvive(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;

		PrimalGamesLang lang = PrimalGamesLang.get();
		for (Player p : com.houzicore.shared.common.util.UtilServer.getPlayers())
		{
			p.sendTitle(
				lang.get(p, "primal_games.title.survive"),
				lang.get(p, "primal_games.title.survive_subtitle"),
				10, 60, 20
			);
		}
	}

	// ─── Scoreboard — Prop Rush Style ─────────────────────────────
	//
	// Layout: Phase → Counts+Disaster → Top Killers → Timers
	// Pattern matches HideSeek.WriteScoreboard() exactly.

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (GetTeamList().isEmpty())
			return;

		GameTeam team = GetTeamList().get(0);
		int alive = team.GetPlayers(true).size();
		long elapsedMs = IsLive() ? (System.currentTimeMillis() - this.GetStateTime()) : 0;
		PrimalGamesLang lang = PrimalGamesLang.get();

		Scoreboard.Reset();

		// ── PHASE + TIMER ──────────────────────────────────────────────
		String phaseLabel;
		String phaseColor;
		if (elapsedMs >= 1200000) { phaseLabel = lang.get(null, "primal_games.scoreboard.phase_deathmatch"); phaseColor = C.cRed; }
		else if (elapsedMs >= 600000) { phaseLabel = lang.get(null, "primal_games.scoreboard.phase_border_closing"); phaseColor = C.cYellow; }
		else { phaseLabel = lang.get(null, "primal_games.scoreboard.phase_survive"); phaseColor = C.cGreen; }

		String timer = formatTimer(IsLive() ? this.getSecondsSinceStart() * 1000L : 0L);

		Scoreboard.Write(C.cGold + "⌚ " + C.cWhite + C.Bold + lang.get(null, "primal_games.scoreboard.phase"));
		Scoreboard.Write(sub(phaseColor + phaseLabel + C.cGray + " • " + C.cWhite + timer));
		Scoreboard.Write(" ");

		// ── COUNTS + DISASTER ALERT ────────────────────────────────────
		int myTopKills = IsLive() ? getTopKills() : 0;
		Scoreboard.Write(sub(C.cGray + "👥 Alive " + C.cGreen + alive
				+ C.cGray + "  •  "
				+ C.cGray + "⚔ Kills " + C.cRed + myTopKills));

		// Disaster sub-line (only when active or pending)
		if (IsLive())
		{
			com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.DisasterManager dm = this.getDisasterManager();
			if (dm != null && dm.isDisasterActive())
			{
				String status = dm.getDisasterStatusLine(null);
				if (status != null)
					Scoreboard.Write(sub(status));
			}
			else if (dm != null && dm.getPendingDisaster() != null)
			{
				Scoreboard.Write(sub(C.cYellow + "⚠ " + dm.getPendingDisaster().getNameEn() + " incoming..."));
			}
		}

		Scoreboard.Write("  ");

		// ── TOP KILLERS (replaces Objective) ──────────────────────────
		Scoreboard.Write(C.cRed + "☠ " + C.cWhite + C.Bold + lang.get(null, "primal_games.scoreboard.top_killers"));

		java.util.List<Player> allPlayers = new java.util.ArrayList<>(GetPlayers(false));
		allPlayers.sort((a, b) -> getKills(b) - getKills(a));

		String emptyEntry = C.cGray + lang.get(null, "primal_games.scoreboard.no_kills");
		String[] slots = { emptyEntry, emptyEntry, emptyEntry };
		for (int i = 0; i < Math.min(3, allPlayers.size()); i++)
		{
			Player px = allPlayers.get(i);
			int kx = getKills(px);
			if (kx > 0)
			{
				String prefix = (i == 0) ? C.cGold + C.Bold : (i == 1) ? C.cWhite : C.cGray;
				String num = (i == 0) ? "" : (i + 1) + ". ";
				slots[i] = prefix + num + px.getName() + C.cGray + " - " + kx;
			}
		}
		Scoreboard.Write(sub(slots[0]));
		Scoreboard.Write(sub(slots[1]));
		Scoreboard.Write(sub(slots[2]));

		Scoreboard.Write("   ");

		// ── TIMERS ────────────────────────────────────────────────────
		if (IsLive())
		{
			Scoreboard.Write(C.cGold + "✦ " + C.cWhite + C.Bold + lang.get(null, "primal_games.scoreboard.time"));

			// Airdrop (only show ≤ 5 min away)
			long airdropT = this.getAirdropManager() != null ? (this.getAirdropManager().getNextDrop() - elapsedMs) : 0;
			if (airdropT > 0 && airdropT <= 300000)
			{
				Scoreboard.Write(sub(C.cGray + "📦 Airdrop " + C.cGreen + formatTimer(airdropT)));
			}
			else if (airdropT <= 0 && !_airdropAnnounced && this.getSecondsSinceStart() > 10)
			{
				_airdropAnnounced = true;
					for (Player p : com.houzicore.shared.common.util.UtilServer.getPlayers())
						p.sendTitle(
							lang.get(p, "primal_games.title.airdrop"),
						lang.get(p, "primal_games.title.airdrop_subtitle"),
						10, 60, 20);
			}

			// Deathmatch countdown
			long dmTime = 1200000L - elapsedMs;
			if (dmTime > 0)
			{
				Scoreboard.Write(sub(C.cGray + "⚔ Deathmatch " + C.cRed + formatTimer(dmTime)));
			}
			else
			{
				if (!_dmAnnounced)
				{
					_dmAnnounced = true;
					for (Player p : com.houzicore.shared.common.util.UtilServer.getPlayers())
						p.sendTitle(
							lang.get(p, "primal_games.title.deathmatch"),
							lang.get(p, "primal_games.title.deathmatch_subtitle"),
							10, 80, 20);
				}
				Scoreboard.Write(sub(C.cRed + C.Bold + "⚔ DEATHMATCH"));
			}
		}
		else
		{
			Scoreboard.Write(C.cRed + lang.get(null, "primal_games.scoreboard.game_end"));
		}

		Scoreboard.Draw();
	}

	// ── Prop Rush formatting helpers ──

	/** Indented sub-line — matches HideSeek.formatBoardSubLine */
	private String sub(String text)
	{
		return C.cGray + "  " + text;
	}

	/** Format millis into Prop Rush timer style (Xm Ys or Xs) */
	private String formatTimer(long millis)
	{
		long totalSeconds = Math.max(0L, millis / 1000L);
		long minutes = totalSeconds / 60L;
		long seconds = totalSeconds % 60L;
		if (minutes > 0L)
			return minutes + "m " + seconds + "s";
		return totalSeconds + "s";
	}

	/** Get top kills count for the scoreboard header */
	private int getTopKills()
	{
		int top = 0;
		for (Player p : GetPlayers(true))
		{
			int k = getKills(p);
			if (k > top) top = k;
		}
		return top;
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
			return Arrays.asList();

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
