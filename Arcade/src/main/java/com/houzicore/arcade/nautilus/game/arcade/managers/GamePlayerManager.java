package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTabTitle;
import com.houzicore.shared.core.visibility.VisibilityManager;
import com.houzicore.shared.core.donation.Donor;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.core.shop.page.ConfirmationPage;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.bootstrap.ArcadeBootstrap;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.shop.ArcadeShop;
import com.houzicore.arcade.nautilus.game.arcade.shop.KitPackage;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.TransitionReason;
import com.houzicore.shared.core.lang.LangManager;


public class GamePlayerManager implements Listener
{
	ArcadeManager Manager;

	public GamePlayerManager(ArcadeManager manager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void PlayerDeath(PlayerDeathEvent event)
	{
		// In Paper 1.21.1, spigot().respawn() causes lethal client desyncs (ghost players).
		// We now rely on GameRule.DO_IMMEDIATE_RESPAWN in GameWorldManager to auto-respawn natively.

		//Dont display message
		if (Manager.GetGame() != null) {
//			event.SetBroadcastType(Manager.GetGame().GetDeathMessageType());

		//Colors
		}
		if (event.getEntity().getKiller() != null)
		{
			Player player = UtilPlayer.searchExact(event.getEntity().getKiller().getName());
			if (player != null)
			{
				Manager.GetDamage().GetCombatManager().Get(player).SetKillerColor(Manager.GetColor(player)+"");
			}
		}

		if (event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			if (player != null)
			{
				Manager.GetDamage().GetCombatManager().Get(player).SetKilledColor(Manager.GetColor(player)+"");
			}
		}
	}

	@EventHandler
	public void PlayerJoin(PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();

		//Player List
		// Shared TablistFix owns player-list rendering, including native player-head components.
		
		//Lobby Name
		Manager.GetLobby().AddPlayerToScoreboards(player, null);

		//Lobby Spawn
		if (Manager.GetGame() == null || !Manager.GetGame().InProgress())
		{
			player.teleport(Manager.GetLobby().GetSpawn());
			ArcadeBootstrap.getInstance().applyCurrentContextState(player);
			return;
		}

		//Game Spawn
		if (Manager.GetGame().IsAlive(player))
		{
			Location loc = Manager.GetGame().GetLocationStore().remove(player.getName());
			if (loc != null && !loc.getWorld().getName().equalsIgnoreCase("world"))
			{
				player.teleport(loc);
			}			
			else
			{
				ArcadeBootstrap.getInstance().getPlayerStateApplier().cleanState(player);
				player.teleport(Manager.GetGame().GetTeam(player).GetSpawn());
			}

			if (Manager.GetGame().isContextRuntime())
			{
				PlayerContextId contextId = Manager.GetGame().GetState() == GameState.Prepare
						? PlayerContextId.ARCADE_PREP
						: PlayerContextId.ARCADE_LIVE;
				ArcadeBootstrap.getInstance().transitionAndApply(player, contextId, TransitionReason.JOIN);
			}
		} 
		else
		{
			Manager.addSpectator(player, true);
			UtilPlayer.message(player, F.main("Game", "\u00A7e" + Manager.GetGame().GetName() + (LangManager.get().isThai(player) ? " \u00A77\u0e01\u0e33\u0e25\u0e31\u0e07\u0e41\u0e02\u0e48\u0e07\u0e02\u0e31\u0e19 \u0e42\u0e1b\u0e23\u0e14\u0e23\u0e2d\u0e40\u0e01\u0e21\u0e16\u0e31\u0e14\u0e44\u0e1b!" : " \u00A77is in progress. Please wait for the next game!")));
		}

		Manager.GetGame().GetScoreboard().applyBoard(player);
	}

	@EventHandler
	public void PlayerRespawn(PlayerRespawnEvent event)
	{
		Game game = Manager.GetGame();
		Player player = event.getPlayer();

		// --- Case 1: No game at all → lobby ---
		if (game == null)
		{
			final Location lobbySpawn = Manager.GetLobby().GetSpawn();
			event.setRespawnLocation(lobbySpawn);
			Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
				if (player.isOnline() && !player.isDead()) {
					player.teleport(lobbySpawn);
					ArcadeBootstrap.getInstance().restoreRoundSnapshotToLobby(player, com.houzicore.shared.api.context.TransitionReason.RETURN_TO_LOBBY);
					Manager.GetLobby().resendAllKitGlowStates(player);
				}
			}, 1L);
			return;
		}

