package com.houzicore.lobby.hub.modules;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;

/**
 * NPC Queue System (#4) — shows a real-time player count hologram above each game NPC.
 * Updates every ~5s via UpdateType.SLOW. Queue counts set externally via setPlayerCount().
 */
public class NpcQueueManager implements Listener {

    private final HubManager _hub;
    private final HologramManager _hologramManager;
    private final Map<String, Hologram> _holograms = new HashMap<>();
    private final Map<String, Location> _npcLocations = new HashMap<>();
    private final Map<String, Integer> _queueCounts = new HashMap<>();

    public NpcQueueManager(HubManager hub, HologramManager hologramManager) {
        _hub = hub;
        _hologramManager = hologramManager;
        hub.getPlugin().getServer().getPluginManager().registerEvents(this, hub.getPlugin());
        initDefaultLocations();
        initHolograms();
    }

    private void initDefaultLocations() {
        org.bukkit.World world = _hub.GetSpawn().getWorld();
        if (world == null) return;
        // Placeholder positions — adjust to match lobby map
        _npcLocations.put("Survival Games", new Location(world, -6, 128, 0.31));
        _npcLocations.put("Skywars",         new Location(world, -2, 128, 0.31));
        _npcLocations.put("Block Hunt",      new Location(world, 2, 128, 0.31));
        _npcLocations.put("Draw My Thing",   new Location(world, 6, 128, 0.31));
    }

    private void initHolograms() {
        _hub.getPlugin().getServer().getScheduler().runTaskLater(_hub.getPlugin(), () -> {
            for (Map.Entry<String, Location> entry : _npcLocations.entrySet()) {
                String name = entry.getKey();
                Location loc = entry.getValue().clone().add(0, 2.5, 0);
                Hologram holo = new Hologram(_hologramManager, loc, buildText(name, 0));
                _holograms.put(name, holo);
                _queueCounts.put(name, 0);
            }
        }, 60L);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SLOW) return;
        for (Map.Entry<String, Hologram> entry : _holograms.entrySet()) {
            String name = entry.getKey();
            int count = _queueCounts.getOrDefault(name, 0);
            entry.getValue().setText(buildText(name, count));
        }
    }

    private String[] buildText(String gameName, int count) {
        String countColor = count == 0 ? C.cGray : (count >= 10 ? C.cGreen : C.cYellow);
        String statusLine = count == 0 ? C.cYellow + "Click NPC to Queue" : C.cGreen + "§l▶ JOIN NOW";
        return new String[] {
            C.cGold + C.Bold + UtilText.toSmallCaps(gameName),
            "§8─────────────────",
            C.cGray + "Players: " + countColor + count,
            statusLine
        };
    }

    /** Call this to update queue count from Redis or ServerStatusManager */
    public void setPlayerCount(String gameName, int count) {
        _queueCounts.put(gameName, count);
    }
}
