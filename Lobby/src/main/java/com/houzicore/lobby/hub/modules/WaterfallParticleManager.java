package com.houzicore.lobby.hub.modules;

import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilWorld;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class WaterfallParticleManager extends MiniPlugin {

    private final List<Location> waterfallLocations = new ArrayList<>();

    public WaterfallParticleManager(HubManager manager) {
        super("Waterfall Emitters", manager.getPlugin());

        long count = 0;
        
        List<Location> points = manager.getMapData("DATA_NAME:WATERFALL_EMITTER");
        if (points != null) {
            for (Location loc : points) {
                // Adjust to the center of the block
                waterfallLocations.add(loc.clone().add(0.5, 0.1, 0.5));
                count++;
            }
        }
        
        System.out.println("Loaded " + count + " Waterfall Emitter points.");
    }

    @EventHandler
    public void onParticleUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.FASTEST) return;
        if (waterfallLocations.isEmpty()) return;

        Player[] players = UtilServer.getPlayers();
        if (players.length == 0) return;

        for (Location loc : waterfallLocations) {
            // Check if any player is within 32 blocks
            boolean playerNearby = false;
            for (Player player : players) {
                if (UtilMath.offset(player.getLocation(), loc) < 32.0) {
                    playerNearby = true;
                    break;
                }
            }

            if (playerNearby) {
                // Main splash
                UtilParticle.PlayParticle(UtilParticle.ParticleType.SPLASH, loc, 
                        0.4f, 0.1f, 0.4f, 0.02f, 6, 
                        UtilParticle.ViewDist.NORMAL, players);
                        
                // Secondary mist/cloud
                Location cloudLoc = loc.clone().add(0, 0.1, 0);
                UtilParticle.PlayParticle(UtilParticle.ParticleType.CLOUD, cloudLoc, 
                        0.3f, 0.15f, 0.3f, 0.01f, 2, 
                        UtilParticle.ViewDist.NORMAL, players);
            }
        }
    }
}