		// --- Case 2: Game in progress, player is alive → respawn at team spawn ---
		if (game.InProgress() && game.IsAlive(player))
		{
			event.setRespawnLocation(game.GetTeam(player).GetSpawn());
			game.RespawnPlayer(player);
			return;
		}

		// --- Case 3: Game still live, player eliminated, has team → spectate in game world ---
		// Only set respawn location here. GameDeathManager handles the spectator transition
		// post-respawn via its own MONITOR-priority PlayerRespawnEvent handler.
		if (game.GetState() != GameState.Dead && game.GetTeam(player) != null && game.WorldData != null && game.WorldData.World != null)
		{
			final Location specLoc = game.GetSpectatorLocation();
			event.setRespawnLocation(specLoc);
			return;
		}

		// --- Case 4: Game ended or no team → lobby ---
		final Location lobbySpawn = Manager.GetLobby().GetSpawn();
		event.setRespawnLocation(lobbySpawn);
		
		Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
			if (player.isOnline()) {
				player.teleport(lobbySpawn);
				ArcadeBootstrap.getInstance().restoreRoundSnapshotToLobby(player, com.houzicore.shared.api.context.TransitionReason.RETURN_TO_LOBBY);
				Manager.GetLobby().resendAllKitGlowStates(player);
			}
		}, 1L);
	}

	public void HandleFakeRespawn(Player player)
	{
		Game game = Manager.GetGame();


		// --- Case 1: No game at all → lobby ---
		if (game == null)
		{
			final Location lobbySpawn = Manager.GetLobby().GetSpawn();
			player.teleport(lobbySpawn);
			ArcadeBootstrap.getInstance().restoreRoundSnapshotToLobby(player, com.houzicore.shared.api.context.TransitionReason.RETURN_TO_LOBBY);
			return;
		}

		// --- Case 2: Game in progress, player is alive → respawn at team spawn ---
		if (game.InProgress() && game.IsAlive(player))
		{
			player.teleport(game.GetTeam(player).GetSpawn());
			game.RespawnPlayer(player);
			return;
		}

		// --- Case 3: Game still live, player eliminated, has team → spectate in game world ---
		if (game.GetState() != GameState.Dead && game.GetTeam(player) != null && game.WorldData != null && game.WorldData.World != null)
		{
			final Location specLoc = game.GetSpectatorLocation();
			player.teleport(specLoc);
            
			// Defer spectator mode — same pattern as GameDeathManager
			Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
				if (player.isOnline() && !player.isDead()) {
					Manager.addSpectator(player, specLoc);
				}
			}, 5L);
			return;
		}

		// --- Case 4: Game ended or no team → lobby ---
		final Location lobbySpawn = Manager.GetLobby().GetSpawn();
		player.teleport(lobbySpawn);
		
		Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
			if (player.isOnline()) {
				ArcadeBootstrap.getInstance().restoreRoundSnapshotToLobby(player, com.houzicore.shared.api.context.TransitionReason.RETURN_TO_LOBBY);
			}
		}, 1L);
	}
	
	@EventHandler
	public void PlayerStateChange(PlayerStateChangeEvent event)
	{
		Recharge.Instance.useForce(event.GetPlayer(), "Return to Hub", 4000);
	}
	
	@EventHandler
	public void DisallowCreativeClick(InventoryClickEvent event)
	{
		if (Manager.GetGame() == null || !Manager.GetGame().InProgress() || Manager.GetGameHostManager().isEventServer())
			return;
		
		if ((event.getInventory().getType() == InventoryType.CREATIVE || event.getInventory().getType() == InventoryType.PLAYER) && !event.getWhoClicked().isOp())
		{
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void TeamInteract(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() == null)
			return;

		Player player = event.getPlayer();

		GameTeam team = Manager.GetLobby().GetClickedTeam(event.getRightClicked());

		if (team == null)
			return;

		//Observer
		if (Manager.IsObserver(player))
		{
			UtilPlayer.message(player, F.main("Game", LangManager.get().isThai(player) ? "\u00A7c\u0e1c\u0e39\u0e49\u0e0a\u0e21\u0e44\u0e21\u0e48\u0e2a\u0e32\u0e21\u0e32\u0e23\u0e16\u0e23\u0e48\u0e27\u0e21\u0e40\u0e25\u0e48\u0e19\u0e40\u0e01\u0e21\u0e44\u0e14\u0e49" : "\u00A7cSpectators cannot join the game."));
			return;
		}
		
		TeamClick(player, team);
	}

	@EventHandler
	public void TeamDamage(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
			return;

		Player player = ((Player) event.getDamager());

		if (!(event.getEntity() instanceof LivingEntity))
			return;

		LivingEntity target = (LivingEntity) event.getEntity();

		GameTeam team = Manager.GetLobby().GetClickedTeam(target);

		if (team == null)
			return;
		
		//Observer
		if (Manager.IsObserver(player))
		{
			UtilPlayer.message(player, F.main("Game", LangManager.get().isThai(player) ? "\u00A7c\u0e1c\u0e39\u0e49\u0e0a\u0e21\u0e44\u0e21\u0e48\u0e2a\u0e32\u0e21\u0e32\u0e23\u0e16\u0e23\u0e48\u0e27\u0e21\u0e40\u0e25\u0e48\u0e19\u0e40\u0e01\u0e21\u0e44\u0e14\u0e49" : "\u00A7cSpectators cannot join the game."));
			return;
		}
		
		TeamClick(player, team);
	}

	public void TeamClick(final Player player, final GameTeam team)
	{
		if (Manager.GetGame() == null)
			return;

		if (Manager.GetGame().GetState() != GameState.Recruit)
			return;

		if (!Manager.GetGame().HasTeam(team))
			return;

		AddTeamPreference(Manager.GetGame(), player, team);
	}

	public void AddTeamPreference(Game game, Player player, GameTeam team)
	{
		GameTeam past = game.GetTeamPreference(player);

		GameTeam current = game.GetTeam(player);
		if (current != null && current.equals(team))
		{
			game.RemoveTeamPreference(player);
			UtilPlayer.message(player, F.main("Team", "You are already on " + F.elem(team.GetFormattedName()) + "."));
			return;
		}

		if (past == null || !past.equals(team))
		{
			if (past != null)
			{
				game.RemoveTeamPreference(player);
			}	

			if (!game.GetTeamPreferences().containsKey(team))
				game.GetTeamPreferences().put(team, new ArrayList<Player>());

			game.GetTeamPreferences().get(team).add(player);
		}

		UtilPlayer.message(player, F.main("Team", "You are " + F.elem(game.GetTeamQueuePosition(player)) + " in queue for " + F.elem(team.GetFormattedName() + " Team") + "."));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void KitInteract(PlayerInteractEntityEvent event)
	{
		if (event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) return;

		if (event.getRightClicked() == null)
			return;

		Player player = event.getPlayer();

		Kit kit = Manager.GetLobby().GetClickedKit(event.getRightClicked());

		if (kit == null)
			return;
		
		//Observer
		if (Manager.IsObserver(player) || Manager.isSpectator(player)) 
		{
			UtilPlayer.message(player, F.main("Game", LangManager.get().isThai(player) ? "\u00A7c\u0e1c\u0e39\u0e49\u0e0a\u0e21\u0e44\u0e21\u0e48\u0e2a\u0e32\u0e21\u0e32\u0e23\u0e16\u0e23\u0e48\u0e27\u0e21\u0e40\u0e25\u0e48\u0e19\u0e40\u0e01\u0e21\u0e44\u0e14\u0e49" : "\u00A7cSpectators cannot join the game."));
			return;
		}

		// If player already has this kit selected → open Trait shop
		// Otherwise → select the kit first
		Kit currentKit = Manager.GetGame() != null ? Manager.GetGame().GetKit(player) : null;
		boolean sameKitSelected = currentKit != null &&
				(currentKit == kit || currentKit.GetName().equalsIgnoreCase(kit.GetName()));

		if (sameKitSelected)
		{
			// Already selected this kit → open trait upgrades
			if (!Manager.getTraitManager().getTraitsForKit(kit).isEmpty())
			{
				Manager.getTraitShop().openForPlayer(player, kit);
			}
			else
			{
				UtilPlayer.message(player, F.main("Trait", com.houzicore.shared.core.lang.LangManager.get().get(player, "arcade.trait.no_traits")));
			}
		}
		else
		{
			// First click → select the kit
			KitClick(player, kit, event.getRightClicked());
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void KitDamage(EntityDamageByEntityEvent event)
	{
		if (Manager.GetGame() != null && Manager.GetGame().InProgress())
			return;

		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		if (!(event.getDamager() instanceof Player))
			return;

		Player player = ((Player) event.getDamager());

		if (!(event.getEntity() instanceof LivingEntity))
			return;

		LivingEntity target = (LivingEntity) event.getEntity();

		Kit kit = Manager.GetLobby().GetClickedKit(target);

		if (kit == null)
			return;
		
		//Observer
		if (Manager.IsObserver(player) || Manager.isSpectator(player))
		{
			UtilPlayer.message(player, F.main("Game", LangManager.get().isThai(player) ? "\u00A7c\u0e1c\u0e39\u0e49\u0e0a\u0e21\u0e44\u0e21\u0e48\u0e2a\u0e32\u0e21\u0e32\u0e23\u0e16\u0e23\u0e48\u0e27\u0e21\u0e40\u0e25\u0e48\u0e19\u0e40\u0e01\u0e21\u0e44\u0e14\u0e49" : "\u00A7cSpectators cannot join the game."));
			return;
		}
		
		KitClick(player, kit, target);
	}

	public void KitClick(final Player player, final Kit kit, final Entity entity)
	{
		kit.DisplayDesc(player);

		if (Manager.GetGame() == null)
			return;

		if (!Manager.GetGame().HasKit(kit))
			return;

		Donor donor = Manager.GetDonation().Get(player.getName());

		if (kit.GetAvailability() == KitAvailability.Free || 									//Free
			
			Manager.hasKitsUnlocked(player) || 														//YouTube
			
			(kit.GetAvailability() == KitAvailability.Achievement && 							//Achievement
			Manager.GetAchievement().hasCategory(player, kit.getAchievementRequirement())) ||	
			
			donor.OwnsUnknownPackage(Manager.GetGame().GetName() + " " + kit.GetName()) ||		//Green
			
			Manager.GetClients().Get(player).GetRank().Has(Rank.MAPDEV) ||						//STAFF
			
			donor.OwnsUnknownPackage(Manager.GetServerConfig().ServerType + " ULTRA") ||		//Single Ultra (Old)
			
			Manager.GetServerConfig().Tournament)												//Tournament
		{
			Manager.GetGame().SetKit(player, kit, true);
		}
		else if (kit.GetAvailability() == KitAvailability.Gem && donor.GetBalance(CurrencyType.Essence) >= kit.GetCost())
		{
			Manager.GetShop().openPageForPlayer(player, new ConfirmationPage<ArcadeManager, ArcadeShop>(
					Manager, Manager.GetShop(), Manager.GetClients(), Manager.GetDonation(), new Runnable()
			{
				public void run()
				{
					if (player.isOnline())
					{
						Manager.GetGame().SetKit(player, kit, true);
					}
				}

			}, null, new KitPackage(Manager.GetGame().GetName(), kit, player), CurrencyType.Essence, player));
		}
		else if (kit.GetAvailability() == KitAvailability.Achievement)
		{
			UtilPlayer.message(player, F.main("Kit", "You have not unlocked all " + F.elem(C.cPurple + Manager.GetGame().GetName() + " Achievements") + "."));
		}
		else
		{
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2f, 0.5f);

			UtilPlayer.message(player, F.main("Kit", LangManager.get().isThai(player) ? "\u00A7c\u0e04\u0e38\u0e13\u0e21\u0e35 " + F.elem(C.cGreen + "Essence") + " \u00A7c\u0e44\u0e21\u0e48\u0e40\u0e1e\u0e35\u0e22\u0e07\u0e1e\u0e2d" : "\u00A7cYou don't have enough " + F.elem(C.cGreen + "Essence") + "."));
		}

	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void TeleportCommand(PlayerCommandPreprocessEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)
			return;
		
		Player player = event.getPlayer();
		
		if (Manager.GetClients().Get(player).GetRank().Has(Rank.MODERATOR))
			return;
		
		if (event.getMessage().toLowerCase().startsWith("/tp"))
		{
			UtilPlayer.message(player, F.main("Game", "Spectate Teleport changed to " + F.elem("/spec <name>") + "."));
			event.setCancelled(true);
			return;
		}
		
		if (!event.getMessage().toLowerCase().startsWith("/spec"))
			return;
		
		event.setCancelled(true);
		
		if (game.IsAlive(player) || !Manager.isSpectator(player))
		{
			UtilPlayer.message(player, F.main("Game", LangManager.get().isThai(player) ? "\u00A7c\u0e40\u0e09\u0e1e\u0e32\u0e30\u0e1c\u0e39\u0e49\u0e0a\u0e21\u0e40\u0e17\u0e48\u0e32\u0e19\u0e31\u0e49\u0e19\u0e17\u0e35\u0e48\u0e2a\u0e32\u0e21\u0e32\u0e23\u0e16\u0e43\u0e0a\u0e49\u0e04\u0e33\u0e2a\u0e31\u0e48\u0e07\u0e19\u0e35\u0e49\u0e44\u0e14\u0e49" : "\u00A7cOnly spectators can use this command."));
			return;
		}
		
		String[] tokens = event.getMessage().split(" ");
		
		if (tokens.length != 2)
		{
			UtilPlayer.message(player, F.main("Game", "Invalid Input. " + F.elem("/spec <Name>") + "."));
			return;
		}
		
		Player target = UtilPlayer.searchOnline(player, tokens[1], true);
		if (target != null)
		{
			UtilPlayer.message(player, F.main("Game", "You teleported to " + F.name(target.getName()) + "."));
			player.teleport(target);
		}
	}

	@EventHandler
	public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event)
	{
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
		    event.getFrom().getBlockZ() == event.getTo().getBlockZ())
		{
			return; // Only execute when crossing block boundaries
		}

		Player player = event.getPlayer();
		Game game = Manager.GetGame();
		if (game == null || game.GetState() != GameState.Recruit)
		{
			return;
		}

		if (Manager.IsObserver(player) || Manager.isSpectator(player))
		{
			return;
		}

		Location to = event.getTo();
		GameLobbyManager lobby = Manager.GetLobby();
		if (lobby == null)
		{
			return;
		}

		// Check if player stepped into a team grid
		for (GameLobbyManager.TeamGrid grid : lobby.getTeamGrids())
		{
			if (grid.isInGrid(to))
			{
				// Check if player is already on this team
				GameTeam currentTeam = game.GetTeam(player);
				if (currentTeam != null && currentTeam.equals(grid.Team))
				{
					return;
				}

				// Check if they are already queuing for this team
				GameTeam currentPref = game.GetTeamPreference(player);
				if (currentPref != null && currentPref.equals(grid.Team))
				{
					return;
				}

				// Check if team is full (apply team balance check)
				if (!game.CanJoinTeam(grid.Team))
				{
					if (com.houzicore.shared.recharge.Recharge.Instance.use(player, "Team Full Bass", 1000, false, false))
					{
						player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.5f, 0.5f);
						String warning = com.houzicore.shared.core.lang.LangManager.get().isThai(player) 
							? "§cทีมนี้เต็มแล้ว!" 
							: "§cThis team is full!";
						com.houzicore.shared.common.util.UtilTextBottom.display(warning, player);
					}
					return;
				}

				// Trigger TeamClick/selection
				TeamClick(player, grid.Team);
				player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.5f, 1.5f);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onKitSelectorInteract(org.bukkit.event.player.PlayerInteractEvent event)
	{
		if (event.getAction() == org.bukkit.event.block.Action.PHYSICAL)
			return;

		if (!event.getAction().name().contains("RIGHT"))
			return;

		Player player = event.getPlayer();
		org.bukkit.inventory.ItemStack item = event.getItem();
		if (item == null || item.getType() != org.bukkit.Material.COMPASS)
			return;

		Game game = Manager.GetGame();
		if (game == null || (game.GetState() != GameState.Recruit && game.GetState() != GameState.Vote))
			return;

		// Open the KitSelectionShop GUI!
		new com.houzicore.arcade.nautilus.game.arcade.managers.voting.KitSelectionShop(
			Manager, 
			Manager.GetClients(), 
			Manager.GetDonation()
		).attemptShopOpen(player);

		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerChangedWorld(org.bukkit.event.player.PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();
		Game game = Manager.GetGame();
		if (game == null || game.GetState() == GameState.Dead || game.InProgress())
		{
			return;
		}

		if (player.getWorld().getName().equalsIgnoreCase("world"))
		{
			Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
				if (player.isOnline())
				{
					Manager.GetLobby().resendAllKitGlowStates(player);
				}
			}, 10L);
		}
	}

}
