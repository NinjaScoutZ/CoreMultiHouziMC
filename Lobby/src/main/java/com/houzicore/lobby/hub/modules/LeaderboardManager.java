package com.houzicore.lobby.hub.modules;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.leaderboard.LeaderboardHologram;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.lobby.hub.HubManager;

public class LeaderboardManager extends MiniPlugin 
{
	private HubManager _hubManager;
	private HologramManager _hologramManager;
	private StatsManager _statsManager;
	
	private File _configFile;
	private YamlConfiguration _config;
	
	private HashMap<Location, LeaderboardHologram> _holograms = new HashMap<>();
	private HashMap<Location, String> _hologramStats = new HashMap<>();

	public LeaderboardManager(HubManager hubManager, java.util.List<Location> defaultLocs) 
	{
		super("Leaderboard Manager", hubManager.getPlugin());
		_hubManager = hubManager;
		_hologramManager = hubManager.getHologramManager();
		_statsManager = hubManager.GetStats();
		
		_configFile = new File(getPlugin().getDataFolder(), "leaderboards.yml");
		loadConfig();
        
        if (_holograms.isEmpty() && defaultLocs != null && !defaultLocs.isEmpty()) {
            String[] defaultStats = {"Global.Kills", "Global.Wins", "Global.Gems", "Global.Exp"};
            int i = 0;
            for (Location loc : defaultLocs) {
                if (i < defaultStats.length) {
                    addLeaderboard(loc, defaultStats[i]);
                    i++;
                }
            }
        }
	}
	
	@Override
	public void addCommands() {
		addCommand(new LeaderboardCommand(this));
	}

	private void loadConfig()
	{
		if (!_configFile.exists()) 
		{
			try 
			{
				_configFile.getParentFile().mkdirs();
				_configFile.createNewFile();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		_config = YamlConfiguration.loadConfiguration(_configFile);
		
		if (_config.getConfigurationSection("holograms") != null) 
		{
			for (String key : _config.getConfigurationSection("holograms").getKeys(false)) 
			{
				Location loc = (Location) _config.get("holograms." + key + ".location");
				String statName = _config.getString("holograms." + key + ".stat");
				
				if (loc != null && statName != null) 
				{
					spawnHologram(loc, statName);
				}
			}
		}
	}
	
	private void saveConfig()
	{
		_config.set("holograms", null);
		
		int i = 0;
		for (Map.Entry<Location, String> entry : _hologramStats.entrySet()) 
		{
			_config.set("holograms." + i + ".location", entry.getKey());
			_config.set("holograms." + i + ".stat", entry.getValue());
			i++;
		}
		
		try 
		{
			_config.save(_configFile);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public boolean addLeaderboard(Location loc, String statName) 
	{
		Location center = loc.getBlock().getLocation().add(0.5, 0, 0.5);
		center.setYaw(loc.getYaw());
		center.setPitch(0);
		
		if (_hologramStats.containsKey(center)) return false;
		
		spawnHologram(center, statName);
		saveConfig();
		return true;
	}
	
	public boolean removeNearestLeaderboard(Location loc) 
	{
		Location nearest = null;
		double minDist = 5.0; // 5 block radius
		
		for (Location hLoc : _hologramStats.keySet()) 
		{
			if (hLoc.getWorld().equals(loc.getWorld())) 
			{
				double dist = hLoc.distance(loc);
				if (dist < minDist) 
				{
					minDist = dist;
					nearest = hLoc;
				}
			}
		}
		
		if (nearest != null) 
		{
			LeaderboardHologram hol = _holograms.remove(nearest);
			if (hol != null) hol.destroy();
			_hologramStats.remove(nearest);
			saveConfig();
			return true;
		}
		return false;
	}
	
	private void spawnHologram(Location loc, String statName) 
	{
		String cleanStat = statName;
		if (cleanStat.contains(".")) {
			cleanStat = cleanStat.split("\\.")[0] + " " + cleanStat.split("\\.")[1];
		}
		
		String title = C.cYellow + C.Bold + "Top 10 " + cleanStat;
		
		LeaderboardHologram hol = new LeaderboardHologram(_hologramManager, _statsManager, loc, title, statName, 10);
		
		_holograms.put(loc, hol);
		_hologramStats.put(loc, statName);
	}
}
