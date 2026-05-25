package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.ArrayList;
import java.util.Iterator;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.common.util.UtilTextTop;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.TransitionReason;
import com.houzicore.shared.core.gadget.gadgets.MorphWither;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
//import com.houzicore.shared.condition.Condition.ConditionType;
import com.houzicore.arcade.bootstrap.ArcadeBootstrap;
import com.houzicore.shared.core.mount.Mount;
import com.houzicore.shared.core.mount.types.MountDragon;
import com.houzicore.shared.updater.UpdateType;
//import com.houzicore.shared.updater.event.RestartServerEvent;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GamePrepareCountdownCommence;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;


import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GameManager implements Listener
{
	ArcadeManager Manager;
	
	public GameManager(ArcadeManager manager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}
	
	@EventHandler
	public void DisplayIP(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		
		Game game = Manager.GetGame();
		if (game == null)
			return;

		if (game.InProgress())
		{
			if (game.GetBossBarText() != null)
			{
				String barText = game.GetBossBarText();
				double barHealth = game.GetBossBarHealth();
				org.bukkit.boss.BarColor barColor = game.GetBossBarColor();
				
				UtilTextTop.displayProgress(barText, barHealth, barColor, UtilServer.getPlayers());
			}
		}
	}
	
	@EventHandler
	public void DisplayPrepareTime(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null || Manager.GetGame().GetState() != GameState.Prepare)
			return;

		Game game = Manager.GetGame();

		double percentage = (double) (System.currentTimeMillis() - game.GetStateTime()) / game.PrepareTime;
		
		for (Player player : UtilServer.getPlayers())
			UtilTextBottom.displayProgress(ActionBarChannel.GAME_STATUS, "Game Start", percentage,
					UtilTime.MakeStr(Math.max(0, game.PrepareTime - (System.currentTimeMillis() - game.GetStateTime()))), player);
	}

	@EventHandler
	public void StateUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		Game game = Manager.GetGame();
		if (game == null)	return;

		// Empty Lobby Reset: If no players remain and we are in a pre-game/lobby phase, abort the game.
		// GameCreationManager will clean this up and spawn a fresh map voting cycle when a player joins.
		if (UtilServer.getPlayers().length == 0)
		{
			if (game.GetState() == GameState.Vote || game.GetState() == GameState.Loading || game.GetState() == GameState.Recruit || game.GetState() == GameState.Prepare)
			{
				game.SetState(GameState.Dead);
				return;
			}
		}

		if (game.GetState() == GameState.Loading)
		{
			// Increased from 30s to 120s because large maps (like Primal Games or UHC) 
			// can easily take > 30 seconds to load all chunks into memory.
			if (UtilTime.elapsed(game.GetStateTime(), 120000))
			{
				game.SetState(GameState.Dead);
			}
		} 
		else if (game.GetState() == GameState.Recruit)
		{
			//Stop Countdown!
			if (game.GetCountdown() != -1 && 
					UtilServer.getPlayers().length < Manager.GetPlayerMin() && 
					!game.GetCountdownForce())
			{
				game.SetCountdown(-1);
				Manager.GetLobby().DisplayWaiting();
			}
	
			if (Manager.IsGameAutoStart())
			{
				if (UtilServer.getPlayers().length >= Manager.GetPlayerFull()) 
					StateCountdown(game, 20, false);

				else if (UtilServer.getPlayers().length >= Manager.GetPlayerMin())
					StateCountdown(game, 60, false);	
				
				else if (game.GetCountdown() != -1)
					StateCountdown(game, -1, false);
			}
			else if (game.GetCountdown() != -1)
			{
				StateCountdown(game, -1, false);
			}
		}
		else if (game.GetState() == GameState.Prepare)
		{
			if (game.CanStartPrepareCountdown())
			{
				if (UtilTime.elapsed(game.GetStateTime(), game.PrepareTime) || game.ForceStart)
				{
					int players = game.GetPlayers(true).size();
					
					if ((players < 2 && !game.ForceStart) || game.PlaySoundGameStart)
					{
						for (Player player : UtilServer.getPlayers())
							player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 2f);
					}

					if (players < 2 && !game.ForceStart) 
					{
						game.Announce(C.cWhite + C.Bold + game.GetName() + " ended, not enough players!");
						game.SetState(GameState.Dead);
					}
					else
					{
						game.SetState(GameState.Live);
					
					// ── Good Luck Title (UX Standard: §a§l + ENDER_DRAGON_GROWL) ──
					com.houzicore.shared.common.util.UtilTextMiddle.display(
						"§a§lเริ่มเกม!", "§eGood Luck!", 5, 40, 20, UtilServer.getPlayers());
					for (Player p : UtilServer.getPlayers())
						p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.5f);
					}
				}
				else
				{
					for (Player player : UtilServer.getPlayers())
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
				}
			}
		}
		else if (game.GetState() == GameState.Live)
		{
			if (game.GameTimeout != -1 && UtilTime.elapsed(game.GetStateTime(), game.GameTimeout) && Manager.IsGameTimeout())
			{
				game.HandleTimeout();
			}
		}
		else if (game.GetState() == GameState.End)
		{
			if (UtilTime.elapsed(game.GetStateTime(), WinCelebrationManager.POSTGAME_SUMMARY_DELAY_TICKS * 50L))
			{
				game.SetState(GameState.Dead);
			}
		}
	}

	public void StateCountdown(Game game, int timer, boolean force)
	{
		if (Manager.GetGameHostManager().isPrivateServer() && Manager.GetGameHostManager().isVoteInProgress())
			return;

		//Disabling Cosmetics
		if (game.GetCountdown() <= 5 && game.GetCountdown() >= 0 && game.GadgetsDisabled)
		{
			if (Manager.getCosmeticManager().isShowingInterface())
			{
				Manager.getCosmeticManager().setActive(false);
				Manager.getCosmeticManager().disableItemsForGame();
			}
		}

		if (force)
			game.SetCountdownForce(true);
		
		//Team Preference
		TeamPreferenceJoin(game);

		//Team Swap
		TeamPreferenceSwap(game);

		//Team Default
		TeamDefaultJoin(game);

		//Team Inform STILL Queued
		if (game.GetCountdown() == -1)
		{
			game.InformQueuePositions();
			//game.AnnounceGame();
		}
		
		//Initialise Countdown
		if (force)
			game.SetCountdownForce(true);

		//Start  Timer
		if (game.GetCountdown() == -1)
			game.SetCountdown(timer + 1);

		//Decrease Timer
		if (game.GetCountdown() > timer + 1 && timer != -1)
			game.SetCountdown(timer + 1);

		//Countdown--
		if (game.GetCountdown() > 0)
			game.SetCountdown(game.GetCountdown() - 1);

		//Voting removed - handled externally

		//Inform Countdown
		if (game.GetCountdown() > 0)		
		{
			com.houzicore.shared.common.util.UtilTextMiddle.display(
				"",
				C.cGreen + com.houzicore.shared.core.lang.LangManager.get().getOrDefault(null, "arcade.lobby.starting_in", "Starting in ") + game.GetCountdown() + "s",
				0, 40, 0,
				com.houzicore.shared.common.util.UtilServer.getPlayers()
			);
		}
		else					
		{
			// Optional: Clear or show "Game in progress" if needed
		}

		if (game.GetCountdown() > 0 && game.GetCountdown() <= 10)
			for (Player player : UtilServer.getPlayers())
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

		//Countdown Ended
		if (game.GetCountdown() == 0)
			game.SetState(GameState.Prepare);
	}
	
