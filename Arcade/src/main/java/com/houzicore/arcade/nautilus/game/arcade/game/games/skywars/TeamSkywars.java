package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilTime.TimeUnit;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.houzicore.shared.core.lang.LangManager;


public class TeamSkywars extends Skywars
{

	private NautHashMap<Player, Player> _teamReqs = new NautHashMap<Player, Player>();

	public TeamSkywars(ArcadeManager manager)
	{
		super(manager, GameType.SkywarsTeams,
				 new String[]
							{
					C.cGray + "The void beckons. Don't fall.",
					"",
					C.cGray + "Loot " + C.cYellow + "Island Chests" + C.cGray + " for weapons and armor,",
					C.cGray + "bridge across the abyss, and " + C.cRed + "eliminate" + C.cGray + " your foes.",
					"",
					C.cGreen + "Together, conquer the skies."
							});

		this.FillTeamsInOrderToCount = 2;

		this.SpawnNearAllies = true;

		this.DamageTeamSelf = false;
	}

	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
			return;

		ArrayList<Location> initialSpawns = new ArrayList<>();
		for (GameTeam team : GetTeamList())
		{
			for (Location spawn : team.GetSpawns())
			{
				if (WorldData.GetDataLocs("YELLOW").contains(spawn) ||
					WorldData.GetDataLocs("BROWN").contains(spawn) ||
					WorldData.GetDataLocs("WHITE").contains(spawn) ||
					WorldData.GetDataLocs("LIME").contains(spawn))
				{
					continue;
				}
				initialSpawns.add(spawn);
			}
		}
		this.GetTeamList().clear();

		TeamColors color = TeamColors.DARK_AQUA;

