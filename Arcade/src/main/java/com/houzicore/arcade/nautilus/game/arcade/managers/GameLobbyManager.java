package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.inventory.ItemStack;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.shared.account.CoreClient;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilBlockText;
//import com.houzicore.shared.common.util.UtilBlockText.TextAlign;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilWorld;
import com.houzicore.shared.core.cosmetic.event.ActivateEssenceBoosterEvent;
import com.houzicore.shared.core.donation.Donor;
import com.houzicore.shared.core.event.CustomTagEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;

import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitSorter;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.scoreboard.ScoreboardSidebar;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class GameLobbyManager implements Listener
{

	public ArcadeManager Manager;

	private Location _gameText;
	private Location _advText;
	private Location _kitText;
	private Location _teamText;

	private Location _kitDisplay;
	private Location _teamDisplay;  

	private Location spawn;

	private NautHashMap<Entity, LobbyEnt> _kits = new NautHashMap<Entity, LobbyEnt>();
	private NautHashMap<Block, Material> _kitBlocks = new NautHashMap<Block, Material>();

	private NautHashMap<Entity, LobbyEnt> _teams = new NautHashMap<Entity, LobbyEnt>();
	private NautHashMap<Block, Material> _teamBlocks = new NautHashMap<Block, Material>();
	private List<TeamGrid> _teamGrids = new ArrayList<TeamGrid>();
	private int _bobTickCount = 0;
	private int _proximityTickCount = 0;

	private long _fireworkStart;
	private Color _fireworkColor;

	private int _advertiseStage = 0;
 
	//Scoreboard
	private NautHashMap<Player, Scoreboard> _scoreboardMap = new NautHashMap<Player, Scoreboard>();
	private final LobbyScoreboardDataProvider _lobbyProvider = new LobbyScoreboardDataProvider();
	private final java.util.Map<java.util.UUID, List<Component>> _cachedLobbyLines = new java.util.HashMap<>();
	private NautHashMap<Player, Integer> _eloMap = new NautHashMap<Player, Integer>();
	private NautHashMap<Player, String> _kitMap = new NautHashMap<Player, String>();

	private int _oldPlayerCount = 0;
	private int _oldMaxPlayerCount = 0; // Used for scoreboard when max player count changes
	
	private String _serverName;

	
	private boolean _colorTick = false;

	public GameLobbyManager(ArcadeManager manager)
	{
		Manager = manager;
		
		World world = UtilWorld.getWorld("world");
		
		world.setTime(6000);
		world.setStorm(false);
		world.setThundering(false);
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setGameRuleValue("doMobSpawning", "false"); // Prevents natural mob spawns
		world.setDifficulty(org.bukkit.Difficulty.EASY); // Prevents vanilla MC from deleting Kit NPCs (Zombies/Skeletons)
		world.setGameRule(org.bukkit.GameRule.DO_IMMEDIATE_RESPAWN, true);

		// Deferred 1-tick purge: world.getEntities() at constructor time only covers spawn chunks.
		// Kit NPCs sit at ~(-17, 28, 0) which may be in unloaded chunks. We remove ALL non-player
		// LivingEntities after 1 tick so that Bukkit has time to register the world fully.
		// This is safe because doMobSpawning=false; the only mobs that can be here are Kit NPC orphans.
		final World lobbyWorld = world;
		Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), () -> {
			int removed = 0;
			for (Entity e : lobbyWorld.getEntities()) {
				if (e instanceof LivingEntity && !(e instanceof Player)) {
					e.remove();
					removed++;
				}
			}
			// if (removed > 0)
		}, 1L);
		
		spawn = new Location(world, 0, 29, 0);

		_gameText = new Location(world, 0, 56, 50);
		_kitText = new Location(world, -40, 46, 0);
		_teamText = new Location(world, 40, 46, 0);
		_advText = new Location(world, 0, 66, -60);

		_kitDisplay = new Location(world, -17, 27, 0);
		_teamDisplay = new Location(world, 18, 27, 0);

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
				
		_serverName = Manager.getPlugin().getConfig().getString("serverstatus.name");
		if (_serverName == null) _serverName = "Unknown";
		_serverName = _serverName.substring(0, Math.min(16,  _serverName.length()));
	}


	private boolean HasScoreboard(Player player)
	{
		com.houzicore.shared.core.scoreboard.PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(player);
		return ps != null && ps.getProvider() == _lobbyProvider;
	}

	public void CreateScoreboards()
	{
		for (Player player : UtilServer.getPlayers())
		{
			CreateScoreboard(player, false);

			com.houzicore.shared.core.scoreboard.PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(player);
			if (ps != null) {
				ps.setProvider(_lobbyProvider);
				ps.draw(ScoreboardManager.getInstance(), player);
			}
		}
		
		for (Player otherPlayer : UtilServer.getPlayers())
		{
			AddPlayerToScoreboards(otherPlayer, null);
		}
	}

	@EventHandler
	public void PlayerJoin(PlayerJoinEvent event) 
	{
		Player player = event.getPlayer();

		// If game is currently in-progress, use GameScoreboard — don't create a conflicting Lobby FastBoard
		if (Manager.GetGame() != null && Manager.GetGame().InProgress())
		{
			Manager.GetGame().GetScoreboard().applyBoard(player);
			
			final Player fPlayer = player;
			final Game fGame = Manager.GetGame();
			Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), () -> {
				if (fPlayer.isOnline() && fGame.InProgress()) {
					fGame.GetScoreboard().applyBoard(fPlayer, true);
				}
			}, 40L);
			return;
		}

		// Delay scoreboard setup to ensure player client has fully joined/loaded and ready for packets
		final Player fPlayer = player;
		Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), () -> {
			if (!fPlayer.isOnline()) return;

			// If the game started in the meantime, apply the game scoreboard instead
			if (Manager.GetGame() != null && Manager.GetGame().InProgress()) {
				Manager.GetGame().GetScoreboard().applyBoard(fPlayer);
				return;
			}

			CreateScoreboard(fPlayer, true);

			com.houzicore.shared.core.scoreboard.PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(fPlayer);
			if (ps != null) {
				ps.setProvider(_lobbyProvider);
				ps.draw(ScoreboardManager.getInstance(), fPlayer);
			}
			resendAllKitGlowStates(fPlayer);
			giveKitSelectorItem(fPlayer);
		}, 20L); // 1-second delay
	}


	private void CreateScoreboard(Player player, boolean resendToAll) 
	{
		_scoreboardMap.put(player, Bukkit.getScoreboardManager().getNewScoreboard());
		Scoreboard scoreboard = _scoreboardMap.get(player);
		player.setScoreboard(scoreboard);

		// Register glow white and glow green teams for per-player glowing outline
		if (scoreboard.getTeam("glow_white") == null)
		{
			org.bukkit.scoreboard.Team glowWhite = scoreboard.registerNewTeam("glow_white");
			glowWhite.setColor(org.bukkit.ChatColor.WHITE);
		}
		if (scoreboard.getTeam("glow_green") == null)
		{
			org.bukkit.scoreboard.Team glowGreen = scoreboard.registerNewTeam("glow_green");
			glowGreen.setColor(org.bukkit.ChatColor.GREEN);
		}

		// FastBoard handles the Sidebar, so we don't register SIDEBAR Objective here.
		
		for (Rank rank : Rank.values())
		{
			// We MUST use Bukkit's Scoreboard Teams to render Overhead NameTags! Tablist is protected by TablistFix.
			String rankPrefix = rank == Rank.ALL ? "" : rank.GetColor() + "" + org.bukkit.ChatColor.BOLD + rank.Name + org.bukkit.ChatColor.RESET + " ";
			scoreboard.registerNewTeam(rank.Name).setPrefix(rankPrefix);

			if (Manager.GetGame() != null && !Manager.GetGame().GetTeamList().isEmpty())
			{
				for (GameTeam team : Manager.GetGame().GetTeamList())
				{
					if(team.GetDisplaytag())
					{
						scoreboard.registerNewTeam(rank.Name + team.GetName().toUpperCase()).setPrefix(team.GetColor() + C.Bold + team.GetName() + team.GetColor() + " ");	
					}
					else 
					{
						if (rank == Rank.ALL)
						{
							scoreboard.registerNewTeam(rank.Name + team.GetName().toUpperCase()).setPrefix(team.GetColor() + "");
						}
						else
						{
							scoreboard.registerNewTeam(rank.Name + team.GetName().toUpperCase()).setPrefix(rankPrefix + team.GetColor());
						}
					}
				}
			}
		}

		if (resendToAll)
		{
			for (Player otherPlayer : UtilServer.getPlayers())
			{
				String teamName = null;
				if (Manager.GetGame() != null && Manager.GetGame().GetTeam(otherPlayer) != null)
					teamName = Manager.GetGame().GetTeam(otherPlayer).GetName().toUpperCase();

				AddPlayerToScoreboards(otherPlayer, teamName);
			}
		}
	}

	public Collection<Scoreboard> GetScoreboards()
	{
		return _scoreboardMap.values();
	}

	public void WriteLine(Player player, int x, int y, int z, BlockFace face, int line, String text)
	{
		Location loc = player.getLocation();
		loc.setX(x);
		loc.setY(y);
		loc.setZ(z);

		Material mat = Material.BLACK_TERRACOTTA;

		if (player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType().isBlock() && player.getInventory().getItemInMainHand().getType() != Material.AIR)
		{
			mat = player.getInventory().getItemInMainHand().getType();
		}

		if (line > 0)
			loc.add(0, line*-6, 0);

//		UtilBlockText.MakeText(text, loc, face, mat.ordinal(), (byte)0, TextAlign.CENTER);

		player.sendMessage("Writing: " + text + " @ " + UtilWorld.locToStrClean(loc));
	}


	public void WriteGameLine(String text, int line, int id, byte data)
	{
		Location loc = new Location(_gameText.getWorld(), _gameText.getX(), _gameText.getY(), _gameText.getZ());

		if (line > 0)
			loc.add(0, line*-6, 0);

		BlockFace face = BlockFace.WEST;

//		UtilBlockText.MakeText(text, loc, face, id, data, TextAlign.CENTER);
	}

	public void WriteAdvertiseLine(String text, int line, int id, byte data)
	{
		Location loc = new Location(_advText.getWorld(), _advText.getX(), _advText.getY(), _advText.getZ());

		if (line > 0)
			loc.add(0, line*-6, 0);

		BlockFace face = BlockFace.EAST;

//		UtilBlockText.MakeText(text, loc, face, id, data, TextAlign.CENTER);
	}

	public void WriteKitLine(String text, int line, int id, byte data)
	{
		Location loc = new Location(_kitText.getWorld(), _kitText.getX(), _kitText.getY(), _kitText.getZ());

		if (line > 0)
			loc.add(0, line*-6, 0);

		BlockFace face = BlockFace.NORTH;

//		UtilBlockText.MakeText(text, loc, face, id, data, TextAlign.CENTER);
	}

	public void WriteTeamLine(String text, int line, int id, byte data)
	{
		Location loc = new Location(_teamText.getWorld(), _teamText.getX(), _teamText.getY(), _teamText.getZ());

		if (line > 0)
			loc.add(0, line*-6, 0);

		BlockFace face = BlockFace.SOUTH;

//		UtilBlockText.MakeText(text, loc, face, id, data, TextAlign.CENTER);
	}

	public Location GetSpawn() 
	{	
		return spawn.clone().add(4 - Math.random()*8, 0, 4 - Math.random()*8);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void TeamGeneration(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Recruit)
			return;

		if (event.GetGame().GetMode() == null)
			WriteGameLine(event.GetGame().WorldData.MapName, 1, 159, (byte)4);
		else
			WriteGameLine(event.GetGame().WorldData.MapName, 2, 159, (byte)4);
		
		CreateKits(event.GetGame());
		CreateTeams(event.GetGame());
	}

	public void CreateTeams(Game game)
	{
		//Text
		WriteTeamLine("Select", 0, 159, (byte)15);
		WriteTeamLine("Team", 1, 159, (byte)4);

		//Remove Old Ents
		for (Entity ent : _teams.keySet())
			if (ent != null) ent.remove();
		_teams.clear();

		//Remove Blocks
		for (Block block : _teamBlocks.keySet())
			if (block != null) block.setType(_teamBlocks.get(block));
		_teamBlocks.clear();
		_teamGrids.clear();
		
		//Smash
		if (game.HideTeamSheep)
		{
			//Text
			WriteTeamLine("Select", 0, 159, (byte)15);
			WriteTeamLine("Kit", 1, 159, (byte)4);
			
			CreateScoreboards();
			return;
		}
				
		//Standard
		ArrayList<GameTeam> teams = new ArrayList<GameTeam>();
		for (GameTeam team : game.GetTeamList())
			if (team.GetVisible())
				teams.add(team);

		Location center = spawn.clone();
		center.setY(26);
		
		int count = teams.size();
		// Spread from angle 110 to 70 for front inner arc
		java.util.List<Location> locs = getArcLocations(center, 16.5, -90, count);

		for (int i=0 ; i<teams.size() ; i++)
		{
			Location entLoc = locs.get(i);
			
			// Snap entity precisely to the intersection of the blocks
			entLoc.setX(entLoc.getBlockX()); // Integer boundary
			entLoc.setZ(entLoc.getBlockZ()); // Integer boundary
			
			// Build the 4x4 floor grid
			placeTeamFloorGrid(entLoc.clone(), teams.get(i).GetColorData(), teams.get(i));

			// Spawn floating TextDisplay at centerLoc + (0.5, 2.5, 0.5)
			Location textLoc = entLoc.clone().add(0.5, 2.5, 0.5);
			textLoc.getChunk().load();

			if (Manager.GetGame() != null) Manager.GetGame().CreatureAllowOverride = true;
			org.bukkit.entity.TextDisplay textDisplay = textLoc.getWorld().spawn(textLoc, org.bukkit.entity.TextDisplay.class);
			if (Manager.GetGame() != null) Manager.GetGame().CreatureAllowOverride = false;

			textDisplay.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
			textDisplay.setBackgroundColor(org.bukkit.Color.fromARGB(100, 0, 0, 0));
			textDisplay.addScoreboardTag("ArcadeLobbyNPC");
			
			String nameText = teams.get(i).GetFormattedName() + " Team" + ChatColor.RESET + "  0 Queued";
			textDisplay.setText(nameText);

			teams.get(i).SetTeamEntity(textDisplay);

			_teams.put(textDisplay, new LobbyEnt(textDisplay, entLoc, teams.get(i)));
		}

		CreateScoreboards();
	}

	public void Cleanup()
	{
		//Remove Old Ents
		for (Entity ent : _teams.keySet())
			if (ent != null) ent.remove();
		_teams.clear();

		//Remove Blocks
		for (Block block : _teamBlocks.keySet())
			if (block != null) block.setType(_teamBlocks.get(block));
		_teamBlocks.clear();
		_teamGrids.clear();

		for (LobbyEnt lobbyEnt : _kits.values())
		{
			if (lobbyEnt != null) {
				lobbyEnt.RemoveHolograms();
				lobbyEnt.RemoveExtraEntities();
				if (lobbyEnt.GetEnt() != null) lobbyEnt.GetEnt().remove();
			}
		}
		_kits.clear();

		for (Block block : _kitBlocks.keySet())
			if (block != null) block.setType(_kitBlocks.get(block));
		_kitBlocks.clear();
	}

	public void CreateKits(Game game)
	{
		// Clean ANY lingering tagged entities from previous games
		if (spawn != null)
		{
			for (Entity e : spawn.getWorld().getEntities())
			{
				if (e.getScoreboardTags().contains("ArcadeLobbyNPC"))
				{
					e.remove();
				}
			}
		}

		//Text
		WriteKitLine("Select", 0, 159, (byte)15);
		WriteKitLine("Kit", 1, 159, (byte)4);

		for (LobbyEnt lobbyEnt : _kits.values())
		{
			lobbyEnt.RemoveHolograms();
			if (lobbyEnt.GetEnt() != null) lobbyEnt.GetEnt().remove();
		}
		_kits.clear();

		for (Block block : _kitBlocks.keySet())
			block.setType(_kitBlocks.get(block));
		_kitBlocks.clear();

		if (game.GetKits().length <= 1)
		{
			WriteKitLine("      ", 0, 159, (byte)15);
			WriteKitLine("      ", 1, 159, (byte)4);
			return;
		}

		ArrayList<Kit> kits = new ArrayList<Kit>();
		for (Kit kit : game.GetKits())
			if (kit.GetAvailability() != KitAvailability.Hide)
				kits.add(kit);

		ArrayList<List<Kit>> kitChunks = new ArrayList<List<Kit>>();
		int lastBreak = 0;
		for (int i = 0; i < kits.size(); i++)
		{
			if (i == kits.size() - 1 || kits.get(i).GetAvailability() == KitAvailability.Null)
			{
				kitChunks.add(kits.subList(lastBreak, i + 1));
				lastBreak = i + 1;
			}
		}
		for (List<Kit> kitList : kitChunks)
			Collections.sort(kitList, new KitSorter());
		kits = new ArrayList<Kit>();
		for (List<Kit> kitList : kitChunks)
			kits.addAll(kitList);

		// Determine Layout Mode
		boolean isTeamRestricted = false;
		if (game.GetTeamList().size() > 0) {
			for (GameTeam team : game.GetTeamList()) {
				for (Kit kit : kits) {
					if (kit.GetAvailability() != KitAvailability.Null) {
						if (!team.KitAllowed(kit)) {
							isTeamRestricted = true;
							break;
						}
					}
				}
				if (isTeamRestricted) break;
			}
		}

		Location center = spawn.clone();
		center.setY(26);

		if (isTeamRestricted)
		{
			// Split by team (Faction Layout)
			ArrayList<GameTeam> teams = new ArrayList<GameTeam>();
			for (GameTeam team : game.GetTeamList()) if (team.GetVisible()) teams.add(team);
			
			if (teams.size() >= 2)
			{
				GameTeam teamA = teams.get(0);
				GameTeam teamB = teams.get(1);
				
				ArrayList<Kit> kitsA = new ArrayList<Kit>();
				ArrayList<Kit> kitsB = new ArrayList<Kit>();
				for (Kit kit : kits) {
					if (teamA.KitAllowed(kit)) kitsA.add(kit);
					if (teamB.KitAllowed(kit)) kitsB.add(kit);
				}

				int mirroredSlots = Math.max(kitsA.size(), kitsB.size());

				// Left Arc (Team A)
				java.util.List<Location> locsA = getCenteredArcLocations(getArcLocations(center, 25.0, 180, mirroredSlots), kitsA.size());
				for (int i=0; i<kitsA.size(); i++) spawnKit(kitsA.get(i), locsA.get(i), center, teamA.GetName());

				// Right Arc (Team B)
				java.util.List<Location> locsB = getCenteredArcLocations(getArcLocations(center, 25.0, 0, mirroredSlots), kitsB.size());
				for (int i=0; i<kitsB.size(); i++) spawnKit(kitsB.get(i), locsB.get(i), center, teamB.GetName());
				
				return;
			}
		}

		// Smash replacement (splitting by expensive kits)
		if (game.ReplaceTeamsWithKits)
		{
			ArrayList<Kit> kitsA = new ArrayList<Kit>();
			ArrayList<Kit> kitsB = new ArrayList<Kit>();
			for (Kit kit : kits) {
				if (kit.GetCost() < 5000) kitsA.add(kit);
				else kitsB.add(kit);
			}

			int mirroredSlots = Math.max(kitsA.size(), kitsB.size());

			// Left Arc (Cheap)
			java.util.List<Location> locsA = getCenteredArcLocations(getArcLocations(center, 25.0, 180, mirroredSlots), kitsA.size());
			for (int i=0; i<kitsA.size(); i++) spawnKit(kitsA.get(i), locsA.get(i), center, null);
			
			// Right Arc (Expensive)
			java.util.List<Location> locsB = getCenteredArcLocations(getArcLocations(center, 25.0, 0, mirroredSlots), kitsB.size());
			for (int i=0; i<kitsB.size(); i++) spawnKit(kitsB.get(i), locsB.get(i), center, null);
			
			return;
		}

		// Standard Front Arc for Normal Games
		java.util.List<Location> locs = getArcLocations(center, 24.5, -90, kits.size());
		for (int i=0; i<kits.size(); i++) spawnKit(kits.get(i), locs.get(i), center, null);
	}
	
	private LobbyEnt spawnKit(Kit kit, Location entLoc, Location center, String factionName)
	{
		if (kit.GetAvailability() == KitAvailability.Null) return null;
		
		byte data = 4;
		if (kit.GetAvailability() == KitAvailability.Gem) 				data = 5;
		else if (kit.GetAvailability() == KitAvailability.Achievement) 	data = 2;

		Location pedestalLoc = entLoc.clone();
		pedestalLoc.setX(pedestalLoc.getBlockX());
		pedestalLoc.setZ(pedestalLoc.getBlockZ());
		pedestalLoc.setY(26.0); // snap Y to center height

		SetKitTeamBlocks(pedestalLoc, 95, data, _kitBlocks);

		Location spawnLoc = pedestalLoc.clone().add(0.5, 3.3, 0.5); // groundY + 1.3
		spawnLoc.setDirection(center.toVector().subtract(spawnLoc.toVector()));
		spawnLoc.getChunk().load();

		Entity ent = kit.SpawnEntity(spawnLoc);
		if (ent == null) return null;
		ent.addScoreboardTag("ArcadeLobbyNPC"); // Ensure Garbage Collection
		ent.setGravity(false);
		ent.setGlowing(false); // Managed client-side

		if (ent instanceof org.bukkit.entity.Mob) {
			((org.bukkit.entity.Mob)ent).setAware(false);
		}
		
		double baseScale = 1.0;
		if (ent instanceof org.bukkit.entity.LivingEntity) {
			org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) ent;
			if (le.getEquipment().getHelmet() == null || le.getEquipment().getHelmet().getType() == org.bukkit.Material.AIR) {
				le.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE_BUTTON));
			}
			
			// Scale up small mobs for better visibility in Arcade Lobby using 1.21 generic scale attribute
			if (le.getType() == org.bukkit.entity.EntityType.RABBIT ||
				le.getType() == org.bukkit.entity.EntityType.CHICKEN ||
				le.getType() == org.bukkit.entity.EntityType.WOLF ||
				le.getType() == org.bukkit.entity.EntityType.SILVERFISH ||
				le.getType() == org.bukkit.entity.EntityType.CAVE_SPIDER) {
				
				baseScale = le.getType() == org.bukkit.entity.EntityType.WOLF ? 2.0 : 2.35;
				org.bukkit.attribute.AttributeInstance scale = le.getAttribute(org.bukkit.attribute.Attribute.SCALE);
				if (scale != null) scale.setBaseValue(baseScale);
			}
		}

		LobbyEnt lobbyEnt = new LobbyEnt(ent, spawnLoc, kit);
		lobbyEnt.SetBaseScale(baseScale);
		
		spawnKitHolograms(lobbyEnt, kit, factionName);
		_kits.put(ent, lobbyEnt);
		return lobbyEnt;
	}

	private boolean shouldSpawnKitShowcaseStand(Entity ent)
	{
		if (!(ent instanceof org.bukkit.entity.LivingEntity))
			return false;

		switch (ent.getType())
		{
			case RABBIT:
			case CHICKEN:
			case SILVERFISH:
			case CAVE_SPIDER:
				return true;
			default:
				return false;
		}
	}

	private org.bukkit.entity.ArmorStand spawnKitShowcaseStand(Location entityLoc, Entity featuredEnt)
	{
		if (entityLoc == null || featuredEnt == null || !shouldSpawnKitShowcaseStand(featuredEnt))
			return null;

		Location standLoc = entityLoc.clone().add(0, -1.45, 0);
		standLoc.setYaw(entityLoc.getYaw());
		standLoc.setPitch(0f);

		org.bukkit.entity.ArmorStand stand = standLoc.getWorld().spawn(standLoc, org.bukkit.entity.ArmorStand.class);
		stand.setGravity(false);
		stand.setVisible(true);
		stand.setSmall(false);
		stand.setInvulnerable(true);
		stand.setArms(true);
		stand.setBasePlate(false);
		stand.setRemoveWhenFarAway(false);
		stand.setMarker(false);
		stand.addScoreboardTag("ArcadeLobbyNPC");

		org.bukkit.inventory.ItemStack head = new org.bukkit.inventory.ItemStack(org.bukkit.Material.PLAYER_HEAD);
		if (head.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta)
		{
			org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
			meta.setOwningPlayer(org.bukkit.Bukkit.getOfflinePlayer("Steve"));
			head.setItemMeta(meta);
		}
		stand.getEquipment().setHelmet(head);

		org.bukkit.inventory.ItemStack chest = new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_CHESTPLATE);
		if (chest.getItemMeta() instanceof org.bukkit.inventory.meta.LeatherArmorMeta)
		{
			org.bukkit.inventory.meta.LeatherArmorMeta meta = (org.bukkit.inventory.meta.LeatherArmorMeta) chest.getItemMeta();
			meta.setColor(org.bukkit.Color.fromRGB(59, 115, 181));
			chest.setItemMeta(meta);
		}
		stand.getEquipment().setChestplate(chest);

		org.bukkit.inventory.ItemStack legs = new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_LEGGINGS);
		if (legs.getItemMeta() instanceof org.bukkit.inventory.meta.LeatherArmorMeta)
		{
			org.bukkit.inventory.meta.LeatherArmorMeta meta = (org.bukkit.inventory.meta.LeatherArmorMeta) legs.getItemMeta();
			meta.setColor(org.bukkit.Color.fromRGB(70, 58, 146));
			legs.setItemMeta(meta);
		}
		stand.getEquipment().setLeggings(legs);

		org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_BOOTS);
		if (boots.getItemMeta() instanceof org.bukkit.inventory.meta.LeatherArmorMeta)
		{
			org.bukkit.inventory.meta.LeatherArmorMeta meta = (org.bukkit.inventory.meta.LeatherArmorMeta) boots.getItemMeta();
			meta.setColor(org.bukkit.Color.fromRGB(66, 52, 40));
			boots.setItemMeta(meta);
		}
		stand.getEquipment().setBoots(boots);

		return stand;
	}

	private String toSmallCaps(String text) {
		text = text.toUpperCase();
		String normal = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String small  = "ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";
		StringBuilder sb = new StringBuilder();
		for (char c : text.toCharArray()) {
			int idx = normal.indexOf(c);
			if (idx != -1) sb.append(small.charAt(idx));
			else sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Spawns floating ArmorStand holograms above Kit NPC entities.
	 * Line 1 (Faction): Optional Team Faction Title
	 * Line 2 (name): Kit Name with rarity gradient
	 * Line 3 (sub): Availability tag (FREE / cost / ACHIEVEMENT)
	 * Line 4 (ins): Right-click to select
	 */
	private void spawnKitHolograms(LobbyEnt lobbyEnt, Kit kit, String factionName)
	{
		Location baseLoc = lobbyEnt.GetLocation().clone();
		double height = 1.95; // default fallback (zombie height)
		if (lobbyEnt.GetEnt() != null) {
			height = lobbyEnt.GetEnt().getHeight();
		}

		double currentY = height + 0.85;
		if (factionName != null) {
			currentY = height + 1.15;
			String parsedFact = com.houzicore.shared.common.util.HouziColorParser.parse("&l<GRADIENT:#ffaa00,#ff5500>" + toSmallCaps(factionName) + "</GRADIENT>");
			lobbyEnt.AddHologram(spawnHologramLine(baseLoc.clone().add(0, currentY, 0), parsedFact));
			currentY -= 0.3;
		}
		
		String instructions = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.get().getString(null, "prop_rush.lobby.holo.instructions_short", "§8\u00BB §eClick to Choose §3| §dUpgrade Traits §8\u00AB");
		
		String kitTitleColor;
		String badge;
		
		if (kit.GetAvailability() == KitAvailability.Free)
		{
			kitTitleColor = "<GRADIENT:#E0EAFC,#CFDEF3>";
			badge = "§f\u2713 Free";
		}
		else if (kit.GetAvailability() == KitAvailability.Achievement)
		{
			kitTitleColor = "<GRADIENT:#DA4453,#89216B>";
			badge = "§d\u2605 " + com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.get().getString(null, "prop_rush.lobby.achievement_kit", "Achievement Kit");
		}
		else
		{
			kitTitleColor = "<GRADIENT:#11998e,#38ef7d>";
			badge = "§a\u25C6 " + kit.GetCost() + " " + com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.get().getString(null, "prop_rush.lobby.essence", "Essence");
		}
		
		String parsedTitle;
		if (kit.getDisplayColor() != null) {
			parsedTitle = kit.getDisplayColor() + "§l" + kit.GetName();
		} else {
			parsedTitle = com.houzicore.shared.common.util.HouziColorParser.parse("&l" + kitTitleColor + kit.GetName() + "</GRADIENT>");
		}
		
		lobbyEnt.AddHologram(spawnHologramLine(baseLoc.clone().add(0, currentY, 0), parsedTitle));
		lobbyEnt.AddHologram(spawnHologramLine(baseLoc.clone().add(0, currentY - 0.3, 0), badge));
		lobbyEnt.AddHologram(spawnHologramLine(baseLoc.clone().add(0, currentY - 0.6, 0), instructions));
	}

	private ArmorStand spawnHologramLine(Location loc, String text)
	{
		Manager.GetGame().CreatureAllowOverride = true;
		ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
		Manager.GetGame().CreatureAllowOverride = false;

		stand.setVisible(false);
		stand.setGravity(false);
		stand.setSmall(true);
		stand.setMarker(true);
		stand.setInvulnerable(true);
		stand.setCustomName(text);
		stand.setCustomNameVisible(true);
		stand.setRemoveWhenFarAway(false);
		stand.addScoreboardTag("ArcadeLobbyNPC");
		return stand;
	}

	private void setTrackedBlock(Block block, Material material, NautHashMap<Block, Material> blockMap)
	{
		if (!blockMap.containsKey(block))
		{
			blockMap.put(block, block.getType());
		}

		block.setType(material);
	}

	private void setTrackedStair(Block block, Material material, BlockFace facing, NautHashMap<Block, Material> blockMap)
	{
		if (!blockMap.containsKey(block))
		{
			blockMap.put(block, block.getType());
		}

		BlockData data = material.createBlockData();
		if (data instanceof Stairs)
		{
			Stairs stairs = (Stairs) data;
			stairs.setFacing(facing);
			stairs.setHalf(org.bukkit.block.data.Bisected.Half.BOTTOM);
			block.setBlockData(stairs, false);
		}
		else
		{
			block.setType(material);
		}
	}

	private void setTrackedSlab(Block block, Material material, Slab.Type slabType, NautHashMap<Block, Material> blockMap)
	{
		if (!blockMap.containsKey(block))
		{
			blockMap.put(block, block.getType());
		}

		BlockData data = material.createBlockData();
		if (data instanceof Slab)
		{
			Slab slab = (Slab) data;
			slab.setType(slabType);
			block.setBlockData(slab, false);
		}
		else
		{
			block.setType(material);
		}
	}

	public void SetKitTeamBlocks(Location loc, int id, byte data, NautHashMap<Block, Material> blockMap) 
	{
		Material mat = Material.GLASS;
		if (id == 95) {
			switch (data) {
				case 0: mat = Material.WHITE_STAINED_GLASS; break;
				case 1: mat = Material.ORANGE_STAINED_GLASS; break;
				case 2: mat = Material.MAGENTA_STAINED_GLASS; break;
				case 3: mat = Material.LIGHT_BLUE_STAINED_GLASS; break;
				case 4: mat = Material.YELLOW_STAINED_GLASS; break;
				case 5: mat = Material.LIME_STAINED_GLASS; break;
				case 6: mat = Material.PINK_STAINED_GLASS; break;
				case 7: mat = Material.GRAY_STAINED_GLASS; break;
				case 8: mat = Material.LIGHT_GRAY_STAINED_GLASS; break;
				case 9: mat = Material.CYAN_STAINED_GLASS; break;
				case 10: mat = Material.PURPLE_STAINED_GLASS; break;
				case 11: mat = Material.BLUE_STAINED_GLASS; break;
				case 12: mat = Material.BROWN_STAINED_GLASS; break;
				case 13: mat = Material.GREEN_STAINED_GLASS; break;
				case 14: mat = Material.RED_STAINED_GLASS; break;
				case 15: mat = Material.BLACK_STAINED_GLASS; break;
				default: mat = Material.WHITE_STAINED_GLASS; break;
			}
		} else if (id == 35) {
			mat = Material.BLACK_WOOL;
		}

		Material stairMat = Material.POLISHED_BLACKSTONE_BRICK_STAIRS;
		Material slabMat = Material.POLISHED_BLACKSTONE_SLAB;

		// Y = -1 Base level (Obsidian / Crying Obsidian)
		for (int x=-2 ; x<=2 ; x++) {
			for (int z=-2 ; z<=2 ; z++) {
				Material b = (Math.abs(x) <= 1 && Math.abs(z) <= 1) ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN;
				setTrackedBlock(loc.getBlock().getRelative(x, -1, z), b, blockMap);
			}
		}

		// Y = 0 Mid level
		for (int x=-1 ; x<=1 ; x++) {
			for (int z=-1 ; z<=1 ; z++) {
				Material b;
				if (x == 0 && z == 0) {
					b = Material.SEA_LANTERN; // Glow core
				} else if (Math.abs(x) + Math.abs(z) == 1) {
					b = Material.LODESTONE; // Gravity stabilizers
				} else {
					b = Material.CRYING_OBSIDIAN;
				}
				setTrackedBlock(loc.getBlock().getRelative(x, 0, z), b, blockMap);
			}
		}

		// Border stairs facing inwards
		setTrackedStair(loc.getBlock().getRelative(0, 0, -2), stairMat, BlockFace.SOUTH, blockMap);
		setTrackedStair(loc.getBlock().getRelative(1, 0, -2), stairMat, BlockFace.SOUTH, blockMap);
		setTrackedStair(loc.getBlock().getRelative(-1, 0, -2), stairMat, BlockFace.SOUTH, blockMap);
		setTrackedStair(loc.getBlock().getRelative(0, 0, 2), stairMat, BlockFace.NORTH, blockMap);
		setTrackedStair(loc.getBlock().getRelative(1, 0, 2), stairMat, BlockFace.NORTH, blockMap);
		setTrackedStair(loc.getBlock().getRelative(-1, 0, 2), stairMat, BlockFace.NORTH, blockMap);
		setTrackedStair(loc.getBlock().getRelative(-2, 0, 0), stairMat, BlockFace.EAST, blockMap);
		setTrackedStair(loc.getBlock().getRelative(-2, 0, 1), stairMat, BlockFace.EAST, blockMap);
		setTrackedStair(loc.getBlock().getRelative(-2, 0, -1), stairMat, BlockFace.EAST, blockMap);
		setTrackedStair(loc.getBlock().getRelative(2, 0, 0), stairMat, BlockFace.WEST, blockMap);
		setTrackedStair(loc.getBlock().getRelative(2, 0, 1), stairMat, BlockFace.WEST, blockMap);
		setTrackedStair(loc.getBlock().getRelative(2, 0, -1), stairMat, BlockFace.WEST, blockMap);

		// Corner slabs
		int[][] cornerOffsets = new int[][] {
			{-2, -2},
			{-2, 2},
			{2, -2},
			{2, 2}
		};
		for (int[] offset : cornerOffsets) {
			setTrackedSlab(loc.getBlock().getRelative(offset[0], 0, offset[1]), slabMat, Slab.Type.BOTTOM, blockMap);
		}

		// Y = 1 Top Level (Containment Ring of Glass / Slabs)
		for (int x=-1 ; x<=1 ; x++) {
			for (int z=-1 ; z<=1 ; z++) {
				if (x == 0 && z == 0) {
					// Hollow center where gravity energy floats up!
					setTrackedBlock(loc.getBlock().getRelative(x, 1, z), Material.AIR, blockMap);
				} else {
					// Colored glass ring
					setTrackedBlock(loc.getBlock().getRelative(x, 1, z), mat, blockMap);
				}
			}
		}

		// Outer border slabs at Y = 1
		for (int x=-2 ; x<=2 ; x++) {
			for (int z=-2 ; z<=2 ; z++) {
				if (Math.abs(x) != 2 && Math.abs(z) != 2) continue;
				if (Math.abs(x) == 2 && Math.abs(z) == 2) continue;
				setTrackedSlab(loc.getBlock().getRelative(x, 1, z), slabMat, Slab.Type.BOTTOM, blockMap);
			}
		}
	}
	
	private java.util.List<Location> getArcLocations(Location center, double radius, double baseAngleDeg, int count) 
	{
		java.util.List<Location> locs = new ArrayList<Location>();
		if (count <= 0) return locs;
		
		double spacingDeg = Math.max(11.5, Math.min(18.0, 168.0 / Math.max(1, count - 1)));
		double totalSpread = spacingDeg * (count - 1);
		double startAngleDeg = baseAngleDeg + (totalSpread / 2.0);
		
		double start = Math.toRadians(startAngleDeg);
		double step = Math.toRadians(-spacingDeg);
		
		for (int i = 0; i < count; i++) {
			double angle = start + (i * step);
			locs.add(center.clone().add(radius * Math.cos(angle), 0, radius * Math.sin(angle)));
		}
		
		return locs;
	}

	private java.util.List<Location> getCenteredArcLocations(java.util.List<Location> slots, int count)
	{
		java.util.List<Location> locs = new ArrayList<Location>();
		if (count <= 0 || slots.isEmpty())
			return locs;

		if (count >= slots.size())
		{
			locs.addAll(slots);
			return locs;
		}

		int startIndex = Math.max(0, (slots.size() - count) / 2);
		for (int i = 0; i < count; i++)
		{
			locs.add(slots.get(startIndex + i).clone());
		}

		return locs;
	}
	
	public void AddKitLocation(Entity ent, Kit kit, Location loc)
	{
		_kits.put(ent, new LobbyEnt(ent, loc, kit));
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		_scoreboardMap.remove(event.getPlayer());
		_kitMap.remove(event.getPlayer());
		_cachedLobbyLines.remove(event.getPlayer().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void DamageCancel(EntityDamageByEntityEvent event)
	{
		if (_kits.containsKey(event.getEntity()))
			event.setCancelled(true); // was setCancelled("Kit Cancel")
	}

	@EventHandler
	public void Update(UpdateEvent event) 
	{
		if (event.getType() == UpdateType.FAST)
		{
			spawn.getWorld().setTime(6000);
			spawn.getWorld().setStorm(false);
			spawn.getWorld().setThundering(false);
			spawn.getWorld().setGameRuleValue("doDaylightCycle", "false");
			spawn.getWorld().setGameRuleValue("doMobSpawning", "false");
		}


		if (event.getType() == UpdateType.TICK)
			UpdateEnts();

		if (event.getType() == UpdateType.FASTEST)
			UpdateFirework();

		if (event.getType() == UpdateType.SLOW)
			UpdateAdvertise();

		if (event.getType() == UpdateType.SEC) 
		{
			if (Manager.GetGame() != null && UtilServer.getPlayers().length == 0)
			{
				GameState state = Manager.GetGame().GetState();
				if (state == GameState.Vote || state == GameState.Loading || state == GameState.Prepare)
				{
					Manager.GetGame().SetState(GameState.Dead);
				}
			}
		}

		ScoreboardDisplay(event);
		ScoreboardSet(event);
	}
	
	@EventHandler
	public void onWeather(org.bukkit.event.weather.WeatherChangeEvent event) {
		
		if (!event.getWorld().equals(spawn.getWorld()))
			return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void StateCleanLobbyEntities(GameStateChangeEvent event) 
	{
		// ── Dead state: wipe tracked entities from the last game ──────────────────
		// NOTE: We do NOT clean on Vote or Loading any more. Doing so caused a 25-second
		// window with zero NPCs in the lobby (Vote=15s + Loading=10s) every time a
		// new game cycle started. Instead, let CreateKits() and CreateTeams() handle
		// their own internal cleanup right before they spawn the fresh set on Recruit.
		// ─────────────────────────────────────────────────────────────────────────────
		if (event.GetState() == GameState.Dead)
		{
			// Formal tracking cleanup (clears _kits / _teams maps and removes entities)
			Cleanup();
		}

		// ── Recruit state: failsafe purge of any orphans still in the world ───────
		// This fires right before TeamGeneration (also on Recruit), guaranteeing a
		// clean slate before CreateKits / CreateTeams spawn the fresh NPC set.
		if (event.GetState() == GameState.Recruit)
		{
			if (spawn != null && spawn.getWorld() != null)
			{
				for (Entity ent : spawn.getWorld().getEntities())
				{
					if (ent instanceof org.bukkit.entity.LivingEntity || ent instanceof org.bukkit.entity.ArmorStand)
					{
						if (ent instanceof Player) continue;
						
						if (ent.getScoreboardTags().contains("ArcadeLobbyNPC") || 
						    ent instanceof org.bukkit.entity.Sheep || 
						    ent instanceof org.bukkit.entity.Zombie || 
						    ent instanceof org.bukkit.entity.Skeleton ||
						    ent instanceof org.bukkit.entity.Slime ||
						    ent instanceof org.bukkit.entity.Villager)
						{
							ent.remove();
						}
					}
				}
			}
		}
	}

	private void UpdateAdvertise() 
	{
		if (Manager.GetGame() == null || Manager.GetGame().GetState() != GameState.Recruit)
			return;
		
		_advertiseStage = (_advertiseStage+1)%2;
		
		if (Manager.GetGame().AdvertiseText(this, _advertiseStage))
		{
			return;
		}

		if (_advertiseStage == 0)
		{
		WriteAdvertiseLine("GET " + com.houzicore.shared.core.common.BrandConfig.mainServerName().toUpperCase(), 0, 159, (byte)4);
			WriteAdvertiseLine("ARCADE", 1, 159, (byte)15);
			WriteAdvertiseLine("MINI-GAMES", 2, 159, (byte)15);

			WriteAdvertiseLine(com.houzicore.shared.core.common.BrandConfig.networkName().toUpperCase(), 4, 159, (byte)15);
		}
		else if (_advertiseStage == 1)
		{
			WriteAdvertiseLine("KEEP CALM", 0, 159, (byte)4);
			WriteAdvertiseLine("AND PLAY", 1, 159, (byte)15);
			WriteAdvertiseLine(com.houzicore.shared.core.common.BrandConfig.mainServerName().toUpperCase(), 2, 159, (byte)4);

			WriteAdvertiseLine(com.houzicore.shared.core.common.BrandConfig.networkName().toUpperCase(), 4, 159, (byte)15);
		}
	}

	public void UpdateEnts()
	{
		long tick = System.currentTimeMillis();
		_bobTickCount++;
		_proximityTickCount++;

		// 1. Bobbing Animation (Every 3 ticks)
		if (_bobTickCount % 3 == 0)
		{
			int kitIndex = 0;
			for (Entity ent : _kits.keySet())
			{
				LobbyEnt lobbyEnt = _kits.get(ent);
				Location baseLoc = lobbyEnt.GetLocation();
				
				// Sine wave offset desynchronized by index
				double offset = Math.sin((_bobTickCount * 0.1) + (kitIndex * 0.8)) * 0.2;
				double targetY = baseLoc.getY() + offset;

				com.github.retrooper.packetevents.util.Vector3d position = new com.github.retrooper.packetevents.util.Vector3d(
					baseLoc.getX(),
					targetY,
					baseLoc.getZ()
				);

				com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport teleportPacket = 
					new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport(
						ent.getEntityId(),
						position,
						baseLoc.getYaw(),
						baseLoc.getPitch(),
						false
					);

				for (Player p : UtilServer.getPlayers())
				{
					com.github.retrooper.packetevents.PacketEvents.getAPI().getPlayerManager().sendPacket(p, teleportPacket);
				}
				kitIndex++;
			}
		}

		// 2. Proximity Scaling (Every 10 ticks)
		if (_proximityTickCount % 10 == 0)
		{
			for (Entity ent : _kits.keySet())
			{
				LobbyEnt lobbyEnt = _kits.get(ent);
				boolean hasNearPlayer = false;
				for (Entity nearby : ent.getNearbyEntities(3.5, 3.5, 3.5))
				{
					if (nearby instanceof Player && !UtilPlayer.isSpectator(nearby) && !Manager.IsObserver((Player) nearby))
					{
						hasNearPlayer = true;
						break;
					}
				}

				if (hasNearPlayer != lobbyEnt.IsPlayerNear())
				{
					lobbyEnt.SetPlayerNear(hasNearPlayer);
					if (ent instanceof org.bukkit.entity.LivingEntity)
					{
						org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) ent;
						org.bukkit.attribute.AttributeInstance scaleAttr = le.getAttribute(org.bukkit.attribute.Attribute.SCALE);
						if (scaleAttr != null)
						{
							double targetScale = hasNearPlayer ? lobbyEnt.GetBaseScale() * 1.1 : lobbyEnt.GetBaseScale();
							scaleAttr.setBaseValue(targetScale);
						}
					}
				}
			}
		}

		// 3. Float particles up from team grids
		for (TeamGrid grid : _teamGrids)
		{
			// Spawn a few particles inside the 4x4 zone
			double px = grid.MinX + Math.random() * 4.0;
			double pz = grid.MinZ + Math.random() * 4.0;
			double py = grid.Y + 1.0 + Math.random() * 1.5; // ground Y is Y+1, so Y+1 to Y+2.5

			org.bukkit.Color particleColor = grid.Team.GetColorBase();
			org.bukkit.Particle.DustOptions dustOptions = new org.bukkit.Particle.DustOptions(particleColor, 1.0f);
			grid.Center.getWorld().spawnParticle(org.bukkit.Particle.DUST, px, py, pz, 1, 0, 0, 0, 0, dustOptions);
		}

		// 4. Orbiting Aura particles (for achievement / gem kits)
		for (Entity ent : _kits.keySet())
		{
			LobbyEnt lobbyEnt = _kits.get(ent);
			Location loc = lobbyEnt.GetLocation(); // use base location for static orbit center
			if (lobbyEnt.GetKit() != null)
			{
				double angle = (tick / 50.0) % (2 * Math.PI);
				double radius = 0.8;
				double px = loc.getX() + Math.cos(angle) * radius;
				double pz = loc.getZ() + Math.sin(angle) * radius;
				double py = loc.getY() + 0.5 + Math.sin(angle * 2) * 0.3;

				org.bukkit.Particle particleType;
				if (lobbyEnt.GetKit().GetAvailability() == com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability.Free)
					particleType = org.bukkit.Particle.HAPPY_VILLAGER;
				else if (lobbyEnt.GetKit().GetAvailability() == com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability.Achievement)
					particleType = org.bukkit.Particle.FLAME;
				else
					particleType = org.bukkit.Particle.WITCH;

				loc.getWorld().spawnParticle(particleType, px, py, pz, 1, 0, 0, 0, 0);
			}
		}

		// 5. Rising gravity energy particles from hollow core of pedestals
		for (Entity ent : _kits.keySet())
		{
			LobbyEnt lobbyEnt = _kits.get(ent);
			Kit kit = lobbyEnt.GetKit();
			if (kit == null) continue;

			Location loc = lobbyEnt.GetLocation(); // center at (x+0.5, y, z+0.5)
			double px = loc.getX();
			double pz = loc.getZ();

			// Particle color based on kit availability
			org.bukkit.Color particleColor = org.bukkit.Color.WHITE;
			if (kit.GetAvailability() == KitAvailability.Free) {
				particleColor = org.bukkit.Color.fromRGB(255, 215, 0); // Gold
			} else if (kit.GetAvailability() == KitAvailability.Gem) {
				particleColor = org.bukkit.Color.fromRGB(50, 205, 50); // Lime Green
			} else if (kit.GetAvailability() == KitAvailability.Achievement) {
				particleColor = org.bukkit.Color.fromRGB(255, 0, 255); // Magenta
			}

			double py = 26.2 + Math.random() * 2.8;
			
			// Portal particles for gravity distortion
			loc.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, px, py, pz, 1, 0.05, 0.1, 0.05, 0.0);
			
			// Colored dust particles rising
			org.bukkit.Particle.DustOptions dust = new org.bukkit.Particle.DustOptions(particleColor, 0.8f);
			loc.getWorld().spawnParticle(org.bukkit.Particle.DUST, px, py, pz, 1, 0.02, 0.05, 0.02, 0.0, dust);
		}

		// 6. Periodic Glow Resend (Every 20 ticks / 1 second) to prevent client desync when world/chunks load
		if (_proximityTickCount % 20 == 0)
		{
			for (Player player : UtilServer.getPlayers())
			{
				resendAllKitGlowStates(player);
			}
		}
	}

	public Kit GetClickedKit(Entity clicked)
	{
		if (clicked == null) return null;
		for (LobbyEnt ent : _kits.values())
			if ((ent.GetEnt() != null && clicked.getUniqueId().equals(ent.GetEnt().getUniqueId()))
					|| ent.GetExtraEntities().stream().anyMatch(extra -> extra != null && clicked.getUniqueId().equals(extra.getUniqueId())))
				return ent.GetKit();

		return null;
	}

	public GameTeam GetClickedTeam(Entity clicked)
	{
		if (clicked == null) return null;
		for (LobbyEnt ent : _teams.values())
			if (ent.GetEnt() != null && clicked.getUniqueId().equals(ent.GetEnt().getUniqueId()))
				return ent.GetTeam();

		return null;
	}

	public void RegisterFireworks(GameTeam winnerTeam)
	{
		if (winnerTeam != null)
		{
			_fireworkColor = Color.GREEN;
			if (winnerTeam.GetColor() == ChatColor.RED)			_fireworkColor = Color.RED;
			if (winnerTeam.GetColor() == ChatColor.AQUA)		_fireworkColor = Color.BLUE;
			if (winnerTeam.GetColor() == ChatColor.YELLOW)		_fireworkColor = Color.YELLOW;

			_fireworkStart = System.currentTimeMillis();
		}
	}

	public void UpdateFirework()
	{
		if (UtilTime.elapsed(_fireworkStart, 10000))
			return;

		UtilFirework.playFirework(spawn.clone().add(Math.random()*160-80, 30 + Math.random()*10, Math.random()*160-80), 
				Type.BALL_LARGE, _fireworkColor, false, false);
	}

	@EventHandler
	public void Combust(EntityCombustEvent event) 
	{
		for (LobbyEnt ent : _kits.values())
			if (event.getEntity().equals(ent.GetEnt()))
			{
				event.setCancelled(true);
				return;
			}
			
		for (LobbyEnt ent : _teams.values())
			if (event.getEntity().equals(ent.GetEnt()))
			{
				event.setCancelled(true);
				return;
			}
	}

	public void DisplayLast(Game game) 
	{
		//Start Fireworks
		RegisterFireworks(game.WinnerTeam);
	}

	public void DisplayNext(Game game, HashMap<String, ChatColor> pastTeams) 
	{
		WriteGameLine(game.GetType().GetLobbyName(), 0, 159, (byte)14);
		
		if (game.GetMode() == null)
			WriteGameLine("      ", 1, 159, (byte)1);
		else
			WriteGameLine(game.GetMode(), 1, 159, (byte)1);	
		
		DisplayWaiting();
		CreateKits(game);
		CreateTeams(game);
	}

	public void DisplayWaiting()
	{
		WriteGameLine("waiting for players", 3, 159, (byte)13);
	}

	@EventHandler
	public void ScoreboardDisplay(UpdateEvent event)
	{
		// NO-OP: FastBoard manages sidebars and GameScoreboard automatically applies Bukkit scoreboards 
	}

	/**
	 * Destroy all Lobby FastBoards so they don't conflict with the In-Game GameScoreboard.
	 * Called when the game transitions OUT of Recruit (i.e. Prepare/Live).
	 */
	public void destroyLobbyBoards()
	{
		// NO-OP: GameScoreboard will change provider to override lobby boards
	}

	/**
	 * Recreate Lobby FastBoards for all online players.
	 * Called when the game returns to Recruit (Waiting Lobby) state.
	 */
	public void recreateLobbyBoards()
	{
		for (Player player : com.houzicore.shared.common.util.UtilServer.getPlayers())
		{
			// Restore player to private scoreboard instead of main scoreboard
			CreateScoreboard(player, true);

			com.houzicore.shared.core.scoreboard.PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(player);
			if (ps != null) {
				ps.setProvider(_lobbyProvider);
				ps.draw(ScoreboardManager.getInstance(), player);
			}
			resendAllKitGlowStates(player);
		}
	}

	@EventHandler
	public void onGameStateChangeScoreboard(GameStateChangeEvent event)
	{
		GameState state = event.GetState();
		// Transitioning into active game — destroy lobby boards so In-Game GameScoreboard takes over cleanly
		if (state == GameState.Prepare || state == GameState.Live)
		{
			destroyLobbyBoards();
			for (Player player : com.houzicore.shared.common.util.UtilServer.getPlayers())
			{
				event.GetGame().GetScoreboard().applyBoard(player);
				
				final Player fPlayer = player;
				final Game fGame = event.GetGame();
				Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), () -> {
					if (fPlayer.isOnline() && fGame.InProgress()) {
						fGame.GetScoreboard().applyBoard(fPlayer, true);
					}
				}, 40L);
			}
		}
		// Returning to lobby — recreate lobby boards for all players
		else if (state == GameState.Recruit || state == GameState.Vote)
		{
			recreateLobbyBoards();
		}
	}
	private int _animTick = 0;


	public void ScoreboardSet(UpdateEvent event) 
	{
		if (event.getType() == UpdateType.TICK) 
		{
			_animTick++;

			if (Manager.GetGame() == null || !Manager.GetGame().InProgress()) 
			{
				for (Player player : com.houzicore.shared.common.util.UtilServer.getPlayers()) 
				{
					com.houzicore.shared.core.scoreboard.PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(player);
					if (ps == null) continue;

					if (ps.getProvider() != _lobbyProvider) {
						ps.setProvider(_lobbyProvider);
					}

					ps.draw(ScoreboardManager.getInstance(), player);
				}
			}
			else
			{
				// In-Game: only use GameScoreboard (Lobby boards are already destroyed via onGameStateChangeScoreboard)
				for (Player player : com.houzicore.shared.common.util.UtilServer.getPlayers())
				{
					Manager.GetGame().GetScoreboard().applyBoard(player);
				}
			}
		}

		if (event.getType() == UpdateType.FAST) 
		{
			if (Manager.GetGame() == null || !Manager.GetGame().InProgress()) 
			{
				Game game = Manager.GetGame();
				for (Player player : com.houzicore.shared.common.util.UtilServer.getPlayers()) 
				{
					_cachedLobbyLines.put(player.getUniqueId(), generateLobbyLines(player));
					
					if (game != null)
					{
						if (game.GetState() == Game.GameState.Loading)
						{
							if (System.currentTimeMillis() - game.GetStateTime() > 3000)
							{
								com.houzicore.shared.common.util.UtilTextMiddle.display(
									"",
									C.cGold + com.houzicore.shared.core.lang.LangManager.get().get(player, "arcade.lobby.loading", "Preparing world..."),
									0, 40, 0,
									player
								);
							}
						}
						else if (game.GetState() == Game.GameState.Recruit)
						{
							if (game.GetCountdown() > 0)
							{
								com.houzicore.shared.common.util.UtilTextMiddle.display(
									"",
									C.cGreen + com.houzicore.shared.core.lang.LangManager.get().get(player, "arcade.lobby.starting_in", "Starting in ") + game.GetCountdown() + "s",
									0, 40, 0,
									player
								);
							}
						}
					}
				}
			}
		}
	}

	private java.util.List<String> deduplicateLines(java.util.List<String> lines) {
		java.util.List<String> result = new java.util.ArrayList<>();
		java.util.HashSet<String> seen = new java.util.HashSet<>();
		int emptyCount = 0;
		int totalProcessed = 0;
		
		for (String line : lines) {
			if (totalProcessed >= 15) break;

			if (line == null || line.trim().isEmpty() || line.equals(" ") || line.equals("  ") || line.equals("   ")) {
				String uniqueEmpty = "§" + Integer.toHexString(emptyCount % 16) + " ";
				result.add(uniqueEmpty);
				seen.add(uniqueEmpty);
				emptyCount++;
			} else if (seen.contains(line)) {
				String uniqueLine = line + "§" + Integer.toHexString(emptyCount % 16) + " ";
				result.add(uniqueLine);
				seen.add(uniqueLine);
				emptyCount++;
			} else {
				seen.add(line);
				result.add(line);
			}
			totalProcessed++;
		}
		return result;
	}

	private void appendWaitingBoardLines(java.util.ArrayList<String> lines, Player player, Game game, String stateStatus)
	{
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang lang = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.get();
		int onlinePlayers = com.houzicore.shared.common.util.UtilServer.getPlayers().length;
		int maxPlayers = Math.max(1, Manager.GetPlayerFull());
		int playersNeeded = Math.max(0, Math.max(0, Manager.GetPlayerMin()) - onlinePlayers);
		String mapName = lang.getString(player, "prop_rush.lobby.loading_map", "Loading...");
		String gameName = game != null ? game.GetName() : "Arcade";
		String modeName = (game != null && game.GetMode() != null && !game.GetMode().trim().isEmpty()) ? game.GetMode() : gameName;

		if (game != null && game.WorldData != null && game.WorldData.MapName != null && !game.WorldData.MapName.equalsIgnoreCase("Null"))
			mapName = game.WorldData.MapName;

		String dateStr = new java.text.SimpleDateFormat("dd/MM/yy").format(new java.util.Date());
		String smallStatus = com.houzicore.shared.common.util.UtilText.toSmallCaps(lang.getString(player, "prop_rush.lobby.status", "Status"));
		String smallLoadout = com.houzicore.shared.common.util.UtilText.toSmallCaps(lang.getString(player, "prop_rush.lobby.loadout", "Loadout"));

		lines.add(" ");
		lines.add(boardLine(" &e⌚ &f" + smallStatus));
		lines.add(boardLine("  &7" + getStatusLine(player, game, stateStatus, playersNeeded, isThai)));
		lines.add("  ");
		lines.add(boardLine("  &7🌏 " + lang.getString(player, "prop_rush.lobby.map", "Map") + ": &a" + truncBoard(mapName, 16)));
		lines.add(boardLine("  &7👥 " + lang.getString(player, "prop_rush.lobby.players", "Players") + ": &a" + onlinePlayers + "&7/&a" + maxPlayers));
		lines.add(boardLine("  &7🎮 " + lang.getString(player, "prop_rush.lobby.mode", "Mode") + ": &a" + truncBoard(modeName, 16)));
		lines.add("   ");
		lines.add(boardLine(" &d📦 &f" + smallLoadout));
		appendPlayerInfoLines(lines, player, game, isThai);
		lines.add("    ");
		lines.add(boardLine(" &7" + dateStr + " &8• &7" + _serverName));
	}

	private void appendPlayerInfoLines(java.util.ArrayList<String> lines, Player player, Game game, boolean isThai)
	{
		if (game == null)
			return;

		com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang lang = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.get();

		// Kit
		boolean hasKit = game.GetKit(player) != null;
		String kitName = hasKit ? game.GetKit(player).GetName() : lang.getString(player, "prop_rush.lobby.none_selected", "None");
		lines.add(boardLine("  &7" + lang.getString(player, "prop_rush.lobby.kit", "Kit") + ": " + (hasKit ? "&a" : "&c") + truncBoard(kitName, 16)));

		// Team / Role
		if (game.GetTeamList() != null && game.GetTeamList().size() > 1 && !game.HideTeamSheep)
		{
			GameTeam team = game.GetTeam(player);
			String teamDisplay = team != null ? team.GetColor() + truncBoard(team.GetName(), 14) : "&c-";
			lines.add(boardLine("  &7" + lang.getString(player, "prop_rush.lobby.team", "Team") + ": " + teamDisplay));
		}
		else
		{
			String roleName = (game.GetMode() != null && !game.GetMode().trim().isEmpty()) ? game.GetMode() : lang.getString(player, "prop_rush.lobby.solo", "Solo");
			lines.add(boardLine("  &7" + lang.getString(player, "prop_rush.lobby.role", "Role") + ": &a" + truncBoard(roleName, 14)));
		}

		// Essence
		int gems = 0;
		if (Manager.GetDonation() != null && Manager.GetDonation().Get(player.getName()) != null)
			gems = Manager.GetDonation().Get(player.getName()).GetEssence();
		lines.add(boardLine("  &7" + lang.getString(player, "prop_rush.lobby.essence", "Essence") + ": &a" + gems));
	}

	private String getStatusLine(Player player, Game game, String stateStatus, int playersNeeded, boolean isThai)
	{
		com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang lang = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.get();

		if ("STARTING".equals(stateStatus) && game != null)
		{
			int cd = Math.max(0, game.GetCountdown());
			return "&a" + lang.getString(player, "prop_rush.lobby.starting_in", "Starting in ") + cd + "s";
		}
		if ("LOADING".equals(stateStatus))
			return "&6" + lang.getString(player, "prop_rush.lobby.loading", "Preparing world...");
		if (playersNeeded > 0)
			return "&e" + lang.getString(player, "prop_rush.lobby.need_more", "Need <count> more").replace("<count>", String.valueOf(playersNeeded));
		return "&a" + lang.getString(player, "prop_rush.lobby.ready", "Ready to start!");
	}

	private String boardLine(String text)
	{
		return com.houzicore.shared.common.util.HouziColorParser.parse(text);
	}

	private String truncBoard(String text, int limit) {
		if (text == null) return "";
		if (text.replaceAll("(?i)\u00A7[0-9A-FK-ORX]", "").length() <= limit) return text;
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\u00A7') {
				sb.append(c);
				if (i + 1 < text.length()) { sb.append(text.charAt(i + 1)); i++; }
				continue;
			}
			sb.append(c);
			count++;
			if (count == limit - 2) { sb.append(".."); break; }
		}
		return sb.toString();
	}

	private String GetKitCustomName(Player player, Game game, LobbyEnt ent)
	{
		CoreClient client = Manager.GetClients().Get(player);
		Donor donor = Manager.GetDonation().Get(player.getName());

		String entityName = ent.GetKit().GetName();

		if (!player.isOnline() || client == null || donor == null)
			return entityName;
		
		if (client.GetRank() == null)
		{
		}
		
		if (game == null)
		{
		}
		
		if (Manager == null)
		{
		}
		
		if (Manager.GetServerConfig() == null)
		{
		}
		
		if (ent.GetKit().GetAvailability() == KitAvailability.Free || 										//Free
			Manager.hasKitsUnlocked(player) || 																	//YouTube
			(ent.GetKit().GetAvailability() == KitAvailability.Achievement && 
			Manager.GetAchievement().hasCategory(player, ent.GetKit().getAchievementRequirement())) ||		//Achievement
			donor.OwnsUnknownPackage(Manager.GetGame().GetName() + " " + ent.GetKit().GetName()) ||			//Green
			Manager.GetClients().Get(player).GetRank().Has(Rank.MAPDEV) ||									//STAFF
			donor.OwnsUnknownPackage(Manager.GetServerConfig().ServerType + " ULTRA") ||					//Single Ultra (Old)
			Manager.GetServerConfig().Tournament)															//Tournament
		{
			entityName = ent.GetKit().GetAvailability().GetColor() + entityName;
		}
		else if (ent.GetKit().GetAvailability() == KitAvailability.Achievement)
		{
			entityName = ChatColor.RED + C.Bold + entityName;
			entityName += ChatColor.RESET + " (" + C.cPurple + com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.get().getString(player, "prop_rush.lobby.achievement_kit", "Achievement Kit") + ChatColor.RESET + ")";
		}
		else
		{
			entityName = ChatColor.RED + C.Bold + entityName;
			entityName += ChatColor.RESET + " (" + C.cGreen + ent.GetKit().GetCost() + " " + com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.get().getString(player, "prop_rush.lobby.essence", "Essence") + ChatColor.RESET + ")";
		}

		return entityName;
	}

	@EventHandler
	public void customEntityName(CustomTagEvent event)
	{
		// TODO: This needs to be changed when removing CustomTagFix

		if (Manager.GetGame() != null)
		{
			String customName = null;

			for (LobbyEnt ent : _kits.values())
			{
				if (ent.GetEnt().getEntityId() == event.getEntityId())
				{
					customName = GetKitCustomName(event.getPlayer(), Manager.GetGame(), ent);
					break;
				}
			}

			if (customName != null)
			{
				event.setCustomName(customName);
			}
		}
	}

	// handle(PacketInfo) removed as it was only for legacy 1.7 support.
	// 1.8+ custom names are handled by customEntityName(CustomTagEvent event).


	public void AddPlayerToScoreboards(Player player, String teamName) 
	{		
		if (teamName == null)
			teamName = "";

		String rankName = Manager.GetClients().Get(player).GetRank().Name;
		boolean rankIsUltra = !Manager.GetClients().Get(player).GetRank().Has(Rank.WARRIOR) && Manager.GetDonation().Get(player.getName()).OwnsUnknownPackage(Manager.GetServerConfig().ServerType + " ULTRA");
		
		if (rankIsUltra)
		{
			rankName = Rank.WARRIOR.Name;
		}
		
		String rankTeamName = rankName + teamName;

		for (Scoreboard scoreboard : GetScoreboards())
		{
			try
			{
				scoreboard.getTeam(rankTeamName).addPlayer(player);
			}
			catch (Exception e)
			{
				// catch block
			}
		}
		
		// Update TablistFix with Team Color
		ChatColor teamColor = null;
		net.kyori.adventure.text.Component kitSuffix = null;
		if (Manager.GetGame() != null)
		{
			com.houzicore.arcade.nautilus.game.arcade.game.GameTeam team = Manager.GetGame().GetTeam(player);
			if (team != null)
				teamColor = team.GetColor();

			com.houzicore.arcade.nautilus.game.arcade.kit.Kit kit = Manager.GetGame().GetKit(player);
			if (kit != null)
				kitSuffix = net.kyori.adventure.text.Component.text(" [" + kit.GetName() + "]", net.kyori.adventure.text.format.NamedTextColor.GRAY);
		}
		String gameName = "Waiting...";
		String mapName = "Unknown";
		if (Manager.GetGame() != null)
		{
			gameName = Manager.GetGame().GetName();
			if (Manager.GetGame().WorldData != null && Manager.GetGame().WorldData.MapName != null) {
				mapName = Manager.GetGame().WorldData.MapName;
			}
		}

		com.houzicore.shared.TablistFix.updateTablist(player, Manager.GetClients(), teamColor, kitSuffix, gameName, mapName);
	}
	
	@EventHandler
	public void disallowInventoryClick(InventoryClickEvent event)
	{
		if (Manager.GetGame() == null)
			return;
		
		if (Manager.GetGame().GetState() != GameState.Recruit)
			return;
		
		if (event.getInventory().getType() == InventoryType.CRAFTING)
		{
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
		}
	}
	
	@EventHandler
	public void InventoryUpdate(UpdateEvent event)
	{
		if (!Manager.IsHotbarInventory())
			return;
		
		if (event.getType() != UpdateType.FAST)
			return; 
		
		if (Manager.GetGame() == null)
			return;
		 
		if (Manager.GetGame().GetState() != GameState.Recruit && Manager.GetGame().GetState() != GameState.Vote && Manager.GetGame().GadgetsDisabled)
			return;
				
		for (Player player : UtilServer.getPlayers())
		{
			if (player.getOpenInventory().getType() != InventoryType.CRAFTING)
				continue;
			
			//Cosmetic Menu
			Manager.getCosmeticManager().giveInterfaceItem(player);

			// Kit Selector Menu
			giveKitSelectorItem(player);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void GemBoosterInteract(ActivateEssenceBoosterEvent event)
	{
		if (!Manager.IsHotbarInventory() || Manager.GetGame() == null || Manager.GetGame().GetState() != GameState.Recruit)
		{
			event.setCancelled(true);
			
			event.getPlayer().sendMessage(F.main("Arcade", "You can't use Gem Boosters right now."));
			
			return;
		}
		
		Manager.GetGame().AddGemBooster(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void explodeBlockBreakFix(EntityExplodeEvent event)
	{
		if (Manager.GetGame() == null)
			return;
		
		if (Manager.GetGame().GetState() == GameState.Live)
			return;
		
		event.blockList().clear();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void velocityEventCancel(PlayerVelocityEvent event)
	{
		if (Manager.GetGame() == null)
			return;
		
		if (Manager.GetGame().GetState() == GameState.Live)
			return;
		
		event.setCancelled(true);
	}

	private List<Component> generateLobbyLines(Player player)
	{
		List<Component> lines = new ArrayList<>();
		String stateStatus = "WAITING";
		Game game = Manager.GetGame();
		if (game != null)
		{
			if (game.GetState() == Game.GameState.Loading)
			{
				stateStatus = "LOADING";
			}
			else if (game.GetState() == Game.GameState.Recruit)
			{
				if (game.GetCountdown() > 0)
				{
					stateStatus = "STARTING";
				}
				else
				{
					stateStatus = "WAITING";
				}
			}
		}

		ArrayList<String> rawLines = new ArrayList<>();
		appendWaitingBoardLines(rawLines, player, game, stateStatus);
		List<String> deduped = deduplicateLines(rawLines);

		com.houzicore.shared.core.scoreboard.PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(player);
		if (ps != null)
		{
			for (String line : deduped)
			{
				lines.add(ps.parseLine(line));
			}
		}
		else
		{
			for (String line : deduped)
			{
				lines.add(LegacyComponentSerializer.legacySection().deserialize(line));
			}
		}
		return lines;
	}

	public class LobbyScoreboardDataProvider implements com.houzicore.shared.core.scoreboard.ScoreboardDataProvider
	{
		@Override
		public Component getTitle(Player player)
		{
			Game game = Manager.GetGame();
			String name = "Arcade";
			GameType type = null;
			if (game != null)
			{
				name = game.GetName();
				type = game.GetType();
			}
			else if (Manager.GetGameList() != null && !Manager.GetGameList().isEmpty())
			{
				name = Manager.GetGameList().get(0).GetName();
				type = Manager.GetGameList().get(0);
			}
			
			return com.houzicore.arcade.nautilus.game.arcade.scoreboard.GameScoreboard.getAnimatedTitle(name, type, _animTick);
		}

		@Override
		public List<Component> getLines(Player player)
		{
			List<Component> lines = _cachedLobbyLines.get(player.getUniqueId());
			if (lines == null) {
				lines = generateLobbyLines(player);
				_cachedLobbyLines.put(player.getUniqueId(), lines);
			}
			return lines;
		}
	}

	public static class TeamGrid
	{
		public final GameTeam Team;
		public final int MinX, MaxX;
		public final int MinZ, MaxZ;
		public final int Y;
		public final Location Center;

		public TeamGrid(GameTeam team, int minX, int maxX, int minZ, int maxZ, int y, Location center)
		{
			this.Team = team;
			this.MinX = minX;
			this.MaxX = maxX;
			this.MinZ = minZ;
			this.MaxZ = maxZ;
			this.Y = y;
			this.Center = center;
		}

		public boolean isInGrid(Location loc)
		{
			int playerY = loc.getBlockY();
			return loc.getBlockX() >= MinX && loc.getBlockX() <= MaxX &&
			       loc.getBlockZ() >= MinZ && loc.getBlockZ() <= MaxZ &&
			       playerY >= Y + 1 && playerY <= Y + 2;
		}
	}

	public List<TeamGrid> getTeamGrids()
	{
		return _teamGrids;
	}

	private Material getStainedGlassMaterial(byte data)
	{
		switch (data)
		{
			case 0: return Material.WHITE_STAINED_GLASS;
			case 1: return Material.ORANGE_STAINED_GLASS;
			case 2: return Material.MAGENTA_STAINED_GLASS;
			case 3: return Material.LIGHT_BLUE_STAINED_GLASS;
			case 4: return Material.YELLOW_STAINED_GLASS;
			case 5: return Material.LIME_STAINED_GLASS;
			case 6: return Material.PINK_STAINED_GLASS;
			case 7: return Material.GRAY_STAINED_GLASS;
			case 8: return Material.LIGHT_GRAY_STAINED_GLASS;
			case 9: return Material.CYAN_STAINED_GLASS;
			case 10: return Material.PURPLE_STAINED_GLASS;
			case 11: return Material.BLUE_STAINED_GLASS;
			case 12: return Material.BROWN_STAINED_GLASS;
			case 13: return Material.GREEN_STAINED_GLASS;
			case 14: return Material.RED_STAINED_GLASS;
			case 15: return Material.BLACK_STAINED_GLASS;
			default: return Material.WHITE_STAINED_GLASS;
		}
	}

	private void placeTeamFloorGrid(Location centerLoc, byte colorData, GameTeam team)
	{
		Material glass = getStainedGlassMaterial(colorData);
		int centerX = centerLoc.getBlockX();
		int centerZ = centerLoc.getBlockZ();
		int floorY = centerLoc.getBlockY() + 1;

		int minX = centerX - 2;
		int maxX = centerX + 1;
		int minZ = centerZ - 2;
		int maxZ = centerZ + 1;

		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				Block block = centerLoc.getWorld().getBlockAt(x, floorY, z);
				setTrackedBlock(block, glass, _teamBlocks);
			}
		}

		Location centerPoint = new Location(centerLoc.getWorld(), centerX, floorY, centerZ);
		_teamGrids.add(new TeamGrid(team, minX, maxX, minZ, maxZ, floorY, centerPoint));
	}

	public void updateKitGlowState(Player player, LobbyEnt lobbyEnt, boolean glow, ChatColor color)
	{
		Entity entity = lobbyEnt.GetEnt();
		if (entity == null || !player.isOnline()) return;

		int entityId = entity.getEntityId();
		String entryName = entity.getUniqueId().toString();

		Scoreboard scoreboard = _scoreboardMap.get(player);
		if (scoreboard == null)
		{
			scoreboard = player.getScoreboard();
		}

		if (scoreboard != null)
		{
			org.bukkit.scoreboard.Team whiteTeam = scoreboard.getTeam("glow_white");
			org.bukkit.scoreboard.Team greenTeam = scoreboard.getTeam("glow_green");

			if (whiteTeam != null && greenTeam != null)
			{
				boolean inWhite = whiteTeam.hasEntry(entryName);
				boolean inGreen = greenTeam.hasEntry(entryName);

				if (glow)
				{
					if (color == ChatColor.GREEN)
					{
						if (inWhite) whiteTeam.removeEntry(entryName);
						if (!inGreen) greenTeam.addEntry(entryName);
					}
					else
					{
						if (inGreen) greenTeam.removeEntry(entryName);
						if (!inWhite) whiteTeam.addEntry(entryName);
					}
				}
				else
				{
					if (inWhite) whiteTeam.removeEntry(entryName);
					if (inGreen) greenTeam.removeEntry(entryName);
				}
			}
		}

		byte flags = 0;
		if (glow)
		{
			flags |= 0x40;
		}

		List<com.github.retrooper.packetevents.protocol.entity.data.EntityData<?>> metadataList = new ArrayList<>();
		metadataList.add(new com.github.retrooper.packetevents.protocol.entity.data.EntityData(
			0, 
			com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes.BYTE, 
			flags
		));

		com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata packet = 
			new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata(entityId, metadataList);

		com.github.retrooper.packetevents.PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
	}

	public void resendAllKitGlowStates(Player player)
	{
		if (Manager.GetGame() == null) return;

		Kit selectedKit = Manager.GetGame().GetKit(player);

		for (LobbyEnt lobbyEnt : _kits.values())
		{
			Kit kit = lobbyEnt.GetKit();
			if (kit == null) continue;

			boolean isSelected = selectedKit != null && selectedKit.GetName().equalsIgnoreCase(kit.GetName());

			if (selectedKit == null)
			{
				updateKitGlowState(player, lobbyEnt, true, ChatColor.WHITE);
			}
			else
			{
				if (isSelected)
				{
					updateKitGlowState(player, lobbyEnt, true, ChatColor.GREEN);
				}
				else
				{
					updateKitGlowState(player, lobbyEnt, false, ChatColor.WHITE);
				}
			}
		}
	}

	public void giveKitSelectorItem(Player player)
	{
		if (Manager.GetGame() == null) return;
		if (Manager.GetGame().GetState() != GameState.Recruit && Manager.GetGame().GetState() != GameState.Vote) return;

		ItemStack selectorItem = ItemStackFactory.Instance.CreateStack(
			Material.COMPASS, 
			(byte)0, 
			1, 
			(short)0, 
			ChatColor.GREEN + com.houzicore.shared.core.lang.LangManager.get().get(player, "arcade.kit_select_item", "Select Kit"), 
			new String[]{
				"", 
				ChatColor.RESET + com.houzicore.shared.core.lang.LangManager.get().get(player, "arcade.kit_select_item_lore", "Click to choose a Kit.")
			}
		);

		player.getInventory().setItem(0, selectorItem);
	}

}