//	@EventHandler
//	public void restartServerCheck(RestartServerEvent event)
//	{
//		if (Manager.GetGame() != null && Manager.GetGame().GetState() != GameState.Recruit)
//			event.setCancelled(true);
//	}
	
	@EventHandler
	public void KitRegister(GameStateChangeEvent event) 
	{
		if (event.GetState() != event.GetGame().KitRegisterState)
			return;

		event.GetGame().RegisterKits();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void KitDeregister(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Dead)
			return;

		event.GetGame().DeregisterKits();
		event.GetGame().deRegisterStats();
	}
	
	
	
	@EventHandler
	public void ScoreboardTitle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		Game game = Manager.GetGame();
		if (game == null)	return;
		
		game.GetScoreboard().UpdateTitle();
	}

	@EventHandler(priority = EventPriority.LOWEST)	//BEFORE PARSE DATA
	public void TeamGeneration(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Recruit)
			return;

		Game game = event.GetGame();
		int count = 1;
		
		for (String team : game.WorldData.SpawnLocs.keySet())
		{
			ChatColor color;

			if (team.equalsIgnoreCase("RED"))			color = ChatColor.RED;
			else if (team.equalsIgnoreCase("YELLOW"))	color = ChatColor.YELLOW;
			else if (team.equalsIgnoreCase("GREEN"))	color = ChatColor.GREEN;
			else if (team.equalsIgnoreCase("BLUE") || team.equalsIgnoreCase("SKY") || team.equalsIgnoreCase("LIGHT_BLUE"))		color = ChatColor.AQUA;
			else
			{
				color = ChatColor.DARK_GREEN;

				if (game.GetTeamList().size()%14 == 0) 		if (game.WorldData.SpawnLocs.size() > 1)		color = ChatColor.RED;
				if (game.GetTeamList().size()%14 == 1) 		color = ChatColor.YELLOW;
				if (game.GetTeamList().size()%14 == 2) 		color = ChatColor.GREEN;
				if (game.GetTeamList().size()%14 == 3) 		color = ChatColor.AQUA;
				if (game.GetTeamList().size()%14 == 4) 		color = ChatColor.GOLD;
				if (game.GetTeamList().size()%14 == 5) 		color = ChatColor.LIGHT_PURPLE;
				if (game.GetTeamList().size()%14 == 6) 		color = ChatColor.DARK_BLUE;
				if (game.GetTeamList().size()%14 == 7) 		color = ChatColor.WHITE;
				if (game.GetTeamList().size()%14 == 8) 		color = ChatColor.BLUE;
				if (game.GetTeamList().size()%14 == 9) 		color = ChatColor.DARK_GREEN;
				if (game.GetTeamList().size()%14 == 10) 	color = ChatColor.DARK_PURPLE;
				if (game.GetTeamList().size()%14 == 11) 	color = ChatColor.DARK_RED;
				if (game.GetTeamList().size()%14 == 12) 	color = ChatColor.DARK_AQUA;
			}
			
			//Random Names
			String teamName = team;
			if (game.WorldData.SpawnLocs.size() > 12)
			{
				teamName = "" + count;
				count++;
			}

			GameTeam newTeam = new GameTeam(game, teamName, color, game.WorldData.SpawnLocs.get(team));
			game.AddTeam(newTeam);
		}

		//Restrict Kits
		game.RestrictKits();

		//Parse Data
		game.ParseData();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void TeamScoreboardCreation(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Recruit)
			return;

		event.GetGame().GetScoreboard().CreateTeams();
	}

	public void TeamPreferenceJoin(Game game)
	{
		//Preferred Team No Longer Full
		for (GameTeam team : game.GetTeamPreferences().keySet())
		{	
			Iterator<Player> queueIterator = game.GetTeamPreferences().get(team).iterator();

			while (queueIterator.hasNext())
			{
				Player player = queueIterator.next();

				if (!game.CanJoinTeam(team))
				{
					break;
				}
									
				queueIterator.remove();

				if (!game.IsPlaying(player))
				{
					PlayerAdd(game, player, team);
				}
				else
				{
					game.SetPlayerTeam(player, team, true);
				}
			}
		}
	}

	public void TeamPreferenceSwap(Game game)
	{
		//Preferred Team No Longer Full
		for (GameTeam team : game.GetTeamPreferences().keySet())
		{	
			Iterator<Player> queueIterator = game.GetTeamPreferences().get(team).iterator();

			while (queueIterator.hasNext())
			{
				Player player = queueIterator.next();

				GameTeam currentTeam = game.GetTeam(player);

				//Not on team, cannot swap
				if (currentTeam == null)
					continue;

				// Other without concurrent (order doesn't matter as first case will fire
				if (team == currentTeam)
				{
					queueIterator.remove();
					continue;
				}

				for (Player other : team.GetPlayers(false))
				{
					if (other.equals(player))
						continue;

					GameTeam otherPref = game.GetTeamPreference(other);
					if (otherPref == null)
						continue;

					if (otherPref.equals(currentTeam))
					{
						UtilPlayer.message(player, F.main("Team", "You swapped team with " + F.elem(team.GetColor() + other.getName()) + "."));
						UtilPlayer.message(other, F.main("Team", "You swapped team with " + F.elem(currentTeam.GetColor() + player.getName()) + "."));

						//Player Swap
						queueIterator.remove();
						game.SetPlayerTeam(player, team, true);

						//Other Swap
						game.SetPlayerTeam(other, currentTeam, true);
						
						break;
					}
				}		
			}
		}
	}

	public void TeamDefaultJoin(Game game) 
	{
		//Team Default
		for (Player player : UtilServer.getPlayers())
		{
			if (player.isDead())
			{
				player.sendMessage(F.main("Afk Monitor", "You are being sent to the Lobby for being AFK."));
				Manager.GetPortal().sendPlayerToServer(player, "Lobby");
			}
			else if (Manager.IsObserver(player))
			{
				
			}
			else if (!game.IsPlaying(player) && Manager.IsTeamAutoJoin())
			{
				PlayerAdd(game, player, null);
			}
		}
	}

	@EventHandler
	public void TeamQueueSizeUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Game game = Manager.GetGame();
		if (game == null)	return;

		for (GameTeam team : game.GetTeamList())
		{
			int amount = 0;
			if (game.GetTeamPreferences().containsKey(team))
			{
				amount = game.GetTeamPreferences().get(team).size();
			}

			if (team.GetTeamEntity() == null)
				continue;
			
			String nameText = "";
			if (game.GetCountdown() == -1)
			{
				nameText = team.GetFormattedName() + " Team" + ChatColor.RESET + "  " + amount + " Queued";
			}
			else
			{
				nameText = team.GetPlayers(false).size() + " Players  " + team.GetFormattedName() + " Team" + ChatColor.RESET + "  " + amount + " Queued";
			}

			if (team.GetTeamEntity() instanceof org.bukkit.entity.TextDisplay)
			{
				((org.bukkit.entity.TextDisplay) team.GetTeamEntity()).setText(nameText);
			}
			else
			{
				team.GetTeamEntity().setCustomName(nameText);
			}
		}
	}
	
	public boolean PlayerAdd(Game game, Player player, GameTeam team)
	{
		if (team == null)
			team = game.ChooseTeam(player);

		if (team == null)
			return false;

		game.SetPlayerTeam(player, team, true);

		return true;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PlayerPrepare(GameStateChangeEvent event)
	{
		final Game game = event.GetGame();

		if (event.GetState() != GameState.Prepare)
			return;

		final ArrayList<Player> players = game.GetPlayers(true);
		
		//Prepare Players
		for (int i=0 ; i<players.size() ; i++)
		{
			final Player player = players.get(i);
			
			final GameTeam team = game.GetTeam(player);
			
			UtilServer.getServer().getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					//Teleport
					team.SpawnTeleport(player);

					if (game.isContextRuntime())
					{
						ArcadeBootstrap.getInstance().applyCurrentContextState(player);
					}
					else
					{
						ArcadeBootstrap.getInstance().applyContextState(player, PlayerContextId.ARCADE_PREP);
					}

					game.ValidateKit(player, game.GetTeam(player));

					if (game.GetKit(player) != null)
						game.GetKit(player).ApplyKit(player);
					
					//Event
					PlayerPrepareTeleportEvent playerStateEvent = new PlayerPrepareTeleportEvent(game, player);
					UtilServer.getServer().getPluginManager().callEvent(playerStateEvent);			
				}
			}, i * game.TickPerTeleport);
		}
		
		//Announce Game after every player is TP'd in
		UtilServer.getServer().getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				game.AnnounceGame();
				game.StartPrepareCountdown();
				
				//Event
				GamePrepareCountdownCommence event = new GamePrepareCountdownCommence(game);
				UtilServer.getServer().getPluginManager().callEvent(event);			
			}
		}, players.size() * game.TickPerTeleport);
		
		//Spectators Move
		for (Player player : UtilServer.getPlayers())
		{
			if (player.isDead())
			{
				continue;
			}
			
			if (Manager.GetGame().IsAlive(player))
				continue;

			Manager.addSpectator(player, true);
		}
	}

	@EventHandler
	public void PlayerTeleportOut(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Dead)
			return;
		
		Manager.clearSpectators();

		final Player[] players = UtilServer.getPlayers();
		
		//Prepare Players
		for (int i=0 ; i<players.length ; i++)
		{
			final Player player = players[i];
			// Log immediately (before delay) what each player's state is

			UtilServer.getServer().getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					// CRITICAL FIX: Never call setGameMode or teleport on a dead player.
					// Paper 1.21 sends a dimension-change log-in packet when GameMode changes
					// during death, locking the player in the DimensionTransition ghost state.
					if (player.isDead())
					{
						return;
					}

					player.eject();
					player.leaveVehicle();
					player.teleport(Manager.GetLobby().GetSpawn());
					ArcadeBootstrap.getInstance().restoreRoundSnapshotToLobby(player, TransitionReason.RETURN_TO_LOBBY);
				}
			}, i + 2);
		}
	}
	
	@EventHandler
	public void gameCleanup(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Prepare || event.GetState() == GameState.Dead)
		{
			Manager.getHologramManager().clearOwner(event.GetGame());
			Manager.getDisplayEntityManager().clearOwner(event.GetGame());
		}

		if (event.GetState() != GameState.Dead)
			return;

		event.GetGame().unregisterModules();
		Manager.GetDisguise().clearDisguises();

		if (event.GetGame().GetScoreboard() != null)
		{
			event.GetGame().GetScoreboard().close();
		}
	}

 
	@EventHandler
	public void WorldFireworksUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		Game game = Manager.GetGame();
		if (game == null)	return;

		if (game.GetState() != GameState.End)
			return;

		Color color = Color.GREEN;

		if (game.WinnerTeam != null)
		{
			if (game.WinnerTeam.GetColor() == ChatColor.RED)				color = Color.RED;
			else if (game.WinnerTeam.GetColor() == ChatColor.AQUA)			color = Color.BLUE;
			else if (game.WinnerTeam.GetColor() == ChatColor.YELLOW)		color = Color.YELLOW;
			else															color = Color.LIME;
		}
		
		Location loc = game.GetSpectatorLocation().clone().add(Math.random()*160-80, 10 + Math.random()*20, Math.random()*160-80);

		UtilFirework.playFirework(loc, Type.BALL_LARGE, color, false, false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void EndUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		Game game = Manager.GetGame();
		if (game == null)	return;

		game.EndCheck();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void EndStateChange(PlayerStateChangeEvent event)
	{
		event.GetGame().EndCheck();
	}
}