		//Create 1 Team for each Spawn
		int i = 0;
		for(Location location : initialSpawns)
		{
			i++;

			ArrayList<Location> spawns = new ArrayList<Location>();
			spawns.add(location);

			addRelativeSpawns(spawns, location);

			//Got Spawns
			color = getNextColor(color);
			int e = 0;
			for(GameTeam teams : GetTeamList())
			{
				if(teams.GetColor() == color.getColor())
				{
					e++;
					if(getColorName(color.getColor()).length <= e)
					{
						e = 0;
					}
				}
			}
			GameTeam team = new GameTeam(this, getColorName(color.getColor())[e], color.getColor(), spawns, true);
			team.SetVisible(true);
			GetTeamList().add(team);
		}
	}

	private void addRelativeSpawns(ArrayList<Location> spawns,	Location location)
	{
		//Gather Extra Spawns
		for(int x = -1; x <= 1; x++)
		{
			for(int z = -1; z <= 1; z++)
			{
				if(x != 0 && z != 0)
				{
					Location newSpawn = location.clone().add(x, 0, z);

					//Search Downward for Solid
					while (UtilBlock.airFoliage(newSpawn.getBlock().getRelative(BlockFace.DOWN)) && newSpawn.getY() > location.getY()-5)
					{
						newSpawn.subtract(0, 1, 0);
					}

					//Move Up out of Solid
					while (!UtilBlock.airFoliage(newSpawn.getBlock()) && newSpawn.getY() < location.getY()+5)
					{
						newSpawn.add(0, 1, 0);
					}

					//On Solid, with 2 Air Above
					if (UtilBlock.airFoliage(newSpawn.getBlock()) &&
						UtilBlock.airFoliage(newSpawn.getBlock().getRelative(BlockFace.UP)) &&
						!UtilBlock.airFoliage(newSpawn.getBlock().getRelative(BlockFace.DOWN)))
					{
						spawns.add(newSpawn);
					}
				}
			}
		}
	}

	private enum TeamColors
	{

		YELLOW(ChatColor.YELLOW, new String[]{"Banana", "Sunshine", "Custard", "Sponge", "Star", "Giraffe", "Lego", "Light"}),
		GREEN(ChatColor.GREEN, new String[]{"Creepers", "Alien", "Seaweed", "Emerald", "Grinch", "Shrub", "Snake", "Leaf"}),
		AQUA(ChatColor.AQUA, new String[]{"Diamond", "Ice", "Pool", "Kraken", "Aquatic", "Ocean"}),
		RED(ChatColor.RED, new String[]{"Heart", "Tomato", "Ruby", "Jam", "Rose", "Apple", "TNT"}),
		GOLD(ChatColor.GOLD, new String[]{"Mango", "Foxes", "Sunset", "Nuggets", "Lion", "Desert", "Gapple"}),
		LIGHT_PURPLE(ChatColor.LIGHT_PURPLE, new String[]{"Dream", "Cupcake", "Cake", "Candy", "Unicorn"}),
		DARK_BLUE(ChatColor.DARK_BLUE, new String[]{"Squid", "Lapis", "Sharks", "Galaxy", "Empoleon"}),
		DARK_RED(ChatColor.DARK_RED, new String[]{"Rose", "Apple", "Twizzler", "Rocket", "Blood"}),
		WHITE(ChatColor.WHITE, new String[]{"Ghosts", "Spookies", "Popcorn", "Seagull", "Rice", "Snowman", "Artic"}),
		BLUE(ChatColor.BLUE, new String[]{"Sky", "Whale", "Lake", "Birds", "Bluebird", "Piplup"}),
		DARK_GREEN(ChatColor.DARK_GREEN, new String[]{"Forest", "Zombies", "Cactus", "Slime", "Toxic", "Poison"}),
		DARK_PURPLE(ChatColor.DARK_PURPLE, new String[]{"Amethyst", "Slugs", "Grape", "Witch", "Magic", "Zula"}),
		DARK_AQUA(ChatColor.DARK_AQUA, new String[]{"Snorlax", "Aquatic", "Clam", "Fish"});

		private ChatColor color;
		private String[] names;

		private TeamColors(ChatColor color, String[] names)
		{
			this.color = color;
			this.names = names;
		}

		public ChatColor getColor()
		{
			return color;
		}

		public String[] getNames()
		{
			return names;
		}

	}

	private String[] getColorName(ChatColor color)
	{
		for(TeamColors colors : TeamColors.values())
		{
			if(colors.getColor() == color)
			{
				return colors.getNames();
			}
		}
		return null;
	}

	private TeamColors getNextColor(TeamColors color)
	{
		for(TeamColors colors : TeamColors.values()) {
			if(colors.ordinal() == color.ordinal() + 1)
			{
				return colors;
			}
		}
		return TeamColors.YELLOW;
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (GetTeamList().isEmpty())
			return;

		Scoreboard.Reset();

		if (IsLive())
		{
			Scoreboard.WriteBlank();
			Scoreboard.Write("§c§l" + UtilText.toSmallCaps("Game Info"));

			ArrayList<GameTeam> alive = new ArrayList<GameTeam>();
			for (GameTeam team : GetTeamList())
			{
				if (team.IsTeamAlive())
					alive.add(team);
			}

			if (GetPlayers(true).size() <= 4)
			{
				for (GameTeam team : GetTeamList())
				{
					for (Player player : team.GetPlayers(true))
					{
						Scoreboard.Write(team.GetColor() + player.getName());
					}
				}
			}
			else if (alive.size() <= 4)
			{
				for (GameTeam team : alive)
				{
					Scoreboard.Write("§f" + team.GetPlayers(true).size() + " " + team.GetColor() + team.GetName());
				}
			}
			else
			{
				Scoreboard.Write("§f" + alive.size() + " Teams Alive");
			}

			Scoreboard.Write("Players left: §a" + GetPlayers(true).size());

			// Find Top Killer Team
			GameTeam topKillerTeam = null;
			int maxTeamKills = 0;

			for (GameTeam team : GetTeamList())
			{
				int teamKills = 0;
				for (Player p : team.GetPlayers(false))
				{
					if (GetStats().containsKey(p) && GetStats().get(p).containsKey("Kills"))
					{
						teamKills += GetStats().get(p).get("Kills");
					}
				}

				if (teamKills > maxTeamKills)
				{
					maxTeamKills = teamKills;
					topKillerTeam = team;
				}

				// Show every alive team's kills
				if (team.IsTeamAlive() && teamKills > 0)
				{
					Scoreboard.Write(team.GetColor() + team.GetName() + " §7(§a" + teamKills + "§7)");
				}
			}

			// Find Top Player
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
			}

			// Show Top Killer Team
			if (topKillerTeam != null && maxTeamKills > 0)
			{
				Scoreboard.WriteBlank();
				Scoreboard.Write("§6§l" + UtilText.toSmallCaps("Top Team Kills"));
				Scoreboard.Write(topKillerTeam.GetColor() + topKillerTeam.GetName() + " §7(§a" + maxTeamKills + "§7)");
			}

			// Show Top Killer
			if (topKiller != null && maxKills > 0)
			{
				Scoreboard.WriteBlank();
				Scoreboard.Write("§6§l" + UtilText.toSmallCaps("Top Killer"));
				Scoreboard.Write(GetTeam(topKiller) != null ? GetTeam(topKiller).GetColor() + topKiller.getName() + " §7(§a" + maxKills + "§7)" : "§f" + topKiller.getName() + " §7(§a" + maxKills + "§7)");
			}

			Scoreboard.WriteBlank();
			Scoreboard.Write("§e§l" + UtilText.toSmallCaps("Time"));
			Scoreboard.Write("§f" + UtilTime.convertString(System.currentTimeMillis() - GetStateTime(), 0, TimeUnit.FIT));
		}
		else
		{
			// Recruit phase
			Scoreboard.WriteBlank();
			Scoreboard.Write("§e§l" + UtilText.toSmallCaps("Status"));
			Scoreboard.Write("Players: §a" + GetPlayers(true).size() + "§7/§a" + Manager.GetPlayerFull());
		}

		Scoreboard.Draw();
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		ArrayList<GameTeam> teamsAlive = new ArrayList<GameTeam>();

		for (GameTeam team : this.GetTeamList())
			if (team.GetPlayers(true).size() > 0)
				teamsAlive.add(team);

		if (teamsAlive.size() <= 1)
		{
			//Announce
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

			//End
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
	public boolean CanJoinTeam(GameTeam team)
	{
		return team.GetSize() < 2;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void teamSelectInteract(PlayerInteractEntityEvent event)
	{
		if (GetState() != GameState.Recruit)
			return;

		if (event.getRightClicked() == null)
			return;

		if (!(event.getRightClicked() instanceof Player))
			return;

		Player player = event.getPlayer();

		//Observer
		if (Manager.IsObserver(player))
		{
			UtilPlayer.message(player, F.main("Game", LangManager.get().isThai(player) ? "\u00A7c\u0e1c\u0e39\u0e49\u0e0a\u0e21\u0e44\u0e21\u0e48\u0e2a\u0e32\u0e21\u0e32\u0e23\u0e16\u0e23\u0e48\u0e27\u0e21\u0e40\u0e25\u0e48\u0e19\u0e40\u0e01\u0e21\u0e44\u0e14\u0e49" : "\u00A7cSpectators cannot join the game."));
			return;
		}

		selectTeamMate(player, (Player)event.getRightClicked());
	}

	@EventHandler
	public void teamSelectCommand(PlayerCommandPreprocessEvent event)
	{
		if (GetState() != GameState.Recruit)
			return;

		if (!event.getMessage().toLowerCase().startsWith("/team "))
			return;

		event.setCancelled(true);

		Player target = UtilPlayer.searchOnline(event.getPlayer(), event.getMessage().split(" ")[1], true);
		if (target == null)
			return;

		//Observer
		if (Manager.IsObserver(event.getPlayer()))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "\u00A7c\u0e1c\u0e39\u0e49\u0e0a\u0e21\u0e44\u0e21\u0e48\u0e2a\u0e32\u0e21\u0e32\u0e23\u0e16\u0e23\u0e48\u0e27\u0e21\u0e40\u0e25\u0e48\u0e19\u0e40\u0e01\u0e21\u0e44\u0e14\u0e49"));
			return;
		}

		if (event.getPlayer().equals(target))
			return;

		selectTeamMate(event.getPlayer(), target);
	}

	public void selectTeamMate(Player player, Player ally)
	{
		//Accept Invite
		if (_teamReqs.containsKey(ally) && _teamReqs.get(ally).equals(player))
		{
			//Remove Prefs
			_teamReqs.remove(player);
			_teamReqs.remove(ally);

			//Inform
			UtilPlayer.message(player, F.main("Game", "You accepted " + ally.getName() + "'s Team Request!"));
			UtilPlayer.message(ally, F.main("Game", player.getName() + " accepted your Team Request!"));

			//Leave Old Teams
			if (GetTeam(player) != null)
				GetTeam(player).DisbandTeam();

			if (GetTeam(ally) != null)
				GetTeam(ally).DisbandTeam();

			//Get Team
			GameTeam team = getEmptyTeam();
			if (team == null)
				return;

			//Join Team
			SetPlayerTeam(player, team, true);
			SetPlayerTeam(ally, team, true);
		}
		//Send Invite
		else
		{ { }
			//Already on Team with Target
			if (GetTeam(player) != null)
				if (GetTeam(player).HasPlayer(ally))
					return;

			//Inform Player
			UtilPlayer.message(player, F.main("Game", "You sent a Team Request to " + ally.getName() + "!"));

			//Inform Target
			if (Recharge.Instance.use(player, "Team Req " + ally.getName(), 2000, false, false))
			{
				UtilPlayer.message(ally, F.main("Game", player.getName() + " sent you a Team Request!"));
				UtilPlayer.message(ally, F.main("Game", "Type " + F.elem("/team " + player.getName()) + " to accept!"));
			}

			//Add Pref
			_teamReqs.put(player, ally);
		}
	}

	@EventHandler
	public void teamQuit(PlayerQuitEvent event)
	{
		if (GetState() != GameState.Recruit)
			return;

		Player player = event.getPlayer();

		if (GetTeam(player) != null)
			GetTeam(player).DisbandTeam();

		Iterator<Player> teamIter = _teamReqs.keySet().iterator();
		while (teamIter.hasNext())
		{
			Player sender = teamIter.next();
			if (sender.equals(player) || _teamReqs.get(sender).equals(player))
				teamIter.remove();
		}
	}

	public GameTeam getEmptyTeam()
	{
		for (GameTeam team : GetTeamList())
		{
			if (team.GetPlayers(false).isEmpty())
				return team;
		}

		return null;
	}

	@Override
	public String GetMode()
	{
		return "Team Mode";
	}
}
