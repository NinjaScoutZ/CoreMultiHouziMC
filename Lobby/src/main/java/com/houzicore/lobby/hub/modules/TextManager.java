package com.houzicore.lobby.hub.modules;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.lobby.hub.HubType;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * TextManager — Lobby Section Labels
 *
 * Replaced legacy UtilBlockText (NMS block letters) with HologramManager floating labels.
 * Each game zone gets a stacked 2-line hologram:
 *   Line 1 (top): zone name in §6 Small Caps + separator
 *   Line 2 (bottom): rotating featured game names in §7
 *
 * Holograms spawn once on enable and are cleared on disable.
 */
public class TextManager extends MiniPlugin
{
	public HubManager Manager;

	private final HologramManager _hologramManager;
	private final List<Hologram> _holograms = new ArrayList<>();

	// Rotating arcade text state
	private String[] arcadeGames;
	private int arcadeIndex = 0;

	// Reference to rotating holograms for live updates
	private Hologram _arcadeLabel;

	// Public location fields used by WelcomeTutorial for camera tracking
	public Location locArcade;
	public Location locSurvival;
	public Location locClassics;
	public Location locComp;

	public TextManager(HubManager manager, HologramManager hologramManager)
	{
		super("Text Creator", manager.getPlugin());

		Manager = manager;
		_hologramManager = hologramManager;

		arcadeGames = new String[]
				{
				"DRAGON ESCAPE",
				"SUPER SPLEEF",
				"SHEEP QUEST",
				"BLOCK HUNT",
				"QUIVER",
				};

		CreateHolograms();
	}

	private void CreateHolograms()
	{
		_holograms.clear();

		// Champions zone
		java.util.List<Location> compLocs = Manager.getMapData("DATA_NAME:TEXT_CHAMPIONS");
		if (compLocs != null && !compLocs.isEmpty()) {
			locComp = compLocs.get(0);
			spawnZoneLabel(locComp,
					"§6§l" + UtilText.toSmallCaps("CHAMPIONS"),
					"§8───  §7ᴅᴏᴍɪɴᴀᴛᴇ  §c·  §7ᴛᴅᴍ  §c·  §7ᴄʟᴀɴs  §8───");
		}

		// Arcade zone
		java.util.List<Location> arcadeLocs = Manager.getMapData("DATA_NAME:TEXT_ARCADE");
		if (arcadeLocs != null && !arcadeLocs.isEmpty()) {
			locArcade = arcadeLocs.get(0);
			_arcadeLabel = spawnZoneLabel(locArcade,
					"§6§l" + UtilText.toSmallCaps("ARCADE"),
					"§7" + arcadeGames[0]);
		}

		// Survival zone
		java.util.List<Location> survLocs = Manager.getMapData("DATA_NAME:TEXT_SURVIVAL");
		if (survLocs != null && !survLocs.isEmpty()) {
			locSurvival = survLocs.get(0);
			spawnZoneLabel(locSurvival,
					"§6§l" + UtilText.toSmallCaps("SURVIVAL"),
					"§8───  §7sᴋʏᴡᴀʀs  §c·  §7ʙʀɪᴅɢᴇs  §c·  §7sɢ  §8───");
		}

		// Classics zone
		java.util.List<Location> classicsLocs = Manager.getMapData("DATA_NAME:TEXT_CLASSICS");
		if (classicsLocs != null && !classicsLocs.isEmpty()) {
			locClassics = classicsLocs.get(0);
			spawnZoneLabel(locClassics,
					"§6§l" + UtilText.toSmallCaps("CLASSICS"),
					"§8───  §7sᴍᴀsʜ  §c·  §7ʙᴜɪʟᴅᴇʀs  §c·  §7ʙʟᴏᴄᴋ ʜᴜɴᴛ  §8───");
		}
	}

	private Hologram spawnZoneLabel(Location location, String titleLine, String subLine)
	{
		final Hologram hologram = new Hologram(_hologramManager, location, titleLine, subLine);
		hologram.start();
		_holograms.add(hologram);
		return hologram;
	}

	/**
	 * Rotate the Arcade zone label every 5 seconds to cycle through featured games.
	 */
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		// Rotate every 5 seconds
		if (System.currentTimeMillis() % 5000 < 1000 && _arcadeLabel != null && _arcadeLabel.isInUse())
		{
			arcadeIndex = (arcadeIndex + 1) % arcadeGames.length;
			_arcadeLabel.setText(
					"§6§l" + UtilText.toSmallCaps("ARCADE"),
					"§7" + arcadeGames[arcadeIndex]);
		}
	}

	@Override
	public void disable()
	{
		for (final Hologram h : _holograms)
		{
			h.stop();
		}
		_holograms.clear();
	}
}
