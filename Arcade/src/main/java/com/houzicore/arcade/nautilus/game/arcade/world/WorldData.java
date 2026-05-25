package com.houzicore.arcade.nautilus.game.arcade.world;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;

import com.houzicore.shared.common.util.FileUtil;
import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.WorldUtil;
import com.houzicore.shared.common.util.ZipUtil;
import com.houzicore.shared.timing.TimingManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

// ChunkPreLoadEvent import removed

import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.event.world.ChunkUnloadEvent;

public class WorldData 
{
	public Game Host;
	
	public int Id = -1;
	
	public String File = null;
	public String Folder = null;
	
	public World World;
	public int MinX = 0;
	public int MinZ = 0;
	public int MaxX = 0;
	public int MaxZ = 0;
	public int CurX = 0; 
	public int CurZ = 0;
	
	public int MinY = -1;
	public int MaxY = 256;
	
	public String MapName = "Null";
	public String MapAuthor = "Null";
	
	public GameType Game = null;
	
	public HashMap<String, ArrayList<Location>> SpawnLocs = new HashMap<String, ArrayList<Location>>();
	private HashMap<String, ArrayList<Location>> DataLocs = new HashMap<String, ArrayList<Location>>();
	private HashMap<String, ArrayList<Location>> CustomLocs = new HashMap<String, ArrayList<Location>>();
	private final Map<String, String> _dataEntries = new HashMap<>();
	
	public WorldData(Game game)
	{
		Host = game;
		
		Id = GetNewId();
	}
	
	// Pre-parsed map data — populated before world load to survive Paper migration
	private com.houzicore.shared.core.map.MapLoadResult _preloadedMapResult = null;

	public void Initialize(String forcedMap)
	{
		final WorldData worldData = this;
		GetFile(forcedMap);
		
		UtilServer.getServer().getScheduler().runTaskAsynchronously(Host.Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				try {
					//Unzip
					worldData.UnzipWorld();
					
					// CRITICAL: Parse WorldConfig/schema.json BEFORE loading the world.
					// Paper's LegacyCraftBukkitWorldMigration destroys the original folder
					// (and its config files) during WorldCreator.createWorld(), so we must
					// read them into memory first.
					TimingManager.start("WorldData pre-parsing MapConfig.");
					worldData.preParseMapConfig();
					TimingManager.stop("WorldData pre-parsing MapConfig.");
					
					//Load World Data Sync
					UtilServer.getServer().getScheduler().runTask(Host.Manager.getPlugin(), new Runnable()
					{
						public void run()
						{
							TimingManager.start("WorldData loading world.");
							//Start World
							
							World = WorldUtil.LoadWorld(new WorldCreator(GetFolder()));
							
							// Auto-Respawn: skips death screen. Combined with manual
							// spectator transition in GameFlagManager.PlayerDeath.
							World.setGameRule(org.bukkit.GameRule.DO_IMMEDIATE_RESPAWN, true);
							
							TimingManager.stop("WorldData loading world.");
							
							World.setDifficulty(Difficulty.HARD);
							World.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);

							TimingManager.start("WorldData applying WorldConfig.");
							// Apply the pre-parsed map data now that we have a World reference
							worldData.applyPreParsedMapConfig();
							TimingManager.stop("WorldData applying WorldConfig.");
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	protected GameType GetGame()
	{
		return Game;
	}
	
	protected void GetFile(String forcedMap)
	{
		if (forcedMap != null)
		{
			// Map Voting - Forced injection!
			File = forcedMap;
			
			// Find the parent GameType that owns this specific map
			for(GameType type : Host.GetFiles().keySet())
			{
				if (Host.GetFiles().get(type).contains(forcedMap))
				{
					Game = type;
					break;
				}
			}
			if (Game == null)
				Game = Host.GetType(); // Fallback
				
			Host.Manager.GetGameCreationManager().SetLastMap(File);
			return;
		}

		if (File == null)
		{
			GameType game = null;
			
			// Filter to games that actually have maps
			ArrayList<GameType> validGames = new ArrayList<>();
			for (GameType type : Host.GetFiles().keySet())
			{
				if (!Host.GetFiles().get(type).isEmpty())
				{
					validGames.add(type);
				}
			}
			
			if (validGames.isEmpty())
			{
				// No maps available at all! Use a fallback to prevent "bound must be positive" crash.
				Game = Host.GetType();
				File = "ErrorNoMapFound";
				Host.Manager.GetGameCreationManager().SetLastMap(File);
				return;
			}
			
			int gameRandom = UtilMath.r(validGames.size());
			game = validGames.get(gameRandom);
			Game = game;
			
			int map = UtilMath.r(Host.GetFiles().get(game).size());
			File = Host.GetFiles().get(game).get(map);
			
			//Don't allow repeat maps.
			if (validGames.size() > 1 || (validGames.size() == 1 && Host.GetFiles().get(validGames.get(0)).size() > 1))
			{
				while (File.equals(Host.Manager.GetGameCreationManager().GetLastMap()))
				{
					GameType _game = null;
					int _gameRandom = UtilMath.r(validGames.size());
					_game = validGames.get(_gameRandom);
					
					int _map = UtilMath.r(Host.GetFiles().get(_game).size());
					File = Host.GetFiles().get(_game).get(_map);
				}
			}
		}
			
		Host.Manager.GetGameCreationManager().SetLastMap(File);
	}
	
	public String GetFolder()
	{
		if (Folder == null) 
		{
			String rawName = "Game" + Id + "_" + GetGame().name() + "_" + File;
			Folder = rawName.toLowerCase().replaceAll("[^a-z0-9_\\-\\.]", "").trim();
		}	
		return Folder;
	}
	
	public Location GetSpawn()
	{
		for (ArrayList<Location> locs : SpawnLocs.values())
		{
			if (locs != null && !locs.isEmpty())
				return locs.get(0);
		}
		if (World != null)
			return World.getSpawnLocation();
		return null;
	}
	
	protected void UnzipWorld() 
	{
		TimingManager.start("UnzipWorld creating folders");
		String folder = GetFolder();
		new File(folder).mkdir();
		new File(folder + java.io.File.separator + "region").mkdir();
		new File(folder + java.io.File.separator + "data").mkdir();
		TimingManager.stop("UnzipWorld creating folders");
		
		TimingManager.start("UnzipWorld UnzipToDirectory");
        String mapFolder = GetGame().GetMapFolderName();
        File zipFile = new File("Maps/" + mapFolder + "/" + File + ".zip");
        if (zipFile.exists()) {
			ZipUtil.UnzipToDirectory("Maps/" + mapFolder + "/" + File + ".zip", folder);
        } else {
        }
		TimingManager.stop("UnzipWorld UnzipToDirectory");
	}
	
	public com.houzicore.shared.core.map.MapLoadOutcome mapLoadOutcome = null;

	/**
	 * Phase 1: Parse map config files (WorldConfig.dat / schema.json) from the
	 * unzipped folder into memory. This MUST be called BEFORE WorldUtil.LoadWorld()
	 * because Paper's LegacyCraftBukkitWorldMigration will move/delete the original
	 * folder contents during world loading, destroying these config files.
	 *
	 * Safe to call from async thread (no Bukkit API usage).
	 */
	private void preParseMapConfig()
	{
		try
		{
			File folder = new File(GetFolder());
			if (!folder.exists())
			{
				org.bukkit.Bukkit.getLogger().warning("[WorldData] Pre-parse: folder does not exist: " + GetFolder());
				return;
			}

			com.houzicore.shared.core.map.HybridMapDataProvider provider
					= new com.houzicore.shared.core.map.HybridMapDataProvider();
			_preloadedMapResult = provider.loadWithResult(folder);

			org.bukkit.Bukkit.getLogger().info("[WorldData] Pre-parsed config for map=" + File
					+ " outcome=" + _preloadedMapResult.getOutcome());
		}
		catch (Exception e)
		{
			org.bukkit.Bukkit.getLogger().warning("[WorldData] Pre-parse failed for map=" + File);
			e.printStackTrace();
		}
	}

	/**
	 * Phase 2: Apply the pre-parsed map data using the now-loaded World reference
	 * to create Location objects. Must be called on the main thread after World is set.
	 */
	private void applyPreParsedMapConfig()
	{
		try
		{
			if (_preloadedMapResult == null)
			{
				org.bukkit.Bukkit.getLogger().warning("[WorldData] No pre-parsed config available for map=" + File
						+ " — attempting direct LoadWorldConfig as fallback");
				LoadWorldConfig();
				return;
			}

			mapLoadOutcome = _preloadedMapResult.getOutcome();

			if (_preloadedMapResult.getOutcome() == com.houzicore.shared.core.map.MapLoadOutcome.LOAD_FAILED) {
				org.bukkit.Bukkit.getLogger().warning("[WorldData] LOAD_FAILED for map=" + File
						+ " game=" + (Game != null ? Game.name() : "?"));
				return;
			}

			com.houzicore.shared.api.map.MapDefinition mapDef = _preloadedMapResult.getDefinition();

			// Log load mode clearly (visible in console for operator diagnostics)
			String parity = _preloadedMapResult.hasParityWarnings()
					? " [" + _preloadedMapResult.getParityWarnings().size() + " parity warning(s)]" : " [parity OK]";
			switch (_preloadedMapResult.getOutcome()) {
				case SCHEMA_ONLY               -> org.bukkit.Bukkit.getLogger().info(
						"[WorldData] SCHEMA_ONLY" + parity + " for map=" + File);
				case SCHEMA_PLUS_DAT_SUPPLEMENT-> org.bukkit.Bukkit.getLogger().warning(
						"[WorldData] SCHEMA_PLUS_DAT_SUPPLEMENT" + parity
						+ " for map=" + File + " (legacy schema — re-export to upgrade)");
				case DAT_FALLBACK              -> org.bukkit.Bukkit.getLogger().warning(
						"[WorldData] DAT_FALLBACK for map=" + File
						+ " (no schema.json — re-export via MapBuilder to upgrade)");
				default -> {}
			}
			
			MapName = mapDef.getMapName();
			MapAuthor = mapDef.getAuthor();
			
			mapDef.getBoundingBox().ifPresent(box -> {
				MinX = box.getMinX();
				MaxX = box.getMaxX();
				MinY = box.getMinY();
				MaxY = box.getMaxY();
				MinZ = box.getMinZ();
				MaxZ = box.getMaxZ();
				CurX = MinX;
				CurZ = MinZ;
			});

			for (String team : mapDef.getTeamNames()) {
			    ArrayList<Location> locs = new ArrayList<>();
			    for (com.houzicore.shared.api.map.MapPoint p : mapDef.getTeamSpawns(team)) {
			        locs.add(new Location(World, p.getX() + 0.5, p.getY(), p.getZ() + 0.5, p.getYaw(), p.getPitch()));
			    }
			    SpawnLocs.put(team, locs);
			}

			for (String key : mapDef.getCustomPointKeys()) {
			    ArrayList<Location> locs = new ArrayList<>();
			    for (com.houzicore.shared.api.map.MapPoint p : mapDef.getCustomPoints(key)) {
			        locs.add(new Location(World, p.getX() + 0.5, p.getY(), p.getZ() + 0.5, p.getYaw(), p.getPitch()));
			    }
			    
			    // In legacy, DATA_NAME and CUSTOM_NAME often went to the exact same list visually if requested.
			    // They were split into two maps. We will populate both maps with the same data to be safe.
			    DataLocs.put(key, locs);
			    CustomLocs.put(key, locs);
			}

			mapDef.getProperties().forEach((k, v) -> _dataEntries.put(k, v));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			_preloadedMapResult = null; // Release memory
			Host.Manager.GetGameWorldManager().RegisterWorld(this);
		}
	}

	/**
	 * Legacy fallback: reads config directly from the folder.
	 * Only used if preParseMapConfig() was not called or failed.
	 */
	public void LoadWorldConfig() 
	{
		try
		{
			File folder = new File(GetFolder());
			if (!folder.exists()) return;

			com.houzicore.shared.core.map.HybridMapDataProvider provider
					= new com.houzicore.shared.core.map.HybridMapDataProvider();
			com.houzicore.shared.core.map.MapLoadResult loadResult = provider.loadWithResult(folder);
			mapLoadOutcome = loadResult.getOutcome();

			if (loadResult.getOutcome() == com.houzicore.shared.core.map.MapLoadOutcome.LOAD_FAILED) {
				org.bukkit.Bukkit.getLogger().warning("[WorldData] LOAD_FAILED for map=" + File
						+ " game=" + (Game != null ? Game.name() : "?"));
				return;
			}

			com.houzicore.shared.api.map.MapDefinition mapDef = loadResult.getDefinition();

			// Log load mode clearly (visible in console for operator diagnostics)
			String parity = loadResult.hasParityWarnings()
					? " [" + loadResult.getParityWarnings().size() + " parity warning(s)]" : " [parity OK]";
			switch (loadResult.getOutcome()) {
				case SCHEMA_ONLY               -> org.bukkit.Bukkit.getLogger().info(
						"[WorldData] SCHEMA_ONLY" + parity + " for map=" + File);
				case SCHEMA_PLUS_DAT_SUPPLEMENT-> org.bukkit.Bukkit.getLogger().warning(
						"[WorldData] SCHEMA_PLUS_DAT_SUPPLEMENT" + parity
						+ " for map=" + File + " (legacy schema — re-export to upgrade)");
				case DAT_FALLBACK              -> org.bukkit.Bukkit.getLogger().warning(
						"[WorldData] DAT_FALLBACK for map=" + File
						+ " (no schema.json — re-export via MapBuilder to upgrade)");
				default -> {}
			}
			
			MapName = mapDef.getMapName();
			MapAuthor = mapDef.getAuthor();
			
			mapDef.getBoundingBox().ifPresent(box -> {
				MinX = box.getMinX();
				MaxX = box.getMaxX();
				MinY = box.getMinY();
				MaxY = box.getMaxY();
				MinZ = box.getMinZ();
				MaxZ = box.getMaxZ();
				CurX = MinX;
				CurZ = MinZ;
			});

			for (String team : mapDef.getTeamNames()) {
			    ArrayList<Location> locs = new ArrayList<>();
			    for (com.houzicore.shared.api.map.MapPoint p : mapDef.getTeamSpawns(team)) {
			        locs.add(new Location(World, p.getX() + 0.5, p.getY(), p.getZ() + 0.5, p.getYaw(), p.getPitch()));
			    }
			    SpawnLocs.put(team, locs);
			}

			for (String key : mapDef.getCustomPointKeys()) {
			    ArrayList<Location> locs = new ArrayList<>();
			    for (com.houzicore.shared.api.map.MapPoint p : mapDef.getCustomPoints(key)) {
			        locs.add(new Location(World, p.getX() + 0.5, p.getY(), p.getZ() + 0.5, p.getYaw(), p.getPitch()));
			    }
			    
			    // In legacy, DATA_NAME and CUSTOM_NAME often went to the exact same list visually if requested.
			    // They were split into two maps. We will populate both maps with the same data to be safe.
			    DataLocs.put(key, locs);
			    CustomLocs.put(key, locs);
			}

			mapDef.getProperties().forEach((k, v) -> _dataEntries.put(k, v));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			Host.Manager.GetGameWorldManager().RegisterWorld(this);
		}
	}

	
	protected Location StrToLoc(String loc)
	{
		String[] coords = loc.split(",");
		
		try
		{
			return new Location(World, Integer.valueOf(coords[0])+0.5, Integer.valueOf(coords[1]), Integer.valueOf(coords[2])+0.5);
		}
		catch (Exception e)
		{
		}
	
		return null;
	}
	
	public boolean LoadChunks(long maxMilliseconds)
	{
		long startTime = System.currentTimeMillis();
		
		for (; CurX <= MaxX; CurX += 16)
        {	
            for (; CurZ <= MaxZ; CurZ += 16) 
            {
    			if (System.currentTimeMillis() - startTime >= maxMilliseconds)
    				return false;
                
    			World.getChunkAt(new Location(World, CurX, 0, CurZ));
            }
            
            CurZ = MinZ;
        }
		
    	return true;
	}
	
	public void Uninitialize() 
	{	
		if (World == null)
			return;
		
		//Wipe World
		MapUtil.UnloadWorld(Host.Manager.getPlugin(), World);
		MapUtil.ClearWorldReferences(World.getName());
		FileUtil.DeleteFolder(new File(World.getName()));
		
		World = null;
	}
	
	public void ChunkUnload(ChunkUnloadEvent event) 
	{
		if (World == null)
			return;
		
		if (!event.getWorld().equals(World))
			return;
		
			// event.setCancelled(true); // ChunkUnloadEvent is not Cancellable in Paper 1.21
	}

	// ChunkLoad cancelled out logic removed as it's legacy and used NMS
	
	public int GetNewId() 
	{
		File file = new File("GameId.dat");

		//Write If Blank
		if (!file.exists())
		{
			try
			{
				FileWriter fstream = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fstream);

				out.write("0");

				out.close();
			}
			catch (Exception e)
			{
			}
		}

		int id = 0;

		//Read
		try
		{
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();

			id = Integer.parseInt(line);

			in.close();
		}
		catch (Exception e)
		{
			id = 0;
		}

		try
		{
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("" + (id + 1));

			out.close();
		}
		catch (Exception e)
		{
		}

		return id;
	}

	public ArrayList<Location> GetDataLocs(String data)
	{
		if (!DataLocs.containsKey(data))
			return new ArrayList<Location>();
		
		return DataLocs.get(data);
	}
	
	public ArrayList<Location> GetCustomLocs(String id)
	{
		if (!CustomLocs.containsKey(id))
			return new ArrayList<Location>();
		
		return CustomLocs.get(id);
	}
	
	public HashMap<String, ArrayList<Location>> GetAllCustomLocs()
	{
		return CustomLocs;
	}

	public Location GetRandomXZ() 
	{
		Location loc = new Location(World, 0, 250, 0);
		
		int xVar = MaxX - MinX;
		int zVar = MaxZ - MinZ;
		
		loc.setX(MinX + UtilMath.r(xVar));
		loc.setZ(MinZ + UtilMath.r(zVar));

		return loc;
	}

	public String get(String key)
	{
		return _dataEntries.get(key);
	}
	
}
